package net.evilengineers.templates4j.extension.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import net.evilengineers.templates4j.extension.json.xpath.JsonXPathBaseListener;
import net.evilengineers.templates4j.extension.json.xpath.JsonXPathLexer;
import net.evilengineers.templates4j.extension.json.xpath.JsonXPathParser;
import net.evilengineers.templates4j.extension.json.xpath.JsonXPathParser.ConditionContext;
import net.evilengineers.templates4j.extension.json.xpath.JsonXPathParser.QueryStepContext;
import net.evilengineers.templates4j.misc.Misc;
import net.evilengineers.templates4j.spi.UserFunction;

public class XPathQueryFunction extends UserFunction {
	@Override
	public String getNamespace() {
		String ns = getClass().getPackage().getName();
		return ns.substring(ns.lastIndexOf('.') + 1);
	}
	
	@Override
	public String getName() {
		return "xpath";
	}

	public Object execute(JsonNode model, String path) throws XPathExpressionException {
		final List<Node> nodes = new ArrayList<>();

		// Add fake root node
		nodes.add(new Node("", model, null));

		JsonXPathParser xpathParser = new JsonXPathParser(new CommonTokenStream(new JsonXPathLexer(new ANTLRInputStream(path))));
		xpathParser.addParseListener(new JsonXPathBaseListener() {
			@Override
			public void exitQueryStep(QueryStepContext ctx) {
				List<Node> candidates = new ArrayList<>();

				// Candidate selection from axis
				String axis = ctx.Any() != null ? "descendant-or-self" : "child";
				if (ctx.axisSpecifier() != null)
					axis = ctx.axisSpecifier().axisName().getText();
				
				if ("child".equals(axis)) {
					candidates = getChildren(nodes);
				} else if ("parent".equals(axis)) {
					candidates = getParents(nodes);
				} else if ("descendant".equals(axis)) {
					candidates = getDescendants(nodes);
				} else if ("descendant-or-self".equals(axis)) {
					candidates = getDescendants(nodes);
					candidates.addAll(nodes);
				}

				// Rule filtering
				String name = "*";
				if (ctx.name() != null)
					name = ctx.name().getText();
				
				if (!"*".equals(name)) {
					Iterator<Node> i = candidates.iterator();
					while (i.hasNext()) {
						Node candidate = i.next();
						if (!candidate.name.equals(name))
							i.remove();
					}
				}
							
				// Condition filtering
				ConditionContext condition = ctx.condition();
				if (condition != null) {
					Iterator<Node> i = candidates.iterator();
					while (i.hasNext()) {
						Node candidate = i.next();
						if ("text".equals(condition.function().getText()) && "=".equals(condition.operator().getText())) {
							String val = Misc.strip(condition.StringLiteral().getText(), 1);
							if (!asString(candidate.node).equals(val))
								i.remove();
						} else {
							i.remove();
						}
					}						
				}

				nodes.clear();
				nodes.addAll(candidates);
			}
		});

		// Do the query; the listener above is doing the actual work
		xpathParser.query();

		return toJsonNodes(nodes);
	}
	
	private static String asString(JsonNode node) {
		if (node.isTextual()) {
			return node.asText();
		} else {
			return node.toString();
		}
	}
	
	private static List<JsonNode> toJsonNodes(List<Node> nodes) {
		List<JsonNode> r = new ArrayList<>();
		for (Node node : nodes)
			r.add(node.node);
		return r;
	}
	
	private static List<Node> getChildren(List<Node> nodes) {
		List<Node> r = new ArrayList<>();
		for (Node node : nodes) {
			Iterator<Map.Entry<String, JsonNode>> i = node.node.fields();
			while (i.hasNext()) {
				Map.Entry<String, JsonNode> entry = i.next();
				if (entry.getValue() instanceof ArrayNode) {
					Iterator<JsonNode> k = entry.getValue().elements();
					while (k.hasNext()) {
						r.add(new Node(entry.getKey(), k.next(), node));
					}
				} else {
					r.add(new Node(entry.getKey(), entry.getValue(), node));
				}
			}
		}
		return r;
	}

	private static List<Node> getParents(List<Node> nodes) {
		List<Node> r = new ArrayList<>();
		for (Node node : nodes)
			if (node.parent != null)
				r.add(node.parent);
		return r;
	}


	private static List<Node> getDescendants(List<Node> elems) {
		List<Node> r = getChildren(elems);
		if (r.size() > 0)
			r.addAll(getDescendants(r));
		return r;
	}
	
	private static class Node {
		private String name;
		private JsonNode node;
		private Node parent;
		
		public Node(String name, JsonNode node, Node parent) {
			this.name = name;
			this.node = node;
			this.parent = parent;
		}
	}
}

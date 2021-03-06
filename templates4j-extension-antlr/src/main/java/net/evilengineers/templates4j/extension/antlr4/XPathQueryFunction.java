package net.evilengineers.templates4j.extension.antlr4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathBaseListener;
import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathLexer;
import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathParser;
import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathParser.ConditionContext;
import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathParser.QueryStepContext;
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

	protected AntlrXPathParser createParser(String query) {
		return new AntlrXPathParser(new CommonTokenStream(new AntlrXPathLexer(new ANTLRInputStream(query))));
	}
	
	public Object execute(ParserInterpreterProvider ctx, final ParseTree model, String query) {
		final Parser parser = ctx.getParserInterpreter();
		
		final List<ParseTree> nodes = new ArrayList<>();

		AntlrXPathParser xpathParser = createParser(query);
		xpathParser.addParseListener(new AntlrXPathBaseListener() {
			boolean first = true;
			
			@Override
			public void exitQueryStep(QueryStepContext ctx) {
				List<ParseTree> candidates = new ArrayList<>();

				// The initial model is setup up differently depending on type of query (absolute or relative) 
				if (first) {
					if (ctx.Next() == null && ctx.Any() == null) {
						nodes.add(model);
					} else {
						ParseTree t = model;
						while (t.getParent() != null)
							t = t.getParent();
						ParserRuleContext fakeroot = new ParserRuleContext();
						fakeroot.children = Arrays.asList(t);
						nodes.add(fakeroot);
					}
					first = false;
				}
				
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
				int ruleIdx = parser.getRuleIndex(name);
				
				if (!"*".equals(name) && ruleIdx >= 0) {
					Iterator<ParseTree> i = candidates.iterator();
					while (i.hasNext()) {
						ParseTree candidate = i.next();
						if (!(candidate instanceof RuleContext) || ((RuleContext) candidate).getRuleIndex() != ruleIdx)
							i.remove();
					}
				}
				
				// Condition filtering
				ConditionContext condition = ctx.condition();
				if (condition != null) {
					Iterator<ParseTree> i = candidates.iterator();
					while (i.hasNext()) {
						ParseTree candidate = i.next();
						if ("text".equals(condition.function().getText()) && "=".equals(condition.operator().getText())) {
							String val = Misc.strip(condition.StringLiteral().getText(), 1);
							
							if (condition.arg() != null) {
								int tokenIdx = Integer.parseInt(condition.arg().getText());
								if (!candidate.getChild(tokenIdx).getText().equals(val))
									i.remove();
								
							} else {
								if (!candidate.getText().equals(val))
									i.remove();
							}
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

		return nodes;
	}
	
	protected static List<ParseTree> getChildren(List<ParseTree> elems) {
		List<ParseTree> r = new ArrayList<>();
		for (ParseTree elem : elems)
			for (int i = 0; i < elem.getChildCount(); i++)
				r.add(elem.getChild(i));
		return r;
	}

	protected static List<ParseTree> getParents(List<ParseTree> elems) {
		List<ParseTree> r = new ArrayList<>();
		for (ParseTree elem : elems)
			if (elem.getParent() != null)
				r.add(elem.getParent());
		return r;
	}
	
	protected static List<ParseTree> getDescendants(List<ParseTree> elems) {
		List<ParseTree> r = getChildren(elems);
		if (r.size() > 0)
			r.addAll(getDescendants(r));
		return r;
	}
}

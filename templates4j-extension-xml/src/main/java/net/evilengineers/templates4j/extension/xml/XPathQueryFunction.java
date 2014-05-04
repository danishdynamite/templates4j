package net.evilengineers.templates4j.extension.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

	public Object execute(Node model, String path) throws XPathExpressionException {
		List<Node> r = new ArrayList<>();
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodelist = (NodeList) xpath.evaluate(path, model, XPathConstants.NODESET);
		for (int i = 0; i < nodelist.getLength(); i++)
			r.add(nodelist.item(i));

		return r;
	}
}

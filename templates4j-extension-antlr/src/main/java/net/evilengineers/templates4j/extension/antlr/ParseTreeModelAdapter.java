package net.evilengineers.templates4j.extension.antlr;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.evilengineers.templates4j.Interpreter;
import net.evilengineers.templates4j.ModelAdapter;
import net.evilengineers.templates4j.ST;
import net.evilengineers.templates4j.misc.STNoSuchPropertyException;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.apache.commons.lang3.StringUtils;

public class ParseTreeModelAdapter implements ModelAdapter {
	private Parser parser;
	private List<String> rules;
	
	public ParseTreeModelAdapter(Parser parser) {
		this.parser = parser;
		this.rules = Arrays.asList(parser.getRuleNames());
	}
	
	@Override
	public Object getProperty(Interpreter interpreter, ST self, Object o, Object property, String propertyName) throws STNoSuchPropertyException {
//		System.err.println("o.class: " + o.getClass() + "  prop: " + propertyName);
		if (o instanceof ParserRuleContext) {
			if (propertyName.endsWith("_list")) {
				propertyName = propertyName.substring(0, propertyName.length() - 5);
				return AntlrUtils.filterByRule(((ParserRuleContext) o).children, propertyName, rules);

			} else if (propertyName.endsWith("_value")) {
				propertyName = propertyName.substring(0, propertyName.length() - 6);
				List<ParseTree> r = AntlrUtils.filterByRule(((ParserRuleContext) o).children, propertyName, rules);
				return r.size() > 0 ? r.get(0).getText() : null;

			} else if (propertyName.equals("parent")) {
				return ((ParserRuleContext) o).getParent();

			} else if (propertyName.equals("filter")) {
				return new FilterList(((ParserRuleContext) o).children, rules);
				
			} else if (propertyName.equals("xpath")) {
			
//				Collection<ParseTree> r = XPath.findAll((ParseTree) o, "/dictionary/vendor/vendorInfo/vendorName='Altiga'/parent::", parser);
				Collection<ParseTree> r = XPath.findAll((ParseTree) o, "/dictionary/vendor/vendorInfo/vendorName[text()='Altiga']/parent::vendorInfo", parser);
				return r.iterator().next();
			
			} else {
				List<ParseTree> r = AntlrUtils.filterByRule(((ParserRuleContext) o).children, propertyName, rules);
				return r.size() > 0 ? r.get(0) : null;
			}
			
		} else if (o instanceof FilterList) {
			if (propertyName.equals("isEmpty")) {
				return ((FilterList) o).isEmpty();
				
			} else {
				String[] tokens = StringUtils.split(propertyName, '=');
				return ((FilterList) o).filter(tokens[0], tokens.length > 1 ? tokens[1] : null);
			}				
		}
		
		throw new STNoSuchPropertyException(null, property, propertyName);
	}
}

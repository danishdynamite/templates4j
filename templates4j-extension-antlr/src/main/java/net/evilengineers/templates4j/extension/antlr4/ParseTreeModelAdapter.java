package net.evilengineers.templates4j.extension.antlr4;

import java.util.Arrays;
import java.util.List;

import net.evilengineers.templates4j.Interpreter;
import net.evilengineers.templates4j.ModelAdapter;
import net.evilengineers.templates4j.ST;
import net.evilengineers.templates4j.misc.ObjectModelAdapter;
import net.evilengineers.templates4j.misc.STNoSuchPropertyException;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class ParseTreeModelAdapter implements ModelAdapter {
	private List<String> rules;
	
	public ParseTreeModelAdapter(Parser parser) {
		this.rules = Arrays.asList(parser.getRuleNames());
	}
	
	@Override
	public Object getProperty(Interpreter interpreter, ST self, Object o, Object property, String propertyName) throws STNoSuchPropertyException {
		if (o instanceof ParserRuleContext) {
			List<ParseTree> r = AntlrUtils.filterByRule(((ParserRuleContext) o).children, propertyName, rules);
			if (!r.isEmpty()) {
				return r.get(0);
			} else {
				return new ObjectModelAdapter().getProperty(interpreter, self, o, property, propertyName);
			}
		}
		
		throw new STNoSuchPropertyException(null, property, propertyName);
	}
}

package net.evilengineers.templates4j.extension.antlr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

public class FilterList extends ArrayList<ParseTree> {
	private static final long serialVersionUID = 1L;

	private List<String> rules;
	
	public FilterList(Collection<ParseTree> elems, List<String> rules) {
		super(elems);
		this.rules = rules;
	}
	
	public FilterList filter(String ruleName, String ruleValue) {
		if (ruleValue == null) {
			int ruleId = AntlrUtils.getRuleId(ruleName, rules);
			if (ruleId >= 0) {
				Iterator<ParseTree> i = iterator();
				while (i.hasNext()) {
					ParseTree elem = i.next();
					if (elem instanceof RuleNode) {
						if (((RuleNode) elem).getRuleContext().getRuleIndex() != ruleId) {
							i.remove();
						} else if (ruleValue != null && !((RuleNode) elem).getRuleContext().getText().equals(ruleValue)) {
							i.remove();
						}
					}
				}
			}
		} else {
			int ruleId = AntlrUtils.getRuleId(ruleName, rules);
			if (ruleId >= 0) {
				Iterator<ParseTree> i = iterator();
				while (i.hasNext()) {
					ParseTree elem = i.next();
					if (elem instanceof ParserRuleContext) {
						boolean f = false;
						for (ParseTree child : ((ParserRuleContext) elem).children) {
							if (child instanceof RuleNode && ((RuleNode) child).getRuleContext().getRuleIndex() == ruleId && ((RuleNode) child).getRuleContext().getText().equals(ruleValue)) {
								f = true;
							}
						}
						if (!f)
							i.remove();	
					} else {
						i.remove();
					}
				}
			}
		}
		return this;
	}	
}

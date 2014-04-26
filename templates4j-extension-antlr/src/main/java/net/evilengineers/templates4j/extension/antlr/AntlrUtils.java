package net.evilengineers.templates4j.extension.antlr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;

public final class AntlrUtils {
	public static String toStringTree(Tree t, String[] ruleNames, String[] tokenNames) {
		return toStringTree(t, ruleNames, tokenNames, 0);
	}

	public static String toStringTree(Tree t, String[] ruleNames, String[] tokenNames, int indent) {
		String s = Utils.escapeWhitespace(getNodeText(t, ruleNames, tokenNames), false);
		if (t.getChildCount() == 0)
			return s;
		StringBuilder buf = new StringBuilder();
		buf.append("(");
		s = Utils.escapeWhitespace(getNodeText(t, ruleNames, tokenNames), false);
		buf.append(s);
		buf.append(" ");

		boolean inlined = true;

		for (int i = 0; i < t.getChildCount(); i++) {
			if (t.getChild(i).getChildCount() > 1) {
				inlined = false;
			}
		}

		for (int i = 0; i < t.getChildCount(); i++) {
			if (inlined) {
				if (i > 0)
					buf.append(" ");
				buf.append(toStringTree(t.getChild(i), ruleNames, tokenNames, indent + 1));
				inlined = true;
			} else {
				buf.append("\n");
				for (int k = 0; k < indent + 1; k++)
					buf.append("    ");
				buf.append(toStringTree(t.getChild(i), ruleNames, tokenNames, indent + 1));
				inlined = false;
			}
		}

		if (!inlined) {
			buf.append("\n");
			for (int k = 0; k < indent; k++)
				buf.append("    ");
		}
		buf.append(")");
		return buf.toString();
	}

	public static String getNodeText(Tree t, String[] ruleNames, String[] tokenNames) {
		if (ruleNames != null) {
			if (t instanceof RuleNode) {
				int ruleIndex = ((RuleNode) t).getRuleContext().getRuleIndex();
				return ruleNames[ruleIndex];
			} else if (t instanceof ErrorNode) {
				return t.toString();
			} else if (t instanceof TerminalNode) {
				Token symbol = ((TerminalNode) t).getSymbol();
				if (symbol != null) {
					return "'" + symbol.getText() + "'";
				}
			}
		}
		// no recog for rule names
		Object payload = t.getPayload();
		if (payload instanceof Token) {
			return ((Token) payload).getText();
		}
		return t.getPayload().toString();
	}
	
	public static List<ParseTree> filterByRule(List<ParseTree> list, String ruleName, List<String> rules) {
		if (list != null) {
			int i = getRuleId(ruleName, rules);
			if (i >= 0)
				return filterByRule(list, i);
		}
		return new ArrayList<>();
	}

	public static int getRuleId(String ruleName, List<String> rules) {
		for (int i = 0; i < rules.size(); i++) {
			if (rules.get(i).equals(ruleName)) {
				return i;
			}
		}
		return -1;
	}
	
	public static List<ParseTree> filterByRule(List<ParseTree> list, int ruleId) {
		List<ParseTree> r = new ArrayList<>();
		if (list != null) {
			for (ParseTree elem : list) {
				if (elem instanceof RuleNode && ((RuleNode) elem).getRuleContext().getRuleIndex() == ruleId) {
					r.add(elem);
				}
			}
		}
		return r;
	}
}

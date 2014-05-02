package net.evilengineers.templates4j.extension.antlr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathBaseListener;
import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathLexer;
import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathParser;
import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathParser.AnyQueryElementContext;
import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathParser.NextQueryElementContext;
import net.evilengineers.templates4j.spi.UserFunction;

public class XPathQueryFunction extends UserFunction {
	
	public XPathQueryFunction(ParserInterpreter interpreter) {
	}
	
	@Override
	public String getName() {
		return "xpath";
	}

	public Object execute(final Parser parser, ParseTree tree, String path) {
		try {
			final List<Expression> expressionChain = new ArrayList<>();
			
			AntlrXPathLexer lexer = new AntlrXPathLexer(new ANTLRInputStream(path));
			CommonTokenStream tokenStream = new CommonTokenStream(lexer);
			final AntlrXPathParser xpathParser = new AntlrXPathParser(tokenStream);
			xpathParser.addParseListener(new AntlrXPathBaseListener() {
				Expression currentExpr; 
				
				@Override
				public void exitAnyQueryElement(AnyQueryElementContext ctx) {
					System.err.println("exitAnyQueryElement: " + AntlrUtils.toStringTree(ctx, xpathParser.getRuleNames(), xpathParser.getTokenNames()));
				}
				
				@Override
				public void exitNextQueryElement(NextQueryElementContext ctx) {
					System.err.println("exitNextQueryElement: " + AntlrUtils.toStringTree(ctx, xpathParser.getRuleNames(), xpathParser.getTokenNames()));
					expressionChain.add(currentExpr = new RuleIdExpr(parser.getRuleIndex(ctx.queryElement().name().getText())));
				}
			});
			
			ParseTree parsetree = xpathParser.query();
			
			System.err.println("xpath tree: \n" + parsetree.getChildCount() + "\n" + AntlrUtils.toStringTree(parsetree, xpathParser.getRuleNames(), xpathParser.getTokenNames()));
			
			System.err.println("data tree: \n" + tree.getChildCount() + "\n" + AntlrUtils.toStringTree(tree, parser.getRuleNames(), parser.getTokenNames()));

			System.err.println("rules: " + Arrays.toString(parser.getRuleNames()));

			ParserRuleContext fakeroot = new ParserRuleContext();
			fakeroot.children = Arrays.asList(tree);

			List<ParseTree> elems = new ArrayList<>();
			elems.add(fakeroot);
			for (Expression expr : expressionChain) {
				System.err.println("elems.size: " + elems.size() + " " + elems);
				elems = expr.eval(elems);
			}
			System.err.println("Result: " + elems);
			
			return "min xpath2";
		} catch (Throwable t) {
			t.printStackTrace();
			return "";
		}
	}
	
	private interface Expression {
		public List<ParseTree> eval(List<ParseTree> elems);
	}
	
	private class RuleIdExpr implements Expression {
		private int ruleId;
		
		public RuleIdExpr(int ruleId) {
			this.ruleId = ruleId;
			System.err.println("RuleIdExpr " + ruleId);
		}
		
		@Override
		public List<ParseTree> eval(List<ParseTree> elems) {
			List<ParseTree> r = new ArrayList<>();
			for (ParseTree elem : elems) {
				int n = elem.getChildCount();
				System.err.println("looking at n: " + n);
				for (int i = 0; i < n; i++) {
					ParseTree child = elem.getChild(i);
					if (child instanceof RuleContext) {
						System.err.println("Child rule: " + ((RuleContext) child).getRuleIndex() + "  -- looking for " + ruleId);
						if (ruleId == ((RuleContext) child).getRuleIndex()) {
							System.err.println("Bingo!");
							r.add(child);
						}
					}
				}
			}
			return r;
		}		
	}
}

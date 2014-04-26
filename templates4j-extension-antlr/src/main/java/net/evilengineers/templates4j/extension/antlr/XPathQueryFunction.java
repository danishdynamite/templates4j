package net.evilengineers.templates4j.extension.antlr;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTree;

import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathLexer;
import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathParser;
import net.evilengineers.templates4j.spi.UserFunction;

public class XPathQueryFunction extends UserFunction {
	@Override
	public String getName() {
		return "xpath";
	}

	public Object execute(Object tree, String path) {
		try {
			AntlrXPathLexer lexer = new AntlrXPathLexer(new ANTLRInputStream(path));
			CommonTokenStream tokenStream = new CommonTokenStream(lexer);
			final AntlrXPathParser parser = new AntlrXPathParser(tokenStream);
			/*parser.addParseListener(new AntlrXPathBaseListener() {
				@Override
				public void exitNormalSelector(NormalSelectorContext ctx) {
					System.err.println("exitNormalSelector: " + AntlrUtils.toStringTree(ctx, Arrays.asList(parser.getRuleNames())));
					
				}
			})*/;
			ParseTree parsetree = parser.query();
			
			System.err.println("xpath tree: \n" + parsetree.getChildCount() + "\n" + AntlrUtils.toStringTree(parsetree, parser.getRuleNames(), parser.getTokenNames()));
			
			return "min xpath2";
		} catch (Throwable t) {
			t.printStackTrace();
			return "";
		}
	}
}

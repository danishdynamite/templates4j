package net.evilengineers.templates4j.extension.antlr4;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

public class XPathQueryFirstResultFunction extends XPathQueryFunction {

	@Override
	public String getName() {
		return "xpath1";
	}

	public Object execute(ParserInterpreterProvider ctx, final ParseTree model, String path) {
		Object r = super.execute(ctx, model, path);
		if (r instanceof List<?>) {
			return ((List<?>) r).size() > 0 ? ((List<?>) r).get(0) : null;
		} else {
			return r;
		}
	}
}

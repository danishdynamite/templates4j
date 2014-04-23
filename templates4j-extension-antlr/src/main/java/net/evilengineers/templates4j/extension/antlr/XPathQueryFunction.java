package net.evilengineers.templates4j.extension.antlr;

import net.evilengineers.templates4j.spi.UserFunction;

public class XPathQueryFunction extends UserFunction {
	@Override
	public String getName() {
		return "xpath";
	}

	public Object execute(Object tree, String path) {
		return "min xpath";
	}
}

package net.evilengineers.templates4j.extension.strings;

import net.evilengineers.templates4j.spi.UserFunction;

public class ToUppercaseStringFunction extends UserFunction {
	@Override
	public String getName() {
		return "toUpper";
	}

	public Object execute(String val) {
		return val.toUpperCase();
	}
}

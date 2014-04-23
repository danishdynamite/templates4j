package net.evilengineers.templates4j.extension.strings;

import net.evilengineers.templates4j.spi.UserFunction;

public class ToLowercaseStringFunction extends UserFunction {
	@Override
	public String getName() {
		return "toLower";
	}

	public Object execute(String val) {
		return val.toLowerCase();
	}
}

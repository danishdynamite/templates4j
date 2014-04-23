package net.evilengineers.templates4j.extension.java;

import net.evilengineers.templates4j.spi.UserFunction;

import org.apache.commons.lang3.StringUtils;

public class ToJavaEnumValueFunction extends UserFunction {
	@Override
	public String getName() {
		return "toJavaClassName";
	}

	public Object execute(String val) {
		String s = val.replace("-", "").replace(".", "");
		StringBuilder sb = new StringBuilder();
		String[] tokens = StringUtils.splitByCharacterTypeCamelCase(s);
		for (int i = 0; i < tokens.length; i++) {
			sb.append(tokens[i].toUpperCase());
			if (i + 1 < tokens.length)
				sb.append("_");
		}
		return sb.toString();
	}
}

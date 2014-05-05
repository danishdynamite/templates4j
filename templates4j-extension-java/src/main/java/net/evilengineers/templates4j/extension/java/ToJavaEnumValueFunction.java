package net.evilengineers.templates4j.extension.java;

import net.evilengineers.templates4j.spi.UserFunction;

import org.apache.commons.lang3.StringUtils;

public class ToJavaEnumValueFunction extends UserFunction {
	@Override
	public String getNamespace() {
		String ns = getClass().getPackage().getName();
		return ns.substring(ns.lastIndexOf('.') + 1);
	}

	@Override
	public String getName() {
		return "toEnumName";
	}

	public Object execute(String arg) {
		String s = arg.toString().replace("-", "").replace(".", "");
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

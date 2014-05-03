package net.evilengineers.templates4j.extension.strings;

import org.apache.commons.lang3.StringUtils;

import net.evilengineers.templates4j.spi.UserFunction;

public class ToJavaEnumName extends UserFunction {
	@Override
	public String getName() {
		return "toJavaEnumName";
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

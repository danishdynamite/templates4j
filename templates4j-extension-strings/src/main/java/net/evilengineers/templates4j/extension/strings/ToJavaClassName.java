package net.evilengineers.templates4j.extension.strings;

import org.apache.commons.lang3.StringUtils;

import net.evilengineers.templates4j.spi.UserFunction;

public class ToJavaClassName extends UserFunction {
	@Override
	public String getName() {
		return "toJavaClassName";
	}

	public Object execute(String arg) {
		StringBuilder sb = new StringBuilder();
		for (String elem : StringUtils.splitByCharacterTypeCamelCase(arg.toString().replace("-", "").replace(".", ""))) {
			sb.append(StringUtils.capitalize(elem.toLowerCase()));
		}
		return sb.toString();
	}
}

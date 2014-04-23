package net.evilengineers.templates4j.extension.java;

import org.apache.commons.lang3.StringUtils;

import net.evilengineers.templates4j.spi.UserFunction;

public class ToJavaClassNameFunction extends UserFunction {
	@Override
	public String getName() {
		return "toJavaClassName";
	}

	public Object execute(String val) {
		StringBuilder sb = new StringBuilder();
		for (String elem : StringUtils.splitByCharacterTypeCamelCase(val.replace("-", "").replace(".", ""))) {
			sb.append(StringUtils.capitalize(elem.toLowerCase()));
		}
		return sb.toString();
	}
}

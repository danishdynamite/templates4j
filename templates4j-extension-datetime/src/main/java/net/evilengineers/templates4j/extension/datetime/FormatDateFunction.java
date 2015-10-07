package net.evilengineers.templates4j.extension.datetime;

import java.util.Date;
import java.util.Locale;

import net.evilengineers.templates4j.spi.UserFunction;

import org.apache.commons.lang3.time.DateFormatUtils;

public class FormatDateFunction extends UserFunction {
	@Override
	public String getNamespace() {
		String ns = getClass().getPackage().getName();
		return ns.substring(ns.lastIndexOf('.') + 1);
	}

	@Override
	public String getName() {
		return "formatDate";
	}

	public Object execute(Date date, String pattern) {
		return DateFormatUtils.format(date, pattern, Locale.getDefault());
	}
}

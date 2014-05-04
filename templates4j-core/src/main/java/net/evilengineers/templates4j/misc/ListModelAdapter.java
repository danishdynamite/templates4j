package net.evilengineers.templates4j.misc;

import java.util.List;

import net.evilengineers.templates4j.Interpreter;
import net.evilengineers.templates4j.ModelAdapter;
import net.evilengineers.templates4j.ST;

public class ListModelAdapter implements ModelAdapter {
	@Override
	public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName) throws STNoSuchPropertyException {
		int i = Integer.parseInt(propertyName);
		List<?> list = (List<?>) o;
		return i >= 0 && i < list.size() ? list.get(i) : null;
	}
}

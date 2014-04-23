package net.evilengineers.templates4j.spi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public abstract class UserFunction {
	private Method method;
	private Class<?>[] params;
	private String paramsAsString;
	private String toString;
	
	public UserFunction() {
		for (Method m : getClass().getMethods()) {
			if (m.getName().equals("execute")) {
				method = m;
				m.setAccessible(true);
				params = m.getParameterTypes();
				paramsAsString = Arrays.toString(params);
				paramsAsString = paramsAsString.substring(1, paramsAsString.length() - 1);
				toString = getClass() + ":" + method.getName() + "(" + paramsAsString + ")";
				break;
			}
		}
		if (method == null)
			throw new NoSuchMethodError("Expected to find a method named \"execute\"");
	}
	
	public Object doExecute(Object[] args) {
		for (int i = 0; i < args.length && i < params.length; i++) {
			if (!params[i].isAssignableFrom(args[i].getClass())) {
				throw new IllegalArgumentException("Argument #" + i + " of type: " + args[i].getClass() + " is not assignable to method parameter of type: " + params[i].getClass());
			}
		}
		try {
			return method.invoke(this, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException("Method invokation on " + toString() + " with args: " + Arrays.toString(args) + " (" + args.length + ") threw exception: ", e);
		}
	}
	
	@Override
	public String toString() {
		return toString;
	}

	public abstract String getName();	
}

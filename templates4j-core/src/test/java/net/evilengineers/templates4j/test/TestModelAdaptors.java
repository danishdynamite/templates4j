package net.evilengineers.templates4j.test;

import net.evilengineers.templates4j.*;
import net.evilengineers.templates4j.misc.STNoSuchPropertyException;
import net.evilengineers.templates4j.misc.STRuntimeMessage;

import org.junit.*;

import static org.junit.Assert.assertEquals;

public class TestModelAdaptors extends BaseTest {
	static class UserAdapter implements ModelAdapter {
		@Override
		public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName)
			throws STNoSuchPropertyException
		{
			if ( propertyName.equals("id") ) return ((User)o).id;
			if ( propertyName.equals("name") ) return ((User)o).getName();
			throw new STNoSuchPropertyException(null, o, "User."+propertyName);
		}
	}

	static class UserAdapterConst implements ModelAdapter {
		@Override
		public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName)
			throws STNoSuchPropertyException
		{
			if ( propertyName.equals("id") ) return "const id value";
			if ( propertyName.equals("name") ) return "const name value";
			throw new STNoSuchPropertyException(null, o, "User."+propertyName);
		}
	}

	static class SuperUser extends User {
		int bitmask;
		public SuperUser(int id, String name) {
			super(id, name);
			bitmask = 0x8080;
		}

		@Override
		public String getName() {
			return "super "+super.getName();
		}
	}

	@Test public void testSimpleAdaptor() throws Exception {
		String templates =
				"foo(x) ::= \"<x.id>: <x.name>\"\n";
		writeFile(tmpdir, "foo.stg", templates);
		STGroup group = new STGroupFile(tmpdir+"/foo.stg");
		group.registerModelAdaptor(User.class, new UserAdapter());
		ST st = group.getInstanceOf("foo");
		st.add("x", new User(100, "parrt"));
		String expecting = "100: parrt";
		String result = st.render();
		assertEquals(expecting, result);
	}

	@Test public void testAdaptorAndBadProp() throws Exception {
		ErrorBufferAllErrors errors = new ErrorBufferAllErrors();
		String templates =
				"foo(x) ::= \"<x.qqq>\"\n";
		writeFile(tmpdir, "foo.stg", templates);
		STGroup group = new STGroupFile(tmpdir+"/foo.stg");
		group.setListener(errors);
		group.registerModelAdaptor(User.class, new UserAdapter());
		ST st = group.getInstanceOf("foo");
		st.add("x", new User(100, "parrt"));
		String expecting = "";
		String result = st.render();
		assertEquals(expecting, result);

		STRuntimeMessage msg = (STRuntimeMessage)errors.errors.get(0);
		STNoSuchPropertyException e = (STNoSuchPropertyException)msg.cause;
		assertEquals("User.qqq", e.propertyName);
	}

	@Test public void testAdaptorCoversSubclass() throws Exception {
		String templates =
				"foo(x) ::= \"<x.id>: <x.name>\"\n";
		writeFile(tmpdir, "foo.stg", templates);
		STGroup group = new STGroupFile(tmpdir+"/foo.stg");
		group.registerModelAdaptor(User.class, new UserAdapter());
		ST st = group.getInstanceOf("foo");
		st.add("x", new SuperUser(100, "parrt")); // create subclass of User
		String expecting = "100: super parrt";
		String result = st.render();
		assertEquals(expecting, result);
	}

	@Test public void testWeCanResetAdaptorCacheInvalidatedUponAdaptorReset() throws Exception {
		String templates =
				"foo(x) ::= \"<x.id>: <x.name>\"\n";
		writeFile(tmpdir, "foo.stg", templates);
		STGroup group = new STGroupFile(tmpdir+"/foo.stg");
		group.registerModelAdaptor(User.class, new UserAdapter());
		group.getModelAdaptor(User.class); // get User, SuperUser into cache
		group.getModelAdaptor(SuperUser.class);

		group.registerModelAdaptor(User.class, new UserAdapterConst());
		// cache should be reset so we see new adaptor
		ST st = group.getInstanceOf("foo");
		st.add("x", new User(100, "parrt"));
		String expecting = "const id value: const name value"; // sees UserAdaptorConst
		String result = st.render();
		assertEquals(expecting, result);
	}

	@Test public void testSeesMostSpecificAdaptor() throws Exception {
		String templates =
				"foo(x) ::= \"<x.id>: <x.name>\"\n";
		writeFile(tmpdir, "foo.stg", templates);
		STGroup group = new STGroupFile(tmpdir+"/foo.stg");
		group.registerModelAdaptor(User.class, new UserAdapter());
		group.registerModelAdaptor(SuperUser.class, new UserAdapterConst()); // most specific
		ST st = group.getInstanceOf("foo");
		st.add("x", new User(100, "parrt"));
		String expecting = "100: parrt";
		String result = st.render();
		assertEquals(expecting, result);

		st.remove("x");
		st.add("x", new SuperUser(100, "parrt"));
		expecting = "const id value: const name value"; // sees UserAdaptorConst
		result = st.render();
		assertEquals(expecting, result);
	}
}

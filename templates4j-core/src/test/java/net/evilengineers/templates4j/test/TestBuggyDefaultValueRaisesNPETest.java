package net.evilengineers.templates4j.test;

import net.evilengineers.templates4j.ST;
import net.evilengineers.templates4j.STGroup;
import net.evilengineers.templates4j.STGroupFile;
import net.evilengineers.templates4j.misc.ErrorBuffer;

import org.junit.Assert;
import org.junit.Test;

public class TestBuggyDefaultValueRaisesNPETest extends BaseTest {
	/**
	 * When the anonymous template specified as a default value for a formalArg
	 * contains a syntax error ST 4.0.2 emits a NullPointerException error
	 * (after the syntax error)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testHandleBuggyDefaultArgument() throws Exception {
		String templates = "main(a={(<\"\")>}) ::= \"\"";
		writeFile(tmpdir, "t.stg", templates);

		final ErrorBuffer errors = new ErrorBuffer();
		STGroup group = new STGroupFile(tmpdir + "/t.stg");
		group.setListener(errors);

		ST st = group.getInstanceOf("main");
		st.render();

		// Check the errors. This contained an "NullPointerException" before
		Assert.assertEquals(
				"t.stg 1:12: mismatched input ')' expecting RDELIM"+newline,
				errors.toString());
	}
}

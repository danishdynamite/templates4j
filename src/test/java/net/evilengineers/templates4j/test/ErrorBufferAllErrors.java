package net.evilengineers.templates4j.test;

import net.evilengineers.templates4j.misc.ErrorBuffer;
import net.evilengineers.templates4j.misc.STMessage;

public class ErrorBufferAllErrors extends ErrorBuffer {
	@Override
	public void runTimeError(STMessage msg) {
		errors.add(msg);
	}
}

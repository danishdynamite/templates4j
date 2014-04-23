package net.evilengineers.templates4j.maven;

import java.security.SecureRandom;

import net.evilengineers.templates4j.spi.UserFunction;

import org.codehaus.plexus.util.Base64;

public class TemplateMarkerFunction extends UserFunction {
	public Object execute(String filename) {
		return fileStartMarker + filename + fileEndMarker;
	}

	@Override
	public String getName() {
		return "createFile";
	}

	public static String getFileStartMarker() {
		return fileStartMarker;
	}

	public static String getFileEndMarker() {
		return fileEndMarker;
	}
	
	private static String fileStartMarker;
	private static String fileEndMarker;
	
	static {
		// Create filemarkers used in the template output
		byte[] markerData = new byte[20];
		new SecureRandom().nextBytes(markerData);
		fileStartMarker = "====[" + new String(Base64.encodeBase64(markerData)) + "::";
		fileEndMarker = "::" + new String(Base64.encodeBase64(markerData)) + "]====";
	}
}

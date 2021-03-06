package net.evilengineers.templates4j;

import net.evilengineers.templates4j.compiler.*;
import net.evilengineers.templates4j.compiler.Compiler;
import net.evilengineers.templates4j.misc.Misc;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;

import java.net.URL;

/** A directory of templates without headers like ST v3 had.  Still allows group
 *  files in directory though like {@link STGroupDir} parent.
 */
public class STRawGroupDir extends STGroupDir {
	public STRawGroupDir(String dirName) {
		super(dirName);
	}

	public STRawGroupDir(String dirName, String delimiterStart, String delimiterStop) {
		super(dirName, delimiterStart, delimiterStop);
	}

	public STRawGroupDir(String dirName, String encoding) {
		super(dirName, encoding);
	}

	public STRawGroupDir(String dirName, String encoding, String delimiterStart, String delimiterStop) {
		super(dirName, encoding, delimiterStart, delimiterStop);
	}

	public STRawGroupDir(URL root, String encoding, String delimiterStart, String delimiterStop) {
		super(root, encoding, delimiterStart, delimiterStop);
	}

	@Override
	public CompiledST loadTemplateFile(String prefix, String unqualifiedFileName, CharStream templateStream) {
		String template = templateStream.substring(0, templateStream.size() - 1);
		String templateName = Misc.getFileNameNoSuffix(unqualifiedFileName);
		String fullyQualifiedTemplateName = prefix + templateName;
		CompiledST impl = new Compiler(this).compile(fullyQualifiedTemplateName, template);
		CommonToken nameT = new CommonToken(STLexer.SEMI); // Seems like a hack, best I could come up with.
		nameT.setInputStream(templateStream);
		rawDefineTemplate(fullyQualifiedTemplateName, impl, nameT);
		impl.defineImplicitlyDefinedTemplates(this);
		return impl;
	}
}

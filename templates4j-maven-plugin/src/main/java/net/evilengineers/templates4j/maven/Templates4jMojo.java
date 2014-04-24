package net.evilengineers.templates4j.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import net.evilengineers.templates4j.Interpreter;
import net.evilengineers.templates4j.ST;
import net.evilengineers.templates4j.STGroup;
import net.evilengineers.templates4j.STGroupFile;
import net.evilengineers.templates4j.extension.antlr.AntlrUtils;
import net.evilengineers.templates4j.extension.antlr.FilterList;
import net.evilengineers.templates4j.extension.antlr.ParseTreeModelAdapter;
import net.evilengineers.templates4j.spi.UserFunction;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.tool.Grammar;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.Scanner;

@Mojo(name="execute-templates", defaultPhase=LifecyclePhase.GENERATE_SOURCES, threadSafe=false, requiresDependencyResolution=ResolutionScope.COMPILE, requiresProject=true, instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class Templates4jMojo extends AbstractMojo {
	
	protected String name = "[Template4J-plugin]";
	
	@Parameter(property = "project.build.sourceEncoding")
	protected String encoding;
	
	@Component
    protected BuildContext ctx;
    
	@Parameter(property="project", required=true)
	protected MavenProject project;
	
	@Parameter(property="grammarFile", required=true)
	protected File grammarFile;

	@Parameter(property="grammarNameRoot", required=false)
	protected String grammarNameRoot;

	@Parameter(property="templateFile", required=true)
	protected File templateFile;

	@Parameter(property="templateName", required=false)
	protected String templateName;
	
	@Parameter(property="inputFile", required=true)
	protected File inputFile;

	@Parameter(property="outputFile", required=false)
	protected File outputFile;

	@Parameter(property="outputDirectory", required=true)
	protected File outputDirectory;

	@Parameter(property="traceTemplateInterpreter", required=true, defaultValue="false")
	protected Boolean traceTemplateInterpreter;

	@Parameter(property="printSyntaxTree", required=true, defaultValue="false")
	protected Boolean printSyntaxTree;

	protected Log log;

	public void execute() throws MojoExecutionException {
		log = getLog();

		info("== Starting ==");
		info(project.toString());
		info("Encoding: " + encoding);
		info("Grammar: " + grammarFile.getAbsolutePath());
		info("Template: " + templateFile.getAbsolutePath());
		info((inputFile.isDirectory() ? "Inputdirectory: " : "Inputfile: ") + inputFile.getAbsolutePath());
		info("Outputdirectory: " + outputDirectory.getAbsolutePath());
		if (outputFile != null)
			info("Outputfile: " + outputFile.getAbsolutePath());

		// Add output directory as a compile source root (must be done before anything else to get Eclipse to play nice)
		project.addCompileSourceRoot(outputDirectory.getAbsolutePath());

		// Figure out if we should do anything
		if (!isRebuildRequired())
			return;
		
		for (String existingSourceRoots : project.getCompileSourceRoots())
			info("Existing compile-source-root: " + existingSourceRoots);

		// Ensure output directory exists
		if (!outputDirectory.exists())
			outputDirectory.mkdirs();

		ctx.removeMessages(grammarFile);
		ctx.removeMessages(templateFile);
		ctx.removeMessages(inputFile);

		// Read grammar
		final Grammar grammer = Grammar.load(grammarFile.getAbsolutePath());
		info("Read grammar from: " + grammarFile.getName());

		// Setup the template interpreter
		Interpreter.trace = traceTemplateInterpreter;
		for (UserFunction fn : Interpreter.autoregisterUserFunctions())
			info("Autoregistered user-function: " + fn.getName() + " -> " + fn);
		
		// Read template / templategroup
		STGroup templateGroup;
		ST template;
		try {
			if (templateFile.getAbsolutePath().endsWith(".stg")) {
				templateGroup = new STGroupFile(templateFile.getAbsolutePath(), encoding);
				info("Read template group from: " + templateFile.getName());
				template = templateGroup.getInstanceOf(templateName);
				if (template != null) {
					info("Found requested template.");
				} else {
					error("Did not find template: " + templateName + " in group.");
					ctx.addMessage(null, 1, 1, "Did not find template: " + templateName, BuildContext.SEVERITY_ERROR, null);
					return;
				}
				
			} else if (templateFile.getAbsolutePath().endsWith(".st")) {
				templateGroup = new STGroup();
				
				template = new ST(templateGroup, IOUtil.toString(new FileReader(templateFile)));
				info("Read template file: " + templateFile.getName());
			} else {
				error("Unknown template extension for file: " + templateFile.getAbsolutePath());
				ctx.addMessage(null, 1, 1, "Unknown template extension for file: " + templateFile.getAbsolutePath(), BuildContext.SEVERITY_ERROR, null);
				return;
			}
		} catch (Exception e) {
			error("Failed to read template from: " + templateFile.getAbsolutePath(), e);
			ctx.addMessage(null, 1, 1, "Failed to read template from: " + templateFile.getAbsolutePath(), BuildContext.SEVERITY_ERROR, e);
			return;
		}
		
		try {
			info("Creating lexer.");
			LexerInterpreter lexEngine = grammer.createLexerInterpreter(new ANTLRInputStream(IOUtil.toString(new FileInputStream(inputFile.getAbsolutePath()), encoding)));
			ParserInterpreter parser = grammer.createParserInterpreter(new CommonTokenStream(lexEngine));
			parser.addParseListener(new ParseTreeListener() {
				@Override
				public void visitTerminal(TerminalNode node) {
				}
				
				@Override
				public void visitErrorNode(ErrorNode node) {
					error("Parser error at: " + node);
					ctx.addMessage(inputFile, node.getSymbol().getLine(), node.getSymbol().getCharPositionInLine(), "Parser error at: " + node, BuildContext.SEVERITY_ERROR, null);
				}
				
				@Override
				public void exitEveryRule(ParserRuleContext ctx) {
				}
				
				@Override
				public void enterEveryRule(ParserRuleContext ctx) { 
				}
			});
	
			// Register model adapters
			templateGroup.registerModelAdaptor(ParserRuleContext.class, new ParseTreeModelAdapter(parser));
			templateGroup.registerModelAdaptor(FilterList.class, new ParseTreeModelAdapter(parser));
			
			ParseTree grammarParseTree = parser.parse(grammarNameRoot != null ? grammer.getRule(grammarNameRoot).index : 0);
			if (printSyntaxTree)
				info("The AST for the grammar is:\n" + AntlrUtils.toStringTree(grammarParseTree, Arrays.asList(parser.getRuleNames())));
			
			template.add("tree", grammarParseTree);
			
			String data = template.render();

			int offsetStartMarker = data.indexOf(TemplateMarkerFunction.getFileStartMarker());
			if (offsetStartMarker < 0) {
				if (outputFile != null) {
					writeToFile(outputFile, data);
				} else {
					error("No filemarkers present in the template output and no outputFile was not specified.");
					ctx.addMessage(templateFile, 1, 1, "No filemarkers present in the template output and no outputFile was not specified.", BuildContext.SEVERITY_ERROR, null);
					return;
				}
			} else {
				do {
					int offsetEndMarker = data.indexOf(TemplateMarkerFunction.getFileEndMarker(), offsetStartMarker);
					if (offsetEndMarker < 0)
						throw new IOException("End of filemarker not found.  Should never happen.");
					
					String filename = data.substring(offsetStartMarker + TemplateMarkerFunction.getFileStartMarker().length(), offsetEndMarker);
	
					// Properly process directory part of filename
					info("Found file marker for: " + filename);
					File dir = outputDirectory;
					if (filename.contains("/")) {
						dir = new File(outputDirectory, filename.substring(0, filename.lastIndexOf("/")));
						dir.mkdirs();
						filename = filename.substring(filename.lastIndexOf("/") + 1);
					}
					info("Will write to: " + dir.getAbsolutePath() + "  filename: " + filename);
	
					// Find local EOF, which is the start of the next filemarker
					offsetStartMarker = data.indexOf(TemplateMarkerFunction.getFileStartMarker(), offsetEndMarker);
					if (offsetStartMarker < 0)
						offsetStartMarker = data.length();
					
					File outputFile = new File(dir, filename);				
					writeToFile(outputFile, data.substring(offsetEndMarker + TemplateMarkerFunction.getFileEndMarker().length(), offsetStartMarker));
					ctx.refresh(outputFile);
				} while (offsetStartMarker < data.length());
			}
		} catch (IOException e) {
			ctx.addMessage(null, 1, 1, "Unexpected exception", BuildContext.SEVERITY_ERROR, e);
			return;
		}
		
		info("Refreshing " + outputDirectory.getAbsoluteFile());
		ctx.refresh(outputDirectory.getAbsoluteFile());

		info("Done!");
	}
	
	private boolean isRebuildRequired() {
		if (ctx.isIncremental()) {
			if (ctx.hasDelta(grammarFile) || ctx.hasDelta(templateFile) || (inputFile.isFile() && ctx.hasDelta(inputFile))) {
				return true;
			} else if (inputFile.isDirectory()) {
				Scanner scanner = ctx.newScanner(inputFile, true);
				info("Scanning...");
				scanner.scan();
				if (scanner.getIncludedFiles() != null) {
					for (String includedFile : scanner.getIncludedFiles()) {
						if (ctx.hasDelta(new File(scanner.getBasedir(), includedFile))) {
							info("Changes detected.  Rebuilding.");
							return true;
						}
					}
				}
			}
			info("Nothing has changed.");
			return false;
		} else {
			info("Full rebuild requested.");
			return true;
		}
	}
	
	private void writeToFile(File file, String content) throws IOException {
		OutputStreamWriter os = new OutputStreamWriter(ctx.newFileOutputStream(file), "UTF-8");
		try {
			os.write(content);
			os.flush();
			info("Wrote " + content.length() + " chars to file: " + file.getAbsolutePath());
		} finally {
			os.close();
		}
	}	

	private final void info(CharSequence s) {
		log.info(name + ": " + s);
	}

	private final void error(CharSequence s) {
		log.error(name + ": " + s);
	}

	private final void error(CharSequence s, Throwable t) {
		log.error(name + ": " + s, t);
	}
}

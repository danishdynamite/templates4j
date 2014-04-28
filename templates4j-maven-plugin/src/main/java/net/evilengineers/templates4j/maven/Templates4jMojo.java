package net.evilengineers.templates4j.maven;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.BitSet;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.evilengineers.templates4j.Interpreter;
import net.evilengineers.templates4j.ST;
import net.evilengineers.templates4j.STErrorListener;
import net.evilengineers.templates4j.STGroup;
import net.evilengineers.templates4j.STGroupFile;
import net.evilengineers.templates4j.extension.antlr.AntlrUtils;
import net.evilengineers.templates4j.extension.antlr.FilterList;
import net.evilengineers.templates4j.extension.antlr.ParseTreeModelAdapter;
import net.evilengineers.templates4j.extension.antlr.XPathQueryFunction;
import net.evilengineers.templates4j.misc.STMessage;
import net.evilengineers.templates4j.spi.UserFunction;

import org.antlr.v4.Tool;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.tool.ANTLRMessage;
import org.antlr.v4.tool.ANTLRToolListener;
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
public class Templates4jMojo extends AbstractMojo implements ANTLRToolListener, STErrorListener, ANTLRErrorListener {
	
	protected String name = "[Template4J-plugin]";
	
	@Parameter(property = "project.build.sourceEncoding")
	protected String encoding;
	
	@Component
    protected BuildContext ctx;
    
	@Parameter(property="project", required=true)
	protected MavenProject project;
	
	@Parameter(property="grammarFile", required=false)
	protected File grammarFile;

	@Parameter(property="grammarNameRoot", required=false)
	protected String grammarNameRoot;

	@Parameter(property="templateFile", required=true)
	protected File templateFile;

	@Parameter(property="templateName", required=false)
	protected String templateName;
	
	@Parameter(property="inputFile", required=false)
	protected File inputFile;

	@Parameter(property="outputFile", required=false)
	protected File outputFile;

	@Parameter(property="outputDirectory", required=false)
	protected File outputDirectory;

	@Parameter(property="traceTemplateInterpreter", required=true, defaultValue="false")
	protected Boolean traceTemplateInterpreter;

	@Parameter(property="printSyntaxTree", required=true, defaultValue="false")
	protected Boolean printSyntaxTree;

	protected Log log;
	
	public void execute() throws MojoExecutionException {
		try {
			log = getLog();
	
			info("== Starting ==");
			info(project.toString());
			info("Encoding: " + encoding);
			info("Template: " + templateFile.getAbsolutePath());
			if (grammarFile != null)
				info("Grammar: " + grammarFile.getAbsolutePath());
			if (inputFile != null)
				info((inputFile.isDirectory() ? "Inputdirectory: " : "Inputfile: ") + inputFile.getAbsolutePath());
			if (outputDirectory != null)
				info("Outputdirectory: " + outputDirectory.getAbsolutePath());
			if (outputFile != null)
				info("Outputfile: " + outputFile.getAbsolutePath());
	
			if (outputDirectory == null) {
				if (outputFile != null) {
					outputDirectory = outputFile.getParentFile();
				} else {
					error("Neither outputDirectory nor outputFile was specified.", null);
				}
			}
			
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
	
			ctx.removeMessages(templateFile);
			if (grammarFile != null)
				ctx.removeMessages(grammarFile);
			if (inputFile != null)
				ctx.removeMessages(inputFile);
	
			// Read grammar
			Grammar grammer = null;
			if (grammarFile != null) {
				Tool antrl = new Tool();
				antrl.addListener(this);
				grammer = antrl.loadGrammar(grammarFile.getAbsolutePath());
				info("Read grammar from: " + grammarFile.getName());
			}
			
			// Setup the template interpreter
			Interpreter.trace = traceTemplateInterpreter;
			for (UserFunction fn : Interpreter.autoregisterUserFunctions())
				info("Autoregistered user-function: " + fn.getName() + " -> " + fn);
			
			Interpreter.registerUserFunction("xpath", new XPathQueryFunction());
			Interpreter.registerUserFunction("xpath:generateLogClass", new XPathQueryFunction());

			// Read template / templategroup
			STGroup templateGroup;
			ST template;
			if (templateFile.getAbsolutePath().endsWith(".stg")) {
				templateGroup = new STGroupFile(templateFile.getAbsolutePath(), encoding);
				templateGroup.setListener(this);
				info("Read template group from: " + templateFile.getName());
				template = templateGroup.getInstanceOf(templateName);
				if (template != null) {
					info("Found requested template.");
				} else {
					throw new SomethingWentWrongException(templateFile, 1, 1, "Did not find template \"" + templateName + "\" in the template group: " + templateFile.getName());
				}
				
			} else if (templateFile.getAbsolutePath().endsWith(".st")) {
				templateGroup = new STGroup();
				templateGroup.setListener(this);
				
				String contents;
				try {
					contents = IOUtil.toString(new FileReader(templateFile));
				} catch (IOException e) {
					throw new SomethingWentWrongException(templateFile, 1, 1, "Unable to load template from: " + templateFile.getAbsolutePath(), e);	
				}
				template = new ST(templateGroup, contents);
				info("Read template file: " + templateFile.getName());
			} else {
				throw new SomethingWentWrongException(templateFile, 1, 1, "Unknown template extension for file: " + templateFile.getName());
			}
			
			if (grammer != null) {
				info("Creating lexer.");
				LexerInterpreter lexEngine;
				try {
					lexEngine = grammer.createLexerInterpreter(new ANTLRFileStream(inputFile.getAbsolutePath(), encoding));
				} catch (IOException e) {
					throw new SomethingWentWrongException(inputFile, 1, 1, "Error reading inputfile: " + e.getMessage(), e);
				}
				
				info("Creating parser.");
				ParserInterpreter parser = grammer.createParserInterpreter(new CommonTokenStream(lexEngine));
				parser.addErrorListener(this);
				parser.addParseListener(new ParseTreeListener() {
					@Override
					public void visitTerminal(TerminalNode node) {
					}
					
					@Override
					public void visitErrorNode(ErrorNode node) {
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
					info("The AST for the grammar is:\n" + AntlrUtils.toStringTree(grammarParseTree, parser.getRuleNames(), parser.getTokenNames()));
				
				template.add("parsetree", grammarParseTree);
			}
			
			template.add("caller", this);
			
			String data = template.render();
	
			try {
				int offsetStartMarker = data.indexOf(TemplateMarkerFunction.getFileStartMarker());
				if (offsetStartMarker < 0) {
					if (outputFile != null) {
						writeToFile(outputFile, data);
					} else {
						throw new SomethingWentWrongException(templateFile, 1, 1, "No filemarkers present in the template output and no outputFile was not specified.");
					}
				} else {
					do {
						int offsetEndMarker = data.indexOf(TemplateMarkerFunction.getFileEndMarker(), offsetStartMarker);
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
				throw new SomethingWentWrongException(templateFile, 1, 1, "Error writing outputfile: ", e);
			}
			
			info("Refreshing " + outputDirectory.getAbsoluteFile());
			ctx.refresh(outputDirectory.getAbsoluteFile());
	
			info("Done!!");
			
		} catch (SomethingWentWrongException e) {
			if (e.getCause() != null && e.getCause() instanceof SomethingWentWrongException) 
				e = (SomethingWentWrongException) e.getCause();
			
			error((e.getFile() != null ? ("[" + e.getFile().getName() + "]: ") : "") + e.getMessage(), e.getCause());
			ctx.addMessage(e.getFile(), e.getLine(), e.getPos(), e.getMessage(), BuildContext.SEVERITY_ERROR, e.getCause());
		}
	}
	
	private boolean isRebuildRequired() {
		if (ctx.isIncremental()) {
			if ((grammarFile != null && ctx.hasDelta(grammarFile)) 
					|| ctx.hasDelta(templateFile) 
					|| (inputFile != null && inputFile.isFile() && ctx.hasDelta(inputFile))) {
				return true;
			} else if (inputFile != null && inputFile.isDirectory()) {
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

	public File getTemplateFile() {
		return templateFile;
	}
	
	public File getInputFile() {
		return inputFile;
	}
	
	public MavenProject getProject() {
		return project;
	}
	
	public Date getBuildTime() {
		if (getProject().getProjectBuildingRequest() != null && getProject().getProjectBuildingRequest().getBuildStartTime() != null)
			return getProject().getProjectBuildingRequest().getBuildStartTime();

		// The data above is not available during an m2e-build, so lets provide a reasonable default: 
		return new Date();
	}
	
	private final void error(CharSequence s, Throwable t) {
		if (t == null) {
			log.error(name + ": " + s);
		} else {
			log.error(name + ": " + s, t);
		}
	}

	@Override
	public final void info(String s) {
		log.info(name + ": " + s);
	}

	@Override
	public final void error(ANTLRMessage msg) {
		String s = msg.getMessageTemplate(false).render();
		throw new SomethingWentWrongException(msg.fileName != null ? new File(msg.fileName) : null, msg.line, msg.charPosition + 1, s, msg.getCause());
	}

	@Override
	public final void warning(ANTLRMessage msg) {
		String s = msg.getMessageTemplate(false).render();
		log.warn(name + ": " + s);
		if (msg.fileName != null)
			ctx.addMessage(new File(msg.fileName), msg.line, msg.charPosition, s, BuildContext.SEVERITY_WARNING, msg.getCause());
	}

	@Override
	public void compileTimeError(STMessage msg) {
		int line = 1;
		int pos = 1;
		Matcher m = Pattern.compile(".* (\\d+):(\\d+).*").matcher(msg.toString());
		if (m.matches()) {
			line = Integer.parseInt(m.group(1));
			pos = Integer.parseInt(m.group(2));
		}
		throw new SomethingWentWrongException(templateFile, line + 1, pos + 1, "Compiletime error: " + msg, msg.cause);
	}

	@Override
	public void runTimeError(STMessage msg) {
		if (msg.cause != null && msg.cause.getMessage() != null && msg.cause instanceof SomethingWentWrongException) {
			throw (SomethingWentWrongException) msg.cause;
		} else {
			String s = msg.toString();
			String search = "SomethingWentWrongException: ";
			int i = s.lastIndexOf(search);
			if (i >= 0)
				s = s.substring(i + search.length());
			search = "Runtime error: ";
			if (s.startsWith(search))
				s = s.substring(search.length());
			throw new SomethingWentWrongException(templateFile, 1, 1, "Runtime error: " + s, msg.cause);
		}
	}

	@Override
	public void IOError(STMessage msg) {
		if (msg.cause != null && msg.cause.getMessage() != null) {
			throw new SomethingWentWrongException(templateFile, 1, 1, "I/O error: " + msg.cause.getMessage(), msg.cause);
		} else {
			throw new SomethingWentWrongException(templateFile, 1, 1, "I/O error: " + msg, msg.cause);
		}
	}

	@Override
	public void internalError(STMessage msg) {
		throw new SomethingWentWrongException(templateFile, 1, 1, "Internal error: " + msg, msg.cause);
	}

	@Override
	public void reportAmbiguity(Parser parser, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
	}

	@Override
	public void reportAttemptingFullContext(Parser parser, DFA dfa, int startIndex, int stopIndex, BitSet ambigAlts, ATNConfigSet configs) {
	}

	@Override
	public void reportContextSensitivity(Parser parser, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
	}

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
		throw new SomethingWentWrongException(inputFile, line, charPositionInLine + 1, "Syntax error: " + msg, e);
	}
	
	public class SomethingWentWrongException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
		private File file;
		private int line;
		private int pos;
		
		public SomethingWentWrongException(File file, int line, int pos, String msg) {
			this(file, line, pos, msg, null);
		}
		
		public SomethingWentWrongException(File file, int line, int pos, String msg, Throwable e) {
			super(msg, e);
			this.file = file;
			this.line = line;
			this.pos = pos;
		}

		public File getFile() {
			return file;
		}

		public int getLine() {
			return line;
		}

		public int getPos() {
			return pos;
		}
	}
}

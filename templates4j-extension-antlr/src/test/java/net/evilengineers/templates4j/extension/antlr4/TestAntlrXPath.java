/*
 [The "BSD license"]
 Copyright (c) 2009 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.evilengineers.templates4j.extension.antlr4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import net.evilengineers.templates4j.extension.antlr.xpath.AntlrXPathParser;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.*;

import static net.evilengineers.templates4j.misc.AntlrUtils.*;
import static org.junit.Assert.*;

public class TestAntlrXPath {
	private static final boolean OUTPUT = false;
	
	XPathQueryFunction fn;
	AntlrErrorListener listener;
	AntlrXPathParser parser;
	
    @Before
    public void setUp() {
		fn = new XPathQueryFunction();
		listener = new AntlrErrorListener();
    }	
	    
	@Test
	public void testQuery1() throws Exception {
		ParseTree t = test("/a/b/c");
		assertNoParseErrors();
		String expected = "(query (queryStep '/' (name 'a')) (queryStep '/' (name 'b')) (queryStep '/' (name 'c')))";
		String actual = toStringTree(t, parser.getRuleNames(), parser.getTokenNames());
		print(expected, actual);
		assertEquals(expected, actual);
	}

	@Test
	public void testQuery2() throws Exception {
		ParseTree t = test("/a/b/c[text()='qwe']");
		assertNoParseErrors();
		String expected = "(query (queryStep '/' (name 'a')) (queryStep '/' (name 'b')) (queryStep '/' (name 'c') '[' (condition (function 'text') '(' ')' (operator '=') ''qwe'') ']'))";
		String actual = toStringTree(t, parser.getRuleNames(), parser.getTokenNames());
		print(expected, actual);
		assertEquals(expected, actual);
	}

	@Test
	public void testQuery3() throws Exception {
		ParseTree t = test("/a/b/c[text(1)='qwe']");
		assertNoParseErrors();
		String expected = "(query (queryStep '/' (name 'a')) (queryStep '/' (name 'b')) (queryStep '/' (name 'c') '[' (condition (function 'text') '(' (arg '1') ')' (operator '=') ''qwe'') ']'))";
		String actual = toStringTree(t, parser.getRuleNames(), parser.getTokenNames());
		print(expected, actual);
		assertEquals(expected, actual);
	}

	@Test
	public void testQuery4() throws Exception {
		ParseTree t = test("/a/b[text()='qwe']/c");
		assertNoParseErrors();
		String expected = "(query (queryStep '/' (name 'a')) (queryStep '/' (name 'b') '[' (condition (function 'text') '(' ')' (operator '=') ''qwe'') ']') (queryStep '/' (name 'c')))";
		String actual = toStringTree(t, parser.getRuleNames(), parser.getTokenNames());
		print(expected, actual);
		assertEquals(expected, actual);
	}

	@Test
	public void testQuery5() throws Exception {
		ParseTree t = test("/a/b[text()='qwe']/parent::");
		assertNoParseErrors();
		String expected = "(query (queryStep '/' (name 'a')) (queryStep '/' (name 'b') '[' (condition (function 'text') '(' ')' (operator '=') ''qwe'') ']') (queryStep '/' (axisSpecifier (axisName 'parent') '::')))";
		String actual = toStringTree(t, parser.getRuleNames(), parser.getTokenNames());
		print(expected, actual);
		assertEquals(expected, actual);
	}

	@Test
	public void testQuery6() throws Exception {
		ParseTree t = test("/a/b[text()='qwe']/parent::*");
		assertNoParseErrors();
		String expected = "(query (queryStep '/' (name 'a')) (queryStep '/' (name 'b') '[' (condition (function 'text') '(' ')' (operator '=') ''qwe'') ']') (queryStep '/' (axisSpecifier (axisName 'parent') '::') (name '*')))";
		String actual = toStringTree(t, parser.getRuleNames(), parser.getTokenNames());
		print(expected, actual);
		assertEquals(expected, actual);
	}

	@Test
	public void testQuery7() throws Exception {
		ParseTree t = test("/a/b[text()='qwe']/parent::foo");
		assertNoParseErrors();
		String expected = "(query (queryStep '/' (name 'a')) (queryStep '/' (name 'b') '[' (condition (function 'text') '(' ')' (operator '=') ''qwe'') ']') (queryStep '/' (axisSpecifier (axisName 'parent') '::') (name 'foo')))";
		String actual = toStringTree(t, parser.getRuleNames(), parser.getTokenNames());
		print(expected, actual);
		assertEquals(expected, actual);
	}

	@Test
	public void testQuery8() throws Exception {
		ParseTree t = test("//a/b");
		assertNoParseErrors();
		String expected = "(query (queryStep '//' (name 'a')) (queryStep '/' (name 'b')))";
		String actual = toStringTree(t, parser.getRuleNames(), parser.getTokenNames());
		print(expected, actual);
		assertEquals(expected, actual);
	}

	@Test
	public void testQuery9() throws Exception {
		ParseTree t = test("/a//b");
		assertNoParseErrors();
		String expected = "(query (queryStep '/' (name 'a')) (queryStep '//' (name 'b')))";
		String actual = toStringTree(t, parser.getRuleNames(), parser.getTokenNames());
		print(expected, actual);
		assertEquals(expected, actual);
	}

	@Test
	public void testQuery10() throws Exception {
		ParseTree t = test("/a//b[text()='qwe']");
		assertNoParseErrors();
		String expected = "(query (queryStep '/' (name 'a')) (queryStep '//' (name 'b') '[' (condition (function 'text') '(' ')' (operator '=') ''qwe'') ']'))";
		String actual = toStringTree(t, parser.getRuleNames(), parser.getTokenNames());
		print(expected, actual);
		assertEquals(expected, actual);
	}

	///////////////////////////////////////////////////////////////////////////////////
	
	private void assertNoParseErrors() {
		assertEquals("Unexpected errors were returned from the parser: ", Arrays.toString(new Object[0]), Arrays.toString(listener.errors.toArray()));
	}
	
    private ParseTree test(String query) {
		parser = fn.createParser(query);
		parser.addErrorListener(listener);
		return parser.query();
    }

	private void print(String expected, String actual) {
		if (OUTPUT) {
			System.err.println("Expected: " + expected);
			System.err.println("Actual: " + actual);
		}
	}
	
	private static class AntlrErrorListener implements ANTLRErrorListener {
		private List<String> errors = new ArrayList<>();
		
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
			errors.add("syntaxError: " + line + ":" + charPositionInLine + " " + msg);
		}

		@Override
		public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
			errors.add("ambiguity: " + startIndex);
		}

		@Override
		public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
			errors.add("attemptingFullContext: " + startIndex);
		}

		@Override
		public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
			errors.add("contextSensitivity: " + startIndex);
		}
	}
}

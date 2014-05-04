grammar AntlrXPath;

/* Grammar rules */

query: 
		queryStep+;

queryStep:
		Any name ('[' condition ']')?
	  | Next axisSpecifier? name ('[' condition ']')?
	  | Next axisSpecifier;

axisSpecifier: 
		axisName '::';

axisName: 
		'child'
	  | 'parent'
	  | 'descendant'
	  | 'descendant-or-self';

name:
		Name | '*';

condition: 
		function '(' arg? ')' operator StringLiteral;

operator: 
		'=';

function: 
		'text';

arg:
		Number;

/* Lexer tokens */

Name: Char (Char | Numeric)*;
Number: Numeric+;
StringLiteral: '\'' StringCharacters? '\'';
Any: '//';
Next: '/';

fragment
StringCharacters: StringCharacter+;

fragment
StringCharacter: ~['\\] | EscapeSequence;

fragment
EscapeSequence: '\\' ['\\];

fragment
Char: 'a'..'z' | 'A'..'Z';

fragment
Numeric: '0'..'9';

fragment
Space: ' ' | '\t';

EOL: '\r'? '\n' | '\n';

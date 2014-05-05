grammar JsonXPath;

/* Grammar rules */

query: 
		queryStep+;

queryStep:
		Any name ('[' condition ']')?
	  | Next axisSpecifier? (name ('[' condition ']')?)?;

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
		function '(' ')' operator StringLiteral;

operator: 
		'=';

function: 
		'text';


/* Lexer tokens */

Name: Char (Char | Numeric)*;
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

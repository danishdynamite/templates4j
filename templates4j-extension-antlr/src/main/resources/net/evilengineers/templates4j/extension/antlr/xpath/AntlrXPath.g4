grammar AntlrXPath;

/* Grammar rules */

query: absoluteQuery | relativeQuery;

absoluteQuery: (Next queryElement)+
             | Any queryElement (Next queryElement)*;

relativeQuery: queryElement (Next queryElement)*;

queryElement: axisSpecifier? name ('[' condition ']')?
			| axisSpecifier;

axisSpecifier: axisName '::';
axisName: 'parent';

name: Name;

condition: function '()' operator StringLiteral;
operator: '=';
function: 'text';


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

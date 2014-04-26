grammar AntlrXPath;

/* Grammar rules */

query: wildcardSelector? (axisSelector | normalSelector)+;

wildcardSelector: '//' element;
normalSelector: '/' element;
axisSelector: '/' axisSpecifier element?;
axisSpecifier: axisName '::';
axisName: 'parent';

element: Identifier ('[' condition ']')?;

condition: function '()' operator StringLiteral;
operator: '=';
function: 'text';


/* Lexer tokens */

Identifier: Char (Char | Numeric)*;
StringLiteral: '\'' StringCharacters? '\'';

/* fragments */
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

/**
 * reference grammar.
 */

parser grammar Grammar;

file
  : mataramHeader* statement*
  ;

mataramHeader
  : 'include' stringLiteral ';'
  ;

// statement

statement
  : ifStatement
  | whileStatement
  | eachStatement
  | switchStatement
  | label
  | assignment
  | defineContextExtends
  | macroCall
  | macroDefinition
  ;


ifStatement
  : 'if' parenthesisedExpr macroBody ('else' 'if' parenthesisedExpr macroBody)* ('else' macroBody)?
  ;

whileStatement
  : 'while' parenthesisedExpr macroBody
  ;

eachStatement
  : 'each' '(' variable 'of' '...' variable ')' macroBody
  ;

switchStatement
  : 'switch' parenthesisedExpr '{' switchCase* '}'
  ;

switchCase
  : ('case' expression)+ macroBody
  | 'default' macroBody
  ;

label
  : identifier ':'
  ;

assignment
  : variable '=' expression ';'
  ;

defineContextExtends
  : context 'extends' context ';'
  ;

macroCall
  : identifier macroArg* ';'
  ;

macroArg
  : (identifier ':')? expression
  ;

macroDefinition
  : 'macro' context identifier '(' macroPram* ')' macroBody
  ;

macroPram
  : '...'? macroTypeName identifier
  ;

macroTypeName
  : 'any'
  | 'label'
  | 'desc'
  | 'type'
  | 'classType'
  | 'identifier'
  | 'expression'
  | 'value'
  | 'string'
  | 'int'
  | 'bool'
  | 'null'
  | 'block' context
  ;

macroBody
  : '{' statement* '}'
  ;

// expression

expression
  : disjunction
  ;

disjunction
  : conjunction ('||' conjunction)*
  ;

conjunction
  : equality ('&&' equality)*
  ;

equality
  : comparison (equalityOperator comparison)*
  ;

comparison
  : additiveExpression (comparisonOperator additiveExpression)*
  ;

additiveExpression
  : multiplicativeExpression (additiveOperator multiplicativeExpression)*
  ;

multiplicativeExpression
  : prefixUnaryExpression (multiplicativeOperator prefixUnaryExpression)*
  ;

prefixUnaryExpression
  : prefixUnaryOperator* postfixUnaryExpression
  ;

postfixUnaryExpression
  : primaryExpression postfixUnarySuffix*
  ;

postfixUnarySuffix
  : postfixUnaryOperator
  | '[' expression ']'
  ;

primaryExpression
  : 'typeof' parenthesisedExpr
  | parenthesisedExpr
  | expressionValue
  ;

parenthesisedExpr
  : '(' expression ')'
  ;

equalityOperator: '==' | '!=' ;
comparisonOperator: '<' | '>' | '<=' | '<=' ;
additiveOperator: '+' | '-' ;
multiplicativeOperator: '*' | '/' | '%' ;
prefixUnaryOperator: '++' | '--' | '-' | '+';
postfixUnaryOperator: '++' | '--';

expressionValue
  : variable
  | stringLiteral
  | integerLiteral
  | numberLiteral
  | typeDescriptor
  | typeInternalName
  | methodDescriptor
  | identifier
  | 'identifier' '(' (identifier | variable) ')'
  ;

variable
  : '$' identifier
  ;

stringLiteral
  : STRING_LITERAL
  ;

integerLiteral
  : INTEGER_LITERAL
  ;

numberLiteral
  : NUMBER_LITERAL
  ;

typeDescriptor
  : 'byte'
  | 'short'
  | 'int'
  | 'long'
  | 'float'
  | 'double'
  | quotedReferenceTypeDescriptor
  | 'type' '(' (quotedReferenceTypeDescriptor | variable) ')'
  | variable
  ;

typeInternalName
  : quotedInternalName
  | 'classType' '(' (quotedInternalName | variable) ')'
  | variable
  ;

methodDescriptor
  : quotedMethodDescriptor
  | 'desc' '(' (quotedMethodDescriptor | variable) ')'
  | variable
  ;

//

context
  : '@' identifier
  ;

baseTypeDescriptorChar: 'B'|'C'|'D'|'F'|'I'|'J'|'S'|'Z';

// lexeical

quotedReferenceTypeDescriptor
  : '`' quotedReferenceTypeDescriptorBody '`'
  ;

quotedReferenceTypeDescriptorBody
  : quotedElement{which is starts with 'L'} ('/' quotedDescriptorElement)* ';'  // Ljava/lang/String; Ljava/$lang;
  | 'L' quotedElementVariable ('/' quotedDescriptorElement)* ';'
  ;

quotedInternalName
  : '`' quotedDescriptorElement ('/' quotedDescriptorElement)* ';' '`'
  ;

/*
 * `()V`
 * `(${arg}IV)V`
 * `(${...args})V`
 */
quotedMethodDescriptor
  : '`' '(' quotedMethodDescriptorParameterType* ')' quotedMethodDescriptorReturnType '`'
  ;

quotedMethodDescriptorParameterType
  : quotedReferenceTypeDescriptorBody
  | baseTypeDescriptorChar
  | quotedElementVariables
  ;

quotedMethodDescriptorReturnType
  : quotedReferenceTypeDescriptorBody
  | baseTypeDescriptorChar
  | quotedElementVariable
  | 'V'
  ;

quotedIdentifier
  : '`' quotedElement '`'
  ;

quotedDescriptorElement
  : quotedElement
  | quotedElementVariable
  ;

quotedElementVariable
  : '$' id
  | '$' '{' identifier '}'
  ;

quotedElementVariables
  : quotedElementVariable
  | '$' '{' '...' identifier '}'
  ;

quotedElement
  : QUOTED_ELEMENT // the sequence excludes '$[\x00-\x1F]' and non-identifier-able charactors
  ;

id
  : ID
  ;

identifier
  : id
  | quotedIdentifier
  ;


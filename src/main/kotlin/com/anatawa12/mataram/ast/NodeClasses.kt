package com.anatawa12.mataram.ast

// s: means non-terminal token (syntax rule)
// r: means rule of syntax
// e: means syntax error fallback node

private fun l(n: List<Node>) = n
private fun l(n: Node?) = listOfNotNull(n)

// s: file
data class MataramFile(
    val headers: List<MataramHeader>,
    val body: List<Node>,
    val eof: Token,
) : Node() {
    override val children = (
        emptyList<Node>()
        + l(headers)
        + l(body)
        + l(eof)
    ).toTypedArray()
}

// s: mataramHeader
sealed class MataramHeader : Node()
// e: mataramHeader
data class SyntaxErrorMataramHeader(
    val tokens: List<Token>,
) : MataramHeader() {
    override val children = (
        emptyList<Node>()
        + l(tokens)
    ).toTypedArray()
}
// r: 'include' stringLiteral ';'
data class IncludeMataramHeader(
    val include: Token,
    val string: LiteralExpr,
    val semi: Token,
) : MataramHeader() {
    override val children = (
        emptyList<Node>()
        + l(include)
        + l(string)
        + l(semi)
    ).toTypedArray()
}

// s: statement
sealed class Statement : Node()
// e: statement
data class SyntaxErrorStatement(
    val tokens: List<Token>,
) : Statement() {
    override val children = (
        emptyList<Node>()
        + l(tokens)
    ).toTypedArray()
}

// s: ifStatement
// r: 'if' parenthesisedExpr macroBody ('else' 'if' parenthesisedExpr macroBody)* ('else' macroBody)?
data class IfStatement(
    val keyIf: Token,
    val condition: ParenthesisedExpr,
    val thenBlock: MacroBody,
    val elseIfs: List<ElseIfRegion>,
    val elseRegion: ElseRegion?,
) : Statement() {
    override val children = (
        emptyList<Node>()
        + l(keyIf)
        + l(condition)
        + l(thenBlock)
        + l(elseIfs)
        + l(elseRegion)
    ).toTypedArray()
}
// r: 'else' 'if' parenthesisedExpr macroBody
data class ElseIfRegion(
    val keyElse: Token,
    val keyIf: Token,
    val condition: ParenthesisedExpr,
    val thenBlock: MacroBody,
) : Node() {
    override val children = (
        emptyList<Node>()
        + l(keyElse)
        + l(keyIf)
        + l(condition)
        + l(thenBlock)
    ).toTypedArray()
}
// r: 'else' macroBody
data class ElseRegion(
    val keyElse: Token,
    val block: MacroBody,
) : Node() {
    override val children = (
        emptyList<Node>()
        + l(keyElse)
        + l(block)
    ).toTypedArray()
}

// s: whileStatement
// r: 'while' parenthesisedExpr macroBody
data class WhileStatement(
    val keyWhile: Token,
    val condition: ParenthesisedExpr,
    val body: MacroBody,
) : Statement() {
    override val children = (
        emptyList<Node>()
        + l(keyWhile)
        + l(condition)
        + l(body)
    ).toTypedArray()
}

// s: eachStatement
// r: 'each' '(' variable 'of' '...' variable ')' macroBody
data class EachStatement(
    val keyEach: Token,
    val leftParenthesis: Token,
    val variable: VariableExpr,
    val keyOf: Token,
    val threeDot: Token,
    val listVariable: VariableExpr,
    val rightParenthesis: Token,
    val body: MacroBody,
) : Statement() {
    override val children = (
        emptyList<Node>()
        + l(keyEach)
        + l(leftParenthesis)
        + l(variable)
        + l(keyOf)
        + l(threeDot)
        + l(listVariable)
        + l(rightParenthesis)
        + l(body)
    ).toTypedArray()
}

// s: switchStatement
// r: 'switch' parenthesisedExpr '{' switchCase* '}'
data class SwitchStatement(
    val keySwitch: Token,
    val expression: ParenthesisedExpr,
    val leftBrace: Token,
    val cases: List<SwitchDefaultOrCaseRegion>,
    val rightBrace: Token,
) : Statement() {
    override val children = (
        emptyList<Node>()
        + l(keySwitch)
        + l(expression)
        + l(leftBrace)
        + l(cases)
        + l(rightBrace)
    ).toTypedArray()
}

// s: switchCase
sealed class SwitchDefaultOrCaseRegion : Node()
// r: 'case' expression
data class SwitchCase(
    val keyCase: Token,
    val expression: Expression,
) : Node() {
    override val children = (
        emptyList<Node>()
        + l(keyCase)
        + l(expression)
    ).toTypedArray()
}
// r: ('case' expression)+ macroBody
data class SwitchCaseRegion(
    val cases: List<SwitchCase>,
    val body: MacroBody,
) : SwitchDefaultOrCaseRegion() {
    override val children = (
        emptyList<Node>()
        + l(cases)
        + l(body)
    ).toTypedArray()
}
// r: 'default' macroBody
data class SwitchDefaultRegion(
    val keyDefault: Token,
    val body: MacroBody,
) : SwitchDefaultOrCaseRegion() {
    override val children = (
        emptyList<Node>()
        + l(keyDefault)
        + l(body)
    ).toTypedArray()
}
// e: switchCase
data class SyntaxErrorSwitchCaseRegion(
    val tokens: List<Token>,
) : SwitchDefaultOrCaseRegion() {
    override val children = (
        emptyList<Node>()
        + l(tokens)
    ).toTypedArray()
}

// s: label
// r: identifier ':'
data class Label(
    val name: Identifier,
    val colon: Token,
) : Statement() {
    override val children = (
        emptyList<Node>()
        + l(name)
        + l(colon)
    ).toTypedArray()
}

// s: assignment
// r: variable '=' expression ';'
data class Assignment(
    val setTarget: VariableExpr,
    val equals: Token,
    val expression: Expression,
    val semi: Token,
) : Statement() {
    override val children = (
        emptyList<Node>()
        + l(setTarget)
        + l(equals)
        + l(expression)
        + l(semi)
    ).toTypedArray()
}

// s: defineContextExtends
// r: context 'extends' context ';'
data class DefineContextExtends(
    val context: ContextSpecifier,
    val extends: Token,
    val superContext: ContextSpecifier,
    val semi: Token,
) : Statement() {
    override val children = (
        emptyList<Node>()
        + l(context)
        + l(extends)
        + l(superContext)
        + l(semi)
    ).toTypedArray()
}

// s: macroCall
// r: identifier macroArg* ';'
data class MacroCall(
    val macroName: Identifier,
    val macroArgs: List<MacroArg>,
    val semi: Token,
) : Statement() {
    override val children = (
        emptyList<Node>()
        + l(macroName)
        + l(macroArgs)
        + l(semi)
    ).toTypedArray()
}

// s: macroArg
// r: (identifier ':')? macroArgValue
data class MacroArg(
    val argName: Identifier?,
    val colon: Token?,
    val value: Expression,
) : Node() {
    override val children = (
        emptyList<Node>()
        + l(argName)
        + l(colon)
        + l(value)
    ).toTypedArray()
}

// s: macroDefinition
// r: 'macro' context identifier '(' macroPram* ')' macroBody
data class MacroDefinition(
    val macroKey: Token,
    val context: ContextSpecifier,
    val name: Identifier,
    val leftParenthesis: Token,
    val params: List<MacroParamBase>,
    val rightParenthesis: Token,
    val body: MacroBody,
) : Statement() {
    override val children = (
        emptyList<Node>()
        + l(macroKey)
        + l(context)
        + l(name)
        + l(leftParenthesis)
        + l(params)
        + l(rightParenthesis)
        + l(body)
    ).toTypedArray()
}

// s: macroPram
sealed class MacroParamBase : Node()
// e: macroPram
data class SyntaxErrorMacroParam(
    val tokens: List<Token>,
) : MacroParamBase() {
    override val children = (
        emptyList<Node>()
        + l(tokens)
    ).toTypedArray()
}
// r: '...'? macroTypeName identifier
data class MacroParam(
    val threeDot: Token?,
    val macroTypeName: MacroType,
    val id: Identifier,
) : MacroParamBase() {
    override val children = (
        emptyList<Node>()
        + l(threeDot)
        + l(macroTypeName)
        + l(id)
    ).toTypedArray()
}

// s: macroTypeName
sealed class MacroType : Node()
// r: 'any'
// r: 'label'
// r: 'desc'
// r: 'type'
// r: 'classType'
// r: 'identifier'
// r: 'expression'
// r: 'value'
// r: 'string'
// r: 'int'
// r: 'bool'
// r: 'null'
data class SimpleMacroType(
    val read: Token,
) : MacroType() {
    override val children = (
        emptyList<Node>()
        + l(read)
    ).toTypedArray()
}
// r: 'block' context
data class BlockMacroType(
    val block: Token,
    val context: ContextSpecifier,
) : MacroType() {
    override val children = (
        emptyList<Node>()
        + l(block)
        + l(context)
    ).toTypedArray()
}

// s: macroBody
// r: '{' statement* '}'
data class MacroBody(
    val leftBrace: Token,
    val body: List<Statement>,
    val rightBrace: Token,
) : Node() {
    override val children = (
        emptyList<Node>()
        + l(leftBrace)
        + l(body)
        + l(rightBrace)
    ).toTypedArray()
}

// s: expression
sealed class Expression : Node()
// e: expression // for Surrounded with brackets
data class SyntaxErrorExpr(
    val tokens: List<Token>,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(tokens)
    ).toTypedArray()
}
// s: comparison
// r: additiveExpression (comparisonOperator additiveExpression)*
// s: additiveExpression
// r: multiplicativeExpression (additiveOperator multiplicativeExpression)*
// s: multiplicativeExpression
// r: prefixUnaryExpression (multiplicativeOperator prefixUnaryExpression)*
data class BinaryOperatorExpr(
    val left: Expression,
    val token: Token,
    val right: Expression,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(left)
        + l(token)
        + l(right)
    ).toTypedArray()
}
// s: prefixUnaryExpression
// r: prefixUnaryOperator* postfixUnaryExpression
data class PrefixUnaryExpr(
    val token: Token,
    val expr: Expression,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(token)
        + l(expr)
    ).toTypedArray()
}
// s: postfixUnaryExpression
// r: primaryExpression postfixUnarySuffix
data class PostfixUnaryExpr(
    val expr: Expression,
    val token: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(expr)
        + l(token)
    ).toTypedArray()
}
// s: postfixUnaryExpression
// r: primaryExpression '[' expression ']'
data class IndexingExpr(
    val expr: Expression,
    val leftBracket: Token,
    val index: Expression,
    val rightBracket: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(expr)
        + l(leftBracket)
        + l(index)
        + l(rightBracket)
    ).toTypedArray()
}
// s: primaryExpression
// r: 'typeof' parenthesisedExpr
data class TypeofExpr(
    val keyTypeof: Token,
    val expr: ParenthesisedExpr,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(keyTypeof)
        + l(expr)
    ).toTypedArray()
}
// s: parenthesisedExpr
// r: '(' expression ')'
data class ParenthesisedExpr(
    val leftParenthesis: Token,
    val expr: Expression,
    val rightParenthesis: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(leftParenthesis)
        + l(expr)
        + l(rightParenthesis)
    ).toTypedArray()
}
// s: variable
// r: '$' identifier
data class VariableExpr(
    val dollar: Token,
    val id: Identifier,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(dollar)
        + l(id)
    ).toTypedArray()
}
// s: stringLiteral
// r: STRING_LITERAL
// s: integerLiteral
// r: INTEGER_LITERAL
// s: numberLiteral
// r: OTHER_NUMBER
// r: DECIMAL_NUMBER
data class LiteralExpr(
    val literal: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(literal)
    ).toTypedArray()
}

// s: typeDescriptor
// r: 'byte'
// r: 'short'
// r: 'int'
// r: 'long'
// r: 'float'
// r: 'double'
data class PrimitiveTypeDescriptor(
    val id: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(id)
    ).toTypedArray()
}
// r: quotedReferenceTypeDescriptor
data class QuotedReferenceTypeDescriptor(
    val quoteStart: Token,
    val elements: List<QuotedElement>,
    val quoteEnd: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(quoteStart)
        + l(elements)
        + l(quoteEnd)
    ).toTypedArray()
}
// r: 'type' '(' (quotedReferenceTypeDescriptor | variable) ')'
data class SurroundedTypeDescriptor(
    val type: Token,
    val left: Token,
    val desc: Expression,
    val right: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(type)
        + l(left)
        + l(desc)
        + l(right)
    ).toTypedArray()
}

// s: typeInternalName
// r: quotedInternalName
data class QuotedTypeInternalName(
    val quoteStart: Token,
    val elements: List<QuotedElement>,
    val quoteEnd: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(quoteStart)
        + l(elements)
        + l(quoteEnd)
    ).toTypedArray()
}
// r: 'classType' '(' (quotedInternalName | variable) ')'
data class SurroundedTypeInternalName(
    val type: Token,
    val left: Token,
    val desc: Expression,
    val right: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(type)
        + l(left)
        + l(desc)
        + l(right)
    ).toTypedArray()
}

// s: methodDescriptor
// r: quotedMethodDescriptor
data class QuotedMethodDescriptor(
    val quoteStart: Token,
    val elements: List<QuotedElement>,
    val quoteEnd: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(quoteStart)
        + l(elements)
        + l(quoteEnd)
    ).toTypedArray()
}
// r: 'desc' '(' (quotedMethodDescriptor | variable) ')'
data class SurroundedMethodDescriptor(
    val type: Token,
    val left: Token,
    val desc: Expression,
    val right: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(type)
        + l(left)
        + l(desc)
        + l(right)
    ).toTypedArray()
}

// s: identifier
sealed class Identifier : Expression()
// r: id
data class SimpleIdentifier(
    val read: Token,
) : Identifier() {
    override val children = (
        emptyList<Node>()
        + l(read)
    ).toTypedArray()
}
// r: quotedIdentifier
data class QuotedIdentifier(
    val quoteStart: Token,
    val identifier: QuotedElementToken,
    val quoteEnd: Token,
) : Identifier() {
    override val children = (
        emptyList<Node>()
        + l(quoteStart)
        + l(identifier)
        + l(quoteEnd)
    ).toTypedArray()
}
// e: quotedIdentifier
data class SyntaxErrorIdentifier(
    val quoteStart: Token,
    val elements: List<QuotedElement>,
    val quoteEnd: Token,
) : Identifier() {
    override val children = (
        emptyList<Node>()
        + l(quoteStart)
        + l(elements)
        + l(quoteEnd)
    ).toTypedArray()
}

// s: expressionValue
// r: 'identifier' '(' (identifier | variable) ')'
data class SurroundedIdentifier(
    val type: Token,
    val left: Token,
    val desc: Expression,
    val right: Token,
) : Identifier() {
    override val children = (
        emptyList<Node>()
        + l(type)
        + l(left)
        + l(desc)
        + l(right)
    ).toTypedArray()
}

// others
// s: context
// r: '@' identifier
data class ContextSpecifier(
    val atMark: Token,
    val id: Identifier,
) : Node() {
    override val children = (
        emptyList<Node>()
        + l(atMark)
        + l(id)
    ).toTypedArray()
}

// e: quotedReferenceTypeDescriptor
// e: quotedInternalName
// e: quotedMethodDescriptor
// e: quotedIdentifier
data class SyntaxErrorQuoted(
    val quoteStart: Token,
    val elements: List<QuotedElement>,
    val quoteEnd: Token,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(quoteStart)
        + l(elements)
        + l(quoteEnd)
    ).toTypedArray()
}

// s: quotedReferenceTypeDescriptor
// s: quotedInternalName
// s: quotedMethodDescriptor
// s: quotedIdentifier
data class UnknownQuoted(
    val quoteStart: Token,
    val elements: List<QuotedElement>,
    val quoteEnd: Token,
    val allows: Set<QuotedType>,
) : Expression() {
    override val children = (
        emptyList<Node>()
        + l(quoteStart)
        + l(elements)
        + l(quoteEnd)
    ).toTypedArray()
}

enum class QuotedType {
    Identifier,
    TypeDescriptor,
    TypeInternalName,
    MethodDescriptor,
}

// quoted syntax tree
sealed class QuotedElement : Node()
sealed class QuotedElementVariable : QuotedElement()
data class SyntaxErrorQuotedElement(
    val tokens: List<Token>,
) : QuotedElement() {
    override val children = (
        emptyList<Node>()
        + l(tokens)
    ).toTypedArray()
}
data class QuotedElementToken(
    val token: Token,
) : QuotedElement() {
    override val children = (
        emptyList<Node>()
        + l(token)
    ).toTypedArray()
}
data class SyntaxErrorQuotedElementVariable(
    val dollar: Token,
    val leftBrace: Token,
    val tokens: List<Token>,
    val rightBrace: Token,
) : QuotedElementVariable() {
    override val children = (
        emptyList<Node>()
        + l(dollar)
        + l(leftBrace)
        + l(tokens)
        + l(rightBrace)
    ).toTypedArray()
}
data class SimpleQuotedElementVariable(
    val dollar: Token,
    val id: Identifier,
) : QuotedElementVariable() {
    override val children = (
        emptyList<Node>()
        + l(dollar)
        + l(id)
    ).toTypedArray()
}
data class SurroundedQuotedElementVariable(
    val dollar: Token,
    val leftBrace: Token,
    val identifier: Identifier,
    val rightBrace: Token,
) : QuotedElementVariable() {
    override val children = (
        emptyList<Node>()
        + l(dollar)
        + l(leftBrace)
        + l(identifier)
        + l(rightBrace)
    ).toTypedArray()
}


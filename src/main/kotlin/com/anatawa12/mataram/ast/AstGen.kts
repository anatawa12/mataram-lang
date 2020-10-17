#!/usr/bin/env kotlin -script
@file:Suppress("PropertyName")

package com.anatawa12.mataram.ast
/*
 * kotlin -script AstGen.kts <path-to-NodeClasses.kt>
 */
import java.io.File
import java.io.PrintStream
import kotlin.reflect.KProperty

fun PrintStream.header() {
    println("package com.anatawa12.mataram.ast")
    println()
    println("// s: means non-terminal token (syntax rule)")
    println("// r: means rule of syntax")
    println("// e: means syntax error fallback node")
    println()
    println("private fun l(n: List<Node>) = n")
    println("private fun l(n: Node?) = listOfNotNull(n)")
    println()
}

fun PrintStream.main() {
    header()

    s("file") {
        c(MataramFile) {
            f("headers", MataramHeader.list())
            f("body", Node.list())
            f("eof", Token)
        }
    }
    s("mataramHeader") {
        makeSealed(MataramHeader)
        c(SyntaxErrorMataramHeader, MataramHeader) {
            e("mataramHeader")
            f("tokens", Token.list())
        }
        c(IncludeMataramHeader, MataramHeader) {
            r("'include' stringLiteral ';'")
            f("include", Token)
            f("string", LiteralExpr)
            f("semi", Token)
        }
    }
    s("statement") {
        makeSealed(Statement)
        c(SyntaxErrorStatement, Statement) {
            e("statement")
            f("tokens", Token.list())
        }
    }
    s("ifStatement") {
        c(IfStatement, Statement) {
            r("'if' parenthesisedExpr macroBody ('else' 'if' parenthesisedExpr macroBody)* ('else' macroBody)?")
            f("keyIf", Token)
            f("condition", ParenthesisedExpr)
            f("thenBlock", MacroBody)
            f("elseIfs", ElseIfRegion.list())
            f("elseRegion", ElseRegion.opt())
        }
        c(ElseIfRegion, Node) {
            r("'else' 'if' parenthesisedExpr macroBody")
            f("keyElse", Token)
            f("keyIf", Token)
            f("condition", ParenthesisedExpr)
            f("thenBlock", MacroBody)
        }
        c(ElseRegion, Node) {
            r("'else' macroBody")
            f("keyElse", Token)
            f("block", MacroBody)
        }
    }
    s("whileStatement") {
        c(WhileStatement, Statement) {
            r("'while' parenthesisedExpr macroBody")
            f("keyWhile", Token)
            f("condition", ParenthesisedExpr)
            f("body", MacroBody)
        }
    }
    s("eachStatement") {
        c(EachStatement, Statement) {
            r("'each' '(' variable 'of' '...' variable ')' macroBody")
            f("keyEach", Token)
            f("leftParenthesis", Token)
            f("variable", VariableExpr)
            f("keyOf", Token)
            f("threeDot", Token)
            f("listVariable", VariableExpr)
            f("rightParenthesis", Token)
            f("body", MacroBody)
        }
    }
    s("switchStatement") {
        c(SwitchStatement, Statement) {
            r("'switch' parenthesisedExpr '{' switchCase* '}'")
            f("keySwitch", Token)
            f("expression", ParenthesisedExpr)
            f("leftBrace", Token)
            f("cases", SwitchDefaultOrCaseRegion.list())
            f("rightBrace", Token)
        }
    }
    s("switchCase") {
        makeSealed(SwitchDefaultOrCaseRegion)
        c(SwitchCase, Node) {
            r("'case' expression")
            f("keyCase", Token)
            f("expression", Expression)
        }
        c(SwitchCaseRegion) {
            r("('case' expression)+ macroBody")
            f("cases", SwitchCase.list())
            f("body", MacroBody)
        }
        c(SwitchDefaultRegion) {
            r("'default' macroBody")
            f("keyDefault", Token)
            f("body", MacroBody)
        }
        c(SyntaxErrorSwitchCaseRegion) {
            e("switchCase")
            f("tokens", Token.list())
        }
    }
    s("label") {
        c(Label, Statement) {
            r("identifier ':'")
            f("name", Identifier)
            f("colon", Token)
        }
    }
    s("assignment") {
        c(Assignment, Statement) {
            r("variable '=' expression ';'")
            f("setTarget", VariableExpr)
            f("equals", Token)
            f("expression", Expression)
            f("semi", Token)
        }
    }
    s("defineContextExtends") {
        c(DefineContextExtends, Statement) {
            r("context 'extends' context ';'")
            f("context", ContextSpecifier)
            f("extends", Token)
            f("superContext", ContextSpecifier)
            f("semi", Token)
        }
    }
    s("macroCall") {
        c(MacroCall, Statement) {
            r("identifier macroArg* ';'")
            f("macroName", Identifier)
            f("macroArgs", MacroArg.list())
            f("semi", Token)
        }
    }
    s("macroArg") {
        c(MacroArg) {
            r("(identifier ':')? macroArgValue")
            f("argName", Identifier.opt())
            f("colon", Token.opt())
            f("value", Expression)
        }
    }
    s("macroDefinition") {
        c(MacroDefinition, Statement) {
            r("'macro' context identifier '(' macroPram* ')' macroBody")
            f("macroKey", Token)
            f("context", ContextSpecifier)
            f("name", Identifier)
            f("leftParenthesis", Token)
            f("params", MacroParamBase.list())
            f("rightParenthesis", Token)
            f("body", MacroBody)
        }
    }
    s("macroPram") {
        makeSealed(MacroParamBase)
        c(SyntaxErrorMacroParam, MacroParamBase) {
            e("macroPram")
            f("tokens", Token.list())
        }
        c(MacroParam, MacroParamBase) {
            r("'...'? macroTypeName identifier")
            f("threeDot", Token.opt())
            f("macroTypeName", MacroType)
            f("id", Identifier)
        }
    }
    s("macroTypeName") {
        makeSealed(MacroType)
        c(SimpleMacroType, MacroType) {
            r("'any'")
            r("'label'")
            r("'desc'")
            r("'type'")
            r("'classType'")
            r("'identifier'")
            r("'expression'")
            r("'value'")
            r("'string'")
            r("'int'")
            r("'bool'")
            r("'null'")
            f("read", Token)
        }
        c(BlockMacroType, MacroType) {
            r("'block' context")
            f("block", Token)
            f("context", ContextSpecifier)
        }
    }
    s("macroBody") {
        c(MacroBody) {
            r("'{' statement* '}'")
            f("leftBrace", Token)
            f("body", Statement.list())
            f("rightBrace", Token)
        }
    }
    s("expression") {
        makeSealed(Expression)
        c(SyntaxErrorExpr, Expression) {
            e("expression // for Surrounded with brackets")
            f("tokens", Token.list())
        }
        c(BinaryOperatorExpr, Expression) {
            s("comparison")
            r("additiveExpression (comparisonOperator additiveExpression)*")
            s("additiveExpression")
            r("multiplicativeExpression (additiveOperator multiplicativeExpression)*")
            s("multiplicativeExpression")
            r("prefixUnaryExpression (multiplicativeOperator prefixUnaryExpression)*")
            f("left", Expression)
            f("token", Token)
            f("right", Expression)
        }
        c(PrefixUnaryExpr, Expression) {
            s("prefixUnaryExpression")
            r("prefixUnaryOperator* postfixUnaryExpression")
            f("token", Token)
            f("expr", Expression)
        }
        c(PostfixUnaryExpr, Expression) {
            s("postfixUnaryExpression")
            r("primaryExpression postfixUnarySuffix")
            f("expr", Expression)
            f("token", Token)
        }
        c(IndexingExpr, Expression) {
            s("postfixUnaryExpression")
            r("primaryExpression '[' expression ']'")
            f("expr", Expression)
            f("leftBracket", Token)
            f("index", Expression)
            f("rightBracket", Token)
        }
        c(TypeofExpr, Expression) {
            s("primaryExpression")
            r("'typeof' parenthesisedExpr")
            f("keyTypeof", Token)
            f("expr", ParenthesisedExpr)
        }
        c(ParenthesisedExpr, Expression) {
            s("parenthesisedExpr")
            r("'(' expression ')'")
            f("leftParenthesis", Token)
            f("expr", Expression)
            f("rightParenthesis", Token)
        }
        c(VariableExpr, Expression) {
            s("variable")
            r("'$' identifier")
            f("dollar", Token)
            f("id", Identifier)
        }
        c(LiteralExpr, Expression) {
            s("stringLiteral")
            r("STRING_LITERAL")
            s("integerLiteral")
            r("INTEGER_LITERAL")
            s("numberLiteral")
            r("OTHER_NUMBER")
            r("DECIMAL_NUMBER")
            f("literal", Token)
        }
    }

    s("typeDescriptor") {
        c(PrimitiveTypeDescriptor, Expression) {
            r("'byte'")
            r("'short'")
            r("'int'")
            r("'long'")
            r("'float'")
            r("'double'")
            f("id", Token)
        }
        c(QuotedReferenceTypeDescriptor, Expression) {
            r("quotedReferenceTypeDescriptor")
            f("quoteStart", Token)
            f("elements", QuotedElement.list())
            f("quoteEnd", Token)
        }
        c(SurroundedTypeDescriptor, Expression) {
            r("'type' '(' (quotedReferenceTypeDescriptor | variable) ')'")
            f("type", Token)
            f("left", Token)
            f("desc", Expression)
            f("right", Token)
        }
    }
    s("typeInternalName") {
        c(QuotedTypeInternalName, Expression) {
            r("quotedInternalName")
            f("quoteStart", Token)
            f("elements", QuotedElement.list())
            f("quoteEnd", Token)
        }
        c(SurroundedTypeInternalName, Expression) {
            r("'classType' '(' (quotedInternalName | variable) ')'")
            f("type", Token)
            f("left", Token)
            f("desc", Expression)
            f("right", Token)
        }
    }
    s("methodDescriptor") {
        c(QuotedMethodDescriptor, Expression) {
            r("quotedMethodDescriptor")
            f("quoteStart", Token)
            f("elements", QuotedElement.list())
            f("quoteEnd", Token)
        }
        c(SurroundedMethodDescriptor, Expression) {
            r("'desc' '(' (quotedMethodDescriptor | variable) ')'")
            f("type", Token)
            f("left", Token)
            f("desc", Expression)
            f("right", Token)
        }
    }
    s("identifier", Expression) {
        makeSealed(Identifier)
        c(SimpleIdentifier, Identifier) {
            r("id")
            f("read", Token)
        }
        c(QuotedIdentifier, Identifier) {
            r("quotedIdentifier")
            f("quoteStart", Token)
            f("identifier", QuotedElementToken)
            f("quoteEnd", Token)
        }
        c(SyntaxErrorIdentifier, Identifier) {
            e("quotedIdentifier")
            f("quoteStart", Token)
            f("elements", QuotedElement.list())
            f("quoteEnd", Token)
        }
    }
    s("expressionValue") {
        c(SurroundedIdentifier, Identifier) {
            r("'identifier' '(' (identifier | variable) ')'")
            f("type", Token)
            f("left", Token)
            f("desc", Expression)
            f("right", Token)
        }
    }
    println("// others")
    s("context") {
        c(ContextSpecifier, Node) {
            r("'@' identifier")
            f("atMark", Token)
            f("id", Identifier)
        }
    }
    c(SyntaxErrorQuoted, Expression) {
        e("quotedReferenceTypeDescriptor")
        e("quotedInternalName")
        e("quotedMethodDescriptor")
        e("quotedIdentifier")
        f("quoteStart", Token)
        f("elements", QuotedElement.list())
        f("quoteEnd", Token)
    }
    c(UnknownQuoted, Expression) {
        s("quotedReferenceTypeDescriptor")
        s("quotedInternalName")
        s("quotedMethodDescriptor")
        s("quotedIdentifier")
        f("quoteStart", Token)
        f("elements", QuotedElement.list())
        f("quoteEnd", Token)
        f("allows", QuotedType.set(), false)
    }
    println(
        """
        enum class $QuotedType {
            Identifier,
            TypeDescriptor,
            TypeInternalName,
            MethodDescriptor,
        }
    """.trimIndent()
    )
    println()
    s("") {
        comments.clear()
        comments.add("quoted syntax tree")
        makeSealed(QuotedElement)
        makeSealed(QuotedElementVariable)
        c(SyntaxErrorQuotedElement, QuotedElement) {
            f("tokens", Token.list())
        }
        c(QuotedElementToken, QuotedElement) {
            f("token", Token)
        }
        c(SyntaxErrorQuotedElementVariable) {
            f("dollar", Token)
            f("leftBrace", Token)
            f("tokens", Token.list())
            f("rightBrace", Token)
        }
        c(SimpleQuotedElementVariable) {
            f("dollar", Token)
            f("id", Identifier)
        }
        c(SurroundedQuotedElementVariable) {
            f("dollar", Token)
            f("leftBrace", Token)
            f("identifier", Identifier)
            f("rightBrace", Token)
        }
    }
}

class SyntaxClass(private val sealed: Boolean = false) {
    private val comments = mutableListOf<String>()
    private val fields = mutableListOf<Triple<String, String, Boolean>>()
    lateinit var className: String
    lateinit var superName: String

    fun r(string: String) {
        comments.add("r: $string")
    }

    fun e(string: String) {
        comments.add("e: $string")
    }

    fun s(string: String) {
        comments.add("s: $string")
    }

    fun f(name: String, type: String, isNode: Boolean = true) {
        fields.add(Triple(name, type, isNode))
    }

    fun PrintStream.print() {
        if (sealed) {
            println("sealed class $className : $superName()")
        } else {
            for (comment in comments) {
                println("// $comment")
            }
            println("data class $className(")
            for ((name, type, _) in fields) {
                println("    val $name: $type,")
            }
            println(") : $superName() {")
            println("    override val children = (")
            println("        emptyList<Node>()")
            for ((name, _, isNode) in fields) {
                if (!isNode) continue
                println("        + l($name)")
            }
            println("    ).toTypedArray()")
            println("}")
        }
    }
}

class SyntaxContext {
    val comments = mutableListOf<String>()
    val classes = mutableListOf<SyntaxClass>()
    lateinit var syntaxName: String
    var superName: String = Node

    fun makeSealed(name: String) {
        classes += SyntaxClass(true).apply {
            this.superName = this@SyntaxContext.superName
            this.className = name
        }
        superName = name
    }

    fun s(string: String) {
        comments.add("s: $string")
    }

    inline fun c(className: String, superName: String = this.superName, block: SyntaxClass. () -> Unit) {
        classes += SyntaxClass().apply {
            this.superName = superName
            this.className = className
            block()
        }
    }

    fun PrintStream.print() {
        for (comment in comments) {
            println("// $comment")
        }
        classes.forEach {
            it.run {
                print()
            }
        }
    }
}

fun PrintStream.s(name: String, superName: String = Node, block: SyntaxContext. () -> Unit) {
    SyntaxContext().apply {
        this.syntaxName = name
        this.superName = superName
        s(syntaxName)
        block()
        print()
        println()
    }
}

inline fun PrintStream.c(className: String, superName: String = Node, block: SyntaxClass. () -> Unit) {
    SyntaxClass().apply {
        this.superName = superName
        this.className = className
        block()
        print()
        println()
    }
}

object Strings {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>) = Constant(prop.name)
}

class Constant(private val str: String) {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>) = str
}

fun String.list() = "List<$this>"
fun String.set() = "Set<$this>"
fun String.opt() = "$this?"

val Node by Strings
val Token by Strings

val MataramFile by Strings
val MataramHeader by Strings
val SyntaxErrorMataramHeader by Strings
val IncludeMataramHeader by Strings
val Statement by Strings
val SyntaxErrorStatement by Strings
val IfStatement by Strings
val ElseIfRegion by Strings
val ElseRegion by Strings
val WhileStatement by Strings
val EachStatement by Strings
val SwitchStatement by Strings
val SwitchDefaultOrCaseRegion by Strings
val SwitchCase by Strings
val SwitchCaseRegion by Strings
val SwitchDefaultRegion by Strings
val SyntaxErrorSwitchCaseRegion by Strings
val Label by Strings
val Assignment by Strings
val DefineContextExtends by Strings
val MacroCall by Strings
val MacroArg by Strings
val MacroDefinition by Strings
val MacroParamBase by Strings
val SyntaxErrorMacroParam by Strings
val MacroParam by Strings
val MacroType by Strings
val SimpleMacroType by Strings
val BlockMacroType by Strings
val MacroBody by Strings
val Expression by Strings
val SyntaxErrorExpr by Strings
val BooleanOrExpr by Strings
val BooleanAndExpr by Strings
val EqualityExpr by Strings
val BinaryOperatorExpr by Strings
val PrefixUnaryExpr by Strings
val PostfixUnaryExpr by Strings
val IndexingExpr by Strings
val TypeofExpr by Strings
val ParenthesisedExpr by Strings
val VariableExpr by Strings
val LiteralExpr by Strings
val PrimitiveTypeDescriptor by Strings
val QuotedReferenceTypeDescriptor by Strings
val SurroundedTypeDescriptor by Strings
val QuotedTypeInternalName by Strings
val SurroundedTypeInternalName by Strings
val QuotedMethodDescriptor by Strings
val SurroundedMethodDescriptor by Strings
val Identifier by Strings
val SimpleIdentifier by Strings
val QuotedIdentifier by Strings
val SyntaxErrorIdentifier by Strings
val SurroundedIdentifier by Strings
val ContextSpecifier by Strings
val SyntaxErrorQuoted by Strings
val UnknownQuoted by Strings
val QuotedType by Strings
val QuotedElement by Strings
val QuotedElementVariable by Strings
val SyntaxErrorQuotedElement by Strings
val QuotedElementToken by Strings
val SyntaxErrorQuotedElementVariable by Strings
val SimpleQuotedElementVariable by Strings
val SurroundedQuotedElementVariable by Strings

PrintStream(File(args[0]).outputStream()).use {
    it.main()
}

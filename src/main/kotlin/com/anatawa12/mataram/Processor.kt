package com.anatawa12.mataram

import com.anatawa12.mataram.ast.*
import com.anatawa12.mataram.macros.*
import com.anatawa12.mataram.parser.*
import kotlin.contracts.contract

class Processor<File: IMataramFile>(val callBack: ProcessorCallBack<File>) {
    private val macros = mutableMapOf<MacroIdentifier, IMacro>()

    fun process(file: File) {
        val ast = file.ast
        for (header in ast.headers) {
            when (header) {
                is SyntaxErrorMataramHeader -> syntaxError()
                is IncludeMataramHeader -> {
                    //callBack.runImport(this, file, header.string)
                    TODO()
                }
            }
        }
    }

    class MacroImpl(
        val parentScope: LocalVariableScope,
        val processor: Processor<*>,
        val statements: List<Statement>,
        override val parameters: MacroParameterDescriptor,
    ): IMacro {
        private val macroForThisContext = mutableMapOf<MacroIdentifier, IMacro>()

        override fun process(ctx: ProcessorContext) {
            for (statement in statements) {
                when (statement) {
                    is SyntaxErrorStatement -> syntaxError()
                    is IfStatement -> TODO()
                    is WhileStatement -> TODO()
                    is EachStatement -> TODO()
                    is SwitchStatement -> TODO()
                    is Label -> TODO()
                    is Assignment -> TODO()
                    is DefineContextExtends -> TODO()
                    is MacroCall -> TODO()
                    is MacroDefinition -> TODO()
                }
            }
            TODO("Not yet implemented")
        }

        private fun process(
            ctx: ProcessorContext,
            parentScope: LocalVariableScope,
            statements: List<Statement>,
        ) {
            val scope = LocalVariableScope(ctx.name, parentScope)
            for (statement in statements) {
                when (statement) {
                    is SyntaxErrorStatement -> syntaxError()
                    is IfStatement -> {
                        TODO() //processExpr(statement.condition, scope)
                    }
                    is WhileStatement -> TODO()
                    is EachStatement -> TODO()
                    is SwitchStatement -> TODO()
                    is Label -> TODO()
                    is Assignment -> TODO()
                    is DefineContextExtends -> TODO()
                    is MacroCall -> TODO()
                    is MacroDefinition -> TODO()
                }
            }
            TODO("Not yet implemented")
        }
    }

    companion object {
    }
}
fun syntaxError(): Nothing = error("the file with syntax error was passed")

class ExprClassPair<out T : MacroRuntimeValue>(val expr: Expression, val value: T)

class ProcessCtx {
    private fun <T : MacroRuntimeValue> e(expr: Expression, expect: MacroValueType<T>): T {
        with(MacroValueTypes) {
            when (expr) {
                is SyntaxErrorExpr -> syntaxError()
                is BinaryOperatorExpr -> {
                    if (expr.token.type == BOOL_OR) {
                        mCheck(expect === boolean, expr) { "expected $expect but was boolean" }
                        return BooleanMacroValue(e(expr.left, boolean).value && e(expr.right, boolean).value).by(expr, expect)
                    } else if (expr.token.type == BOOL_AND) {
                        return BooleanMacroValue(e(expr.left, boolean).value && e(expr.right, boolean).value).by(expr, expect)
                    }
                    return when (expr.token.type) {
                        EQUALS -> BooleanMacroValue(e(expr.left, any) == e(expr.right, any)).by(expr, expect)
                        NOT_EQUALS -> BooleanMacroValue(e(expr.left, any) != e(expr.right, any)).by(expr, expect)

                        //@formatter:off
                        LESS_THAN        -> BooleanMacroValue(e(expr.left, number).asBigDecimal() <  e(expr.right, number).asBigDecimal()).by(expr, expect)
                        GRATER_THAN      -> BooleanMacroValue(e(expr.left, number).asBigDecimal() >  e(expr.right, number).asBigDecimal()).by(expr, expect)
                        LESS_OR_EQUALS   -> BooleanMacroValue(e(expr.left, number).asBigDecimal() <= e(expr.right, number).asBigDecimal()).by(expr, expect)
                        GRATER_OR_EQUALS -> BooleanMacroValue(e(expr.left, number).asBigDecimal() >= e(expr.right, number).asBigDecimal()).by(expr, expect)
                        //@formatter:on

                        PLUS, MINUS, STAR, SLASH, MODULO -> {
                            if (integer == expect) {
                                val left = e(expr.left, expect).value
                                val right = e(expr.right, expect).value
                                val value = when (expr.token.type) {
                                    //@formatter:off
                                    PLUS   -> left + right
                                    MINUS  -> left - right
                                    STAR   -> left * right
                                    SLASH  -> left / right
                                    MODULO -> left % right
                                    //@formatter:on
                                    else -> error("logic failre")
                                }
                                return IntegerMacroValue(value).by(expr, integer)
                            } else {
                                val left = e(expr.left, number).asBigDecimal()
                                val right = e(expr.right, number).asBigDecimal()
                                val value = when (expr.token.type) {
                                    //@formatter:off
                                    PLUS   -> left + right
                                    MINUS  -> left - right
                                    STAR   -> left * right
                                    SLASH  -> left / right
                                    MODULO -> left % right
                                    //@formatter:on
                                    else -> error("logic failre")
                                }
                                return NumberMacroValue(value).by(expr, expect)
                            }
                        }
                        else -> error("unknown binary operator: ${expr.token.type}")
                    }
                }
                is PostfixUnaryExpr -> TODO()
                is IndexingExpr -> TODO()
                is TypeofExpr -> TODO()
                is ParenthesisedExpr -> TODO()
                is VariableExpr -> TODO()
                is LiteralExpr -> TODO()
                is PrimitiveTypeDescriptor -> TODO()
                is QuotedReferenceTypeDescriptor -> TODO()
                is SurroundedTypeDescriptor -> TODO()
                is QuotedTypeInternalName -> TODO()
                is SurroundedTypeInternalName -> TODO()
                is QuotedMethodDescriptor -> TODO()
                is SurroundedMethodDescriptor -> TODO()
                is SimpleIdentifier -> TODO()
                is QuotedIdentifier -> TODO()
                is SyntaxErrorIdentifier -> TODO()
                is SurroundedIdentifier -> TODO()
                is SyntaxErrorQuoted -> TODO()
                is UnknownQuoted -> TODO()
            }
            TODO("Not yet implemented")
        }
    }
}

private fun <T : MacroRuntimeValue> MacroRuntimeValue.by(node: Node, expect: MacroValueType<T>): T {
    return expect.castOrNull(this)
        ?: throw MacroRuntimeException(node.firstToken.first, "expected $expect but was ${this.type}")
}

class MacroRuntimeException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(pos: CharPosition, message: String) : super("(${pos.line + 1}, ${pos.column}): $message")
}

inline fun mCheck(condition: Boolean, node: Node, message: () -> String) {
    contract {
        returns() implies condition
    }
    if (!condition) throw MacroRuntimeException(node.firstToken.first, message())
}

inline fun mWarn(condition: Boolean, node: Node, message: () -> String) {
    if (!condition) throw MacroRuntimeException(node.firstToken.first, message())
}

class LocalVariableScope(
    /**
     * used for [getMacro]
     */
    val currentScope: IdentifierMacroValue,
    val parent: LocalVariableScope? = null,
) {
    private val variable = mutableMapOf<IdentifierMacroValue, MacroRuntimeValue>()
    private val macros = mutableMapOf<IdentifierMacroValue, MutableMap<IdentifierMacroValue, IMacro>>()

    fun getVariable(name: IdentifierMacroValue): MacroRuntimeValue? {
        var scope = this
        while (true) {
            val variable = scope.variable[name]
            if (variable != null) return variable
            scope = scope.parent ?: return null
        }
    }

    fun getMacro(name: IdentifierMacroValue): IMacro? {
        var scope = this
        while (true) {
            val variable = scope.macros[currentScope]?.get(name)
            if (variable != null) return variable
            scope = scope.parent ?: return null
        }
    }
}

data class MacroIdentifier(val context: IdentifierMacroValue, val name: IdentifierMacroValue)

open class ProcessorContext(val name: IdentifierMacroValue)

interface ProcessorCallBack<File: IMataramFile> {
    fun runImport(process: Processor<File>, from: File, name: String): IMacro
}

interface IMataramFile {
    val ast: MataramFile
}

package com.anatawa12.mataram.parser

import com.anatawa12.mataram.ast.*
import com.anatawa12.mataram.util.dropLast
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class Parser(private val lex: ILexer) {
    /**
     * @param readBody you must read including rightBrace
     */
    private inline fun <T> parenthesised(
        leftType: TokenType,
        readBody: (left: Token) -> T,
        fallback: (left: Token, tokens: List<Token>, right: Token) -> T,
        rightType: TokenType,
    ): T {
        val left = lex.read(leftType)
        val mark = lex.mark()
        return try {
            readBody(left)
        } catch (e: UnexpectedTokenWasCaused) {
            lex.readUntil(rightType)
            val list = lex.getTokensSince(mark)
            val tokens = list.dropLast(1)
            val rightBrace = list.last()
            fallback(left, tokens, rightBrace)
        } finally {
            mark.dispose()
        }
    }

    /**
     * @param readBody you must read including rightBrace
     */
    private inline fun <T> parenthesisedList(
        leftType: TokenType,
        readBody: () -> T,
        fallback: (tokens: List<Token>) -> T,
        rightType: TokenType,
    ): Triple<Token, List<T>, Token> {
        val left = lex.read(leftType)
        val list = mutableListOf<T>()
        var right: Token
        val mark = lex.mark()
        try {
            while (!lex.matchLookAhead(rightType)) {
                list += readBody()
                lex.mark(mark)
            }
            right = lex.read(rightType)
        } catch (e: UnexpectedTokenWasCaused) {
            lex.readUntil(rightType)
            val tokens = lex.getTokensSince(mark)
            list += fallback(tokens.dropLast(1))
            right = tokens.last()
        } finally {
            mark.dispose()
        }
        return Triple(left, list, right)
    }

    /**
     * @param readBody you must read including rightBrace
     */
    private inline fun statement(readBody: () -> Statement): Statement {
        val mark = lex.mark()
        return try {
            readBody()
        } catch (e: UnexpectedTokenWasCaused) {
            lex.readUntil(SEMI)
            val list = lex.getTokensSince(mark)
            SyntaxErrorStatement(list)
        } finally {
            mark.dispose()
        }
    }

    val file = grammar {
        val headers = mutableListOf<MataramHeader>()
        while (lex.matchLookAhead(headerKeywords))
            headers.add(mataramHeader())

        val body = mutableListOf<Statement>()
        while (!lex.matchLookAhead(EOF))
            body.add(statement())
        val eof = lex.read(EOF)
        MataramFile(headers, body, eof)
    }

    val headerKeywords = "include".split('|').toSet()
    val mataramHeader = grammar {
        val mark = lex.mark()
        try {
            when (lex.peek(ID)?.word) {
                "include" -> {
                    val include = lex.read("include")
                    val string = stringLiteral()
                    val semi = lex.read(SEMI)
                    IncludeMataramHeader(include, string, semi)
                }
                else -> lex.unexpectedTokenError()
            }
        } catch (e: UnexpectedTokenWasCaused) {
            lex.readUntil(SEMI)
            val list = lex.getTokensSince(mark)
            SyntaxErrorMataramHeader(list)
        } finally {
            mark.dispose()
        }
    }

    // statement

    val statement: Grammar<Statement> = grammar {
        when (lex.lookAheadType()) {
            DOLLAR -> assignment()
            AT_MARK -> defineContextExtends()
            ID -> when (lex.peek(ID)?.word) {
                "macro" -> macroDefinition()
                "if" -> ifStatement()
                "while" -> whileStatement()
                "each" -> eachStatement()
                "switch" -> switchStatement()
                else -> {
                    if (lex.lookAheadType(2) == COLON) label()
                    else macroCall()
                }
            }
            else -> macroCall()
        }
    }

    val ifStatement = grammar {
        val keyIf = lex.read("if")
        val condition = parenthesisedExpression()
        val thenBlock = macroBody()
        val elseIfs = mutableListOf<ElseIfRegion>()
        while (lex.matchLookAhead("else")
            && lex.matchLookAhead("if")
        ) {
            val keyElse1 = lex.read("else")
            val keyIf1 = lex.read("if")
            val condition1 = parenthesisedExpression()
            val thenBlock1 = macroBody()
            elseIfs.add(ElseIfRegion(keyElse1, keyIf1, condition1, thenBlock1))
        }
        val elseRegion = if (lex.matchLookAhead("else")) {
            ElseRegion(lex.read("else"), macroBody())
        } else {
            null
        }
        IfStatement(keyIf, condition, thenBlock, elseIfs, elseRegion)
    }

    val whileStatement = grammar {
        val keyWhile = lex.read("while")
        val condition = parenthesisedExpression()
        val body = macroBody()
        WhileStatement(keyWhile, condition, body)
    }

    val eachStatement = grammar {
        val keyEach = lex.read("each")
        val leftParenthesis = lex.read(L_PARENTHESIS)
        val variable = variable()
        val keyOf = lex.read("of")
        val threeDot = lex.read(THREE_DOT)
        val listVariable = variable()
        val rightParenthesis = lex.read(R_PARENTHESIS)
        val body = macroBody()
        EachStatement(
            keyEach, leftParenthesis, variable, keyOf,
            threeDot, listVariable, rightParenthesis, body
        )
    }

    val switchStatement = grammar {
        val keySwitch = lex.read("switch")
        val expression = parenthesisedExpression()

        val (leftBrace, cases, rightBrace) = parenthesisedList(
            leftType = L_BRACE,
            readBody = { switchCase() },
            fallback = ::SyntaxErrorSwitchCaseRegion,
            rightType = R_BRACE,
        )

        SwitchStatement(keySwitch, expression, leftBrace, cases, rightBrace)
    }

    val switchCase = grammar {
        if (lex.matchLookAhead("default")) {
            val keyDefault = lex.read("default")
            val body = macroBody()
            SwitchDefaultRegion(keyDefault, body)
        } else {
            val cases = mutableListOf<SwitchCase>()
            do {
                val keyCase = lex.read("case")
                val expression = expression()
                cases += SwitchCase(keyCase, expression)
            } while (lex.matchLookAhead("case"))
            val body = macroBody()
            SwitchCaseRegion(cases, body)
        }
    }

    val label = grammar {
        val name = identifier()
        val colon = lex.read(COLON)
        Label(name, colon)
    }

    val assignment = grammar {
        statement {
            val setTarget = variable()
            val equals = lex.read(ASSIGN)
            val expression = expression()
            val semi = lex.read(SEMI)
            Assignment(setTarget, equals, expression, semi)
        }
    }

    val defineContextExtends = grammar {
        statement {
            val context = context()
            val extends = lex.read(ASSIGN)
            val superContext = context()
            val semi = lex.read(SEMI)
            DefineContextExtends(context, extends, superContext, semi)
        }
    }

    val macroCall = grammar {
        statement {
            val macroName = identifier()

            val macroArgs = mutableListOf<MacroArg>()
            while (!lex.matchLookAhead(SEMI))
                macroArgs.add(macroArg())
            val semi = lex.read(SEMI)
            MacroCall(macroName, macroArgs, semi)
        }
    }

    val macroArg = grammar {
        val mark = lex.mark()
        val (argName, colon) = try {
            Pair(
                identifier().also { if (it is SyntaxErrorIdentifier) throw UnexpectedTokenWasCaused() },
                lex.read(COLON),
            )
        } catch (e: UnexpectedTokenWasCaused) {
            lex.backTo(mark)
            Pair(null, null)
        } finally {
            mark.dispose()
        }
        val value = expression()
        MacroArg(argName, colon, value)
    }

    val macroDefinition = grammar {
        val macroKey = lex.read("macro")
        val context = context()
        val name = identifier()

        val (leftParenthesis, params, rightParenthesis) = parenthesisedList(
            leftType = L_PARENTHESIS,
            readBody = { macroParam() },
            fallback = ::SyntaxErrorMacroParam,
            rightType = R_PARENTHESIS,
        )

        val body = macroBody()
        MacroDefinition(
            macroKey, context, name,
            leftParenthesis, params, rightParenthesis, body
        )
    }

    val macroParam = grammar {
        val threeDot = lex.tryRead(THREE_DOT)
        val macroTypeName = macroTypeName()
        val id = identifier()
        MacroParam(threeDot, macroTypeName, id)
    }

    val macroTypeNameStrings = ("any|label|desc|type|classType"
            + "|identifier|expression|value|string|int|bool|null").split('|').toSet()

    val macroTypeName = grammar {
        if (lex.matchLookAhead("block")) {
            val block = lex.read("block")
            val context = context()
            BlockMacroType(block, context)
        } else {
            val id = lex.read(macroTypeNameStrings)
            SimpleMacroType(id)
        }
    }

    val macroBody = grammar {
        val (leftBrace, body, rightBrace) = parenthesisedList(
            leftType = L_BRACE,
            readBody = { statement() },
            fallback = ::SyntaxErrorStatement,
            rightType = R_BRACE,
        )
        MacroBody(leftBrace, body, rightBrace)
    }

    val expression: Grammar<Expression> = grammar {
        disjunction()
    }

    fun binaryOperatorGrammar(
        elementGrammarGetter: () -> Grammar<Expression>,
        constructor: (Expression, Token, Expression) -> Expression,
        vararg operators: TokenType,
    ): Grammar<Expression> {
        val element by lazy(LazyThreadSafetyMode.NONE, elementGrammarGetter)
        return grammar {
            var result = element()
            while (lex.matchLookAhead(*operators)) {
                val infixToken = lex.read(*operators)
                val right = element()
                result = constructor(result, infixToken, right)
            }
            result
        }
    }

    val disjunction = binaryOperatorGrammar({ conjunction }, ::BinaryOperatorExpr, BOOL_OR)
    val conjunction = binaryOperatorGrammar({ equality }, ::BinaryOperatorExpr, BOOL_AND)
    val equality = binaryOperatorGrammar({ comparison }, ::BinaryOperatorExpr, EQUALS, NOT_EQUALS)
    val comparison = binaryOperatorGrammar(
        { additiveExpression },
        ::BinaryOperatorExpr,
        LESS_THAN,
        GRATER_THAN,
        LESS_OR_EQUALS,
        GRATER_OR_EQUALS
    )
    val additiveExpression = binaryOperatorGrammar({ multiplicativeExpression }, ::BinaryOperatorExpr, PLUS, MINUS)
    val multiplicativeExpression =
        binaryOperatorGrammar({ prefixUnaryExpression }, ::BinaryOperatorExpr, STAR, SLASH, MODULO)
    val prefixUnaryExpression = grammar {
        val prefixes = mutableListOf<Token>()
        while (true) {
            when (lex.lookAheadType()) {
                INCREMENT, DECREMENT, PLUS, MINUS -> prefixes += lex.read(INCREMENT, DECREMENT, PLUS, MINUS)
                else -> break
            }
        }
        var expr: Expression
        expr = postfixUnaryExpression()
        for (token in prefixes.asReversed()) {
            expr = PrefixUnaryExpr(token, expr)
        }
        expr
    }
    val postfixUnaryExpression = grammar {
        var result = primaryExpression()

        while (true) {
            when (lex.lookAheadType()) {
                INCREMENT, DECREMENT -> {
                    val operator = lex.read(INCREMENT, DECREMENT)
                    result = PostfixUnaryExpr(result, operator)
                }
                L_BRACKET -> {
                    val leftBracket = lex.read(L_BRACKET)
                    val bracketMark = lex.mark()
                    var index: Expression
                    var rightBracket: Token
                    try {
                        index = expression()
                        rightBracket = lex.read(R_BRACKET)
                    } catch (e: UnexpectedTokenWasCaused) {
                        lex.readUntil(R_BRACKET)
                        val list = lex.getTokensSince(bracketMark)
                        index = SyntaxErrorExpr(list.drop(1))
                        rightBracket = list.last()
                    } finally {
                        bracketMark.dispose()
                    }
                    result = IndexingExpr(result, leftBracket, index, rightBracket)
                }
                else -> break
            }
        }

        result
    }

    val primaryExpression = grammar {
        when {
            lex.matchLookAhead(L_PARENTHESIS) -> {
                parenthesisedExpression()
            }
            lex.matchLookAhead("typeof") -> {
                val keyTypeof = lex.read("typeof")
                val expr = parenthesisedExpression()
                TypeofExpr(keyTypeof, expr)
            }
            else -> {
                expressionValue()
            }
        }
    }

    val parenthesisedExpression = grammar {
        parenthesised(
            leftType = L_PARENTHESIS,
            readBody = { left ->
                val expr = expression()
                val right = lex.read(R_PARENTHESIS)
                ParenthesisedExpr(left, expr, right)
            },
            fallback = { left, tokens, right ->
                ParenthesisedExpr(left, SyntaxErrorExpr(tokens), right)
            },
            rightType = R_PARENTHESIS
        )
    }

    val expressionValue = grammar {
        when (lex.lookAheadType()) {
            // typeDescriptor: quotedReferenceTypeDescriptor
            // typeInternalName: quotedInternalName
            // methodDescriptor: quotedMethodDescriptor
            // identifier: quotedIdentifier
            QUOTE -> quotedValue(
                QuotedType.MethodDescriptor,
                QuotedType.TypeDescriptor,
                QuotedType.TypeInternalName,
                QuotedType.Identifier,
            )
            ID -> {
                if (lex.matchLookAhead(ID, index = 1) && lex.matchLookAhead(L_PARENTHESIS, index = 2)) {
                    when (lex.peek(ID)?.word) {
                        // typeDescriptor: 'type' '(' (quotedReferenceTypeDescriptor | variable) ')'
                        "type" -> parenthesisedNonExpressionValue(
                            "type",
                            { quotedValue(QuotedType.TypeDescriptor) },
                        ) { type, left, desc, right ->
                            SurroundedTypeDescriptor(type, left, desc, right)
                        }
                        // typeInternalName: 'classType' '(' (quotedInternalName | variable) ')'
                        "classType" -> parenthesisedNonExpressionValue(
                            "classType",
                            { quotedValue(QuotedType.TypeInternalName) },
                        ) { type, left, desc, right ->
                            SurroundedTypeInternalName(type, left, desc, right)
                        }
                        // methodDescriptor: 'desc' '(' (quotedReferenceTypeDescriptor | variable) ')'
                        "desc" -> parenthesisedNonExpressionValue(
                            "desc",
                            { quotedValue(QuotedType.TypeDescriptor) },
                        ) { type, left, desc, right ->
                            SurroundedMethodDescriptor(type, left, desc, right)
                        }
                        // expressionValue: 'identifier' '(' (identifier | variable) ')'
                        "identifier" -> parenthesisedNonExpressionValue(
                            "identifier",
                            { identifier() },
                        ) { type, left, desc, right ->
                            SurroundedIdentifier(type, left, desc, right)
                        }
                        else -> lex.unexpectedTokenError()
                    }
                } else {
                    when (lex.peek(ID)?.word) {
                        // typeDescriptor: keywords
                        "byte", "short", "int", "long", "float", "double" -> PrimitiveTypeDescriptor(lex.read(ID))
                        else -> lex.unexpectedTokenError()
                    }
                }
            }
            // typeDescriptor: variable
            // typeInternalName: variable
            // methodDescriptor: variable
            DOLLAR -> variable()
            // expressionValue: integer
            INTEGER_LITERAL -> integerLiteral()
            // expressionValue: number
            NUMBER_LITERAL -> numberLiteral()
            // expressionValue: string
            STRING_LITERAL -> stringLiteral()
            else -> lex.unexpectedTokenError()
        }
    }

    private inline fun parenthesisedNonExpressionValue(
        keyword: String,
        readBody: () -> Expression,
        build: (type: Token, left: Token, desc: Expression, right: Token) -> Expression
    ): Expression {
        val type = lex.read(keyword)
        return parenthesised(
            leftType = L_PARENTHESIS,
            readBody = { left ->
                val desc = if (lex.matchLookAhead(DOLLAR)) variable()
                else readBody()
                val right = lex.read(R_PARENTHESIS)
                build(type, left, desc, right)
            },
            fallback = { left, tokens, right ->
                SyntaxErrorExpr(listOf(type, left) + tokens + right)
            },
            rightType = R_PARENTHESIS,
        )
    }

    val variable = grammar {
        val dollar = lex.read(DOLLAR)
        val id = identifier()
        VariableExpr(dollar, id)
    }

    val stringLiteral = grammar { LiteralExpr(lex.read(STRING_LITERAL)) }
    val integerLiteral = grammar { LiteralExpr(lex.read(INTEGER_LITERAL)) }
    val numberLiteral = grammar { LiteralExpr(lex.read(NUMBER_LITERAL)) }

    val context = grammar {
        val atMark = lex.read(AT_MARK)
        val id = identifier()
        ContextSpecifier(atMark, id)
    }

    val identifier = grammar {
        if (lex.matchLookAhead(QUOTE)) quotedValue(QuotedType.Identifier) as Identifier
        else id()
    }

    @Suppress("SpellCheckingInspection")
    /**
     * the syntax of this element:
     * // . ; [ / < > :
     *
     *
     *
     * identifier is always be able to internalName
     *
     * `identifier` -> identifier/internalName
     * `Ljava/lang/String;` -> type: because there is ';'
     * `java/lang/String` -> internalName: because there is '/' and there isn't ';' and does not starts with 'L'
     * `$variable/lang/String` -> internalName: because starts with variable
     * `(Ljava/lang/String;)V` -> desc: because there is ';'
     * `(III)V` -> desc/identifier/internalName: because there is not ';'
     */
    private fun quotedValue(vararg allows: QuotedType): Expression {
        val quoteStart = lex.read(QUOTE)

        val elements = mutableListOf<QuotedElement>()

        val quoteMark = lex.mark()
        var quoteEnd: Token
        try {
            while (!lex.matchLookAhead(QUOTE)) {
                elements += when (lex.lookAheadType()) {
                    DOLLAR -> quotedElementVariable()
                    QUOTED_SEMI -> QuotedElementToken(lex.read(QUOTED_SEMI))
                    SLASH -> QuotedElementToken(lex.read(SLASH))
                    else -> QuotedElementToken(lex.read(QUOTED_ELEMENT))
                }
                lex.mark(quoteMark)
            }
            quoteEnd = lex.read(QUOTE)
        } catch (e: UnexpectedTokenWasCaused) {
            lex.readUntil(QUOTE)
            val list = lex.getTokensSince(quoteMark)
            elements += SyntaxErrorQuotedElement(list.drop(1))
            quoteEnd = list.last()
        } finally {
            quoteMark.dispose()
        }

        val ables = EnumSet.noneOf(QuotedType::class.java)

        if (canIdentifier(elements)) ables += QuotedType.Identifier
        if (canTypeDescriptor(elements)) ables += QuotedType.TypeDescriptor
        if (canTypeInternalName(elements)) ables += QuotedType.TypeInternalName
        if (canMethodDescriptor(elements)) ables += QuotedType.MethodDescriptor

        val parsed = (allows.toSet() - ables)

        if (parsed.isEmpty()) {
            lex.syntaxError(quoteStart.first, quoteEnd.last, "invalid syntax for $allows")
            when (allows.singleOrNull()) {
                QuotedType.Identifier -> SyntaxErrorIdentifier(quoteStart, elements, quoteEnd)
                QuotedType.TypeDescriptor,
                QuotedType.TypeInternalName,
                QuotedType.MethodDescriptor,
                null -> SyntaxErrorQuoted(quoteStart, elements, quoteEnd)
            }
        }

        if (parsed.size == 1) {
            when (parsed.single()!!) {
                QuotedType.Identifier -> QuotedIdentifier(quoteStart, elements.single() as QuotedElementToken, quoteEnd)
                QuotedType.TypeDescriptor -> QuotedReferenceTypeDescriptor(quoteStart, elements, quoteEnd)
                QuotedType.TypeInternalName -> QuotedTypeInternalName(quoteStart, elements, quoteEnd)
                QuotedType.MethodDescriptor -> QuotedMethodDescriptor(quoteStart, elements, quoteEnd)
            }
        }

        return UnknownQuoted(quoteStart, elements, quoteEnd, parsed)
    }

    private fun canIdentifier(elements: List<QuotedElement>): Boolean {
        if (elements.size != 1) return false
        val token = elements.single() as? Token ?: return false
        if (token.type != QUOTED_ELEMENT) return false
        return true
    }

    /**
     * 'L' elem ('/' elem)* ';'
     */
    private fun canTypeDescriptor(elements: List<QuotedElement>): Boolean {
        if (elements.size < 2) return false
        val last = elements.last() as? Token ?: return false
        if (last.type != SEMI) return false
        val first = elements.first() as? Token ?: return false
        if (first.type != QUOTED_ELEMENT) return false
        if (first.word[0] != 'L') return false
        val dropCount: Int
        if (first.word.length == 1) {
            dropCount = 2
            val element = elements[1]
            if (element is QuotedElementToken && element.token.type != QUOTED_ELEMENT) return false
        } else {
            dropCount = 1
        }
        for ((slash, element) in elements.asSequence().dropLast(1).drop(dropCount).windowed(2)) {
            if (slash !is QuotedElementToken) return false
            if (slash.token.type != SLASH) return false
            if (element is QuotedElementToken && element.token.type != QUOTED_ELEMENT) return false
        }
        return true
    }

    /**
     * elem ('/' elem)*
     */
    private fun canTypeInternalName(elements: List<QuotedElement>): Boolean {
        if (elements.isEmpty()) return false
        val first = elements[1]
        if (first is QuotedElementToken && first.token.type != QUOTED_ELEMENT) return false
        for ((slash, element) in elements.asSequence().drop(1).windowed(2)) {
            if (slash !is QuotedElementToken) return false
            if (slash.token.type != SLASH) return false
            if (element is QuotedElementToken && element.token.type != QUOTED_ELEMENT) return false
        }
        return true
    }

    /**
     * '(' (baseType | typeDescriptor)* ')' (baseType | typeDescriptor | 'V')
     */
    private fun canMethodDescriptor(elements: List<QuotedElement>): Boolean {
        if (elements.isEmpty()) return false
        var isExpectingReturnType = false
        var state = CanMethodDescriptorState.First
        for (element in elements) {
            when (state) {
                CanMethodDescriptorState.First -> {
                    if (element !is QuotedElementToken) return false
                    if (element.token.type != QUOTED_ELEMENT) return false
                    if (element.token.word[0] != '(') return false
                    val text = element.token.word
                    state = canMethodDescriptorCheckType(1, text) ?: return false
                }
                CanMethodDescriptorState.TypeExpected,
                CanMethodDescriptorState.TypeRequired,
                -> {
                    if (element !is QuotedElementToken) {
                        // is variable
                        state = CanMethodDescriptorState.TypeExpected
                        continue
                    }
                    if (element.token.type != QUOTED_ELEMENT) return false
                    val text = element.token.word
                    if (text[0] == ')') {
                        if (isExpectingReturnType || state == CanMethodDescriptorState.TypeRequired) {
                            return false
                        } else {
                            // expecting return type
                            isExpectingReturnType = true
                            state = canMethodDescriptorCheckType(1, text) ?: return false
                        }
                    } else {
                        state = canMethodDescriptorCheckType(0, text) ?: return false
                    }
                }
                CanMethodDescriptorState.SlashOrSemiExpected -> {
                    if (element !is QuotedElementToken) return false
                    state = when (element.token.type) {
                        COLON -> CanMethodDescriptorState.TypeDescElementExpected
                        SEMI -> CanMethodDescriptorState.TypeExpected
                        else -> return false
                    }
                }
                CanMethodDescriptorState.TypeDescElementExpected -> {
                    if (element !is QuotedElementToken) {
                        // is variable
                    } else {
                        if (element.token.type != QUOTED_ELEMENT) return false
                    }
                    state = CanMethodDescriptorState.SlashOrSemiExpected
                }
            }
        }

        if (!isExpectingReturnType) return false
        if (state != CanMethodDescriptorState.TypeExpected) return false

        return true
    }

    private fun canMethodDescriptorCheckType(i: Int, text: String): CanMethodDescriptorState? {
        var i = i
        var state = CanMethodDescriptorState.TypeExpected
        var wasArray = false
        while (i < text.length) {
            when (text[i]) {
                'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z' -> wasArray = false
                '[' -> wasArray = true
                'L' -> {
                    wasArray = false
                    state = if (i == text.lastIndex) CanMethodDescriptorState.TypeDescElementExpected
                    else CanMethodDescriptorState.SlashOrSemiExpected
                    break
                }
                else -> return null
            }
            i++
        }
        if (wasArray) state = CanMethodDescriptorState.TypeRequired
        return state
    }

    @Suppress("SpellCheckingInspection")
    /**
     * ```
     *     '(Ljava' / '${lang}' / 'StringBuilder' ; '[' '${string}' ')V'
     *    ^        ^ ^         ^ ^               ^ ^   ^           ^    ^
     * First       | Element   | Element         | | Required  Expect   |
     *        SashOrSemi  SashOrSemi    SashOrSemi Expect            Expect
     * ```
     */
    private enum class CanMethodDescriptorState {
        First,
        TypeExpected,
        TypeRequired,

        // after element of class type descriptor
        SlashOrSemiExpected,

        // after '/' or 'L'
        TypeDescElementExpected,
    }

    val quotedElementVariable = grammar {
        val dollar = lex.read(DOLLAR)
        if (lex.matchLookAhead(L_BRACE)) {
            parenthesised(
                leftType = L_BRACE,
                readBody = { left ->
                    val identifier = identifier()
                    val right = lex.read(R_BRACE)
                    SurroundedQuotedElementVariable(dollar, left, identifier, right)
                },
                fallback = { left, tokens, right ->
                    SyntaxErrorQuotedElementVariable(dollar, left, tokens, right)
                },
                rightType = R_BRACE
            )
        } else {
            val id = id()
            SimpleQuotedElementVariable(dollar, id)
        }
    }


    // lexical

    val id = grammar { SimpleIdentifier(lex.read(ID)) }

}
// */

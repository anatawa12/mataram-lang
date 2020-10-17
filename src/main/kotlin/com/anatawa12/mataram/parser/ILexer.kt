package com.anatawa12.mataram.parser

import com.anatawa12.mataram.ast.CharPosition
import com.anatawa12.mataram.ast.Token

interface ILexer {
    fun matchLookAhead(vararg expects: TokenType, index: Int = 1): Boolean
    fun matchLookAhead(keyword: String, index: Int = 1): Boolean = matchLookAhead(setOf(keyword), index)
    fun matchLookAhead(expectKeywords: Set<String>, index: Int = 1): Boolean = peek(ID)?.word in expectKeywords

    fun read(vararg expects: TokenType): Token
    fun read(expect: String): Token = read(setOf(expect))
    fun read(expects: Set<String>): Token {
        if (peek(ID)?.word !in expects) unexpectedTokenError()
        return read(ID)
    }

    fun tryRead(vararg expects: TokenType): Token? {
        peek(*expects) ?: return null
        return read()
    }

    fun peek(vararg expects: TokenType, index: Int = 1): Token?

    fun lookAheadType(index: Int = 1): TokenType = peek(index = index)!!.type

    /**
     * reads tokens until [expect] token came.
     * the [expect] token will be read.
     */
    fun readUntil(expect: TokenType): List<Token>

    fun mark(): Mark
    fun mark(mark: Mark)
    fun backTo(mark: Mark)
    fun getTokensSince(mark: Mark): List<Token>

    /**
     * targets current token
     */
    fun unexpectedTokenError(): Nothing

    fun syntaxError(begin: CharPosition, end: CharPosition, msg: String)

    object ImplUtils {
        fun Token.takeIfMatch(expects: Array<out TokenType>): Token? =
            if (expects.isEmpty() || this.type in expects) this else null
    }
}


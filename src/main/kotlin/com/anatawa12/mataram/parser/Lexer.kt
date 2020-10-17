package com.anatawa12.mataram.parser

import com.anatawa12.mataram.ast.CharPosition
import com.anatawa12.mataram.ast.Token
import com.anatawa12.mataram.parser.ILexer.ImplUtils.takeIfMatch

class Lexer(private val text: String, private val triviaTokens: Set<TokenType>, tokens: Sequence<LexicalToken>) :
    ILexer {
    private val markImpls = hashSetOf<MarkImpl>()
    private val lookaheads = ArrayDeque<Token>(2)

    private val iterator = iterator {
        val prefixTrivia = mutableListOf<Token>()
        var lastTokenWasAt = CharPosition(0, 0, 0)
        for (lexicalToken in tokens) {
            lastTokenWasAt = lexicalToken.last
            if (lexicalToken.type in triviaTokens) {
                prefixTrivia += lexicalToken.toToken(text, emptyList())
            } else {
                val token = lexicalToken.toToken(text, prefixTrivia.toList())
                prefixTrivia.clear()
                yield(token)
            }
        }
        val eofLexTokenAt = CharPosition(lastTokenWasAt.index + 1, lastTokenWasAt.line, lastTokenWasAt.column + 1)
        val eofLexToken = LexicalToken(eofLexTokenAt, lastTokenWasAt, EOF)
        while (true) {
            val token = eofLexToken.toToken(text, prefixTrivia.toList())
            prefixTrivia.clear()
            yield(token)
        }
    }

    private fun makeLookAhead(count: Int) {
        while (lookaheads.size < count) {
            lookaheads.addLast(iterator.next())
        }
    }

    private fun getLookahead(index: Int): Token {
        makeLookAhead(index)
        return lookaheads[index - 1]
    }

    override fun matchLookAhead(vararg expects: TokenType, index: Int): Boolean {
        return getLookahead(index).takeIfMatch(expects) != null
    }

    override fun peek(vararg expects: TokenType, index: Int): Token? {
        return getLookahead(index).takeIfMatch(expects)
    }

    override fun read(vararg expects: TokenType): Token {
        makeLookAhead(1)
        val token = lookaheads.removeFirst().takeIfMatch(expects) ?: unexpectedTokenError()
        markImpls.forEach { it.tokens += token }
        return token
    }

    override fun readUntil(expect: TokenType): List<Token> {
        val mark = mark()
        try {
            while (read().type != expect) {
                // loop for condition
            }
            return getTokensSince(mark)
        } finally {
            mark.dispose()
        }
    }

    private fun verifyMark(mark: Mark): MarkImpl {
        require(mark in markImpls) { "invalid mark was passed" }
        return mark as MarkImpl
    }

    override fun mark(): Mark = MarkImpl().apply { markImpls += this }

    override fun mark(mark: Mark) {
        val markImpl = verifyMark(mark)
        markImpl.tokens.clear()
    }

    override fun backTo(mark: Mark) {
        val markImpl = verifyMark(mark)
        val tokens = markImpl.tokens.toList()
        val size = markImpl.tokens.size
        for (aMarkImpl in markImpls) {
            check(size <= aMarkImpl.tokens.size) { "some active marks targeting backed tokens are exists" }
            repeat(aMarkImpl.tokens.size - size) {
                aMarkImpl.tokens.removeAt(aMarkImpl.tokens.lastIndex)
            }
        }
        val lookaheadOld = lookaheads.toList()
        lookaheads.clear()
        lookaheads.addAll(tokens)
        lookaheads.addAll(lookaheadOld)
    }

    override fun getTokensSince(mark: Mark): List<Token> {
        val markImpl = verifyMark(mark)
        return markImpl.tokens.toList()
    }

    override fun unexpectedTokenError(): Nothing {
        throw UnexpectedTokenWasCaused()
    }

    override fun syntaxError(begin: CharPosition, end: CharPosition, msg: String) {
        println("syntax error: $msg")
    }

    inner class MarkImpl : Mark {
        val tokens = arrayListOf<Token>()
        override fun dispose() {
            tokens.clear()
            markImpls.remove(this)
        }
    }
}

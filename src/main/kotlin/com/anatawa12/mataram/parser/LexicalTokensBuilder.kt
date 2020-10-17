package com.anatawa12.mataram.parser

import com.anatawa12.mataram.ast.CharPosition

class LexicalTokensAndTextBuilder {
    private val builder = LexicalTokensBuilder()

    private val stringBuilder = StringBuilder()
    private val tokens = mutableListOf<LexicalToken>()

    fun add(token: TokenType) = add(token, token.name)
    fun add(token: TokenType, string: String) {
        stringBuilder.append(string)
        tokens.add(builder.build(token, string))
    }

    fun build() = stringBuilder.toString() to tokens.toList()

    companion object {
        inline operator fun invoke(block: LexicalTokensAndTextBuilder.() -> Unit) = LexicalTokensAndTextBuilder().apply(block).build()
    }
}

class LexicalTokensBuilder {
    private var index = 0
    private var line = 0
    private var column = 0

    private fun onAfterNewLine() {
        line++
        column = 0
    }

    fun build(token: TokenType, string: CharSequence): LexicalToken = build(token, -1, string)
    fun build(token: TokenType, currentIndex: Int, string: CharSequence): LexicalToken {
        val first = CharPosition(index, line, column)
        index += string.length
        if (currentIndex == -1) check(currentIndex == index)
        var status = Status.Normal
        for (c in string) {
            status = when (c) {
                '\r' -> when (status) {
                    Status.Normal -> {
                        Status.AfterCR
                    }
                    Status.AfterCR, Status.AfterNewLine -> {
                        onAfterNewLine()
                        Status.AfterCR
                    }
                }
                '\n' -> when (status) {
                    Status.Normal, Status.AfterCR -> {
                        Status.AfterNewLine
                    }
                    Status.AfterNewLine -> {
                        onAfterNewLine()
                        Status.AfterNewLine
                    }
                }
                else -> when (status) {
                    Status.Normal -> {
                        Status.Normal
                    }
                    Status.AfterCR, Status.AfterNewLine -> {
                        onAfterNewLine()
                        Status.Normal
                    }
                }
            }
            column++
        }
        // because currently column and index is targeting next character
        column--
        index--
        val last = CharPosition(index, line, column)
        index++
        column++
        when (status) {
            Status.Normal -> {
                // nop
            }
            Status.AfterCR, Status.AfterNewLine -> {
                onAfterNewLine()
            }
        }

        return LexicalToken(first, last, token)
    }
    fun build(token: TokenType) = build(token, token.name)

    private enum class Status {
        Normal,
        AfterCR,
        AfterNewLine
    }
}

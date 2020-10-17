package com.anatawa12.mataram.ast

import com.anatawa12.mataram.parser.TokenType

class Token(
    val text: String,
    val type: TokenType,
    val first: CharPosition,
    val last: CharPosition,
    val prefixTrivia: List<Token>
) : Node() {
    val word: String by lazy { (first.index..last.index).takeIf { !it.isEmpty() }?.let { text.substring(it) } ?: "" }

    override fun toString(): String {
        return "Token($type, trivia=$prefixTrivia, $word)"
    }

    override val children: Array<out Node> get() = prefixTrivia.toTypedArray()
}

data class CharPosition(val index: Int, val line: Int, val column: Int)

package com.anatawa12.mataram.parser

import com.anatawa12.mataram.ast.CharPosition
import com.anatawa12.mataram.ast.Token

data class LexicalToken(val first: CharPosition, val last: CharPosition, val type: TokenType) {
    fun toToken(text: String, prefixTrivia: List<Token>): Token = Token(text, type, first, last, prefixTrivia)
}

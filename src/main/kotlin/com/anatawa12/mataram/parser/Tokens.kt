@file:JvmName("Tokens")

package com.anatawa12.mataram.parser

// INITIAL
@JvmField val ID = TokenType("ID")
@JvmField val NUMBER_LITERAL = TokenType("NUMBER_LITERAL")
@JvmField val INTEGER_LITERAL = TokenType("INTEGER_LITERAL")
@JvmField val STRING_LITERAL = TokenType("STRING_LITERAL")

@JvmField val DOLLAR = TokenType("$")
@JvmField val AT_MARK = TokenType("@")
@JvmField val COLON = TokenType(":")
@JvmField val SEMI = TokenType(";")
@JvmField val L_PARENTHESIS = TokenType("(")
@JvmField val R_PARENTHESIS = TokenType(")")
@JvmField val L_BRACKET = TokenType("[")
@JvmField val R_BRACKET = TokenType("]")
@JvmField val L_BRACE = TokenType("{")
@JvmField val R_BRACE = TokenType("}")
@JvmField val EQUALS = TokenType("==")
@JvmField val NOT_EQUALS = TokenType("!=")
@JvmField val LESS_OR_EQUALS = TokenType("<=")
@JvmField val GRATER_OR_EQUALS = TokenType(">=")
@JvmField val LESS_THAN = TokenType("<")
@JvmField val GRATER_THAN = TokenType(">")
@JvmField val ASSIGN = TokenType("=")
@JvmField val DOT = TokenType(".")
@JvmField val THREE_DOT = TokenType("...")
@JvmField val PLUS = TokenType("+")
@JvmField val MINUS = TokenType("-")
@JvmField val STAR = TokenType("*")
@JvmField val SLASH = TokenType("/")
@JvmField val MODULO = TokenType("%")
@JvmField val BOOL_OR = TokenType("||")
@JvmField val BOOL_AND = TokenType("&&")
@JvmField val INCREMENT = TokenType("++")
@JvmField val DECREMENT = TokenType("--")

@JvmField val QUOTE = TokenType("`") // push and go INSIDE_QUOTE

// INSIDE_QUOTE
@JvmField val QUOTED_ELEMENT = TokenType("QUOTED_ELEMENT")
@JvmField val QUOTED_SEMI = TokenType(";")
/*
"$" // go INSIDE_QUOTE_AFTER_DOLLAR
"`" // pop
// INSIDE_QUOTE_AFTER_DOLLAR
"{" // go INSIDE_QUOTE_AFTER_BRACE
id // go INSIDE_QUOTE
// INSIDE_QUOTE_AFTER_BRACE
"..."
id
"`" // push and go INSIDE_QUOTE
"}" // go INSIDE_QUOTE
 */

@JvmField val EOF = TokenType("EOF")


// comments
@JvmField val COMMENT = TokenType("COMMENT")
@JvmField val WHITE_SPACE = TokenType("WHITE_SPACE")

package com.anatawa12.mataram.parser

import java.io.StringReader

fun flexBasedLexer(input: String): Lexer {
    val jflex = JFlexLexer(input)
    return Lexer(input, setOf(WHITE_SPACE, COMMENT), Sequence { FlexBasedLexicalTokenIterator(jflex) })
}

private class FlexBasedLexicalTokenIterator(val lexer: JFlexLexer): Iterator<LexicalToken> {
    private var next: LexicalToken? = null

    override fun hasNext(): Boolean {
        if (next == null) next = lexer.yylex()
        return next != null
    }

    override fun next(): LexicalToken {
        if (!hasNext()) throw NoSuchElementException()
        val result = next!!
        next = null
        return result
    }
}

fun main() {
    //*
    val lex = flexBasedLexer(src)
    while (!lex.matchLookAhead(EOF)) {
        val read = lex.read()
        println(read)
    }
    // */
    /*
    val lex = JFlexLexer(src)
    while (true) {
        val read = lex.yylex() ?: break
        println(read)
    }
    println(EOF)
    // */
}

private val src = """
// class 
macro@root class (
    classType name
    block@java_class block
) {};

@java_class extends @java_annotation_holder;
@java_class extends @java_access_modifier;

macro@java_class field (
    identifier name
    type type
    block@java_field block
) {};

macro@java_class method (
    identifier name
    desc desc
    block@java_method block
) {};

// method

@java_method extends @java_annotation_holder;
@java_method extends @java_access_modifier;

macro@java_field addLine (expression line) {};

// and instructions

// field

@java_field extends @java_annotation_holder;
@java_field extends @java_access_modifier;

macro@java_field defaults (value value) {};

// common

/*
 * 0x0001: public
 * 0x0002: private
 * 0x0004: protected
 * 0x0008: static
 * 0x0010: final
 * 0x0020: super
 * 0x0020: synchronized
 * 0x0040: volatile
 * 0x0040: bridge
 * 0x0080: transient
 * 0x0080: varargs
 * 0x0100: native
 * 0x0200: interface
 * 0x0400: abstract
 * 0x0800: strict
 * 0x1000: synthetic
 * 0x2000: annotation
 * 0x4000: enum
 * 0x8000: mandated
 * 
 * flags: one of public, private, protected, static, final, 
 *     super, synchronized, volatile, bridge, transient, 
 *     varargs, native, interface, abstract, strict, 
 *     synthetic, annotation, enum, mandated.
 */
macro@java_access_modifier access (
    ...identifier flags
) {};

// annotation

macro@java_annotation_holder annotation (
    type type
    block@java_annotation block
) {};

macro@java_annotation array (
    identifier name
    block@java_annotation_array block
) {};
"""

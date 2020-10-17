package com.anatawa12.mataram

import com.anatawa12.mataram.parser.Lexer
import com.anatawa12.mataram.parser.LexicalTokensAndTextBuilder
import com.anatawa12.mataram.parser.Parser
import com.anatawa12.mataram.parser.*

fun main() {
/*
    val (text, tokens) = LexicalTokensAndTextBuilder {
        add(ID, "macro")
        add(AT_MARK)
        add(ID, "root")
        add(ID, "class")
        add(L_PARENTHESIS)
        add(ID, "classType")
        add(ID, "name")
        add(ID, "block")
        add(AT_MARK)
        add(ID, "java_class")
        add(ID, "block")
        add(R_PARENTHESIS)
        add(L_BRACE)
        add(R_BRACE)
    }

    val lexer = Lexer(text, setOf(), tokens.asSequence())
 */
    val lexer = flexBasedLexer(src)
    val parser = Parser(lexer)

    val tree = parser.file()

    println(tree)
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

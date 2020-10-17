package com.anatawa12.mataram.parser

import com.anatawa12.mataram.ast.Node

fun <T : Node?> grammar(run: () -> T) =
    Grammar(run)

class Grammar<T : Node?>(val run: () -> T) {
    operator fun invoke(): T = run()
}

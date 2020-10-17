package com.anatawa12.mataram.ast

abstract class Node {
    val firstToken: Token get() {
        var node = this
        while (true) {
            if (node is Token) return node
            node = node.children[0]
        }
    }

    open val children: Array<out Node> = emptyArray()
}

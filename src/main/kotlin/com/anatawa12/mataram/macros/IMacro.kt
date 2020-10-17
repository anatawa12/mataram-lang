package com.anatawa12.mataram.macros

import com.anatawa12.mataram.ProcessorContext

interface IMacro {
    val parameters: MacroParameterDescriptor
    fun process(ctx: ProcessorContext)
}

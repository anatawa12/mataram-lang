package com.anatawa12.mataram.macros

import kotlin.reflect.KClass
import kotlin.reflect.cast

class MacroValueType<T : MacroRuntimeValue>(val name: String, val type: KClass<T>) {
    fun cast(value: MacroRuntimeValue) = type.cast(value)
    @Suppress("UNCHECKED_CAST")
    fun castOrNull(value: MacroRuntimeValue): T? = if (checkInstance(value)) value as T else null
    fun checkInstance(value: MacroRuntimeValue): Boolean = type.isInstance(value)

    override fun toString(): String = name
}

object MacroValueTypes {
    val any = MacroValueType("Any", MacroRuntimeValue::class)

    val string = MacroValueType("String", StringMacroValue::class)
    val integer = MacroValueType("Integer", IntegerMacroValue::class)
    val number = MacroValueType("Number", AnyNumberMacroValue::class)
    val typeDescriptor = MacroValueType("TypeDescriptor", TypeDescriptorMacroValue::class)
    val typeInternalName = MacroValueType("TypeInternalName", TypeInternalNameMacroValue::class)
    val methodDescriptor = MacroValueType("MethodDescriptor", MethodDescriptorMacroValue::class)
    val identifier = MacroValueType("Identifier", IdentifierMacroValue::class)
    val boolean = MacroValueType("Boolean", BooleanMacroValue::class)
}

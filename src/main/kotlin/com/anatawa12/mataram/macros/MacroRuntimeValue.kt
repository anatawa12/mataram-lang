package com.anatawa12.mataram.macros

import java.math.BigDecimal
import java.math.BigInteger

abstract class MacroRuntimeValue(val type: MacroValueType<*>) {
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
}

sealed class SimpleMacroRuntimeValue<T>(type: MacroValueType<*>) : MacroRuntimeValue(type) {
    abstract val value: T
}
data class StringMacroValue(override val value: String) : SimpleMacroRuntimeValue<String>(MacroValueTypes.string)
sealed class AnyNumberMacroValue<T : Number>(type: MacroValueType<*>) : SimpleMacroRuntimeValue<T>(type) {
    abstract fun asBigDecimal(): BigDecimal
}
data class IntegerMacroValue(override val value: BigInteger) : AnyNumberMacroValue<BigInteger>(MacroValueTypes.integer) {
    override fun asBigDecimal(): BigDecimal = value.toBigDecimal()
}
data class NumberMacroValue(override val value: BigDecimal) : AnyNumberMacroValue<BigDecimal>(MacroValueTypes.number) {
    override fun asBigDecimal(): BigDecimal = value
}
data class TypeDescriptorMacroValue(override val value: String) : SimpleMacroRuntimeValue<String>(MacroValueTypes.typeDescriptor)
data class TypeInternalNameMacroValue(override val value: String) : SimpleMacroRuntimeValue<String>(MacroValueTypes.typeInternalName)
data class MethodDescriptorMacroValue(override val value: String) : SimpleMacroRuntimeValue<String>(MacroValueTypes.typeDescriptor)
data class IdentifierMacroValue(override val value: String) : SimpleMacroRuntimeValue<String>(MacroValueTypes.identifier)
data class BooleanMacroValue(override val value: Boolean) : SimpleMacroRuntimeValue<Boolean>(MacroValueTypes.methodDescriptor)

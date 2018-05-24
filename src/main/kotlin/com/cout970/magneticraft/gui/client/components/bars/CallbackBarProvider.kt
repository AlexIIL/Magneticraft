package com.cout970.magneticraft.gui.client.components.bars

import com.cout970.magneticraft.misc.crafting.TimedCraftingProcess
import com.cout970.magneticraft.misc.gui.ValueAverage
import com.cout970.magneticraft.util.clamp
import com.cout970.magneticraft.util.ensureNonZero

val ZERO = { 0.0 }

open class CallbackBarProvider(val callback: () -> Number, val max: () -> Number,
                               val min: () -> Number) : IBarProvider {

    override fun getLevel(): Float {
        val min = min.invoke().toDouble()
        val max = max.invoke().toDouble()
        val value = callback.invoke().toDouble()

        return clamp((value - min) / ensureNonZero(max - min), 1.0, 0.0).toFloat()
    }
}

class StaticBarProvider(val minVal: Double, val maxVal: Double, callback: () -> Number)
    : CallbackBarProvider(callback = callback, max = { maxVal }, min = { minVal })

fun TimedCraftingProcess.toBarProvider() = CallbackBarProvider(this::timer, this::limit, ZERO)
fun ValueAverage.toBarProvider(max: Number) = CallbackBarProvider(this::storage, { max }, ZERO)


fun CallbackBarProvider.toPercentText(prefix: String, postfix: String = "%") = {
    listOf("$prefix${(getLevel() * 100.0).toInt()}$postfix")
}

fun CallbackBarProvider.toIntText(prefix: String = "", postfix: String = "") = {
    listOf("$prefix${callback().toInt()}$postfix")
}

fun CallbackBarProvider.toFluidPerTick(prefix: String = "") = {
    listOf("$prefix${callback().toInt()}mB/t")
}

fun CallbackBarProvider.toEnergyText(prefix: String = "", postfix: String = "") = {
    listOf("$prefix${String.format("%.2fW", callback())}$postfix")
}

fun CallbackBarProvider.toHeatPerTickText(prefix: String = "Heat: ", postfix: String = "W") = {
    listOf("$prefix${String.format("%.2f", callback())}$postfix")
}
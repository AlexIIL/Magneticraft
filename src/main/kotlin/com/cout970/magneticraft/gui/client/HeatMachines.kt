package com.cout970.magneticraft.gui.client

import com.cout970.magneticraft.config.Config
import com.cout970.magneticraft.gui.client.components.CompBackground
import com.cout970.magneticraft.gui.client.components.bars.*
import com.cout970.magneticraft.gui.client.core.GuiBase
import com.cout970.magneticraft.gui.client.core.TankIO
import com.cout970.magneticraft.gui.client.core.dsl
import com.cout970.magneticraft.gui.common.ContainerBigCombustionChamber
import com.cout970.magneticraft.gui.common.ContainerCombustionChamber
import com.cout970.magneticraft.gui.common.ContainerGasificationUnit
import com.cout970.magneticraft.gui.common.ContainerSteamBoiler
import com.cout970.magneticraft.util.guiTexture
import com.cout970.magneticraft.util.vector.Vec2d
import com.cout970.magneticraft.util.vector.vec2Of

/**
 * Created by cout970 on 2017/08/10.
 */

fun guiCombustionChamber(gui: GuiBase, container: ContainerCombustionChamber) = gui.run {
    val tile = container.tile

    +CompBackground(guiTexture("combustion_chamber"))

    val burningCallback = CallbackBarProvider(
        { tile.combustionChamberModule.maxBurningTime - tile.combustionChamberModule.burningTime.toDouble() },
        tile.combustionChamberModule::maxBurningTime, { 0.0 })

    +CompVerticalBar(burningCallback, 1, Vec2d(69, 16), burningCallback.toPercentText("Fuel: "))
    +CompHeatBar(tile.node, Vec2d(80, 16))
}

fun guiSteamBoiler(gui: GuiBase, container: ContainerSteamBoiler) = gui.run {
    val tile = container.tile
    val texture = guiTexture("steam_boiler")

    +CompBackground(texture)

    val production = tile.boilerModule.production.toBarProvider(tile.boilerModule.maxProduction)

    +CompHeatBar(tile.node, Vec2d(58, 16))
    +CompVerticalBar(production, 3, Vec2d(69, 16), production.toFluidPerTick())

    +CompFluidBar(vec2Of(80, 16), texture, vec2Of(0, 166), tile.waterTank)
    +CompFluidBar(vec2Of(102, 16), texture, vec2Of(0, 166), tile.steamTank)
}

fun guiGasificationUnit(gui: GuiBase, container: ContainerGasificationUnit) = gui.dsl {
    val tile = container.tile

    bars {
        heatBar(tile.heatNode)
        electricConsumption(tile.process.consumption, Config.gasificationUnitConsumption)
        progressBar(tile.process.timedProcess)
        slotPair()
        tank(tile.tank, TankIO.OUT)
    }
}

fun guiBigCombustionChamber(gui: GuiBase, container: ContainerBigCombustionChamber) = gui.dsl {
    val tile = container.tile

    val burningCallback = CallbackBarProvider(
        { tile.bigCombustionChamberModule.maxBurningTime - tile.bigCombustionChamberModule.burningTime.toDouble() },
        tile.bigCombustionChamberModule::maxBurningTime, { 0.0 })

    bars {
        heatBar(tile.node)
        genericBar(1, 6, burningCallback, burningCallback.toPercentText("Fuel: "))
        tank(tile.tank, TankIO.IN)
        singleSlot()
    }
}
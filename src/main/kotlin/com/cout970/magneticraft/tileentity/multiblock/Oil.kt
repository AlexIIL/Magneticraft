package com.cout970.magneticraft.tileentity.multiblock

import com.cout970.magneticraft.api.internal.energy.ElectricNode
import com.cout970.magneticraft.misc.ElectricConstants
import com.cout970.magneticraft.misc.fluid.Tank
import com.cout970.magneticraft.misc.tileentity.DoNotRemove
import com.cout970.magneticraft.misc.tileentity.RegisterTileEntity
import com.cout970.magneticraft.multiblock.MultiblockOilHeater
import com.cout970.magneticraft.multiblock.MultiblockPumpjack
import com.cout970.magneticraft.multiblock.MultiblockRefinery
import com.cout970.magneticraft.multiblock.core.Multiblock
import com.cout970.magneticraft.registry.ELECTRIC_NODE_HANDLER
import com.cout970.magneticraft.tileentity.modules.*
import com.cout970.magneticraft.util.vector.rotatePoint
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos

@RegisterTileEntity("pumpjack")
class TilePumpjack : TileMultiblock(), ITickable {

    override fun getMultiblock(): Multiblock = MultiblockPumpjack

    val tank = Tank(64000).apply { clientFluidName = "oil" }
    val node = ElectricNode(ref)

    val fluidModule = ModuleFluidHandler(tank,
            capabilityFilter = { it, side -> if (side == facing.opposite) it else null })

    val storageModule = ModuleInternalStorage(
            mainNode = node,
            capacity = 10_000,
            lowerVoltageLimit = ElectricConstants.TIER_1_MACHINES_MIN_VOLTAGE,
            upperVoltageLimit = ElectricConstants.TIER_1_MACHINES_MIN_VOLTAGE
    )

    val openGuiModule = ModuleOpenGui()

    val fluidExportModule = ModuleFluidExporter(tank, {
        listOf(facing.rotatePoint(BlockPos.ORIGIN, BlockPos(0, 0, 1)) to facing.opposite)
    })

    val ioModule: ModuleMultiblockIO = ModuleMultiblockIO(
            facing = { facing },
            connectionSpots = listOf(ConnectionSpot(
                    capability = ELECTRIC_NODE_HANDLER!!,
                    pos = BlockPos(-1, 0, 0),
                    side = EnumFacing.UP,
                    getter = { if (active) energyModule else null }
            ))
    )

    val energyModule = ModuleElectricity(
            electricNodes = listOf(node),
            canConnectAtSide = ioModule::canConnectAtSide,
            connectableDirections = ioModule::getConnectableDirections
    )

    val pumpjackModule = ModulePumpjack(
            energy = storageModule,
            tank = tank,
            ref = { pos.offset(facing, 5) },
            active = ::active
    )

    override val multiblockModule = ModuleMultiblockCenter(
            multiblockStructure = getMultiblock(),
            facingGetter = { facing },
            capabilityGetter = ioModule::getCapability
    )

    init {
        initModules(multiblockModule, fluidModule, energyModule, storageModule, ioModule, pumpjackModule,
                openGuiModule, fluidExportModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}


@RegisterTileEntity("oil_heater")
class TileOilHeater : TileMultiblock(), ITickable {

    override fun getMultiblock(): Multiblock = MultiblockOilHeater

    val node = ElectricNode(ref)
    val openGuiModule = ModuleOpenGui()

    val ioModule: ModuleMultiblockIO = ModuleMultiblockIO(
            facing = { facing },
            connectionSpots = listOf()
    )

    val energyModule = ModuleElectricity(
            electricNodes = listOf(node),
            canConnectAtSide = ioModule::canConnectAtSide,
            connectableDirections = ioModule::getConnectableDirections
    )

//    val processModule = ModuleElectricProcessing(
//            costPerTick = Config.hydraulicPressMaxConsumption.toFloat(),
//            workingRate = 1f,
//            storage = node,
//            craftingProcess = HydraulicPressCraftingProcess(
//                    inventory = inventory,
//                    inputSlot = 0,
//                    outputSlot = 1,
//                    mode = hydraulicPressModule::mode
//            )
//    )

    override val multiblockModule = ModuleMultiblockCenter(
            multiblockStructure = getMultiblock(),
            facingGetter = { facing },
            capabilityGetter = ioModule::getCapability
    )

    init {
        initModules(multiblockModule, ioModule, energyModule, openGuiModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}

@RegisterTileEntity("refinery")
class TileRefinery : TileMultiblock(), ITickable {

    override fun getMultiblock(): Multiblock = MultiblockRefinery

    val node = ElectricNode(ref)
    val openGuiModule = ModuleOpenGui()

    val ioModule: ModuleMultiblockIO = ModuleMultiblockIO(
            facing = { facing },
            connectionSpots = listOf()
    )

    val energyModule = ModuleElectricity(
            electricNodes = listOf(node),
            canConnectAtSide = ioModule::canConnectAtSide,
            connectableDirections = ioModule::getConnectableDirections
    )

    override val multiblockModule = ModuleMultiblockCenter(
            multiblockStructure = getMultiblock(),
            facingGetter = { facing },
            capabilityGetter = ioModule::getCapability
    )

    init {
        initModules(multiblockModule, ioModule, energyModule, openGuiModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}


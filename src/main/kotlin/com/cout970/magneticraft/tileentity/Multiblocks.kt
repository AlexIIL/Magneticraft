package com.cout970.magneticraft.tileentity

import com.cout970.magneticraft.api.internal.energy.ElectricNode
import com.cout970.magneticraft.block.Multiblocks
import com.cout970.magneticraft.config.Config
import com.cout970.magneticraft.misc.ElectricConstants
import com.cout970.magneticraft.misc.block.get
import com.cout970.magneticraft.misc.crafting.GrinderCraftingProcess
import com.cout970.magneticraft.misc.crafting.SieveCraftingProcess
import com.cout970.magneticraft.misc.fluid.Tank
import com.cout970.magneticraft.misc.inventory.Inventory
import com.cout970.magneticraft.misc.inventory.InventoryCapabilityFilter
import com.cout970.magneticraft.misc.tileentity.DoNotRemove
import com.cout970.magneticraft.misc.tileentity.RegisterTileEntity
import com.cout970.magneticraft.misc.world.isServer
import com.cout970.magneticraft.multiblock.*
import com.cout970.magneticraft.multiblock.core.Multiblock
import com.cout970.magneticraft.registry.ELECTRIC_NODE_HANDLER
import com.cout970.magneticraft.registry.ITEM_HANDLER
import com.cout970.magneticraft.tileentity.core.TileBase
import com.cout970.magneticraft.tileentity.modules.*
import com.cout970.magneticraft.util.interpolate
import com.cout970.magneticraft.util.iterateArea
import com.cout970.magneticraft.util.vector.*
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

/**
 * Created by cout970 on 2017/07/03.
 */

@RegisterTileEntity("multiblock_gap")
class TileMultiblockGap : TileBase() {

    val multiblockModule = ModuleMultiblockGap()

    init {
        initModules(multiblockModule)
    }
}

abstract class TileMultiblock : TileBase() {

    val facing: EnumFacing
        get() = getBlockState()[Multiblocks.PROPERTY_MULTIBLOCK_ORIENTATION]?.facing ?: EnumFacing.NORTH
    val active: Boolean
        get() = getBlockState()[Multiblocks.PROPERTY_MULTIBLOCK_ORIENTATION]?.active ?: false

    abstract fun getMultiblock(): Multiblock

    @Suppress("LeakingThis")
    abstract val multiblockModule: ModuleMultiblockCenter

    override fun shouldRenderInPass(pass: Int): Boolean {
        return if (active) super.shouldRenderInPass(pass) else pass == 1
    }

    override fun getRenderBoundingBox(): AxisAlignedBB {
        val size = getMultiblock().size.toVec3d()
        val center = getMultiblock().center.toVec3d()
        val box = Vec3d.ZERO toAABBWith size
        val boxWithOffset = box.offset(-center)
        val normalizedBox = EnumFacing.SOUTH.rotateBox(vec3Of(0.5), boxWithOffset)
        val alignedBox = facing.rotateBox(vec3Of(0.5), normalizedBox)
        return alignedBox.offset(pos)
    }
}

@RegisterTileEntity("solar_panel")
class TileSolarPanel : TileMultiblock(), ITickable {

    override fun getMultiblock(): Multiblock = MultiblockSolarPanel

    val node = ElectricNode(ref, capacity = 8.0)

    val ioModule: ModuleMultiblockIO = ModuleMultiblockIO(
            facing = { facing },
            connectionSpots = listOf(
                    ConnectionSpot(
                            capability = ELECTRIC_NODE_HANDLER!!,
                            pos = BlockPos(0, 0, -5),
                            side = EnumFacing.NORTH,
                            getter = { energyModule }
                    ),
                    ConnectionSpot(
                            capability = ELECTRIC_NODE_HANDLER!!,
                            pos = BlockPos(0, 0, 0),
                            side = EnumFacing.SOUTH,
                            getter = { energyModule }
                    )
            )
    )

    val energyModule = ModuleElectricity(
            electricNodes = listOf(node),
            canConnectAtSide = ioModule::canConnectAtSide,
            connectableDirections = ioModule::getConnectableDirections
    )

    override val multiblockModule = ModuleMultiblockCenter(
            multiblockStructure = getMultiblock(),
            facingGetter = { facing },
            capabilityGetter = { _, _, _ -> null }
    )


    //client animation
    var currentAngle = 0f
    var deltaTime = System.currentTimeMillis()

    init {
        initModules(multiblockModule, energyModule, ioModule)
    }

//    fun getConnectableDirections(): List<Pair<BlockPos, EnumFacing>> {
//        val base = ModuleElectricity.NEGATIVE_DIRECTIONS.map { it.toBlockPos() to it.opposite }
//        if (world.getTile<TileSolarPanel>(pos.offset(EnumFacing.NORTH, 5)) != null) {
//            return base + EnumFacing.NORTH.let { BlockPos.ORIGIN.offset(it, 5) to it.opposite }
//        }
//        if (world.getTile<TileSolarPanel>(pos.offset(EnumFacing.WEST, 5)) != null) {
//            return base + EnumFacing.WEST.let { BlockPos.ORIGIN.offset(it, 5) to it.opposite }
//        }
//        return base
//    }

    @DoNotRemove
    override fun update() {
        super.update()
        if (world.isServer) {
            if (active && world.isDaytime && world.provider.hasSkyLight()) {
                var count = 0
                iterateArea(0..2, 0..2) { i, j ->
                    val offset = facing.rotatePoint(BlockPos.ORIGIN, BlockPos(i - 1, 0, j))
                    if (world.canBlockSeeSky(pos + offset)) {
                        count++
                    }
                }
                if (count > 0) {
                    val limit = interpolate(node.voltage, ElectricConstants.TIER_1_MAX_VOLTAGE,
                            ElectricConstants.TIER_1_GENERATORS_MAX_VOLTAGE)
                    node.applyPower((1 - limit) * Config.solarPanelMaxProduction * (count / 9f), false)
                }
            }
        }
    }

//    fun canConnectAtSide(facing: EnumFacing?): Boolean = facing?.axis != EnumFacing.Axis.Y
}

@RegisterTileEntity("shelving_unit")
class TileShelvingUnit : TileMultiblock(), ITickable {

    override fun getMultiblock(): Multiblock = MultiblockShelvingUnit

    val inventory = Inventory(ModuleShelvingUnitMb.MAX_CHESTS * 27)
    val invModule = ModuleInventory(inventory, capabilityFilter = ModuleInventory.ALLOW_NONE)

    val shelvingUnitModule = ModuleShelvingUnitMb(inventory)

    override val multiblockModule = ModuleMultiblockCenter(
            multiblockStructure = getMultiblock(),
            facingGetter = { facing },
            capabilityGetter = shelvingUnitModule::getCapability
    )

    init {
        initModules(multiblockModule, shelvingUnitModule, invModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}

@RegisterTileEntity("steam_engine")
class TileSteamEngine : TileMultiblock(), ITickable {

    override fun getMultiblock(): Multiblock = MultiblockSteamEngine

    val tank = Tank(16000)
    val node = ElectricNode(ref, capacity = 8.0)

    val fluidModule = ModuleFluidHandler(tank)

    val energyModule = ModuleElectricity(
            electricNodes = listOf(node),
            connectableDirections = { emptyList() },
            capabilityFilter = { false }
    )

    val storageModule = ModuleInternalStorage(
            mainNode = node,
            capacity = 10_000,
            lowerVoltageLimit = ElectricConstants.TIER_1_GENERATORS_MAX_VOLTAGE,
            upperVoltageLimit = ElectricConstants.TIER_1_GENERATORS_MAX_VOLTAGE
    )

    val steamGeneratorModule = ModuleSteamGenerator(
            steamTank = tank,
            storage = storageModule
    )

    val steamEngineMbModule = ModuleSteamEngineMb(
            facingGetter = { facing },
            steamProduction = steamGeneratorModule.production
    )

    val ioModule: ModuleMultiblockIO = ModuleMultiblockIO(
            facing = { facing },
            connectionSpots = listOf(ConnectionSpot(
                    capability = ELECTRIC_NODE_HANDLER!!,
                    pos = BlockPos(-2, 0, -2),
                    side = EnumFacing.UP,
                    getter = { energyModule }
            ))
    )

    override val multiblockModule = ModuleMultiblockCenter(
            multiblockStructure = getMultiblock(),
            facingGetter = { facing },
            capabilityGetter = ioModule::getCapability,
            dynamicCollisionBoxes = steamEngineMbModule::getDynamicCollisionBoxes
    )

    init {
        initModules(multiblockModule, fluidModule, energyModule, storageModule, steamGeneratorModule,
                steamEngineMbModule, ioModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}

@RegisterTileEntity("grinder")
class TileGrinder : TileMultiblock(), ITickable {

    override fun getMultiblock(): Multiblock = MultiblockGrinder

    val node = ElectricNode(ref, capacity = 8.0)

    val inventory = Inventory(3)

    val openGuiModule = ModuleOpenGui()

    val ioModule: ModuleMultiblockIO = ModuleMultiblockIO(
            facing = { facing },
            connectionSpots = listOf(
                    ConnectionSpot(
                            capability = ELECTRIC_NODE_HANDLER!!,
                            pos = BlockPos(1, 1, -1),
                            side = EnumFacing.EAST,
                            getter = { energyModule }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(0, 0, -2), EnumFacing.SOUTH,
                            getter = { InventoryCapabilityFilter(inventory, emptyList(), listOf(1, 2)) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(0, 3, 0), EnumFacing.UP,
                            getter = { InventoryCapabilityFilter(inventory, listOf(0), emptyList()) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(1, 3, 0), EnumFacing.UP,
                            getter = { InventoryCapabilityFilter(inventory, listOf(0), emptyList()) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(2, 3, 0), EnumFacing.UP,
                            getter = { InventoryCapabilityFilter(inventory, listOf(0), emptyList()) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(0, 3, 1), EnumFacing.UP,
                            getter = { InventoryCapabilityFilter(inventory, listOf(0), emptyList()) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(1, 3, 1), EnumFacing.UP,
                            getter = { InventoryCapabilityFilter(inventory, listOf(0), emptyList()) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(2, 3, 1), EnumFacing.UP,
                            getter = { InventoryCapabilityFilter(inventory, listOf(0), emptyList()) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(0, 3, 2), EnumFacing.UP,
                            getter = { InventoryCapabilityFilter(inventory, listOf(0), emptyList()) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(1, 3, 2), EnumFacing.UP,
                            getter = { InventoryCapabilityFilter(inventory, listOf(0), emptyList()) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(2, 3, 2), EnumFacing.UP,
                            getter = { InventoryCapabilityFilter(inventory, listOf(0), emptyList()) }
                    )
            )
    )

    val energyModule = ModuleElectricity(
            electricNodes = listOf(node),
            canConnectAtSide = ioModule::canConnectAtSide,
            connectableDirections = ioModule::getConnectableDirections
    )

    val storageModule = ModuleInternalStorage(
            mainNode = node,
            capacity = 10_000,
            lowerVoltageLimit = ElectricConstants.TIER_1_MACHINES_MIN_VOLTAGE,
            upperVoltageLimit = ElectricConstants.TIER_1_MACHINES_MIN_VOLTAGE
    )

    val invModule = ModuleInventory(
            inventory = inventory,
            capabilityFilter = ModuleInventory.ALLOW_NONE
    )

    val processModule = ModuleElectricProcessing(
            craftingProcess = GrinderCraftingProcess(invModule, 0, 1, 2),
            storage = storageModule,
            workingRate = 1f,
            costPerTick = Config.grinderMaxConsumption.toFloat()
    )

    override val multiblockModule = ModuleMultiblockCenter(
            multiblockStructure = getMultiblock(),
            facingGetter = { facing },
            capabilityGetter = ioModule::getCapability
    )

    init {
        initModules(multiblockModule, energyModule, storageModule, processModule, invModule, openGuiModule, ioModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}

@RegisterTileEntity("sieve")
class TileSieve : TileMultiblock(), ITickable {

    override fun getMultiblock(): Multiblock = MultiblockSieve

    val node = ElectricNode(ref, capacity = 8.0)

    val inventory = Inventory(4)

    val openGuiModule = ModuleOpenGui()

    val ioModule: ModuleMultiblockIO = ModuleMultiblockIO(
            facing = { facing },
            connectionSpots = listOf(
                    ConnectionSpot(
                            capability = ELECTRIC_NODE_HANDLER!!,
                            pos = BlockPos(-1, 1, 0),
                            side = EnumFacing.SOUTH,
                            getter = { energyModule }
                    ),
                    ConnectionSpot(
                            capability = ELECTRIC_NODE_HANDLER!!,
                            pos = BlockPos(1, 1, 0),
                            side = EnumFacing.SOUTH,
                            getter = { energyModule }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(0, 1, 0), EnumFacing.UP,
                            getter = { InventoryCapabilityFilter(inventory, listOf(0), emptyList()) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(0, 0, -1), EnumFacing.DOWN,
                            getter = { InventoryCapabilityFilter(inventory, emptyList(), listOf(1)) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(0, 0, -2), EnumFacing.DOWN,
                            getter = { InventoryCapabilityFilter(inventory, emptyList(), listOf(2)) }
                    ),
                    ConnectionSpot(ITEM_HANDLER!!, BlockPos(0, 0, -3), EnumFacing.DOWN,
                            getter = { InventoryCapabilityFilter(inventory, emptyList(), listOf(3)) }
                    )
            )
    )

    val energyModule = ModuleElectricity(
            electricNodes = listOf(node),
            canConnectAtSide = ioModule::canConnectAtSide,
            connectableDirections = ioModule::getConnectableDirections
    )

    val storageModule = ModuleInternalStorage(
            mainNode = node,
            capacity = 10_000,
            lowerVoltageLimit = ElectricConstants.TIER_1_MACHINES_MIN_VOLTAGE,
            upperVoltageLimit = ElectricConstants.TIER_1_MACHINES_MIN_VOLTAGE
    )

    val invModule = ModuleInventory(
            inventory = inventory,
            capabilityFilter = ModuleInventory.ALLOW_NONE
    )

    val processModule = ModuleElectricProcessing(
            craftingProcess = SieveCraftingProcess(invModule, 0, 1, 2, 3),
            storage = storageModule,
            workingRate = 1f,
            costPerTick = Config.sieveMaxConsumption.toFloat()
    )

    override val multiblockModule = ModuleMultiblockCenter(
            multiblockStructure = getMultiblock(),
            facingGetter = { facing },
            capabilityGetter = ioModule::getCapability
    )

    init {
        initModules(multiblockModule, energyModule, storageModule, processModule, invModule, ioModule, openGuiModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}



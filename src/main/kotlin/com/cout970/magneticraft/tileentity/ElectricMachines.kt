package com.cout970.magneticraft.tileentity

import com.cout970.magneticraft.api.internal.energy.ElectricNode
import com.cout970.magneticraft.block.ElectricMachines
import com.cout970.magneticraft.config.Config
import com.cout970.magneticraft.misc.ElectricConstants
import com.cout970.magneticraft.misc.block.getOrientation
import com.cout970.magneticraft.misc.crafting.FurnaceCraftingProcess
import com.cout970.magneticraft.misc.energy.IMachineEnergyInterface
import com.cout970.magneticraft.misc.energy.RfStorage
import com.cout970.magneticraft.misc.inventory.Inventory
import com.cout970.magneticraft.misc.inventory.InventoryCapabilityFilter
import com.cout970.magneticraft.misc.tileentity.DoNotRemove
import com.cout970.magneticraft.misc.tileentity.RegisterTileEntity
import com.cout970.magneticraft.tileentity.core.TileBase
import com.cout970.magneticraft.tileentity.modules.*
import com.cout970.magneticraft.util.add
import com.cout970.magneticraft.util.fromCelsiusToKelvin
import com.cout970.magneticraft.util.getBlockPos
import com.cout970.magneticraft.util.newNbt
import com.cout970.magneticraft.util.vector.toAABBWith
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

/**
 * Created by cout970 on 2017/06/29.
 */

@RegisterTileEntity("battery")
class TileBattery : TileBase(), ITickable {

    val facing: EnumFacing get() = getBlockState().getOrientation()
    val node = ElectricNode(ref, capacity = 8.0)

    val electricModule = ModuleElectricity(
            electricNodes = listOf(node),
            canConnectAtSide = this::canConnectAtSide
    )
    val storageModule = ModuleInternalStorage(
            capacity = Config.blockBatteryCapacity,
            mainNode = node,
            maxChargeSpeed = 640.0,
            upperVoltageLimit = ElectricConstants.TIER_1_BATTERY_CHARGE_VOLTAGE,
            lowerVoltageLimit = ElectricConstants.TIER_1_BATTERY_DISCHARGE_VOLTAGE
    )

    val inventory = Inventory(2)
    val invModule = ModuleInventory(inventory)

    val itemChargeModule = ModuleChargeItems(
            inventory = inventory,
            storage = storageModule,
            chargeSlot = 0,
            dischargeSlot = 1,
            transferRate = Config.blockBatteryTransferRate
    )

    init {
        initModules(electricModule, storageModule, invModule, itemChargeModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }

    fun canConnectAtSide(facing: EnumFacing?): Boolean {
        return facing?.axis == EnumFacing.Axis.Y || facing == this.facing.opposite
    }
}

@RegisterTileEntity("electric_furnace")
class TileElectricFurnace : TileBase(), ITickable {

    val facing: EnumFacing get() = getBlockState().getOrientation()
    val node = ElectricNode(ref)

    val electricModule = ModuleElectricity(
            electricNodes = listOf(node)
    )
    val storageModule = ModuleInternalStorage(
            capacity = 10000,
            mainNode = node
    )
    val invModule = ModuleInventory(Inventory(2), capabilityFilter = {
        InventoryCapabilityFilter(it, inputSlots = listOf(0), outputSlots = listOf(1))
    })
    val processModule = ModuleElectricProcessing(
            craftingProcess = FurnaceCraftingProcess(invModule, 0, 1),
            storage = storageModule,
            workingRate = 1f,
            costPerTick = Config.electricFurnaceMaxConsumption.toFloat()
    )

    init {
        initModules(electricModule, storageModule, invModule, processModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}

@RegisterTileEntity("infinite_energy")
class TileInfiniteEnergy : TileBase(), ITickable {

    val node = ElectricNode(ref)

    val electricModule = ModuleElectricity(
            listOf(node)
    )

    init {
        initModules(electricModule)
    }

    @DoNotRemove
    override fun update() {
        node.voltage = ElectricConstants.TIER_1_GENERATORS_MAX_VOLTAGE
        super.update()
        node.voltage = ElectricConstants.TIER_1_GENERATORS_MAX_VOLTAGE
    }
}


@RegisterTileEntity("airlock")
class TileAirLock : TileBase(), ITickable {

    val node = ElectricNode(ref)

    val electricModule = ModuleElectricity(
            electricNodes = listOf(node)
    )

    val airlockModule = ModuleAirlock(node)

    init {
        initModules(airlockModule, electricModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}


@RegisterTileEntity("thermopile")
class TileThermopile : TileBase(), ITickable {

    val node = ElectricNode(ref)

    val electricModule = ModuleElectricity(
            electricNodes = listOf(node)
    )
    val storageModule = ModuleInternalStorage(
            capacity = 10000,
            mainNode = node,
            upperVoltageLimit = ElectricConstants.TIER_1_GENERATORS_MAX_VOLTAGE - 5,
            lowerVoltageLimit = ElectricConstants.TIER_1_GENERATORS_MAX_VOLTAGE - 10
    )

    val thermopileModule = ModuleThermopile(node)

    init {
        initModules(electricModule, storageModule, thermopileModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}

@RegisterTileEntity("wind_turbine")
class TileWindTurbine : TileBase(), ITickable {

    val facing: EnumFacing get() = getBlockState().getOrientation()
    val node = ElectricNode(ref)

    val electricModule = ModuleElectricity(
            electricNodes = listOf(node)
    )
    val storageModule = ModuleInternalStorage(
            capacity = 10000,
            mainNode = node,
            upperVoltageLimit = ElectricConstants.TIER_1_GENERATORS_MAX_VOLTAGE - 5,
            lowerVoltageLimit = ElectricConstants.TIER_1_GENERATORS_MAX_VOLTAGE - 10
    )

    val windTurbineModule = ModuleWindTurbine(
            electricNode = node,
            facingGetter = { facing }
    )

    init {
        initModules(electricModule, storageModule, windTurbineModule)
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }

    override fun getRenderBoundingBox(): AxisAlignedBB {
        return (Vec3d(-6.0, -6.0, -6.0) toAABBWith Vec3d(6.0, 6.0, 6.0)).offset(pos)
    }
}

@RegisterTileEntity("wind_turbine_gap")
class TileWindTurbineGap : TileBase() {

    var centerPos: BlockPos? = null

    override fun save(): NBTTagCompound = newNbt {
        if (centerPos != null) {
            add("center", centerPos!!)
        }
    }

    override fun load(nbt: NBTTagCompound) {
        if (nbt.hasKey("center")) {
            centerPos = nbt.getBlockPos("center")
        }
    }
}


@RegisterTileEntity("electric_heater")
class TileElectricHeater : TileBase(), ITickable {

    val node = ElectricNode(ref)

    val electricModule = ModuleElectricity(
            electricNodes = listOf(node)
    )

    val storageModule = ModuleInternalStorage(
            capacity = 10000,
            mainNode = node
    )

    val electricHeaterModule = ModuleElectricHeater(storageModule)

    val updateBlockstate = ModuleUpdateBlockstate { currentState ->
        if (electricHeaterModule.heat > 50.fromCelsiusToKelvin())
            currentState.withProperty(ElectricMachines.PROPERTY_WORKING_MODE, ElectricMachines.WorkingMode.ON)
        else
            currentState.withProperty(ElectricMachines.PROPERTY_WORKING_MODE, ElectricMachines.WorkingMode.OFF)
    }

    init {
        initModules(electricModule, storageModule, electricHeaterModule, updateBlockstate)
    }

    override fun shouldRefresh(world: World?, pos: BlockPos?, oldState: IBlockState, newSate: IBlockState): Boolean {
        return oldState.block !== newSate.block
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}

@RegisterTileEntity("rf_heater")
class TileRfHeater : TileBase(), ITickable {

    val storage = RfStorage(80_000)

    val rfModule = ModuleRf(storage)

    val electricHeaterModule = ModuleElectricHeater(object : IMachineEnergyInterface {
        override fun getSpeed(): Double = 1.0

        override fun hasEnergy(amount: Double): Boolean {
            return storage.energyStored > amount.toInt()
        }

        override fun useEnergy(amount: Double) {
            storage.extractEnergy(amount.toInt(), false)
        }
    })

    val updateBlockstate = ModuleUpdateBlockstate { currentState ->
        if (electricHeaterModule.heat > 50.fromCelsiusToKelvin())
            currentState.withProperty(ElectricMachines.PROPERTY_WORKING_MODE, ElectricMachines.WorkingMode.ON)
        else
            currentState.withProperty(ElectricMachines.PROPERTY_WORKING_MODE, ElectricMachines.WorkingMode.OFF)
    }

    init {
        initModules(electricHeaterModule, rfModule, updateBlockstate)
    }

    override fun shouldRefresh(world: World?, pos: BlockPos?, oldState: IBlockState, newSate: IBlockState): Boolean {
        return oldState.block !== newSate.block
    }

    @DoNotRemove
    override fun update() {
        super.update()
    }
}

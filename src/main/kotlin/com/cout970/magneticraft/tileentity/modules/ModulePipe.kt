package com.cout970.magneticraft.tileentity.modules

import com.cout970.magneticraft.AABB
import com.cout970.magneticraft.api.core.ITileRef
import com.cout970.magneticraft.api.internal.registries.tool.wrench.WrenchRegistry
import com.cout970.magneticraft.block.FluidMachines
import com.cout970.magneticraft.block.core.IOnActivated
import com.cout970.magneticraft.block.core.OnActivatedArgs
import com.cout970.magneticraft.misc.fluid.Tank
import com.cout970.magneticraft.misc.tileentity.getModule
import com.cout970.magneticraft.misc.world.isClient
import com.cout970.magneticraft.misc.world.isServer
import com.cout970.magneticraft.registry.FLUID_HANDLER
import com.cout970.magneticraft.registry.fromTile
import com.cout970.magneticraft.tileentity.core.IModule
import com.cout970.magneticraft.tileentity.core.IModuleContainer
import com.cout970.magneticraft.tileentity.core.TileBase
import com.cout970.magneticraft.tileentity.modules.pipe.INetworkNode
import com.cout970.magneticraft.tileentity.modules.pipe.Network
import com.cout970.magneticraft.tileentity.modules.pipe.PipeNetwork
import com.cout970.magneticraft.tileentity.modules.pipe.PipeType
import com.cout970.magneticraft.util.add
import com.cout970.magneticraft.util.newNbt
import com.cout970.magneticraft.util.vector.plus
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate

/**
 * Created by cout970 on 2017/08/28.
 */
class ModulePipe(
        val tank: Tank,
        val type: PipeType,
        override val name: String = "module_pipe"
) : IModule, INetworkNode, IOnActivated {

    override lateinit var container: IModuleContainer

    enum class ConnectionState { PASSIVE, DISABLE, ACTIVE }
    enum class ConnectionType { NONE, PIPE, TANK }

    val connectionStates = Array(6) { ConnectionState.PASSIVE }
    var pipeNetwork: PipeNetwork? = null

    //@formatter:off
    override var network: Network<*>?
        set(it) { pipeNetwork = it as? PipeNetwork }
        get() = pipeNetwork
    //@formatter:on

    override val ref: ITileRef get() = container.ref

    override val sides: List<EnumFacing> = EnumFacing.values()
            .filter { connectionStates[it.ordinal] != ConnectionState.DISABLE }.toList()

    fun getConnectionType(side: EnumFacing, render: Boolean): ConnectionType {
        val tile = world.getTileEntity(pos + side) ?: return ConnectionType.NONE

        if (tile is TileBase && tile.getModule<ModulePipe>() != null) {
            val mod = tile.getModule<ModulePipe>()

            if (mod != null && mod.type == type) {
                if (render && (connectionStates[side.ordinal] == ConnectionState.DISABLE
                                || mod.connectionStates[side.opposite.ordinal] == ConnectionState.DISABLE)) {
                    return ConnectionType.NONE
                }
                return ConnectionType.PIPE
            }
        } else {
            val handler = FLUID_HANDLER!!.fromTile(tile, side.opposite)
            if (handler != null) {
                if (render && connectionStates[side.ordinal] == ConnectionState.DISABLE) {
                    return ConnectionType.NONE
                }
                return ConnectionType.TANK
            }
        }

        return ConnectionType.NONE
    }

    override fun update() {
        if (world.isClient) return

        if (pipeNetwork == null) {
            pipeNetwork = PipeNetwork.createNetwork(this).apply { expand() }
        }

        enumValues<EnumFacing>().forEach { side ->
            val state = connectionStates[side.ordinal]
            if (state == ModulePipe.ConnectionState.DISABLE) return@forEach

            val tile = world.getTileEntity(pos + side) ?: return@forEach
            val handler = FLUID_HANDLER!!.fromTile(tile, side.opposite) ?: return@forEach

            if (state == ModulePipe.ConnectionState.PASSIVE) {
                if (FluidUtil.tryFluidTransfer(handler, getNetworkTank(), type.maxRate, false) != null) {
                    FluidUtil.tryFluidTransfer(handler, getNetworkTank(), type.maxRate, true)
                }
            } else if (state == ModulePipe.ConnectionState.ACTIVE) {
                if (FluidUtil.tryFluidTransfer(getNetworkTank(), handler, type.maxRate, false) != null) {
                    FluidUtil.tryFluidTransfer(getNetworkTank(), handler, type.maxRate, true)
                }
            }
        }
    }

    override fun onActivated(args: OnActivatedArgs): Boolean {
        if (args.heldItem.isEmpty || !WrenchRegistry.isWrench(args.heldItem)) return false

        val boxes = FluidMachines.fluidPipeSides(args.worldIn, args.pos)
        val side = boxes.find { it.second.containsPoint(args.hit) }?.first ?: return false

        if (args.worldIn.isServer) {
            val current = connectionStates[side.ordinal].ordinal
            connectionStates[side.ordinal] = ModulePipe.ConnectionState.values()[(current + 1) % ModulePipe.ConnectionState.values().size]
            container.sendUpdateToNearPlayers()
        }
        return true
    }

    private fun AABB.containsPoint(vec: Vec3d): Boolean {
        return vec.x >= this.minX && vec.x <= this.maxX &&
                vec.y >= this.minY && vec.y <= this.maxY &&
                vec.z >= this.minZ && vec.z <= this.maxZ
    }

    fun getNetworkTank() = FluidHandlerConcatenate(pipeNetwork?.members?.map { it.tank } ?: emptyList())

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(cap: Capability<T>, facing: EnumFacing?): T? {
        if (cap == FLUID_HANDLER && facing != null && connectionStates[facing.ordinal] == ModulePipe.ConnectionState.PASSIVE) {
            return getNetworkTank() as T
        }
        return null
    }

    override fun onBreak() {
        if (world.isServer) {
            container.tile.invalidate()
        }
        pipeNetwork?.split(this)
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        connectionStates.indices.forEach {
            connectionStates[it] = ModulePipe.ConnectionState.values()[nbt.getInteger(it.toString())]
        }
    }

    override fun serializeNBT(): NBTTagCompound = newNbt {
        connectionStates.forEachIndexed { index, state ->
            add(index.toString(), state.ordinal)
        }
    }
}
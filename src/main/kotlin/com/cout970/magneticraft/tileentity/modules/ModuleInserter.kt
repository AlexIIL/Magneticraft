package com.cout970.magneticraft.tileentity.modules

import com.cout970.magneticraft.misc.inventory.*
import com.cout970.magneticraft.misc.world.isServer
import com.cout970.magneticraft.registry.ITEM_HANDLER
import com.cout970.magneticraft.registry.fromTile
import com.cout970.magneticraft.tileentity.core.IModule
import com.cout970.magneticraft.tileentity.core.IModuleContainer
import com.cout970.magneticraft.util.add
import com.cout970.magneticraft.util.newNbt
import com.cout970.magneticraft.util.vector.plus
import com.cout970.magneticraft.util.vector.toBlockPos
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.items.IItemHandler

/**
 * Created by cout970 on 2017/06/20.
 */
class ModuleInserter(
    val facingGetter: () -> EnumFacing,
    val inventory: Inventory,
    override val name: String = "module_conveyor_belt"
) : IModule {

    override lateinit var container: IModuleContainer
    val facing get() = facingGetter()
    val machine = InsertStateMachine(this)

    override fun update() {
        machine.tick()
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        machine.state = State.values()[nbt.getInteger("state")]
        machine.transition = Transition.values()[nbt.getInteger("transition")]
        machine.animationTime = nbt.getFloat("animationTime")
        machine.maxAnimationTime = nbt.getFloat("maxAnimationTime")
        machine.sleep = nbt.getInteger("sleep")
    }

    override fun serializeNBT(): NBTTagCompound = newNbt {
        add("state", machine.state.ordinal)
        add("transition", machine.transition.ordinal)
        add("animationTime", machine.animationTime)
        add("maxAnimationTime", machine.maxAnimationTime)
        add("sleep", machine.sleep)
    }
}

class InsertStateMachine(val mod: ModuleInserter) {
    companion object {
        const val delay = 10f
    }

    var state = State.CONTRACTED
    var transition = Transition.MOVE_TO_DROP_LOW
    val moving: Boolean get() = state == State.TRANSITION
    var animationTime = 0f
    var maxAnimationTime = delay
    var sleep = 0

    fun tick() {

        if (sleep > 0) {
            sleep--
            return
        }
        if (moving && animationTime < maxAnimationTime) animationTime += 1f

        if (state == State.TRANSITION && animationTime >= maxAnimationTime) {
            state = when (transition) {
                Transition.MOVE_TO_DROP_LOW -> State.EXTENDED_LOW
                Transition.ROTATING -> State.CONTRACTED_INVERSE
                Transition.ROTATING_INVERSE -> State.CONTRACTED
                Transition.MOVE_FROM_DROP_LOW -> State.CONTRACTED
                Transition.MOVE_TO_DROP_LOW_INVERSE -> State.EXTENDED_LOW_INVERSE
                Transition.MOVE_FROM_DROP_LOW_INVERSE -> State.CONTRACTED_INVERSE
                Transition.MOVE_TO_DROP_HIGH -> State.EXTENDED_HIGH
                Transition.MOVE_FROM_DROP_HIGH -> State.CONTRACTED
                Transition.MOVE_TO_DROP_HIGH_INVERSE -> State.EXTENDED_HIGH_INVERSE
                Transition.MOVE_FROM_DROP_HIGH_INVERSE -> State.CONTRACTED_INVERSE
            }
        }

        when (state) {
            State.CONTRACTED -> {
                if (shouldGrabItems()) {
                    if (canGrab(Level.HIGH)) {
                        transition(Transition.MOVE_TO_DROP_HIGH)
                    } else if (canGrab(Level.LOW)) {
                        transition(Transition.MOVE_TO_DROP_LOW)
                    }
                } else if (shouldDropItems()) {
                    transition(Transition.ROTATING)
                }
            }
            State.CONTRACTED_INVERSE -> {
                if (shouldDropItems()) {
                    if (canDrop(Level.HIGH)) {
                        transition(Transition.MOVE_TO_DROP_HIGH_INVERSE)
                    } else if (canDrop(Level.LOW)) {
                        transition(Transition.MOVE_TO_DROP_LOW_INVERSE)
                    }
                } else if (shouldGrabItems()) {
                    transition(Transition.ROTATING_INVERSE)
                }
            }
            State.EXTENDED_LOW -> if (canGrab(Level.LOW) && grab(Level.LOW)) transition(Transition.MOVE_FROM_DROP_LOW) else sleep()
            State.EXTENDED_HIGH -> if (canGrab(Level.HIGH) && grab(Level.HIGH)) transition(Transition.MOVE_FROM_DROP_HIGH) else sleep()
            State.EXTENDED_LOW_INVERSE -> if (canDrop(Level.LOW) && drop(Level.LOW)) transition(Transition.MOVE_FROM_DROP_LOW_INVERSE) else sleep()
            State.EXTENDED_HIGH_INVERSE -> if (canDrop(Level.HIGH) && drop(Level.HIGH)) transition(Transition.MOVE_FROM_DROP_HIGH_INVERSE) else sleep()
            else -> Unit
        }
    }

    fun transition(new: Transition) {
        state = State.TRANSITION
        transition = new
        maxAnimationTime = delay
        animationTime = 0f
        mod.container.sendUpdateToNearPlayers()
    }

    fun sleep() {
        sleep = delay.toInt()
        mod.container.sendUpdateToNearPlayers()
    }

    fun shouldGrabItems() = mod.inventory[0].isEmpty
    fun shouldDropItems() = mod.inventory[0].isNotEmpty

    private fun getInv(offset: BlockPos): IItemHandler? {
        val tile = mod.world.getTileEntity(mod.pos + offset) ?: return null
        return ITEM_HANDLER!!.fromTile(tile, EnumFacing.UP)
    }

    fun canGrab(level: Level): Boolean {
        if (shouldDropItems()) return false
        val offset = offset(level, false)
        val handler = getInv(offset) ?: return false

        val slot = (0 until handler.slots).firstOrNull { handler[it].isNotEmpty } ?: return false
        return handler.extractItem(slot, 64, true).isNotEmpty
    }

    fun canDrop(level: Level): Boolean {
        if (shouldGrabItems()) return false
        val offset = offset(level, true)
        val handler = getInv(offset) ?: return false

        val slot = (0 until handler.slots).firstOrNull { handler[it].isEmpty } ?: return false
        return handler.insertItem(slot, mod.inventory[0], true).isEmpty
    }

    fun grab(level: Level): Boolean {
        if (mod.world.isServer) {
            val offset = offset(level, false)
            val handler = getInv(offset) ?: return false

            val slot = (0 until handler.slots).firstOrNull { handler[it].isNotEmpty } ?: return false
            val extracted = handler.extractItem(slot, 64, true)
            if (extracted.isEmpty) return false

            mod.inventory[0] = handler.extractItem(slot, 64, false)
            mod.container.sendUpdateToNearPlayers()
        }
        return true
    }

    fun drop(level: Level): Boolean {
        if (mod.world.isServer) {
            val offset = offset(level, true)
            val handler = getInv(offset) ?: return false

            val remaining = handler.insertItem(mod.inventory[0], true)
            if (remaining.isNotEmpty) return false

            handler.insertItem(mod.inventory[0], false)
            mod.inventory[0] = ItemStack.EMPTY
            mod.container.sendUpdateToNearPlayers()
        }
        return true
    }

    fun offset(level: Level, inverse: Boolean): BlockPos {
        return if (inverse) {
            if (level == Level.LOW) mod.facing.opposite.toBlockPos() + EnumFacing.DOWN else mod.facing.opposite.toBlockPos()
        } else {
            if (level == Level.LOW) mod.facing.toBlockPos() + EnumFacing.DOWN else mod.facing.toBlockPos()
        }
    }
}

enum class Level {
    LOW, HIGH
}

enum class State {
    CONTRACTED,
    CONTRACTED_INVERSE,
    EXTENDED_LOW,
    EXTENDED_HIGH,
    EXTENDED_LOW_INVERSE,
    EXTENDED_HIGH_INVERSE,
    TRANSITION
}

enum class Transition(val animation: String) {
    MOVE_TO_DROP_LOW("animation0"),
    ROTATING("animation1"),
    ROTATING_INVERSE("animation2"),
    MOVE_FROM_DROP_LOW("animation3"),
    MOVE_TO_DROP_LOW_INVERSE("animation4"),
    MOVE_FROM_DROP_LOW_INVERSE("animation5"),
    MOVE_TO_DROP_HIGH("animation6"),
    MOVE_FROM_DROP_HIGH("animation7"),
    MOVE_TO_DROP_HIGH_INVERSE("animation8"),
    MOVE_FROM_DROP_HIGH_INVERSE("animation9"),
}
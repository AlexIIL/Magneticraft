package com.cout970.magneticraft.tileentity.modules

import com.cout970.magneticraft.api.core.ITileRef
import com.cout970.magneticraft.block.AutomaticMachines
import com.cout970.magneticraft.block.core.IOnActivated
import com.cout970.magneticraft.block.core.OnActivatedArgs
import com.cout970.magneticraft.item.itemblock.ItemBlockBase
import com.cout970.magneticraft.misc.inventory.insertItem
import com.cout970.magneticraft.misc.inventory.isNotEmpty
import com.cout970.magneticraft.misc.inventory.withSize
import com.cout970.magneticraft.misc.tileentity.TimeCache
import com.cout970.magneticraft.misc.tileentity.getTile
import com.cout970.magneticraft.misc.world.dropItem
import com.cout970.magneticraft.misc.world.isServer
import com.cout970.magneticraft.registry.ITEM_HANDLER
import com.cout970.magneticraft.registry.fromTile
import com.cout970.magneticraft.tileentity.TileConveyorBelt
import com.cout970.magneticraft.tileentity.core.IModule
import com.cout970.magneticraft.tileentity.core.IModuleContainer
import com.cout970.magneticraft.tileentity.modules.conveyor_belt.*
import com.cout970.magneticraft.util.getList
import com.cout970.magneticraft.util.list
import com.cout970.magneticraft.util.newNbt
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.IItemHandler

/**
 * Created by cout970 on 2017/06/17.
 */
class ModuleConveyorBelt(
    val facingGetter: () -> EnumFacing,
    override val name: String = "module_conveyor_belt"
) : IModule, IOnActivated {

    override lateinit var container: IModuleContainer

    val tileRefCallback = object : ITileRef {
        override fun getWorld(): World = container.world
        override fun getPos(): BlockPos = container.pos
    }

    val facing get() = facingGetter()

    val boxes = mutableListOf<BoxedItem>()
    var toUpdate = false
    private val newBoxes = mutableListOf<BoxedItem>()
    private var flip = true
    private var needsSync = true

    fun generateGlobalBitMap(): IBitMap {
        val map = getSurroundMap()
        val bitmap = FullBitMap(BitMap(), map.mapValues { BitMap() })

        boxes.forEach { bitmap.mark(it.getHitBox()) }

        map.forEach { pos, mod ->
            mod.boxes.forEach {
                bitmap.mark(pos.fromExternalToLocal(it.getHitBox()))
            }
        }
        return bitmap
    }

    val hasBack = TimeCache(tileRefCallback, 10) {
        world.getTile<TileConveyorBelt>(pos.add(facing.opposite.directionVec))?.facing == facing
    }

    val hasFront = TimeCache(tileRefCallback, 10) {
        world.getTile<TileConveyorBelt>(pos.add(facing.directionVec)) != null
    }

    val hasLeft = TimeCache(tileRefCallback, 10) {
        world.getTile<TileConveyorBelt>(pos.add(facing.rotateYCCW().directionVec))?.facing == facing.rotateY()
    }

    val hasRight = TimeCache(tileRefCallback, 10) {
        world.getTile<TileConveyorBelt>(pos.add(facing.rotateY().directionVec))?.facing == facing.rotateYCCW()
    }

    val isCorner
        get() = !hasBack() && hasFront() && (hasLeft() xor hasRight())

    override fun update() {
        advanceSimulation()
    }

    fun advanceSimulation() {
        // debug
//        if (world.isServer || world.totalWorldTime % 20 != 0L) return

        if (toUpdate) {
            boxes.clear()
            boxes.addAll(newBoxes)
            newBoxes.clear()
            toUpdate = false
        }

        val tile = world.getTileEntity(pos.offset(facing))
        val frontTile = tile as? TileConveyorBelt
        val frontInv = tile?.let { ITEM_HANDLER!!.fromTile(it, facing.opposite) }

        // if there is no block in front don't go all the way
        val limit = if (frontTile == null) 13f else 15f

        val fullBitMap = generateGlobalBitMap()
        val now = world.totalWorldTime

        boxes.forEach {

            if (it.lastTick != now) {
                if (it.position <= limit) {

                    val speed = (if (it.route.isShort) 2f else 1f)

                    it.locked = checkCollision(fullBitMap, it, speed)
                    if (!it.locked) {
                        var hitbox = it.getHitBox()
                        fullBitMap.unmark(hitbox)

                        it.move(speed)
                        hitbox = it.getHitBox()
                        fullBitMap.mark(hitbox)
                    }
                } else {
                    it.locked = true
                }
                it.lastTick = now
            }
        }
        // Move to next belt
        if (frontTile != null) {
            val removed = boxes.removeAll {
                if (it.position <= limit) return@removeAll false
                frontTile.conveyorModule.addItem(it.item, facing, it.route)
            }
            if (removed) {
                markUpdate()
            }
        } else if (frontInv != null) {
            val removed = boxes.removeAll {
                if (it.position <= limit)
                    return@removeAll false

                if (frontInv.insertItem(it.item, true).isEmpty) {
                    if (world.isServer) {
                        frontInv.insertItem(it.item, false)
                    }
                    return@removeAll true
                }
                return@removeAll false
            }
            if (removed) {
                markUpdate()
            }
        }

        if (needsSync && world.isServer) {
            container.sendUpdateToNearPlayers()
            needsSync = false
        }
    }

    // Note: this cannot be called on deserializeNBT or the game will crash of StackOverflow
    val getSurroundMap = TimeCache(tileRefCallback, 10) {
        val map = mutableMapOf<BitmapLocation, ModuleConveyorBelt>()

        world.getTile<TileConveyorBelt>(pos.offset(facing.opposite))?.let {
            map += BitmapLocation.IN_BACK to it.conveyorModule
        }
        world.getTile<TileConveyorBelt>(pos.offset(facing.rotateY()))?.let {
            map += BitmapLocation.IN_RIGHT to it.conveyorModule
        }
        world.getTile<TileConveyorBelt>(pos.offset(facing.rotateYCCW()))?.let {
            map += BitmapLocation.IN_LEFT to it.conveyorModule
        }
        world.getTile<TileConveyorBelt>(pos.offset(facing))?.let {
            when (it.facing) {
                facing -> map += BitmapLocation.OUT_FRONT to it.conveyorModule
                facing.opposite -> map += BitmapLocation.OUT_BACK to it.conveyorModule
                facing.rotateY() -> map += BitmapLocation.OUT_RIGHT to it.conveyorModule
                facing.rotateYCCW() -> map += BitmapLocation.OUT_LEFT to it.conveyorModule
                else -> Unit
            }
        }
        map
    }

    private fun checkCollision(thisBitMap: IBitMap, boxedItem: BoxedItem, speed: Float): Boolean {
        val thisTemp = thisBitMap.copy()
        val hitbox = boxedItem.getHitBox()

        thisTemp.unmark(hitbox)

        boxedItem.move(speed)
        val newHitbox = boxedItem.getHitBox()

        val tempResult = !thisTemp.test(newHitbox)
        boxedItem.move(-speed)
        return tempResult
    }

    fun addItem(stack: ItemStack, simulated: Boolean): Boolean {

        val route = if (flip) Route.LEFT_FORWARD else Route.RIGHT_FORWARD

        if (!simulated)
            flip = !flip

        val box = BoxedItem(stack.copy(), 2f, route, world.totalWorldTime)
        val bitmap = BitMap().apply { boxes.forEach { mark(it.getHitBox()) } }

        val test = bitmap.test(box.getHitBox())

        if (test && !simulated) {
            boxes += box
            markUpdate()
        }

        return test
    }

    fun addItem(stack: ItemStack, side: EnumFacing, oldRoute: Route): Boolean {
        val newRoute = getRoute(facing, side, oldRoute)
        val box = BoxedItem(stack.copy(), 0f, newRoute, world.totalWorldTime)
        val bitmap = BitMap().apply { boxes.forEach { mark(it.getHitBox()) } }

        val test = bitmap.test(box.getHitBox())

        if (test) {
            boxes += box
            markUpdate()
        }

        return test
    }

    fun getRoute(facing: EnumFacing, side: EnumFacing, oldRoute: Route): Route {

        if (facing == side) {
            return if (!oldRoute.leftSide) Route.RIGHT_FORWARD else Route.LEFT_FORWARD
        }
        if (facing.opposite == side) {
            return if (oldRoute.leftSide) Route.RIGHT_FORWARD else Route.LEFT_FORWARD
        }

        if (isCorner) {

            if (facing.rotateY() == side) {
                return if (oldRoute.leftSide) Route.LEFT_SHORT else Route.RIGHT_CORNER
            }
            if (facing.rotateYCCW() == side) {
                return if (oldRoute.leftSide) Route.LEFT_CORNER else Route.RIGHT_SHORT
            }
        } else {
            if (facing.rotateY() == side) {
                return if (oldRoute.leftSide) Route.LEFT_SHORT else Route.LEFT_LONG
            }
            if (facing.rotateYCCW() == side) {
                return if (oldRoute.leftSide) Route.RIGHT_LONG else Route.RIGHT_SHORT
            }
        }

        throw IllegalStateException("Illegal side: $side for facing: $facing")
    }


    fun removeItem(): ItemStack {
        if (boxes.isNotEmpty()) {
            val lastBox = boxes.maxBy { it.position } ?: return ItemStack.EMPTY
            boxes.remove(lastBox)
            markUpdate()
            return lastBox.item
        }
        return ItemStack.EMPTY
    }

    fun removeItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        if (boxes.isEmpty()) return ItemStack.EMPTY
        val lastBox = boxes[slot]

        val extracted = if (amount > lastBox.item.count) {
            lastBox.item
        } else {
            lastBox.item.withSize(amount)
        }

        if (!simulate) {
            if (amount >= lastBox.item.count) {
                boxes.remove(lastBox)
            } else {
                boxes[slot] = lastBox.copy(item = lastBox.item.withSize(lastBox.item.count - amount))
            }
            boxes.remove(lastBox)
            markUpdate()
        }
        return extracted
    }


    private fun markUpdate() {
        needsSync = true
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        newBoxes.clear()
        newBoxes += nbt.getList("items")
            .map { it as NBTTagCompound }
            .map { BoxedItem(it) }
        toUpdate = true
    }

    override fun serializeNBT(): NBTTagCompound {
        return newNbt {
            list("items") {
                boxes.map { it.serializeNBT() }.forEach { appendTag(it) }
            }
        }
    }

    override fun onBreak() {
        boxes.forEach {
            world.dropItem(it.item, pos)
        }
        boxes.clear()
    }

    override fun onActivated(args: OnActivatedArgs): Boolean {
        if (args.heldItem.isNotEmpty) {
            val item = args.heldItem.item
            if (item is ItemBlockBase && item.blockBase == AutomaticMachines.conveyorBelt) {
                return false
            }
            val success = addItem(args.heldItem, false)
            if (success && world.isServer) {
                args.playerIn.setHeldItem(args.hand, ItemStack.EMPTY)
            }
            return true
        } else {
            val item = removeItem()
            if (item.isNotEmpty) {
                if (world.isServer) {
                    args.playerIn.inventory.addItemStackToInventory(item)
                }
                return true
            }
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(cap: Capability<T>, facing: EnumFacing?): T? {

        if (cap == ITEM_HANDLER && facing == EnumFacing.UP) {
            return object : IItemHandler {

                override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
                    if (slot != 0) return stack
                    return if (addItem(stack, simulate)) ItemStack.EMPTY else stack
                }

                override fun getStackInSlot(slot: Int): ItemStack =
                    if (slot == 0) ItemStack.EMPTY else boxes[slot - 1].item

                override fun getSlotLimit(slot: Int): Int = 64

                override fun getSlots(): Int = boxes.size + 1

                override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack =
                    if (slot == 0) ItemStack.EMPTY else removeItem(slot - 1, amount, simulate)
            } as T
        }

        return null
    }
}
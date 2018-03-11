package com.cout970.magneticraft.tileentity.modules

import com.cout970.magneticraft.config.Config
import com.cout970.magneticraft.gui.common.core.DATA_ID_ITEM_AMOUNT
import com.cout970.magneticraft.misc.inventory.withSize
import com.cout970.magneticraft.misc.network.IntSyncVariable
import com.cout970.magneticraft.misc.network.SyncVariable
import com.cout970.magneticraft.misc.world.dropItem
import com.cout970.magneticraft.registry.ITEM_HANDLER
import com.cout970.magneticraft.tileentity.core.IModule
import com.cout970.magneticraft.tileentity.core.IModuleContainer
import com.cout970.magneticraft.util.add
import com.cout970.magneticraft.util.newNbt
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

class ModuleStackInventory(
        val maxItems: Int,
        override val name: String = "module_stack_inventory"
) : IModule {

    override lateinit var container: IModuleContainer

    var stackType: ItemStack = ItemStack.EMPTY
    var amount: Int = 0

    override fun onBreak() {

        if (stackType.isEmpty) return
        var items = min(amount, Config.containerMaxItemDrops)
        while (items > 0) {
            val stackSize = min(items, stackType.maxStackSize)
            world.dropItem(stackType.withSize(stackSize), pos)
            items -= stackType.maxStackSize
        }
        stackType = ItemStack.EMPTY
    }

    override fun <T> getCapability(cap: Capability<T>, facing: EnumFacing?): T? {

        if (cap == ITEM_HANDLER) {
            return object : IItemHandler {

                override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
                    if (slot != 0 || stack.isEmpty) return stack

                    if (stackType.isEmpty) {
                        // This doesn't handle the case where maxItems is less than 64 items
                        if (!simulate) {
                            stackType = stack.withSize(1)
                            amount = stack.count
                            container.sendUpdateToNearPlayers()
                        }
                        return ItemStack.EMPTY
                    }

                    if (ItemHandlerHelper.canItemStacksStack(stackType, stack)) {
                        val space = maxItems - amount
                        val toAdd = min(space, stack.count)
                        val itemsLeft = stack.count - toAdd

                        if (!simulate) {
                            amount += toAdd
                        }
                        return if (itemsLeft == 0) ItemStack.EMPTY else stack.withSize(itemsLeft)
                    }
                    return stack
                }

                override fun getStackInSlot(slot: Int): ItemStack = when (slot) {
                    0 -> ItemStack.EMPTY
                    else -> stackType.withSize(min(amount, 64))
                }

                override fun getSlotLimit(slot: Int): Int = 64

                override fun getSlots(): Int = 2

                override fun extractItem(slot: Int, count: Int, simulate: Boolean): ItemStack {
                    if (slot != 1 || stackType.isEmpty || amount == 0) return ItemStack.EMPTY

                    val toExtract = min(min(count, amount), 64)

                    if (toExtract <= 0) return ItemStack.EMPTY

                    val result = stackType.withSize(toExtract)
                    if (!simulate) {
                        amount -= toExtract
                        if (amount <= 0) {
                            stackType = ItemStack.EMPTY
                            container.sendUpdateToNearPlayers()
                        }
                    }
                    return result
                }
            } as T
        }
        return null
    }

    override fun getGuiSyncVariables(): List<SyncVariable> {
        return listOf(IntSyncVariable(
                id = DATA_ID_ITEM_AMOUNT,
                getter = { amount },
                setter = { amount = it }
        ))
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        if (nbt.hasKey("amount")) {
            amount = nbt.getInteger("amount")
            stackType = ItemStack(nbt.getCompoundTag("stackType"))
        }
    }

    override fun serializeNBT(): NBTTagCompound = newNbt {
        add("amount", amount)
        add("stackType", stackType.serializeNBT())
    }
}
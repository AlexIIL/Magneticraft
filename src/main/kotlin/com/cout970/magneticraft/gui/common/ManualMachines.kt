package com.cout970.magneticraft.gui.common

import com.cout970.magneticraft.IVector2
import com.cout970.magneticraft.gui.common.core.ContainerBase
import com.cout970.magneticraft.misc.inventory.InventoryRegion
import com.cout970.magneticraft.tileentity.TileBox
import com.cout970.magneticraft.tileentity.TileInserter
import com.cout970.magneticraft.util.vector.vec2Of
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.items.SlotItemHandler

/**
 * Created by cout970 on 2017/08/10.
 */

class ContainerBox(val tile: TileBox, player: EntityPlayer, world: World, blockPos: BlockPos)
    : ContainerBase(player, world, blockPos) {

    init {
        tile.invModule.inventory.let { inv ->
            for (index in 0 until inv.slots) {
                val pos = getPosFromIndex(index)
                addSlotToContainer(SlotItemHandler(inv, index, pos.xi, pos.yi))
            }
            inventoryRegions += InventoryRegion(0..26)
        }
        bindPlayerInventory(player.inventory)
    }

    private fun getPosFromIndex(index: Int): IVector2 {
        return vec2Of(index % 9, index / 9) * 18 + vec2Of(8, 8)
    }
}

class ContainerInserter(val tile: TileInserter, player: EntityPlayer, world: World, blockPos: BlockPos)
    : ContainerBase(player, world, blockPos) {

    init {
        tile.invModule.inventory.let { inv ->
            addSlotToContainer(SlotItemHandler(inv, 0, 83, 50))
            inventoryRegions += InventoryRegion(0..0)
        }
        bindPlayerInventory(player.inventory)
    }
}
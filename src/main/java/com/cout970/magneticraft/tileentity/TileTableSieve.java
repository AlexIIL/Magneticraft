package com.cout970.magneticraft.tileentity;

import com.cout970.magneticraft.ManagerApi;
import com.cout970.magneticraft.api.access.RecipeRegister;
import com.cout970.magneticraft.api.access.RecipeTableSieve;
import com.cout970.magneticraft.util.MiscUtils;
import net.darkaqua.blacksmith.Game;
import net.darkaqua.blacksmith.inventory.InventoryUtils;
import net.darkaqua.blacksmith.inventory.SimpleInventoryHandler;
import net.darkaqua.blacksmith.raytrace.Cube;
import net.darkaqua.blacksmith.scanner.ObjectScanner;
import net.darkaqua.blacksmith.storage.IDataCompound;
import net.darkaqua.blacksmith.util.Direction;
import net.darkaqua.blacksmith.util.WorldRef;
import net.darkaqua.blacksmith.vectors.Vect3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Created by cout970 on 28/12/2015.
 */
public class TileTableSieve extends TileBase {

    private SimpleInventoryHandler inv = new SimpleInventoryHandler(1, 1);
    private int progress;
    private static final int MAX_PROGRESS = 50;
    private boolean blocked;

    @Override
    public void update() {

        if (blocked) {
            if (getWorld().getWorldTime() % 20 == 0) {
                blocked = !ejectItem(getRecipeOutput(inv.getStackInSlot(0)));
            }
            if (!blocked) {
                inv.setStackInSlot(0, null);
            }
        } else {
            if (inv.getStackInSlot(0) == null) {
                if (getWorld().getWorldTime() % 20 == 0) {
                    suckUpItems();
                }
            } else if (hasValidRecipe(inv.getStackInSlot(0))) {
                progress++;
                if (progress >= MAX_PROGRESS) {
                    progress = 0;
                    blocked = true;
                }
            }
        }
    }

    private boolean ejectItem(ItemStack stack) {
        WorldRef ref = new WorldRef(getWorld(), getWorldRef().getPosition().down());
        TileEntity tile = ref.getTileEntity();
        IItemHandler inventory = ObjectScanner.findInTileEntity(tile, ManagerApi.ITEM_HANDLER, Direction.UP);
        if (inventory != null && InventoryUtils.insertAllInInventory(inventory, stack)) {
            return true;
        }
        if (ref.getBlockState().getBlock().isPassable(getWorld(), getPos())) {
            if (Game.isServer()) {
                MiscUtils.dropItem(getWorldRef(), new Vect3d(0.5, -0.0625f * 6, 0.5), stack, false);
            }
            return true;
        }
        return false;
    }

    private boolean hasValidRecipe(ItemStack stack) {
        return RecipeRegister.getTableSieveRecipe(stack) != null;
    }

    private ItemStack getRecipeOutput(ItemStack stack) {
        RecipeTableSieve r = RecipeRegister.getTableSieveRecipe(stack);
        return r == null ? null : r.getOutput();
    }

    private void suckUpItems() {
        List<Entity> list = getWorld().getEntitiesInAABBexcluding(null, Cube.fullBlock().translate(getWorldRef().getPosition().toVect3d().add(0, 1, 0)).toAxisAlignedBB(), e -> true);
        for (Entity e : list) {
            if (e instanceof EntityItem) {
                ItemStack stack = ((EntityItem) e).getEntityItem();
                if (hasValidRecipe(stack) && InventoryUtils.canInsertSomething(inv, 0, stack)) {
                    ItemStack output = inv.insertItem(0, stack, false);
                    if (output == null || output.stackSize <= 0) {
                        e.setDead();
                    } else {
                        ((EntityItem) e).setEntityItemStack(output);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void loadData(IDataCompound data) {
        inv.load(data, "inv");
    }

    @Override
    public void saveData(IDataCompound data) {
        inv.save(data, "inv");
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (Game.isServer()) {
            MiscUtils.dropItem(getWorldRef(), new Vect3d(0.5, 0.5, 0.5), inv.getStackInSlot(0), true);
            inv.setStackInSlot(0, null);
        }
    }
}
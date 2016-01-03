package com.cout970.magneticraft.item;

import com.cout970.magneticraft.Magneticraft;
import com.cout970.magneticraft.api.tool.IHammer;
import com.cout970.magneticraft.client.model.item.SwordRenderModel;
import net.darkaqua.blacksmith.api.inventory.IItemStack;
import net.darkaqua.blacksmith.api.render.model.IItemModelProvider;
import net.darkaqua.blacksmith.api.render.model.IRenderModel;
import net.darkaqua.blacksmith.api.render.model.defaults.ItemFlatModelProvider;
import net.darkaqua.blacksmith.api.util.ResourceReference;
import net.darkaqua.blacksmith.api.util.WorldRef;

/**
 * Created by cout970 on 21/12/2015.
 */
public class ItemIronHammer extends ItemBase implements IHammer{

    public ItemIronHammer(){
        maxDamage = 250;
        maxStackSize = 1;
    }

    @Override
    public String getItemName() {
        return "iron_hammer";
    }

    public IItemModelProvider getModelProvider(){
        return new ItemFlatModelProvider(new ResourceReference(Magneticraft.ID, "items/" + getItemName().toLowerCase())){
            @Override
            public IRenderModel getModelForVariant(IItemStack stack) {
                if(model == null){
                    model = new SwordRenderModel(identifier);
                }
                return model;
            }
        };
    }

    @Override
    public IItemStack tick(IItemStack hammer, WorldRef ref) {
        if (hammer.getDamage() < hammer.getMaxDamage()) {
            hammer.setDamage(hammer.getDamage() + 1);
            return hammer;
        } else {
            return null;
        }
    }

    @Override
    public boolean canHammer(IItemStack hammer, WorldRef ref) {
        return true;
    }

    @Override
    public int getMaxHits(IItemStack hammer, WorldRef ref) {
        return 8;
    }
}

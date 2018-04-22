package com.cout970.magneticraft.registry

import com.cout970.magneticraft.api.internal.registries.generators.thermopile.ThermopileRecipeManager
import com.cout970.magneticraft.api.internal.registries.generators.thermopile.ThermopileRecipeNoDecay
import com.cout970.magneticraft.api.internal.registries.generators.thermopile.ThermopileRecipeWithDecay
import com.cout970.magneticraft.api.internal.registries.machines.crushingtable.CrushingTableRecipeManager
import com.cout970.magneticraft.api.internal.registries.machines.grinder.GrinderRecipeManager
import com.cout970.magneticraft.api.internal.registries.machines.hydraulicpress.HydraulicPressRecipeManager
import com.cout970.magneticraft.api.internal.registries.machines.sieve.SieveRecipeManager
import com.cout970.magneticraft.api.internal.registries.machines.sluicebox.SluiceBoxRecipeManager
import com.cout970.magneticraft.api.registries.machines.hydraulicpress.HydraulicPressMode
import com.cout970.magneticraft.api.registries.machines.hydraulicpress.HydraulicPressMode.*
import com.cout970.magneticraft.block.Decoration
import com.cout970.magneticraft.block.Ores
import com.cout970.magneticraft.integration.ItemHolder
import com.cout970.magneticraft.integration.crafttweaker.ifNonEmpty
import com.cout970.magneticraft.item.CraftingItems
import com.cout970.magneticraft.item.EnumMetal
import com.cout970.magneticraft.item.EnumMetal.*
import com.cout970.magneticraft.item.MetallicItems
import com.cout970.magneticraft.misc.inventory.stack
import com.cout970.magneticraft.misc.inventory.withSize
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.init.Blocks.COBBLESTONE
import net.minecraft.init.Items
import net.minecraft.init.Items.GOLD_INGOT
import net.minecraft.init.Items.IRON_INGOT
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fml.common.registry.GameRegistry


/**
 * Created by cout970 on 11/06/2016.
 * Modified by Yurgen
 *
 * Called by CommonProxy to register all the recipes in the mod
 */
fun registerRecipes() {

    //@formatter:off

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //                                                  GRINDER RECIPES
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    EnumMetal.values().forEach { metal ->
        metal.getOres().firstOrNull()?.let {
            addGrinderRecipe(it, metal.getRockyChunk(), Blocks.GRAVEL.stack(), 0.15f, 50f)
        }
        if(metal.subComponents.isEmpty()){
            addGrinderRecipe(metal.getIngot(), metal.getDust(), ItemStack.EMPTY, 0.0f, 50f)
        }
    }
    ItemHolder.tinOre?.ifNonEmpty {
        addGrinderRecipe(it, TIN.getRockyChunk(), Blocks.GRAVEL.stack(), 0.15f, 50f)
    }
    ItemHolder.osmiumOre?.ifNonEmpty {
        addGrinderRecipe(it, OSMIUM.getRockyChunk(), Blocks.GRAVEL.stack(), 0.15f, 50f)
    }
    ItemHolder.dimensionalShard?.ifNonEmpty { shard ->
        ItemHolder.dimensionalShardOre0?.ifNonEmpty {ore ->
            addGrinderRecipe(ore, shard.withSize(4), shard.withSize(1), 0.5f, 50f)
        }
        ItemHolder.dimensionalShardOre1?.ifNonEmpty { ore ->
            addGrinderRecipe(ore, shard.withSize(4), shard.withSize(1), 0.5f, 50f)
        }
        ItemHolder.dimensionalShardOre2?.ifNonEmpty { ore ->
            addGrinderRecipe(ore, shard.withSize(4), shard.withSize(1), 0.5f, 50f)
        }
    }

    addGrinderRecipe(Blocks.REDSTONE_ORE.stack(), Items.REDSTONE.stack(4), Blocks.GRAVEL.stack(), 0.15f, 50f)
    addGrinderRecipe(Blocks.LAPIS_ORE.stack(), Items.DYE.stack(6, 4), Blocks.GRAVEL.stack(), 0.15f, 50f)
    addGrinderRecipe(Blocks.QUARTZ_ORE.stack(), Items.QUARTZ.stack(3), Items.QUARTZ.stack(1), 0.5f, 60f)
    addGrinderRecipe(Blocks.EMERALD_ORE.stack(), Items.EMERALD.stack(2), Blocks.GRAVEL.stack(), 0.15f, 50f)
    addGrinderRecipe(Blocks.DIAMOND_ORE.stack(), Items.DIAMOND.stack(1), Items.DIAMOND.stack(1), 0.75f, 50f)

    addGrinderRecipe(Blocks.GLOWSTONE.stack(), Items.GLOWSTONE_DUST.stack(4), ItemStack.EMPTY, 0.0f, 40f)
    addGrinderRecipe(Blocks.SANDSTONE.stack(), Blocks.SAND.stack(4), ItemStack.EMPTY, 0.0f, 40f)
    addGrinderRecipe(Blocks.RED_SANDSTONE.stack(), Blocks.SAND.stack(4, 1), ItemStack.EMPTY, 0.0f, 40f)
    addGrinderRecipe(Items.BLAZE_ROD.stack(), Items.BLAZE_POWDER.stack(4), CraftingItems.Type.SULFUR.stack(1), 0.5f, 50f)
    addGrinderRecipe(Blocks.WOOL.stack(), Items.STRING.stack(4), ItemStack.EMPTY, 0.0f, 40f)
    addGrinderRecipe(Items.BONE.stack(), Items.DYE.stack(5, 15), Items.DYE.stack(3, 15), 0.5f, 40f)
    addGrinderRecipe(Items.REEDS.stack(), Items.SUGAR.stack(1), Items.SUGAR.stack(2), 0.5f, 40f)
    addGrinderRecipe(Blocks.COBBLESTONE.stack(), Blocks.GRAVEL.stack(1), Blocks.SAND.stack(1), 0.5f, 60f)
    addGrinderRecipe(Blocks.QUARTZ_BLOCK.stack(), Items.QUARTZ.stack(4), ItemStack.EMPTY, 0.0f, 50f)

    addGrinderRecipe(Ores.OreType.PYRITE.stack(1), CraftingItems.Type.SULFUR.stack(4), Blocks.GRAVEL.stack(), 0.01f, 40f)

    ItemHolder.sawdust?.ifNonEmpty { sawdust ->
        addGrinderRecipe(Blocks.LOG.stack(), sawdust.withSize(8), sawdust.withSize(4), 0.5f, 100f)
        addGrinderRecipe(Blocks.PLANKS.stack(), sawdust.withSize(2), sawdust.withSize(1), 0.5f, 80f)
    }

    ItemHolder.pulverizedCoal?.ifNonEmpty { pulverizedCoal ->
        addGrinderRecipe(Blocks.COAL_ORE.stack(), pulverizedCoal.withSize(1), pulverizedCoal.withSize(1), 0.25f, 50f)
        addGrinderRecipe(Blocks.COAL_BLOCK.stack(), pulverizedCoal.withSize(9), pulverizedCoal.withSize(1), 0.15f, 120f)
        addGrinderRecipe(Items.COAL.stack(), pulverizedCoal.withSize(1), pulverizedCoal.withSize(1), 0.05f, 40f)
    }

    ItemHolder.pulverizedObsidian?.ifNonEmpty { pulverizedObsidian ->
        addGrinderRecipe(Blocks.OBSIDIAN.stack(), pulverizedObsidian.withSize(4), pulverizedObsidian.withSize(1), 0.25f, 80f)
    }

    ItemHolder.pulverizedCharcoal?.ifNonEmpty { pulverizedCharcoal ->
        addGrinderRecipe(Items.COAL.stack(meta = 1), pulverizedCharcoal.withSize(1), pulverizedCharcoal.withSize(1), 0.15f, 40f)
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //                                                  SIEVE RECIPES
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    EnumMetal.values().filter { it.isOre }.forEach {
        val subComponents =
                if(it.isComposite) {
                    it.subComponents.map { it.invoke() }.map { it.getChunk() to 1f }
                } else {
                    EnumMetal.subProducts[it]?.map { it.getDust() to 0.15f } ?: emptyList()
                }

        when(subComponents.size){
            0 -> addSieveRecipe(it.getRockyChunk(), it.getChunk(), 1f, 50f)
            1 -> addSieveRecipe(it.getRockyChunk(), it.getChunk(), 1f, subComponents[0].first, subComponents[0].second, 50f)
            2 -> addSieveRecipe(it.getRockyChunk(), it.getChunk(), 1f, subComponents[0].first, subComponents[0].second,
                    subComponents[1].first, subComponents[1].second, 50f)
        }
    }

    addSieveRecipe(Blocks.GRAVEL.stack(), Items.FLINT.stack(), 1f, Items.FLINT.stack(), 0.15f, Items.FLINT.stack(), 0.05f, 50f)
    addSieveRecipe(Blocks.SAND.stack(), Items.GOLD_NUGGET.stack(), 0.04f, Items.GOLD_NUGGET.stack(), 0.02f, Items.QUARTZ.stack(), 0.01f, 80f)
    addSieveRecipe(Blocks.SOUL_SAND.stack(), Items.QUARTZ.stack(), 0.15f, Items.QUARTZ.stack(), 0.1f, Items.QUARTZ.stack(), 0.05f, 80f)

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //                                              CRUSHING TABLE RECIPES
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // skulls
    addCrushingTableRecipe(Items.SKULL.stack(meta = 4), Items.GUNPOWDER.stack(8))    // creeper
    addCrushingTableRecipe(Items.SKULL.stack(meta = 0), Items.DYE.stack(8, 15)) // skeleton
    addCrushingTableRecipe(Items.SKULL.stack(meta = 2), Items.ROTTEN_FLESH.stack(4)) // zombie

    // ores
    EnumMetal.values().forEach { metal ->
        metal.getOres().firstOrNull()?.let {
            addCrushingTableRecipe(it, metal.getRockyChunk())
        }
    }
    ItemHolder.tinOre?.ifNonEmpty {
        addCrushingTableRecipe(it, TIN.getRockyChunk())
    }
    ItemHolder.osmiumOre?.ifNonEmpty {
        addCrushingTableRecipe(it, OSMIUM.getRockyChunk())
    }

    addCrushingTableRecipe(Ores.OreType.PYRITE.stack(), CraftingItems.Type.SULFUR.stack(2))
    // limestone
    addCrushingTableRecipe(Decoration.limestone.stack(), Decoration.limestone.stack(1, 2))
    addCrushingTableRecipe(Decoration.burnLimestone.stack(), Decoration.burnLimestone.stack(1, 2))
    // light plates
    EnumMetal.values().filter { it.useful }.forEach {
        addCrushingTableRecipe(it.getIngot(), it.getLightPlate())
    }
    // rods
    addCrushingTableRecipe(Items.BLAZE_ROD.stack(), Items.BLAZE_POWDER.stack(5))
    addCrushingTableRecipe(Items.BONE.stack(), Items.DYE.stack(4, 15))
    // blocks
    addCrushingTableRecipe(Blocks.STONE.stack(), Blocks.COBBLESTONE.stack())
    addCrushingTableRecipe(Blocks.STONE.stack(1, 6), Blocks.STONE.stack(1, 5))
    addCrushingTableRecipe(Blocks.STONE.stack(1, 4), Blocks.STONE.stack(1, 3))
    addCrushingTableRecipe(Blocks.STONE.stack(1, 2), Blocks.STONE.stack(1, 1))
    addCrushingTableRecipe(Blocks.STONEBRICK.stack(), Blocks.STONEBRICK.stack(1, 2))
    addCrushingTableRecipe(Blocks.STONEBRICK.stack(1, 1), Blocks.MOSSY_COBBLESTONE.stack())
    addCrushingTableRecipe(Blocks.PRISMARINE.stack(1, 1), Blocks.PRISMARINE.stack())
    addCrushingTableRecipe(Blocks.END_BRICKS.stack(1), Blocks.END_STONE.stack(1))

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //                                                  SMELTING RECIPES
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    addSmeltingRecipe(Decoration.burnLimestone.stack(1, 0), Decoration.limestone.stack(1, 0))
    addSmeltingRecipe(Decoration.burnLimestone.stack(1, 2), Decoration.limestone.stack(1, 2))

    //ores
    addSmeltingRecipe(MetallicItems.ingots.stack(1, 2), Ores.ores.stack(1, 0))
    addSmeltingRecipe(MetallicItems.ingots.stack(1, 3), Ores.ores.stack(1, 1))
    addSmeltingRecipe(MetallicItems.ingots.stack(1, 4), Ores.ores.stack(1, 2))
    addSmeltingRecipe(MetallicItems.ingots.stack(1, 5), Ores.ores.stack(1, 3))

    EnumMetal.values().forEach {
        if(it.isComposite) {
            addSmeltingRecipe(it.subComponents[0]().getIngot().withSize(2), it.getRockyChunk())
        } else {
            addSmeltingRecipe(it.getIngot(), it.getDust())
            if(it.isOre){
                addSmeltingRecipe(it.getIngot(), it.getRockyChunk())
                addSmeltingRecipe(it.getIngot().withSize(2), it.getChunk())
            }
        }
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //                                                SLUICE BOX RECIPES
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    EnumMetal.values()
            .filter { it.isOre }
            .forEach {

        val subComponents =
                if(it.isComposite) {
                    it.subComponents.map { it.invoke() }.map { it.getChunk() to 1f }
                } else {
                    EnumMetal.subProducts[it]?.map { it.getDust() to 0.15f } ?: emptyList()
                }

        addSluiceBoxRecipe(it.getRockyChunk(), it.getChunk(), subComponents + listOf(COBBLESTONE.stack() to 0.15f))
    }

    addSluiceBoxRecipe(Blocks.GRAVEL.stack(), Items.FLINT.stack(), listOf(Items.FLINT.stack() to 0.15f))

    addSluiceBoxRecipe(Blocks.SAND.stack(), ItemStack.EMPTY,
            listOf(
                    Items.GOLD_NUGGET.stack() to 0.01f,
                    Items.GOLD_NUGGET.stack() to 0.005f,
                    Items.GOLD_NUGGET.stack() to 0.0025f,
                    Items.GOLD_NUGGET.stack() to 0.00125f,
                    Items.GOLD_NUGGET.stack() to 0.000625f,
                    Items.GOLD_NUGGET.stack() to 0.0003125f,
                    Items.GOLD_NUGGET.stack() to 0.00015625f,
                    Items.GOLD_NUGGET.stack() to 0.000078125f,
                    Items.GOLD_NUGGET.stack() to 0.0000390625f
            ))

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //                                                  THERMOPILE RECIPES
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    addThermopileRecipe(Blocks.AIR, -1)
    addThermopileRecipe(Blocks.SNOW, -100)
    addThermopileRecipe(Blocks.ICE, -100)
    addThermopileRecipe(Blocks.PACKED_ICE, -80)
    addThermopileRecipe(Blocks.SNOW_LAYER, -50)
    addThermopileRecipe(Blocks.TORCH, 5)
    addThermopileRecipe(Blocks.LIT_PUMPKIN, 3)
    addThermopileRecipe(Blocks.FIRE, 25)
    addThermopileRecipe(Blocks.MAGMA, 25)

    addThermopileRecipe(Blocks.WATER, -25)
    addThermopileRecipeWithDecay(Blocks.LAVA, 100, Blocks.OBSIDIAN.defaultState,-201, 0.00333f)

    val fluids = FluidRegistry.getRegisteredFluids()
            .values
            .filter { it != FluidRegistry.WATER }
            .filter { it != FluidRegistry.LAVA }
            .filter { it.canBePlacedInWorld() }

    fluids.forEach { fluid ->
        when {
            fluid.temperature < 310 -> {
                addThermopileRecipe(fluid.block, -25 + (-100 * (1 - (fluid.temperature / 310f))).toInt())
            }
            fluid.temperature > 310 -> addThermopileRecipe(fluid.block, (100 * (fluid.temperature / 1300f)).toInt())
        }
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //                                                  HYDRAULIC PRESS RECIPES
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //@formatter:on

    // Heavy recipes
    addHydraulicPressRecipe(IRON_INGOT.stack(2), IRON.getHeavyPlate(), HEAVY, 120f)
    addHydraulicPressRecipe(GOLD_INGOT.stack(2), GOLD.getHeavyPlate(), HEAVY, 50f)
    addHydraulicPressRecipe(EnumMetal.COPPER.getIngot().withSize(2), EnumMetal.COPPER.getHeavyPlate(), HEAVY, 100f)
    addHydraulicPressRecipe(EnumMetal.LEAD.getIngot().withSize(2), EnumMetal.LEAD.getHeavyPlate(), HEAVY, 50f)
    addHydraulicPressRecipe(EnumMetal.TUNGSTEN.getIngot().withSize(2), EnumMetal.TUNGSTEN.getHeavyPlate(), HEAVY, 250f)
    addHydraulicPressRecipe(EnumMetal.STEEL.getIngot().withSize(2), EnumMetal.STEEL.getHeavyPlate(), HEAVY, 140f)

    // Medium recipes
    addHydraulicPressRecipe(IRON_INGOT.stack(1), IRON.getLightPlate(), MEDIUM, 120f)
    addHydraulicPressRecipe(GOLD_INGOT.stack(1), GOLD.getLightPlate(), MEDIUM, 50f)
    addHydraulicPressRecipe(EnumMetal.COPPER.getIngot().withSize(1), EnumMetal.COPPER.getLightPlate(), MEDIUM, 100f)
    addHydraulicPressRecipe(EnumMetal.LEAD.getIngot().withSize(1), EnumMetal.LEAD.getLightPlate(), MEDIUM, 50f)
    addHydraulicPressRecipe(EnumMetal.TUNGSTEN.getIngot().withSize(1), EnumMetal.TUNGSTEN.getLightPlate(), MEDIUM, 250f)
    addHydraulicPressRecipe(EnumMetal.COBALT.getIngot().withSize(1), EnumMetal.COBALT.getLightPlate(), MEDIUM, 120f)
    addHydraulicPressRecipe(EnumMetal.STEEL.getIngot().withSize(1), EnumMetal.STEEL.getLightPlate(), MEDIUM, 140f)

    // Light recipes
    listOf(
            IRON_INGOT.stack() to ItemHolder.ironPlate,
            GOLD_INGOT.stack() to ItemHolder.goldPlate,
            EnumMetal.COPPER.getIngot() to ItemHolder.copperPlate,
            EnumMetal.TIN.getIngot() to ItemHolder.tinPlate,
            EnumMetal.SILVER.getIngot() to ItemHolder.silverPlate,
            EnumMetal.LEAD.getIngot() to ItemHolder.leadPlate,
            EnumMetal.ALUMINIUM.getIngot() to ItemHolder.aluminiumPlate,
            EnumMetal.NICKEL.getIngot() to ItemHolder.nickelPlate,
            ItemHolder.platinumIngot to ItemHolder.platinumPlate,
            ItemHolder.iridiumIngot to ItemHolder.iridiumPlate,
            EnumMetal.MITHRIL.getIngot() to ItemHolder.mithilPlate,
            EnumMetal.STEEL.getIngot() to ItemHolder.steelPlate,
            ItemHolder.electrumIngot to ItemHolder.electrumPlate,
            ItemHolder.invarIngot to ItemHolder.invarPlate,
            ItemHolder.constantanIngot to ItemHolder.constantanPlate,
            ItemHolder.signalumIngot to ItemHolder.signalumPlate,
            ItemHolder.lumiumIngot to ItemHolder.lumiumPlate,
            ItemHolder.enderiumIngot to ItemHolder.enderiumPlate
    ).forEach { (a, b) ->
        a?.ifNonEmpty {
            b?.ifNonEmpty {
                addHydraulicPressRecipe(a, b, LIGHT, 80f)
            }
        }
    }

    // utility
    addHydraulicPressRecipe(Blocks.STONE.stack(), Blocks.COBBLESTONE.stack(), LIGHT, 55f)
    addHydraulicPressRecipe(Blocks.STONE.stack(meta = 6), Blocks.STONE.stack(meta = 5), LIGHT, 55f)
    addHydraulicPressRecipe(Blocks.STONE.stack(meta = 4), Blocks.STONE.stack(meta = 3), LIGHT, 55f)
    addHydraulicPressRecipe(Blocks.STONE.stack(meta = 2), Blocks.STONE.stack(meta = 1), LIGHT, 55f)
    addHydraulicPressRecipe(Blocks.STONEBRICK.stack(meta = 1), Blocks.MOSSY_COBBLESTONE.stack(), LIGHT, 55f)
    addHydraulicPressRecipe(Blocks.STONEBRICK.stack(), Blocks.STONEBRICK.stack(meta = 2), LIGHT, 55f)
    addHydraulicPressRecipe(Blocks.END_BRICKS.stack(), Blocks.END_STONE.stack(), LIGHT, 100f)
    addHydraulicPressRecipe(Blocks.RED_SANDSTONE.stack(meta = 2), Blocks.RED_SANDSTONE.stack(), LIGHT, 40f)
    addHydraulicPressRecipe(Blocks.SANDSTONE.stack(meta = 2), Blocks.SANDSTONE.stack(), LIGHT, 40f)
    addHydraulicPressRecipe(Blocks.PRISMARINE.stack(meta = 1), Blocks.PRISMARINE.stack(), LIGHT, 50f)
    addHydraulicPressRecipe(Blocks.ICE.stack(), Blocks.PACKED_ICE.stack(), LIGHT, 200f)
}


private fun addSmeltingRecipe(result: ItemStack, input: ItemStack) {
    if (input.isEmpty)
        throw IllegalStateException("Trying to register furnace recipe with empty input stack: $input")
    if (result.isEmpty)
        throw IllegalStateException("Trying to register furnace recipe with empty result empty stack: $result")

    GameRegistry.addSmelting(input, result, 0.1f) // i don't care about xp
}

private fun addCrushingTableRecipe(input: ItemStack, output: ItemStack) {
    CrushingTableRecipeManager.registerRecipe(CrushingTableRecipeManager.createRecipe(input, output, true))
}

private fun addSluiceBoxRecipe(input: ItemStack, output: ItemStack,
                               otherOutput: List<Pair<ItemStack, Float>> = emptyList()) {
    SluiceBoxRecipeManager.registerRecipe(SluiceBoxRecipeManager.createRecipe(input, output, otherOutput, true))
}

private fun addThermopileRecipe(input: Block, heat: Int) {
    ThermopileRecipeManager.registerRecipe(ThermopileRecipeNoDecay(input, heat))
}

private fun addThermopileRecipeWithDecay(input: Block, heat: Int, replacement: IBlockState, limit: Int, prob: Float) {
    ThermopileRecipeManager.registerRecipe(ThermopileRecipeWithDecay(input, heat, replacement, limit, prob))
}


private fun addSieveRecipe(input: ItemStack, output0: ItemStack, prob0: Float, output1: ItemStack, prob1: Float,
                           output2: ItemStack,
                           prob2: Float, duration: Float) {
    SieveRecipeManager.registerRecipe(
            SieveRecipeManager.createRecipe(input, output0, prob0, output1, prob1, output2, prob2, duration, true))
}

private fun addSieveRecipe(input: ItemStack, output0: ItemStack, prob0: Float, output1: ItemStack, prob1: Float,
                           duration: Float) {
    SieveRecipeManager.registerRecipe(
            SieveRecipeManager.createRecipe(input, output0, prob0, output1, prob1, output1, 0f, duration, true))
}

private fun addSieveRecipe(input: ItemStack, output0: ItemStack, prob0: Float, duration: Float) {
    SieveRecipeManager.registerRecipe(
            SieveRecipeManager.createRecipe(input, output0, prob0, output0, 0f, output0, 0f, duration, true))
}

private fun addGrinderRecipe(input: ItemStack, output0: ItemStack, output1: ItemStack, prob: Float, ticks: Float) {
    GrinderRecipeManager.registerRecipe(GrinderRecipeManager.createRecipe(input, output0, output1, prob, ticks, true))
}

private fun addHydraulicPressRecipe(input: ItemStack, output: ItemStack, mode: HydraulicPressMode, ticks: Float) {
    HydraulicPressRecipeManager.registerRecipe(HydraulicPressRecipeManager.createRecipe(input, output, ticks, mode, true))
}


/* OLD RECIPES

//
//    //ICEBOX RECIPES
//    addIceboxRecipeWater(ItemStack(Items.SNOWBALL), 125, false)
//    addIceboxRecipeWater(ItemStack(Blocks.SNOW), 500, false)
//    addIceboxRecipeWater(ItemStack(Blocks.ICE), 900, true)
//    addIceboxRecipeWater(ItemStack(Blocks.PACKED_ICE), 1000, false)

//
//    //KILN RECIPES
//    addKilnRecipe(ItemStack(COAL_BLOCK), BlockCoke.defaultState, 50, COKE_REACTION_TEMP, CARBON_SUBLIMATION_POINT)
//    addKilnRecipe(ItemStack(Blocks.LOG, 1, 0), BlockCharcoalSlab.defaultState, 25, COKE_REACTION_TEMP, CARBON_SUBLIMATION_POINT)
//    addKilnRecipe(ItemStack(Blocks.SAND), Blocks.GLASS.defaultState, 25, GLASS_MAKING_TEMP, QUARTZ_MELTING_POINT)
//    addKilnRecipe(ItemStack(Blocks.CLAY), Blocks.HARDENED_CLAY.defaultState, 25, DEFAULT_SMELTING_TEMPERATURE, QUARTZ_MELTING_POINT)
//    addKilnRecipe(ItemStack(BlockFluxedGravel), BlockGlazedBrick.defaultState, 25, FURNACE_BRICK_TEMP, QUARTZ_MELTING_POINT)
//    addKilnRecipe(ItemStack(Blocks.SPONGE, 1, 1), ItemStack(Blocks.SPONGE, 1, 0), 25, WATER_BOILING_POINT, COKE_REACTION_TEMP)
//
//    //KILN SHELF RECIPES
//    addKilnRecipe(ItemStack(COAL, 1, 0), ItemStack(ItemCoke), 50, COKE_REACTION_TEMP, CARBON_SUBLIMATION_POINT)
//    addKilnRecipe(ItemStack(CLAY_BALL), ItemStack(BRICK), 25, DEFAULT_SMELTING_TEMPERATURE, QUARTZ_MELTING_POINT)
//    addKilnRecipe(ItemStack(CHORUS_FRUIT), ItemStack(CHORUS_FRUIT_POPPED, 1, 0), 25, DEFAULT_SMELTING_TEMPERATURE, QUARTZ_MELTING_POINT)
 */

//private fun addKilnRecipe(input: ItemStack, output: ItemStack, duration: Int, minTemp: Double, maxTemp: Double) {
//    KilnRecipeManager.registerRecipe(KilnRecipeManager.createRecipe(input, output, duration, minTemp, maxTemp, true))
//}
//
//private fun addKilnRecipe(input: ItemStack, output: IBlockState, duration: Int, minTemp: Double, maxTemp: Double) {
//    KilnRecipeManager.registerRecipe(KilnRecipeManager.createRecipe(input, output, duration, minTemp, maxTemp, true))
//}
//
//private fun addIceboxRecipe(input: ItemStack, output: FluidStack, heat: Long, specificHeat: Double, minTemp: Double,
//                            maxTemp: Double, reverse: Boolean) {
//    IceboxRecipeManager.registerRecipe(
//            IceboxRecipeManager.createRecipe(input, output, heat, specificHeat, minTemp, maxTemp, reverse))
//}
//
//private fun addIceboxRecipeWater(input: ItemStack, output: Int, reverse: Boolean) {
//    IceboxRecipeManager.registerRecipe(IceboxRecipeManager.createRecipe(input, FluidStack(FluidRegistry.WATER, output),
//            (WATER_HEAT_OF_FUSION * output / 1000).toLong(), WATER_HEAT_CAPACITY, WATER_MELTING_POINT,
//            WATER_BOILING_POINT, reverse))
//}
//


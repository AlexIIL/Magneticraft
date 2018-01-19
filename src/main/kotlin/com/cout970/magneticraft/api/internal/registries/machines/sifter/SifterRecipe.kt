package com.cout970.magneticraft.api.internal.registries.machines.sifter

import com.cout970.magneticraft.api.internal.ApiUtils
import com.cout970.magneticraft.api.registries.machines.sifter.ISieveRecipe
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

/**
 * Created by cout970 on 22/08/2016.
 */
data class SifterRecipe(
        private val input: ItemStack,
        private val primary: ItemStack,
        private val primaryChance: Float,
        private val secondary: ItemStack,
        private val secondaryChance: Float,
        private val tertiary: ItemStack,
        private val tertiaryChance: Float,
        private val ticks: Float,
        private val oreDict: Boolean
) : ISieveRecipe {

    override fun getInput(): ItemStack = input.copy()

    override fun getPrimary(): ItemStack = primary.copy()

    override fun getPrimaryChance(): Float = primaryChance

    override fun getSecondary(): ItemStack = secondary

    override fun getTertiary(): ItemStack = tertiary

    override fun getSecondaryChance(): Float = secondaryChance

    override fun getTertiaryChance(): Float = tertiaryChance

    override fun getDuration(): Float = ticks

    override fun matches(input: ItemStack): Boolean {
        if (input.isEmpty) return false
        if (ApiUtils.equalsIgnoreSize(input, this.input)) return true
        if (oreDict) {
            val ids = OreDictionary.getOreIDs(this.input)
            return OreDictionary.getOreIDs(input).any { it in ids }
        }
        return false
    }
}
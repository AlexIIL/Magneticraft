package com.cout970.magneticraft

import com.cout970.magneticraft.misc.inventory.isNotEmpty
import com.cout970.magneticraft.tilerenderer.core.ModelCache
import com.cout970.magneticraft.util.addPostfix
import com.cout970.magneticraft.util.addPrefix
import com.cout970.magneticraft.util.logError
import com.cout970.magneticraft.util.resource
import com.cout970.modelloader.ModelSerializer
import com.cout970.modelloader.api.ModelUtilties
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.launchwrapper.Launch
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Timer
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.oredict.OreDictionary
import org.lwjgl.input.Keyboard
import java.io.File

@Suppress("unused")
/**
 * Created by cout970 on 11/06/2016.
 *
 * Stuff that only works in the dev-environment
 */
object Debug {

    val DEBUG = Launch.blackboard["fml.deobfuscatedEnvironment"] as Boolean
    var srcDir: File? = null

    fun preInit(event: FMLPreInitializationEvent) {
        srcDir = searchSourceDir(event.modConfigurationDirectory)
        if (srcDir == null) {
            logError("Error trying to find the source directory")
        }
    }

    fun searchSourceDir(configDir: File): File? {
        var temp: File? = configDir
        while (temp != null && temp.isDirectory) {
            if (File(temp, "build.gradle").exists()) {
                return temp
            }
            temp = temp.parentFile
        }
        return temp
    }

    var cache = emptyList<ModelCache>()
    var last = -1L

    fun getOrLoad(loc: ResourceLocation = resource("models/block/mcx/test.mcx")): List<ModelCache> {
        val locLasModified = lastModified(loc)
        if (last != locLasModified) {
            try {
                cache = loadModel(loc)
                last = locLasModified
            } catch (e: Exception) {
            }
        }
        return cache
    }

    fun lastModified(loc: ResourceLocation): Long {
        val path = "assets/${loc.resourceDomain}/${loc.resourcePath}"

        val url = Thread.currentThread().contextClassLoader.getResource(path)
        return File(url.file).lastModified()
    }

    fun loadModel(loc: ResourceLocation): List<ModelCache> {
        val resourceManager = Minecraft.getMinecraft().resourceManager
        val res = resourceManager.getResource(loc)
//        Thread.sleep(100)
        val data = ModelSerializer.load(res.inputStream)
        println("loading")

        val textureGrouped = data.parts.groupBy { it.texture }
        return textureGrouped.map {
            ModelCache {
                ModelUtilties.renderModelParts(data, it.value)
            }.apply { texture = it.key.addPrefix("textures/").addPostfix(".png") }
        }
    }

    // To use this, add breakpoint in
    // net.minecraft.inventory.ContainerWorkbench#onCraftMatrixChanged
    // and set the breakpoint condition to:
    // "Debug.INSTANCE.createRecipe(craftMatrix, player)"
    fun createRecipe(inv: InventoryCrafting, player: EntityPlayer): Boolean {

        try {
            val chars = listOf(
                    "A", "B", "C",
                    "D", "E", "F",
                    "G", "H", "I"
            )

            val map = mutableMapOf<JsonObject, String>()
            val slots = Array(9, { " " })
            for (y in 0 until inv.height) {
                for (x in 0 until inv.width) {
                    val stack = inv.getStackInRowAndColumn(x, y)
                    if (stack.isNotEmpty) {
                        val itemJson = deserializeToJson(stack)
                        val char = map.getOrPut(itemJson) { chars[map.size] }
                        slots[x + y * 3] = char
                    }
                }
            }

            val pattern = JsonArray().apply {
                var start = 0 to 0
                var end = 2 to 2
                if (slots[0].isBlank() && slots[3].isBlank() && slots[6].isBlank()) {
                    //ignore first column
                    start = start.first + 1 to start.second
                }
                if (slots[2].isBlank() && slots[5].isBlank() && slots[8].isBlank()) {
                    //ignore last column
                    end = end.first - 1 to end.second
                }
                if (slots[0].isBlank() && slots[1].isBlank() && slots[2].isBlank()) {
                    //ignore first row
                    start = start.first to start.second + 1
                }
                if (slots[6].isBlank() && slots[7].isBlank() && slots[8].isBlank()) {
                    //ignore last row
                    end = end.first to end.second - 1
                }

                for (y in start.second..end.second) {
                    var thisLine = ""
                    for (x in start.first..end.first) {
                        thisLine += slots[x + y * 3]
                    }
                    add(thisLine)
                }
            }

            val key = JsonObject().also { keyObj ->
                map.forEach { stack, itemKey ->
                    keyObj.add(itemKey, stack)
                }
            }

            val handItem = player.getHeldItem(EnumHand.MAIN_HAND)

            val obj = JsonObject()
            obj.addProperty("type", "forge:ore_shaped")
            obj.add("pattern", pattern)
            obj.add("key", key)
            obj.add("result", deserializeToJson(handItem))

            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonStr = gson.toJson(obj)

            if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
                val folder = File(srcDir, "src/main/resources/assets/magneticraft/recipes")
                val fileName = handItem.unlocalizedName.replace(".name", "").replaceBeforeLast(".", "").substring(1)
                val file = File(folder, fileName + ".json")
                file.writeText(jsonStr)
                println("saved: ${file.exists()}, path: ${file.absolutePath}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun deserializeToJson(stack: ItemStack) = JsonObject().apply {
        if (stack.isEmpty) return@apply
        val ids = OreDictionary.getOreIDs(stack)
        if (ids.isNotEmpty()) {
            val name = OreDictionary.getOreName(ids.first())
            addProperty("type", "forge:ore_dict")
            addProperty("ore", name)
        } else {
            addProperty("item", stack.item.registryName.toString())
            if (stack.hasSubtypes) {
                addProperty("data", stack.itemDamage)
            }
            if (stack.tagCompound != null) {
                addProperty("nbt", stack.tagCompound.toString())
            }
        }
    }

    //useful function to change the amount of tick per second used in minecraft
    fun setTicksPerSecond(tps: Int) {
        val timerField = Minecraft::class.java.getDeclaredField("timer")
        timerField.isAccessible = true
        val timer = timerField.get(Minecraft.getMinecraft()) as Timer
        val tickField = Timer::class.java.getDeclaredField("tickLength")
        tickField.isAccessible = true
        tickField.set(timer, 1000.0f / tps.toFloat())
    }
}

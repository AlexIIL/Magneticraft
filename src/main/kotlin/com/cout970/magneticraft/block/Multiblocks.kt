package com.cout970.magneticraft.block

import com.cout970.magneticraft.block.core.*
import com.cout970.magneticraft.item.itemblock.blockListOf
import com.cout970.magneticraft.item.itemblock.itemBlockListOf
import com.cout970.magneticraft.misc.CreativeTabMg
import com.cout970.magneticraft.misc.block.get
import com.cout970.magneticraft.misc.player.sendMessage
import com.cout970.magneticraft.misc.tileentity.getModule
import com.cout970.magneticraft.misc.tileentity.getTile
import com.cout970.magneticraft.misc.world.isServer
import com.cout970.magneticraft.multiblock.*
import com.cout970.magneticraft.multiblock.core.Multiblock
import com.cout970.magneticraft.multiblock.core.MultiblockContext
import com.cout970.magneticraft.multiblock.core.MultiblockManager
import com.cout970.magneticraft.tileentity.*
import com.cout970.magneticraft.tileentity.modules.ModuleMultiblockGap
import com.cout970.magneticraft.util.resource
import com.cout970.magneticraft.util.vector.plus
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.IStringSerializable
import net.minecraft.util.text.TextFormatting

/**
 * Created by cout970 on 2017/07/03.
 */
object Multiblocks : IBlockMaker {

    val PROPERTY_MULTIBLOCK_ORIENTATION = PropertyEnum.create("multiblock_orientation",
            MultiblockOrientation::class.java)!!

    lateinit var gap: BlockBase private set
    lateinit var solarPanel: BlockBase private set
    lateinit var shelvingUnit: BlockBase private set
    lateinit var steamEngine: BlockBase private set
    lateinit var grinder: BlockBase private set
    lateinit var sieve: BlockBase private set
    lateinit var solarTower: BlockBase private set
    lateinit var solarMirror: BlockBase private set
    lateinit var container: BlockBase private set
    lateinit var pumpjack: BlockBase private set

    override fun initBlocks(): List<Pair<Block, ItemBlock?>> {
        val builder = BlockBuilder().apply {
            material = Material.IRON
            creativeTab = CreativeTabMg
            hasCustomModel = true
            tileConstructor = { a, b, c, d ->
                BlockBase.states_ = b
                BlockMultiblock(c, d, a)
            }
            states = MultiblockOrientation.values().toList()
            alwaysDropDefault = true
        }

        gap = builder.withName("multiblock_gap").copy {
            states = null
            factory = factoryOf(::TileMultiblockGap)
            onDrop = { emptyList() }
            onActivated = func@{
                val tile = it.worldIn.getTile<TileMultiblockGap>(it.pos) ?: return@func false
                val module = tile.multiblockModule
                val storedPos = module.centerPos ?: return@func false
                val mainBlockPos = it.pos.subtract(storedPos)
                val actionable = it.worldIn.getModule<IOnActivated>(mainBlockPos) ?: return@func false
                return@func actionable.onActivated(it)
            }
            onBlockBreak = func@{
                val mod = it.worldIn.getModule<ModuleMultiblockGap>(it.pos) ?: return@func
                if (mod.multiblock == null) {
                    val relPos = mod.centerPos ?: return@func
                    val absPos = it.pos + relPos
                    it.worldIn.setBlockToAir(absPos)
                }
            }
        }.build()

        solarPanel = builder.withName("solar_panel").copy {
            factory = factoryOf(::TileSolarPanel)
            generateDefaultItemModel = false
            customModels = listOf(
                    "model" to resource("models/block/mcx/solar_panel.mcx")
            )
            onActivated = defaultOnActivated({ MultiblockSolarPanel })
            onBlockPlaced = Multiblocks::placeWithOrientation
            pickBlock = CommonMethods::pickDefaultBlock
        }.build()

        shelvingUnit = builder.withName("shelving_unit").copy {
            factory = factoryOf(::TileShelvingUnit)
            generateDefaultItemModel = false
            customModels = listOf(
                    "model" to resource("models/block/mcx/shelving_unit.mcx")
            )
            onActivated = defaultOnActivated({ MultiblockShelvingUnit })
            onBlockPlaced = Multiblocks::placeWithOrientation
            pickBlock = CommonMethods::pickDefaultBlock
        }.build()

        steamEngine = builder.withName("steam_engine").copy {
            factory = factoryOf(::TileSteamEngine)
            generateDefaultItemModel = false
            customModels = listOf(
                    "model" to resource("models/block/mcx/steam_engine.mcx")
            )
            onActivated = defaultOnActivated({ MultiblockSteamEngine })
            onBlockPlaced = Multiblocks::placeWithOrientation
            pickBlock = CommonMethods::pickDefaultBlock
        }.build()

        grinder = builder.withName("grinder").copy {
            factory = factoryOf(::TileGrinder)
            generateDefaultItemModel = false
            customModels = listOf(
                    "model" to resource("models/block/mcx/grinder.mcx")
            )
            onActivated = defaultOnActivated({ MultiblockGrinder })
            onBlockPlaced = Multiblocks::placeWithOrientation
            pickBlock = CommonMethods::pickDefaultBlock
        }.build()

        sieve = builder.withName("sieve").copy {
            factory = factoryOf(::TileSieve)
            generateDefaultItemModel = false
            customModels = listOf(
                    "model" to resource("models/block/mcx/sieve.mcx")
            )
            onActivated = defaultOnActivated({ MultiblockSieve })
            onBlockPlaced = Multiblocks::placeWithOrientation
            pickBlock = CommonMethods::pickDefaultBlock
        }.build()

        solarTower = builder.withName("solar_tower").copy {
            factory = factoryOf(::TileSolarTower)
            generateDefaultItemModel = false
            customModels = listOf(
                    "model" to resource("models/block/mcx/solar_tower.mcx")
            )
            onActivated = defaultOnActivated({ MultiblockSolarTower })
            onBlockPlaced = Multiblocks::placeWithOrientation
            pickBlock = CommonMethods::pickDefaultBlock
        }.build()

        solarMirror = builder.withName("solar_mirror").copy {
            factory = factoryOf(::TileSolarMirror)
            generateDefaultItemModel = false
            customModels = listOf(
                    "model" to resource("models/block/mcx/solar_mirror.mcx")
            )
            onActivated = defaultOnActivated({ MultiblockSolarMirror })
            onBlockPlaced = Multiblocks::placeWithOrientation
            pickBlock = CommonMethods::pickDefaultBlock
        }.build()

        container = builder.withName("container").copy {
            factory = factoryOf(::TileContainer)
            generateDefaultItemModel = false
            customModels = listOf(
                    "model" to resource("models/block/mcx/container.mcx")
            )
            onActivated = defaultOnActivated({ MultiblockContainer })
            onBlockPlaced = Multiblocks::placeWithOrientation
            pickBlock = CommonMethods::pickDefaultBlock
        }.build()

        pumpjack = builder.withName("pumpjack").copy {
            factory = factoryOf(::TilePumpjack)
            generateDefaultItemModel = false
            customModels = listOf(
                    "model" to resource("models/block/mcx/pumpjack.mcx")
            )
            onActivated = defaultOnActivated({ MultiblockPumpjack })
            onBlockPlaced = Multiblocks::placeWithOrientation
            pickBlock = CommonMethods::pickDefaultBlock
        }.build()

        return itemBlockListOf(solarPanel, shelvingUnit, steamEngine, grinder, sieve, solarTower, solarMirror,
                container, pumpjack) + blockListOf(gap)
    }

    fun placeWithOrientation(it: OnBlockPlacedArgs): IBlockState {
        val placer = it.placer ?: return it.defaultValue
        return it.defaultValue.withProperty(PROPERTY_MULTIBLOCK_ORIENTATION,
                MultiblockOrientation.of(placer.horizontalFacing, false))
    }

    fun defaultOnActivated(multiblock: () -> Multiblock): (OnActivatedArgs) -> Boolean {
        return func@{
            val state = it.state[PROPERTY_MULTIBLOCK_ORIENTATION] ?: return@func false
            if (state.active) {
                val actionable = it.worldIn.getModule<IOnActivated>(it.pos) ?: return@func false
                return@func actionable.onActivated(it)
            } else {
                if (it.hand != EnumHand.MAIN_HAND) return@func false
                if (it.worldIn.isServer) {
                    val context = MultiblockContext(
                            multiblock = multiblock(),
                            world = it.worldIn,
                            center = it.pos,
                            facing = state.facing,
                            player = it.playerIn
                    )
                    activateMultiblock(context)
                }
                return@func true
            }
        }
    }

    fun activateMultiblock(context: MultiblockContext) {
        val errors = MultiblockManager.checkMultiblockStructure(context)
        val playerIn = context.player!!
        if (errors.isNotEmpty()) {
            playerIn.sendStatusMessage(errors.first(), true)
        } else {
            MultiblockManager.activateMultiblockStructure(context)
            playerIn.sendMessage("text.magneticraft.multiblock.activate", color = TextFormatting.GREEN)
        }
    }

    enum class MultiblockOrientation(
            override val stateName: String,
            override val isVisible: Boolean,
            val active: Boolean,
            val facing: EnumFacing) : IStatesEnum, IStringSerializable {

        INACTIVE_NORTH("inactive_north", true, false, EnumFacing.NORTH),
        INACTIVE_SOUTH("inactive_south", false, false, EnumFacing.SOUTH),
        INACTIVE_WEST("inactive_west", false, false, EnumFacing.WEST),
        INACTIVE_EAST("inactive_east", false, false, EnumFacing.EAST),
        ACTIVE_NORTH("active_north", false, true, EnumFacing.NORTH),
        ACTIVE_SOUTH("active_south", false, true, EnumFacing.SOUTH),
        ACTIVE_WEST("active_west", false, true, EnumFacing.WEST),
        ACTIVE_EAST("active_east", false, true, EnumFacing.EAST);

        override fun getName() = name.toLowerCase()
        override val properties: List<IProperty<*>> get() = listOf(PROPERTY_MULTIBLOCK_ORIENTATION)

        override fun getBlockState(block: Block): IBlockState =
                block.defaultState.withProperty(PROPERTY_MULTIBLOCK_ORIENTATION, this)

        companion object {
            fun of(facing: EnumFacing, active: Boolean) = when {
                facing == EnumFacing.NORTH && !active -> INACTIVE_NORTH
                facing == EnumFacing.SOUTH && !active -> INACTIVE_SOUTH
                facing == EnumFacing.WEST && !active -> INACTIVE_WEST
                facing == EnumFacing.EAST && !active -> INACTIVE_EAST
                facing == EnumFacing.NORTH && active -> ACTIVE_NORTH
                facing == EnumFacing.SOUTH && active -> ACTIVE_SOUTH
                facing == EnumFacing.WEST && active -> ACTIVE_WEST
                facing == EnumFacing.EAST && active -> ACTIVE_EAST
                else -> INACTIVE_NORTH
            }
        }
    }
}
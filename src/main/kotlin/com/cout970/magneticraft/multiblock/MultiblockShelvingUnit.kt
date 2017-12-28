package com.cout970.magneticraft.multiblock

import com.cout970.magneticraft.block.MultiblockParts
import com.cout970.magneticraft.block.Multiblocks
import com.cout970.magneticraft.multiblock.components.MainBlockComponent
import com.cout970.magneticraft.multiblock.components.SingleBlockComponent
import com.cout970.magneticraft.multiblock.core.BlockData
import com.cout970.magneticraft.multiblock.core.IMultiblockComponent
import com.cout970.magneticraft.multiblock.core.Multiblock
import com.cout970.magneticraft.multiblock.core.MultiblockContext
import com.cout970.magneticraft.tilerenderer.core.PIXEL
import com.cout970.magneticraft.util.vector.toAABBWith
import com.cout970.magneticraft.util.vector.unaryMinus
import com.cout970.magneticraft.util.vector.vec3Of
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.ITextComponent

/**
 * Created by cout970 on 2017/07/04.
 */
object MultiblockShelvingUnit : Multiblock() {

    override val name: String = "shelving_unit"
    override val size: BlockPos = BlockPos(5, 3, 2)
    override val scheme: List<MultiblockLayer>
    override val center: BlockPos = BlockPos(2, 0, 0)

    init {
        val replacement = Multiblocks.gap.defaultState

        val pBlock = MultiblockParts.PartType.GRATE.getBlockState(MultiblockParts.parts)
        val P: IMultiblockComponent = SingleBlockComponent(pBlock, replacement)

        val M: IMultiblockComponent = MainBlockComponent(Multiblocks.shelvingUnit) { context, activate ->
            Multiblocks.MultiblockOrientation.of(context.facing, activate).getBlockState(Multiblocks.shelvingUnit)
        }

        scheme = yLayers(
                zLayers(listOf(P, P, P, P, P), // y = 2
                        listOf(P, P, P, P, P)),

                zLayers(listOf(P, P, P, P, P), // y = 1
                        listOf(P, P, P, P, P)),

                zLayers(listOf(P, P, M, P, P), // y = 0
                        listOf(P, P, P, P, P))
        )
    }

    override fun getControllerBlock() = Multiblocks.shelvingUnit

    val hitbox = listOf(
            (Vec3d.ZERO toAABBWith vec3Of(5, 1 - 5 * PIXEL, 2)),
            (vec3Of(0, 1 - 5 * PIXEL, 0) toAABBWith vec3Of(5, 2 - 5 * PIXEL, 2)),
            (vec3Of(0, 2 - 5 * PIXEL, 0) toAABBWith vec3Of(5, 3 - 5 * PIXEL, 2)),
            (vec3Of(0, 3 - 5 * PIXEL, 0) toAABBWith vec3Of(5, 3, 2))
    ).map { it.offset(-center) }

    override fun getGlobalCollisionBoxes(): List<AxisAlignedBB> = hitbox


    override fun checkExtraRequirements(data: MutableList<BlockData>,
                                        context: MultiblockContext): List<ITextComponent> = listOf()
}
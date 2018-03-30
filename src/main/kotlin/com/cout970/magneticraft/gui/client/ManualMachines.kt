package com.cout970.magneticraft.gui.client

import com.cout970.magneticraft.gui.client.components.CompBackground
import com.cout970.magneticraft.gui.client.core.GuiBase
import com.cout970.magneticraft.gui.common.ContainerBox
import com.cout970.magneticraft.util.guiTexture

/**
 * Created by cout970 on 2017/08/10.
 */

fun guiBox(gui: GuiBase, container: ContainerBox) = gui.run {
    val tile = container.tile
    +CompBackground(guiTexture("box"))
}

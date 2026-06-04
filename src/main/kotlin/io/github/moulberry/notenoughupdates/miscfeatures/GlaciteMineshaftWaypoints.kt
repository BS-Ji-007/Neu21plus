/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.miscfeatures

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.LocationChangeEvent
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos

@NEUAutoSubscribe
class GlaciteMineshaftWaypoints {

    var entrance: BlockPos? = null
    fun isEnabled() = NotEnoughUpdates.INSTANCE.config.mining.mineshaftExitWaypoint

    fun onLocationChange(event: LocationChangeEvent) {
        entrance = if (event.newLocation == "mineshaft") {
            Minecraft.getInstance().player?.position
        } else {
            null
        }
    }

    fun onRenderLast(event: RenderWorldLastEvent) {
        if (!isEnabled()) return
        val pos = entrance ?: return
        RenderUtils.renderBoundingBox(
            pos,
            0x7020CC00,
            event.partialTicks,
            true
        )
        RenderUtils.renderWayPoint(
            "§aExit", pos.up(1),
            event.partialTicks
        )
    }
}

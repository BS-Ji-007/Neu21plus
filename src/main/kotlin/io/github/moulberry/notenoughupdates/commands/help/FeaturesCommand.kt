/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.commands.help

import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.RegisterBrigadierCommandEvent
import io.github.moulberry.notenoughupdates.util.Constants
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.brigadier.reply
import io.github.moulberry.notenoughupdates.util.brigadier.thenExecute
import io.github.moulberry.notenoughupdates.util.brigadier.withHelp
import net.minecraft.event.ClickEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.ChatFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@NEUAutoSubscribe
class FeaturesCommand {
    @SubscribeEvent
    fun onCommands(event: RegisterBrigadierCommandEvent) {
        event.command("neufeatures") {
            thenExecute {
                reply("")
                val url = Constants.MISC?.get("featureslist")?.asString
                if (url == null) {
                    Utils.showOutdatedRepoNotification("misc.json or missing featureslist")
                    return@thenExecute
                }

                if (Utils.openUrl(url)) {
                    reply(
                        ChatFormatting.DARK_PURPLE.toString() + "" + ChatFormatting.BOLD + "NEU" + ChatFormatting.RESET +
                                ChatFormatting.GOLD + "> Opening Feature List in browser."
                    )
                } else {
                    val clickTextFeatures = ChatComponentText(
                        (ChatFormatting.DARK_PURPLE.toString() + "" + ChatFormatting.BOLD + "NEU" + ChatFormatting.RESET +
                                ChatFormatting.GOLD + "> Click here to open the Feature List in your browser.")
                    )
                    clickTextFeatures.chatStyle =
                        Utils.createClickStyle(ClickEvent.Action.OPEN_URL, url)
                    reply(clickTextFeatures)
                }
                reply("")
            }
        }.withHelp("List all of NEUs features")
    }
}

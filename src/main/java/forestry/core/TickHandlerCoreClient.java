/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;

import forestry.core.config.Constants;
import forestry.core.utils.GeneticsUtil;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class TickHandlerCoreClient {

	private static boolean hasNaturalistEye;

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft != null) {
				PlayerEntity player = minecraft.player;
				if (player != null) {
					boolean hasNaturalistEye = GeneticsUtil.hasNaturalistEye(player);
					if (TickHandlerCoreClient.hasNaturalistEye != hasNaturalistEye) {
						TickHandlerCoreClient.hasNaturalistEye = hasNaturalistEye;
						//TODO - I think this is the correct field
						WorldRenderer renderGlobal = minecraft.levelRenderer;
						if (renderGlobal != null) {
							renderGlobal.setBlocksDirty(
									(int) player.getX() - 32, (int) player.getY() - 32, (int) player.getZ() - 32,
									(int) player.getX() + 32, (int) player.getY() + 32, (int) player.getZ() + 32);
						}
					}
				}
			}
		}
	}
}

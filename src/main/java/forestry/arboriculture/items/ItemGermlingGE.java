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
package forestry.arboriculture.items;

import javax.annotation.Nullable;
import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import forestry.api.arboriculture.TreeManager;
import forestry.api.arboriculture.genetics.EnumGermlingType;
import forestry.api.arboriculture.genetics.IAlleleTreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.TreeChromosomes;
import forestry.api.core.ItemGroups;
import forestry.api.genetics.ICheckPollinatable;
import forestry.api.genetics.IPollinatable;
import forestry.api.recipes.IVariableFermentable;
import forestry.arboriculture.genetics.TreeHelper;
import forestry.core.config.Config;
import forestry.core.genetics.ItemGE;
import forestry.core.items.definitions.IColoredItem;
import forestry.core.network.packets.PacketFXSignal;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.NetworkUtil;

import genetics.api.GeneticHelper;
import genetics.api.organism.IOrganismType;

public class ItemGermlingGE extends ItemGE implements IVariableFermentable, IColoredItem {

	private final EnumGermlingType type;

	public ItemGermlingGE(EnumGermlingType type) {
		super(new Item.Properties().tab(ItemGroups.tabArboriculture));
		this.type = type;
	}

	@Override
	protected final IOrganismType getType() {
		return type;
	}

	@Override
	protected IAlleleTreeSpecies getSpecies(ItemStack itemStack) {
		return GeneticHelper.getOrganism(itemStack).getAllele(TreeChromosomes.SPECIES, true);
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
		return GeneticHelper.createOrganism(stack, type, TreeHelper.getRoot().getDefinition());
	}

	@Override
	public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> subItems) {
		if (this.allowdedIn(tab)) {
			addCreativeItems(subItems, true);
		}
	}

	public void addCreativeItems(NonNullList<ItemStack> subItems, boolean hideSecrets) {
		for (ITree individual : TreeHelper.getRoot().getIndividualTemplates()) {
			// Don't show secrets unless ordered to.
			if (hideSecrets && individual.isSecret() && !Config.isDebug) {
				continue;
			}

			ItemStack stack = new ItemStack(this);
			GeneticHelper.setIndividual(stack, individual);
			subItems.add(stack);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getColorFromItemStack(ItemStack itemstack, int renderPass) {
		return getSpecies(itemstack).getGermlingColour(type, renderPass);
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		BlockRayTraceResult traceResult = (BlockRayTraceResult) getPlayerPOVHitResult(worldIn, playerIn, RayTraceContext.FluidMode.ANY);
		BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(playerIn, handIn, traceResult));

		ItemStack itemStack = playerIn.getItemInHand(handIn);
		if (traceResult.getType() == RayTraceResult.Type.BLOCK) {
			BlockPos pos = traceResult.getBlockPos();

			Optional<ITree> treeOptional = TreeManager.treeRoot.create(itemStack);
			if (treeOptional.isPresent()) {
				ITree tree = treeOptional.get();
				if (type == EnumGermlingType.SAPLING) {
					return onItemRightClickSapling(itemStack, worldIn, playerIn, pos, tree, context);
				} else if (type == EnumGermlingType.POLLEN) {
					return onItemRightClickPollen(itemStack, worldIn, playerIn, pos, tree);
				}
			}

		}
		return new ActionResult<>(ActionResultType.PASS, itemStack);
	}


	private static ActionResult<ItemStack> onItemRightClickPollen(ItemStack itemStackIn, World worldIn, PlayerEntity player, BlockPos pos, ITree tree) {
		ICheckPollinatable checkPollinatable = GeneticsUtil.getCheckPollinatable(worldIn, pos);
		if (checkPollinatable == null || !checkPollinatable.canMateWith(tree)) {
			return new ActionResult<>(ActionResultType.FAIL, itemStackIn);
		}

		IPollinatable pollinatable = GeneticsUtil.getOrCreatePollinatable(player.getGameProfile(), worldIn, pos, true);
		if (pollinatable == null || !pollinatable.canMateWith(tree)) {
			return new ActionResult<>(ActionResultType.FAIL, itemStackIn);
		}

		if (worldIn.isClientSide) {
			return new ActionResult<>(ActionResultType.SUCCESS, itemStackIn);
		} else {
			pollinatable.mateWith(tree);

			BlockState blockState = worldIn.getBlockState(pos);
			PacketFXSignal packet = new PacketFXSignal(PacketFXSignal.VisualFXType.BLOCK_BREAK, PacketFXSignal.SoundFXType.BLOCK_BREAK, pos, blockState);
			NetworkUtil.sendNetworkPacket(packet, pos, worldIn);

			if (!player.isCreative()) {
				itemStackIn.shrink(1);
			}
			return new ActionResult<>(ActionResultType.SUCCESS, itemStackIn);
		}
	}


	private static ActionResult<ItemStack> onItemRightClickSapling(ItemStack itemStackIn, World worldIn, PlayerEntity player, BlockPos pos, ITree tree, BlockItemUseContext context) {
		// x, y, z are the coordinates of the block "hit", can thus either be the soil or tall grass, etc.
		BlockState hitBlock = worldIn.getBlockState(pos);
		if (!hitBlock.canBeReplaced(context)) {
			if (!worldIn.isEmptyBlock(pos.above())) {
				return new ActionResult<>(ActionResultType.FAIL, itemStackIn);
			}
			pos = pos.above();
		}

		if (tree.canStay(worldIn, pos)) {
			if (TreeManager.treeRoot.plantSapling(worldIn, tree, player.getGameProfile(), pos)) {
				if (!player.isCreative()) {
					itemStackIn.shrink(1);
				}
				return new ActionResult<>(ActionResultType.SUCCESS, itemStackIn);
			}
		}
		return new ActionResult<>(ActionResultType.FAIL, itemStackIn);
	}

	@Override
	public float getFermentationModifier(ItemStack itemstack) {
		itemstack = GeneticsUtil.convertToGeneticEquivalent(itemstack);
		Optional<ITree> treeOptional = TreeManager.treeRoot.create(itemstack);
		return treeOptional.map(tree -> tree.getGenome().getActiveValue(TreeChromosomes.SAPPINESS) * 10)
			.orElse(1.0f);
	}

	@Override
	public int getBurnTime(ItemStack itemStack) {
		return 100;
	}
}

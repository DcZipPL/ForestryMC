package forestry.book.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.book.IBookEntry;
import forestry.core.gui.GuiUtil;

@OnlyIn(Dist.CLIENT)
public class GuiButtonEntry extends Button {
	public final IBookEntry entry;

	public GuiButtonEntry(int x, int y, IBookEntry entry, IPressable action) {
		super(x, y, Minecraft.getInstance().font.width(entry.getTitle().getString()) + 9, 11, entry.getTitle(), action);
		this.entry = entry;
	}

	@Override
	public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			FontRenderer fontRenderer = Minecraft.getInstance().font;
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

			ITextComponent text = getMessage();
			TextFormatting color = TextFormatting.DARK_GRAY;
			if (isHovered) {
				color = TextFormatting.GOLD;
			}
			GuiUtil.enableUnicode();
			fontRenderer.draw(transform, text.copy().withStyle(color), this.x + 9, this.y, -1);
			GuiUtil.resetUnicode();

			ItemStack stack = entry.getStack();
			if (!stack.isEmpty()) {
				RenderSystem.pushMatrix();
				RenderSystem.translatef(x, y, getBlitOffset());
				RenderSystem.enableRescaleNormal();
				RenderSystem.scalef(0.5F, 0.5F, 0.5F);
				GuiUtil.drawItemStack(fontRenderer, stack, 0, 0);
				RenderHelper.turnOff();
				RenderSystem.popMatrix();
			}
		}
	}
}

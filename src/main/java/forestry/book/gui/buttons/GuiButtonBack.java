package forestry.book.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.text.StringTextComponent;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.book.gui.GuiForesterBook;

@OnlyIn(Dist.CLIENT)
public class GuiButtonBack extends Button {
	public GuiButtonBack(int x, int y, IPressable action) {
		super(x, y, 18, 9, StringTextComponent.EMPTY, action);
	}

	@Override
	public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

			TextureManager manager = Minecraft.getInstance().getTextureManager();
			manager.bind(GuiForesterBook.TEXTURE);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			blit(transform, x, y, 36 + (isHovered ? 18 : 0), 181, 18, 9);
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return false;
	}
}

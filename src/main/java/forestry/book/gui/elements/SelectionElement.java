package forestry.book.gui.elements;

import javax.annotation.Nullable;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.book.gui.GuiForesterBook;
import forestry.core.gui.Drawable;
import forestry.core.gui.GuiConstants;
import forestry.core.gui.elements.Alignment;
import forestry.core.gui.elements.ButtonElement;
import forestry.core.gui.elements.GuiElementFactory;
import forestry.core.gui.elements.layouts.ContainerElement;

@OnlyIn(Dist.CLIENT)
public abstract class SelectionElement<R> extends ContainerElement {
	private static final Drawable CRAFTING_COUNT = new Drawable(GuiForesterBook.TEXTURE, 104, 181, 34, 14);
	private static final Drawable RIGHT_BUTTON = new Drawable(GuiForesterBook.TEXTURE, 138, 181, 10, 9);
	private static final Drawable LEFT_BUTTON = new Drawable(GuiForesterBook.TEXTURE, 148, 181, 10, 9);

	private int index = -1;

	@Nullable
	protected final ButtonElement leftButton;
	@Nullable
	protected final ButtonElement rightButton;
	@Nullable
	protected final ContainerElement text;
	protected final ContainerElement selectedElement;
	protected final R[] recipes;

	protected SelectionElement(int width, int height, R[] recipes) {
		this(width, height, recipes, 0);
	}

	protected SelectionElement(int width, int height, R[] recipes, int yOffset) {
		setSize(width, height + (recipes.length > 1 ? 16 : 0));
		this.recipes = recipes;
		if (recipes.length > 1) {
			drawable(0, 0, CRAFTING_COUNT).setAlign(Alignment.BOTTOM_CENTER);
			text = pane(width, getHeight());
			leftButton = add(new ButtonElement.Builder()
					.pos(-27, -2)
					.textures(LEFT_BUTTON)
					.action(e -> setIndex(index - 1))
					.align(Alignment.BOTTOM_CENTER)
					.create()
			);

			rightButton = add(new ButtonElement.Builder()
					.pos(27, -2)
					.textures(RIGHT_BUTTON)
					.action(e -> setIndex(index + 1))
					.align(Alignment.BOTTOM_CENTER)
					.create()
			);
		} else {
			text = null;
			leftButton = null;
			rightButton = null;
		}
		selectedElement = GuiElementFactory.pane(0, 2, width, getHeight());
	}

	protected final void setIndex(int index) {
		if (index == this.index || index >= recipes.length || index < 0) {
			return;
		}
		this.index = index;
		selectedElement.clear();
		onIndexUpdate(index, recipes[index]);
		if (text != null) {
			text.clear();
			text.label((index + 1) + "/" + recipes.length, Alignment.BOTTOM_CENTER, GuiConstants.BLACK_STYLE).setYPosition(2);
		}
		if (leftButton != null) {
			leftButton.setEnabled(index > 0);
		}
		if (rightButton != null) {
			rightButton.setEnabled(index < recipes.length - 1);
		}
	}

	protected abstract void onIndexUpdate(int index, R recipe);
}

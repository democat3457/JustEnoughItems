package mezz.jei.gui.ingredients;

import com.google.common.base.Joiner;
import mezz.jei.Internal;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.pipeline.LightUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("deprecation")
public class GuiItemStackFast {
	private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	private static final int blacklistItemColor = Color.yellow.getRGB();
	private static final int blacklistWildColor = Color.red.getRGB();
	private static final int blacklistModColor = Color.blue.getRGB();

	@Nonnull
	private final Rectangle area;
	private final int padding;
	private final ItemModelMesher itemModelMesher;

	@Nullable
	private ItemStack itemStack;

	public GuiItemStackFast(int xPosition, int yPosition, int padding) {
		this.padding = padding;
		final int size = 16 + (2 * padding);
		this.area = new Rectangle(xPosition, yPosition, size, size);
		this.itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
	}

	@Nonnull
	public Rectangle getArea() {
		return area;
	}

	public void setItemStack(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@Nullable
	public ItemStack getItemStack() {
		return itemStack;
	}

	public void clear() {
		this.itemStack = null;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return (itemStack != null) && area.contains(mouseX, mouseY);
	}

	public void renderItemAndEffectIntoGUI() {
		if (itemStack == null) {
			return;
		}

		IBakedModel bakedModel = itemModelMesher.getItemModel(itemStack);

		if (Config.isEditModeEnabled()) {
			renderEditMode();
			GlStateManager.enableBlend();
		}

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(area.x + padding + 8.0f, area.y + padding + 8.0f, 150.0F);
			GlStateManager.scale(16F, -16F, 16F);

			bakedModel = ForgeHooksClient.handleCameraTransforms(bakedModel, ItemCameraTransforms.TransformType.GUI, false);

			GlStateManager.translate(-0.5F, -0.5F, -0.5F);

			renderModel(bakedModel, itemStack);

			if (itemStack.hasEffect()) {
				renderEffect(bakedModel);
			}
		}
		GlStateManager.popMatrix();
	}

	private void renderModel(IBakedModel model, ItemStack stack) {
		this.renderModel(model, -1, stack);
	}

	private void renderModel(IBakedModel model, int color, ItemStack stack) {
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexBuffer = tessellator.getBuffer();
		vertexBuffer.begin(7, DefaultVertexFormats.ITEM);

		for (EnumFacing enumfacing : EnumFacing.VALUES) {
			this.renderQuads(vertexBuffer, model.getQuads(null, enumfacing, 0), color, stack);
		}

		this.renderQuads(vertexBuffer, model.getQuads(null, null, 0), color, stack);
		tessellator.draw();
	}

	private void renderQuads(VertexBuffer vertexBuffer, List quads, int color, ItemStack stack) {
		boolean flag = color == -1 && stack != null;
		BakedQuad bakedquad;
		int j;

		ItemColors itemColors = Minecraft.getMinecraft().getItemColors();

		for (Object quad : quads) {
			bakedquad = (BakedQuad) quad;
			j = color;

			if (flag && bakedquad.hasTintIndex()) {
				j = itemColors.getColorFromItemstack(stack, bakedquad.getTintIndex());
				if (EntityRenderer.anaglyphEnable) {
					j = TextureUtil.anaglyphColor(j);
				}

				j |= -16777216;
			}
			LightUtil.renderQuadColor(vertexBuffer, bakedquad, j);
		}
	}

	private void renderEffect(IBakedModel model) {
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(514);
		GlStateManager.blendFunc(768, 1);
		textureManager.bindTexture(RES_ITEM_GLINT);
		GlStateManager.matrixMode(5890);

		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
		GlStateManager.translate(f, 0.0F, 0.0F);
		GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
		this.renderModel(model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
		GlStateManager.translate(-f1, 0.0F, 0.0F);
		GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
		this.renderModel(model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.matrixMode(5888);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		textureManager.bindTexture(TextureMap.locationBlocksTexture);
	}

	private void renderModel(IBakedModel model, int color) {
		this.renderModel(model, color, null);
	}

	public void renderSlow() {
		if (Config.isEditModeEnabled()) {
			renderEditMode();
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		RenderItem renderItem = minecraft.getRenderItem();
		renderItem.renderItemAndEffectIntoGUI(itemStack, area.x + padding, area.y + padding);
		GlStateManager.disableBlend();
	}

	public void renderOverlay(Minecraft minecraft) {
		if (itemStack == null) {
			return;
		}
		FontRenderer font = getFontRenderer(minecraft, itemStack);
		RenderItem renderItem = minecraft.getRenderItem();
		renderItem.renderItemOverlayIntoGUI(font, itemStack, area.x + padding, area.y + padding, null);
	}

	private void renderEditMode() {
		if (itemStack == null) {
			return;
		}

		if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.ITEM)) {
			GuiScreen.drawRect(area.x + padding, area.y + padding, area.x + 8 + padding, area.y + 16 + padding, blacklistItemColor);
		}
		if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.WILDCARD)) {
			GuiScreen.drawRect(area.x + 8 + padding, area.y + padding, area.x + 16 + padding, area.y + 16 + padding, blacklistWildColor);
		}
		if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.MOD_ID)) {
			GuiScreen.drawRect(area.x + padding, area.y + 8 + padding, area.x + 16 + padding, area.y + 16 + padding, blacklistModColor);
		}
	}

	@Nonnull
	public static FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		Item item = itemStack.getItem();
		FontRenderer fontRenderer = item.getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRendererObj;
		}
		return fontRenderer;
	}

	public void drawHovered(Minecraft minecraft) {
		if (itemStack == null) {
			return;
		}

		renderSlow();
		renderOverlay(minecraft);

		GlStateManager.disableDepth();
		Gui.drawRect(area.x, area.y, area.x + area.width, area.y + area.height, 0x7FFFFFFF);
		GlStateManager.enableDepth();
	}

	public void drawTooltip(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (itemStack == null) {
			return;
		}
		List<String> tooltip = getTooltip(minecraft, itemStack);
		FontRenderer fontRenderer = getFontRenderer(minecraft, itemStack);
		TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);
	}

	@Nonnull
	private static List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		List<String> list = itemStack.getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, itemStack.getRarity().rarityColor + list.get(k));
			} else {
				list.set(k, TextFormatting.GRAY + list.get(k));
			}
		}

		int maxWidth = Constants.MAX_TOOLTIP_WIDTH;
		for (String tooltipLine : list) {
			int width = minecraft.fontRendererObj.getStringWidth(tooltipLine);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}

		if (Config.isColorSearchEnabled()) {
			Collection<String> colorNames = Internal.getColorNamer().getColorNames(itemStack);
			if (!colorNames.isEmpty()) {
				String colorNamesString = Joiner.on(", ").join(colorNames);
				String colorNamesLocalizedString = TextFormatting.GRAY + Translator.translateToLocalFormatted("jei.tooltip.item.colors", colorNamesString);
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(colorNamesLocalizedString, maxWidth));
			}
		}

		if (Config.isEditModeEnabled()) {
			list.add("");
			list.add(TextFormatting.ITALIC + Translator.translateToLocal("gui.jei.editMode.description"));
			if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.ITEM)) {
				String description = TextFormatting.YELLOW + Translator.translateToLocal("gui.jei.editMode.description.show");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, maxWidth));
			} else {
				String description = TextFormatting.YELLOW + Translator.translateToLocal("gui.jei.editMode.description.hide");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, maxWidth));
			}

			if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.WILDCARD)) {
				String description = TextFormatting.RED + Translator.translateToLocal("gui.jei.editMode.description.show.wild");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, maxWidth));
			} else {
				String description = TextFormatting.RED + Translator.translateToLocal("gui.jei.editMode.description.hide.wild");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, maxWidth));
			}

			if (Config.isItemOnConfigBlacklist(itemStack, Config.ItemBlacklistType.MOD_ID)) {
				String description = TextFormatting.BLUE + Translator.translateToLocal("gui.jei.editMode.description.show.mod.id");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, maxWidth));
			} else {
				String description = TextFormatting.BLUE + Translator.translateToLocal("gui.jei.editMode.description.hide.mod.id");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, maxWidth));
			}
		}

		return list;
	}
}

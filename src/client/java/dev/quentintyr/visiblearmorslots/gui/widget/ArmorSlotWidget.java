package dev.quentintyr.visiblearmorslots.gui.widget;

import dev.quentintyr.visiblearmorslots.gui.SlotInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Custom slot widget for armor pieces with validation
 */
public class ArmorSlotWidget {
    private final SlotInfo.SlotType slotType;
    private final int x;
    private final int y;

    // Empty slot background sprites (moved to container/slot/ in 1.21.4)
    private static final Identifier EMPTY_HELMET_SLOT = Identifier.withDefaultNamespace("container/slot/helmet");
    private static final Identifier EMPTY_CHEST_SLOT = Identifier.withDefaultNamespace("container/slot/chestplate");
    private static final Identifier EMPTY_LEGS_SLOT = Identifier.withDefaultNamespace("container/slot/leggings");
    private static final Identifier EMPTY_BOOTS_SLOT = Identifier.withDefaultNamespace("container/slot/boots");

    public ArmorSlotWidget(SlotInfo.SlotType slotType, int x, int y) {
        this.slotType = slotType;
        this.x = x;
        this.y = y;
    }

    public void render(GuiGraphicsExtractor context, ItemStack stack, int mouseX, int mouseY) {
        if (stack.isEmpty()) {
            // Draw empty slot texture
            Identifier emptyTexture = getEmptySlotTexture();
            context.blitSprite(RenderPipelines.GUI_TEXTURED, emptyTexture, x, y, 16, 16);
        } else {
            // Draw item with count (always 1 for armor)
            context.fakeItem(stack, x, y);
            context.itemDecorations(Minecraft.getInstance().font, stack, x, y);
        }
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }

    private Identifier getEmptySlotTexture() {
        return switch (slotType) {
            case HELMET -> EMPTY_HELMET_SLOT;
            case CHESTPLATE -> EMPTY_CHEST_SLOT;
            case LEGGINGS -> EMPTY_LEGS_SLOT;
            case BOOTS -> EMPTY_BOOTS_SLOT;
            default -> EMPTY_HELMET_SLOT;
        };
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public SlotInfo.SlotType getSlotType() {
        return slotType;
    }
}

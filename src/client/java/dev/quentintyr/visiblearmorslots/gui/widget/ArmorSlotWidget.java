package dev.quentintyr.visiblearmorslots.gui.widget;

import dev.quentintyr.visiblearmorslots.gui.SlotInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Custom slot widget for armor pieces with validation
 */
public class ArmorSlotWidget {
    private final SlotInfo.SlotType slotType;
    private final int x;
    private final int y;

    private static final Identifier EMPTY_HELMET_SLOT = Identifier.parse(
            "minecraft:textures/item/empty_armor_slot_helmet.png");
    private static final Identifier EMPTY_CHEST_SLOT = Identifier.parse(
            "minecraft:textures/item/empty_armor_slot_chestplate.png");
    private static final Identifier EMPTY_LEGS_SLOT = Identifier.parse(
            "minecraft:textures/item/empty_armor_slot_leggings.png");
    private static final Identifier EMPTY_BOOTS_SLOT = Identifier.parse(
            "minecraft:textures/item/empty_armor_slot_boots.png");

    public ArmorSlotWidget(SlotInfo.SlotType slotType, int x, int y) {
        this.slotType = slotType;
        this.x = x;
        this.y = y;
    }

    public void render(GuiGraphicsExtractor context, ItemStack stack, int mouseX, int mouseY) {
        if (stack.isEmpty()) {
            // Draw empty slot texture
            Identifier emptyTexture = getEmptySlotTexture();
            context.blit(RenderPipelines.GUI_TEXTURED, emptyTexture, x, y, 0, 0, 16, 16, 16, 16);
        } else {
            // Draw item with count (always 1 for armor)
            context.fakeItem(stack, x, y);
            context.itemDecorations(Minecraft.getInstance().font, stack, x, y);
        }
    }

    public boolean canAcceptItem(ItemStack stack) {
        if (stack.isEmpty())
            return true;

        Item item = stack.getItem();
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) {
            return false;
        }

        EquipmentSlot itemSlot = equippable.slot();
        return itemSlot == slotType.getEquipmentSlot();
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

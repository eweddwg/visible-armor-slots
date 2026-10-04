package dev.quentintyr.visiblearmorslots.gui;

import dev.quentintyr.visiblearmorslots.action.ActionType;
import dev.quentintyr.visiblearmorslots.config.ModConfig;
import dev.quentintyr.visiblearmorslots.gui.widget.ArmorSlotWidget;
import dev.quentintyr.visiblearmorslots.gui.widget.OffhandSlotWidget;
import dev.quentintyr.visiblearmorslots.mixin.client.HandledScreenAccessor;
import dev.quentintyr.visiblearmorslots.network.SlotActionPayload;
import dev.quentintyr.visiblearmorslots.util.KeyCodes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced armor slots overlay system
 */
public class ArmorSlotsOverlay {
    private static final Identifier COLUMN_TEXTURE_FULL = Identifier.parse(
            "visiblearmorslots:textures/gui/extra-slots.png");
    private static final Identifier COLUMN_TEXTURE_COMPACT = Identifier.parse(
            "visiblearmorslots:textures/gui/extra-slots-no-second-hand.png");
    private static final Identifier COLUMN_TEXTURE_FULL_DARK = Identifier.parse(
            "visiblearmorslots:textures/gui/dark-extra-slots.png");
    private static final Identifier COLUMN_TEXTURE_COMPACT_DARK = Identifier.parse(
            "visiblearmorslots:textures/gui/dark-extra-slots-no-second-hand.png");

    private final List<ArmorSlotWidget> armorSlots = new ArrayList<>();
    private OffhandSlotWidget offhandSlot;
    private int baseX, baseY;
    private int columnHeight = 100; // 100 with offhand, 78 without
    private double lastMouseX = -1;
    private double lastMouseY = -1;

    public int getBaseX() {
        return baseX;
    }

    public int getBaseY() {
        return baseY;
    }

    public int getColumnHeight() {
        return columnHeight;
    }

    private boolean visible = false;

    public void initialize(AbstractContainerScreen<?> screen) {
        if (screen == null) {
            dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.warn("Attempted to initialize overlay with null screen");
            visible = false;
            return;
        }

        if (!ModConfig.getInstance().isEnabled()) {
            visible = false;
            return;
        }

        // Don't show on inventory screen
        if (screen instanceof InventoryScreen) {
            visible = false;
            return;
        }

        // Respect allowed container whitelist (use screen handler type registry id if
        // available)
        try {
            MenuType<?> type = screen.getMenu().getType();
            Identifier handlerId = BuiltInRegistries.MENU.getKey(type);
            dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.info("Container opened: {} - Allowed: {}",
                handlerId, ModConfig.getInstance().isContainerAllowed(handlerId));
            if (handlerId != null && !ModConfig.getInstance().isContainerAllowed(handlerId)) {
                visible = false;
                return;
            }
        } catch (Throwable ignored) {
        }

        visible = true;
        columnHeight = ModConfig.getInstance().isShowOffhandSlot() ? 100 : 78;
        calculatePosition(screen);
        createSlots();
    }

    private void calculatePosition(AbstractContainerScreen<?> screen) {
        HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
        int screenLeft = accessor.getLeftPos();
        int screenTop = accessor.getTopPos();
        int screenHeight = accessor.getImageHeight();

        ModConfig config = ModConfig.getInstance();

        // Start from the chosen side, then apply margins and any auto offset
        if (config.getPositioning() == ModConfig.Side.RIGHT) {
            baseX = screenLeft + accessor.getImageWidth() + config.getMarginX();
        } else {
            baseX = screenLeft - 28 - config.getMarginX();

            // Optional extra shift to avoid potion effects overlay (left side only)
            if (config.isAutoPositioning()) {
                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;
                if (player != null && player.hasEffect(MobEffects.REGENERATION)) {
                    baseX -= 24; // shift further left
                }
            }
        }

        // Bottom-align the column regardless of whether the offhand slot is shown.
        // Full column height (with offhand) is 100; compact is 78. We always keep a
        // 4px padding from the bottom (matching previous logic: 104 = 100 + 4).
        // Using (columnHeight + 4) keeps the bottom edge consistent when the height
        // changes.
        baseY = screenTop + screenHeight - (columnHeight + 4) + config.getMarginY();
    }

    private void createSlots() {
        armorSlots.clear();

        int itemX = baseX + 4;

        // Create armor slots from top to bottom
        armorSlots.add(new ArmorSlotWidget(SlotInfo.SlotType.HELMET, itemX, baseY + 4));
        armorSlots.add(new ArmorSlotWidget(SlotInfo.SlotType.CHESTPLATE, itemX, baseY + 22));
        armorSlots.add(new ArmorSlotWidget(SlotInfo.SlotType.LEGGINGS, itemX, baseY + 40));
        armorSlots.add(new ArmorSlotWidget(SlotInfo.SlotType.BOOTS, itemX, baseY + 58));

        // Create offhand slot only if enabled in config
        if (ModConfig.getInstance().isShowOffhandSlot()) {
            offhandSlot = new OffhandSlotWidget(itemX, baseY + 80);
        } else {
            offhandSlot = null;
        }
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (!visible)
            return;

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;

        // Draw column background (choose texture based on offhand visibility and dark mode)
        boolean offhandShown = offhandSlot != null;
        ModConfig config = ModConfig.getInstance();
        Identifier tex;
        if (config.isDarkMode()) {
            tex = offhandShown ? COLUMN_TEXTURE_FULL_DARK : COLUMN_TEXTURE_COMPACT_DARK;
        } else {
            tex = offhandShown ? COLUMN_TEXTURE_FULL : COLUMN_TEXTURE_COMPACT;
        }
        int texHeight = offhandShown ? 100 : 78;
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, baseX, baseY, 0, 0, 24, texHeight, 24, texHeight);

        // Render armor slots with fresh data
        for (int i = 0; i < armorSlots.size(); i++) {
            ArmorSlotWidget slot = armorSlots.get(i);
            ItemStack stack = player.getItemBySlot(slot.getSlotType().getEquipmentSlot());
            slot.render(graphics, stack, mouseX, mouseY);

            // Highlight slot if mouse is over it
            if (slot.isMouseOver(mouseX, mouseY)) {
                graphics.fill(slot.getX(), slot.getY(), slot.getX() + 16, slot.getY() + 16,
                        0x80FFFFFF); // Semi-transparent white overlay
            }
        }

        // Render offhand slot with fresh data if enabled
        if (offhandSlot != null) {
            ItemStack offhandStack = player.getOffhandItem();
            offhandSlot.render(graphics, offhandStack, mouseX, mouseY);

            if (offhandSlot.isMouseOver(mouseX, mouseY)) {
                graphics.fill(offhandSlot.getX(), offhandSlot.getY(),
                        offhandSlot.getX() + 16, offhandSlot.getY() + 16,
                        0x80FFFFFF);
            }
        }
    }

    public void renderTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!visible || !ModConfig.getInstance().shouldShowTooltips())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;

        try {
            // Check armor slots for tooltips
            for (ArmorSlotWidget slot : armorSlots) {
                if (slot.isMouseOver(mouseX, mouseY)) {
                    ItemStack stack = player.getItemBySlot(slot.getSlotType().getEquipmentSlot());
                    if (!stack.isEmpty()) {
                        graphics.setTooltipForNextFrame(mc.font, stack, mouseX, mouseY);
                    } else {
                        // Show empty slot tooltip
                        Component tooltip = Component.translatable("gui.visiblearmorslots.empty." +
                                slot.getSlotType().name().toLowerCase());
                        graphics.setTooltipForNextFrame(mc.font, tooltip, mouseX, mouseY);
                    }
                    return;
                }
            }

            // Check offhand slot
            if (offhandSlot != null && offhandSlot.isMouseOver(mouseX, mouseY)) {
                ItemStack offhandStack = player.getOffhandItem();
                if (!offhandStack.isEmpty()) {
                    graphics.setTooltipForNextFrame(mc.font, offhandStack, mouseX, mouseY);
                } else {
                    Component tooltip = Component.translatable("gui.visiblearmorslots.empty.offhand");
                    graphics.setTooltipForNextFrame(mc.font, tooltip, mouseX, mouseY);
                }
            }
        } catch (Exception e) {
            // Catch tooltip rendering errors to prevent crashes from mod conflicts (e.g., Iceberg)
            dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.debug("Tooltip rendering failed (likely mod conflict): {}", e.getMessage());
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean isShiftPressed, boolean isCtrlPressed) {
        if (!visible)
            return false;

        // Check armor slots
        for (ArmorSlotWidget slot : armorSlots) {
            if (slot.isMouseOver((int) mouseX, (int) mouseY)) {
                handleSlotClick(slot.getSlotType(), isShiftPressed, isCtrlPressed);
                return true;
            }
        }

        // Check offhand slot
        if (offhandSlot != null && offhandSlot.isMouseOver((int) mouseX, (int) mouseY)) {
            handleSlotClick(SlotInfo.SlotType.OFFHAND, isShiftPressed, isCtrlPressed);
            return true;
        }

        // If click is inside overlay column, block vanilla drop logic
        if (mouseX >= baseX && mouseX < baseX + 24 && mouseY >= baseY && mouseY < baseY + columnHeight) {
            return true;
        }

        return false;
    }

    private void handleSlotClick(SlotInfo.SlotType slotType, boolean isShiftPressed, boolean isCtrlPressed) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.screen() == null)
            return;

        ActionType actionType = isShiftPressed ? ActionType.QUICK_TRANSFER : ActionType.MOUSE_SWAP;
        sendSlotAction(actionType, slotType.getEquipmentSlot(), -1, isShiftPressed, isCtrlPressed);
    }

    private void sendSlotAction(ActionType actionType, EquipmentSlot targetSlot,
            int hotbarSlot, boolean isShiftPressed, boolean isCtrlPressed) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.warn("Cannot send slot action - player is null");
            return;
        }

        boolean isCreative = mc.player.getAbilities().instabuild;

        SlotActionPayload payload = new SlotActionPayload(
                actionType, targetSlot, hotbarSlot,
                isShiftPressed, isCtrlPressed, isCreative);

        try {
            ClientPlayNetworking.send(payload);
        } catch (Exception e) {
            dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.error(
                "Failed to send slot action {}: {}", actionType, e.getMessage()
            );
        }
    }

    public boolean keyPressed(int keyCode, boolean isShiftPressed, boolean isCtrlPressed) {
        if (!visible)
            return false;

        // Handle Q key for dropping armor
        if (keyCode == KeyCodes.KEY_Q) {
            // Check which slot the mouse is over (tracked from render)
            for (ArmorSlotWidget slot : armorSlots) {
                if (slot.isMouseOver((int) lastMouseX, (int) lastMouseY)) {
                    sendSlotAction(ActionType.DROP, slot.getSlotType().getEquipmentSlot(), -1, false, false);
                    return true;
                }
            }

            // Check offhand slot
            if (offhandSlot != null && offhandSlot.isMouseOver((int) lastMouseX, (int) lastMouseY)) {
                sendSlotAction(ActionType.DROP, SlotInfo.SlotType.OFFHAND.getEquipmentSlot(), -1, false, false);
                return true;
            }
        }

        // Handle hotbar swapping (keys 1-9)
        if (keyCode >= KeyCodes.KEY_1 && keyCode <= KeyCodes.KEY_9) {
            int hotbarSlot = keyCode - KeyCodes.KEY_1;

            // Check which armor slot the mouse is over
            for (ArmorSlotWidget slot : armorSlots) {
                if (slot.isMouseOver((int) lastMouseX, (int) lastMouseY)) {
                    sendSlotAction(ActionType.HOTBAR_SWAP, slot.getSlotType().getEquipmentSlot(), hotbarSlot, false,
                            false);
                    return true;
                }
            }

            // Check offhand slot
            if (offhandSlot != null && offhandSlot.isMouseOver((int) lastMouseX, (int) lastMouseY)) {
                sendSlotAction(ActionType.HOTBAR_SWAP, SlotInfo.SlotType.OFFHAND.getEquipmentSlot(), hotbarSlot, false,
                        false);
                return true;
            }
        }

        // Handle F key for offhand swap
        if (keyCode == KeyCodes.KEY_F && offhandSlot != null) {
            sendSlotAction(ActionType.OFFHAND_SWAP, SlotInfo.SlotType.OFFHAND.getEquipmentSlot(), -1, false, false);
            return true;
        }

        return false;
    }

    public boolean isVisible() {
        return visible;
    }
}

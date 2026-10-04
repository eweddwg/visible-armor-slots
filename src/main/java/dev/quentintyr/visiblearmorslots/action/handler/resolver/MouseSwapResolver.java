package dev.quentintyr.visiblearmorslots.action.handler.resolver;

import dev.quentintyr.visiblearmorslots.network.SlotActionPayload;
import dev.quentintyr.visiblearmorslots.util.InventoryUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Handles mouse click actions (left/right click swapping)
 */
public class MouseSwapResolver {

    public static void resolve(SlotActionPayload action, ServerPlayer player) {
        if (player == null || player.containerMenu == null) {
            return;
        }
        
        EquipmentSlot targetSlot = action.targetSlot();
        if (targetSlot == null)
            return;

        // Get current item in equipment slot (supports OFFHAND too)
        ItemStack currentEquipped = player.getItemBySlot(targetSlot);

        // Get cursor item
        ItemStack cursorStack = player.containerMenu.getCarried();

        // Validate that the cursor item can be equipped in this slot
        if (!canEquipInSlot(cursorStack, targetSlot)) {
            return; // Invalid item for this slot - do nothing
        }

        // Perform the swap (same logic for both creative and survival)
        performSwap(player, targetSlot, currentEquipped, cursorStack, action.mouseButton());
    }

    private static boolean canEquipInSlot(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) {
            return true; // Can always remove items
        }

        // Check the equippable component for a matching slot (ArmorItem is gone in 26.x)
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
            return equippable.slot() == slot;
        }

        // Allow any item in offhand
        return slot == EquipmentSlot.OFFHAND;
    }

    private static void performSwap(ServerPlayer player, EquipmentSlot slot,
            ItemStack equipped, ItemStack cursor, int button) {
        if (button == 2) {
            // Middle-click clones the stack in creative, does nothing in survival.
            // Game mode is read server-side, never trusted from the payload.
            if (player.isCreative() && cursor.isEmpty() && !equipped.isEmpty()) {
                player.containerMenu.setCarried(equipped.copy());
                InventoryUtil.syncInventoryFull(player);
            }
            return;
        }
        if (button != 0 && button != -1) {
            // Unknown mouse buttons do nothing, like vanilla.
            return;
        }
        if (button == 1) {
            performRightClick(player, slot, equipped.copy(), cursor.copy());
            return;
        }
        if (cursor.isEmpty()) {
            // Pick up the whole stack
            player.setItemSlot(slot, ItemStack.EMPTY);
            player.containerMenu.setCarried(equipped.copy());
        } else if (equipped.isEmpty()) {
            // Place the whole stack
            player.setItemSlot(slot, cursor.copy());
            player.containerMenu.setCarried(ItemStack.EMPTY);
        } else if (ItemStack.isSameItemSameComponents(equipped, cursor)) {
            // Merge into the slot up to its max, remainder stays on cursor
            int room = equipped.getMaxStackSize() - equipped.getCount();
            int move = Math.min(room, cursor.getCount());
            if (move > 0) {
                equipped.grow(move);
                cursor.shrink(move);
            }
            player.setItemSlot(slot, equipped);
            player.containerMenu.setCarried(cursor);
        } else {
            // Different items: full swap
            player.setItemSlot(slot, cursor.copy());
            player.containerMenu.setCarried(equipped.copy());
        }

        // Force inventory sync to client
        InventoryUtil.syncInventoryFull(player);
    }

    /**
     * Vanilla right-click semantics: empty cursor picks up half (rounded up),
     * otherwise place a single item when the slot is empty or mergeable,
     * full swap for mismatched items.
     */
    private static void performRightClick(ServerPlayer player, EquipmentSlot slot,
            ItemStack equipped, ItemStack cursor) {
        if (cursor.isEmpty()) {
            int take = (equipped.getCount() + 1) / 2;
            ItemStack taken = equipped.split(take);
            player.setItemSlot(slot, equipped);
            player.containerMenu.setCarried(taken);
        } else if (equipped.isEmpty()
                || (ItemStack.isSameItemSameComponents(equipped, cursor)
                    && equipped.getCount() < equipped.getMaxStackSize())) {
            ItemStack one = cursor.split(1);
            if (equipped.isEmpty()) {
                player.setItemSlot(slot, one);
            } else {
                equipped.grow(1);
                player.setItemSlot(slot, equipped);
            }
            player.containerMenu.setCarried(cursor);
        } else {
            player.setItemSlot(slot, cursor);
            player.containerMenu.setCarried(equipped);
        }

        // Force inventory sync to client
        InventoryUtil.syncInventoryFull(player);
    }
}

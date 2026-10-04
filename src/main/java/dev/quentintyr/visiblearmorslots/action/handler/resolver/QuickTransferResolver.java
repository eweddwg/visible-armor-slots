package dev.quentintyr.visiblearmorslots.action.handler.resolver;

import dev.quentintyr.visiblearmorslots.network.SlotActionPayload;
import dev.quentintyr.visiblearmorslots.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Handles shift-click actions to move items to inventory
 */
public class QuickTransferResolver {

    public static void resolve(SlotActionPayload action, ServerPlayer player) {
        if (player == null || player.getInventory() == null) {
            return;
        }
        
        EquipmentSlot targetSlot = action.targetSlot();
        if (targetSlot == null)
            return;

        ItemStack equipped = player.getItemBySlot(targetSlot);
        if (equipped.isEmpty())
            return;

        // Move what fits (merging into partial stacks first), leave the rest equipped
        ItemStack remainder = insertIntoMainInventory(player, equipped.copy());
        player.setItemSlot(targetSlot, remainder);

        // Force inventory sync to client
        InventoryUtil.syncInventory(player);
    }

    private static ItemStack insertIntoMainInventory(ServerPlayer player, ItemStack stack) {
        // Returns whatever did not fit. Tries main inventory first, then hotbar,
        // topping up partial stacks before taking empty slots.
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 9; i < 36; i++) { // Skip hotbar, start with main inventory
                if (stack.isEmpty())
                    return stack;
                ItemStack slotStack = player.getInventory().getItem(i);
                if (pass == 0) {
                    if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, stack)) {
                        int move = Math.min(slotStack.getMaxStackSize() - slotStack.getCount(), stack.getCount());
                        if (move > 0) {
                            slotStack.grow(move);
                            stack.shrink(move);
                        }
                    }
                } else if (slotStack.isEmpty()) {
                    player.getInventory().setItem(i, stack.copy());
                    stack.setCount(0);
                    return stack;
                }
            }
        }

        // Try hotbar if main inventory is full: merge first, then empty slots
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < 9; i++) {
                if (stack.isEmpty())
                    return stack;
                ItemStack slotStack = player.getInventory().getItem(i);
                if (pass == 0) {
                    if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, stack)) {
                        int move = Math.min(slotStack.getMaxStackSize() - slotStack.getCount(), stack.getCount());
                        if (move > 0) {
                            slotStack.grow(move);
                            stack.shrink(move);
                        }
                    }
                } else if (slotStack.isEmpty()) {
                    player.getInventory().setItem(i, stack.copy());
                    stack.setCount(0);
                    return stack;
                }
            }
        }

        return stack; // No space available, remainder stays equipped
    }
}

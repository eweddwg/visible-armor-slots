package dev.quentintyr.visiblearmorslots.action.handler.resolver;

import dev.quentintyr.visiblearmorslots.network.SlotActionPayload;
import dev.quentintyr.visiblearmorslots.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * Handles number key swapping with hotbar slots
 */
public class HotbarSwapResolver {

    public static void resolve(SlotActionPayload action, ServerPlayer player) {
        if (player == null || player.getInventory() == null) {
            return;
        }
        
        EquipmentSlot targetSlot = action.targetSlot();
        if (targetSlot == null)
            return;

        int hotbarSlot = action.hotbarSlot();
        if (hotbarSlot < 0 || hotbarSlot > 8)
            return;

        ItemStack equipped = player.getItemBySlot(targetSlot);
        ItemStack hotbarStack = player.getInventory().getStack(hotbarSlot);

        // Validate that the hotbar item can be equipped in this slot
        if (!canEquipInSlot(hotbarStack, targetSlot)) {
            return; // Invalid item for this slot - do nothing
        }

        // Swap the items
        player.setItemSlot(targetSlot, hotbarStack.copy());
        player.getInventory().setStack(hotbarSlot, equipped.copy());

        // Force inventory sync to client
        InventoryUtil.syncInventory(player);
    }

    private static boolean canEquipInSlot(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) {
            return true; // Can always remove items
        }

        // Check if it's armor and matches the slot
        if (stack.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getEquipmentSlot() == slot;
        }

        // Allow non-armor items only in offhand
        return slot == EquipmentSlot.OFFHAND;
    }
}

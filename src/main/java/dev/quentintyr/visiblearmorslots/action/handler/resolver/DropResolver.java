package dev.quentintyr.visiblearmorslots.action.handler.resolver;

import dev.quentintyr.visiblearmorslots.network.SlotActionPayload;
import dev.quentintyr.visiblearmorslots.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Handles dropping equipped armor items (Q key)
 */
public class DropResolver {

    public static void resolve(SlotActionPayload action, ServerPlayer player) {
        if (player == null) {
            return;
        }
        
        EquipmentSlot targetSlot = action.targetSlot();
        if (targetSlot == null)
            return;

        ItemStack equipped = player.getItemBySlot(targetSlot);
        if (equipped.isEmpty())
            return;

        // Drop the equipped item into the world. SERVER_ONLY: our client does not
        // predict this action, so the server must play the swing animation itself
        // (PREDICTED would skip it, used when the client already animated locally).
        player.drop(equipped, false, Prediction.SERVER_ONLY);

        // Clear the equipment slot
        player.setItemSlot(targetSlot, ItemStack.EMPTY);

        // Force inventory sync to client
        InventoryUtil.syncInventory(player);
    }
}

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

        // Vanilla parity: throw works only with an empty cursor.
        if (!player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        
        EquipmentSlot targetSlot = action.targetSlot();
        if (targetSlot == null)
            return;

        ItemStack equipped = player.getItemBySlot(targetSlot).copy();
        if (equipped.isEmpty())
            return;

        // Vanilla parity: Q drops one item, Ctrl+Q drops the whole stack.
        int drop = action.isCtrlPressed() ? equipped.getCount() : 1;
        ItemStack taken = equipped.split(drop);
        player.setItemSlot(targetSlot, equipped);

        // Drop the taken items into the world
        player.drop(taken, false, Prediction.SERVER_ONLY);

        // Force inventory sync to client
        InventoryUtil.syncInventory(player);
    }
}

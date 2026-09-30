package dev.quentintyr.visiblearmorslots.action.handler.resolver;

import dev.quentintyr.visiblearmorslots.network.SlotActionPayload;
import dev.quentintyr.visiblearmorslots.util.InventoryUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles F key swapping with off-hand
 */
public class OffhandSwapResolver {

    public static void resolve(SlotActionPayload action, ServerPlayer player) {
        if (player == null) {
            return;
        }
        
        // For offhand swap, we swap the mainhand with offhand
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        // Perform the swap
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, offHand);
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, mainHand);

        // Force inventory sync to client
        InventoryUtil.syncInventory(player);
    }
}

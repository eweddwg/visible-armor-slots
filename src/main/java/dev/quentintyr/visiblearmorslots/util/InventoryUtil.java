package dev.quentintyr.visiblearmorslots.util;

import net.minecraft.server.level.ServerPlayer;

/**
 * Utility methods for inventory operations
 */
public class InventoryUtil {

    /**
     * Forces a full inventory sync from server to client.
     * This ensures that all inventory changes are immediately visible to the player.
     *
     * All three calls are load-bearing, do not trim: armor slots live in the
     * closed inventoryMenu (never in the open container menu), equipment
     * broadcast packets exclude the player themselves, so this manual push is
     * the only path that updates their own armor client-side.
     *
     * @param player The player whose inventory should be synced
     */
    public static void syncInventory(ServerPlayer player) {
        player.containerMenu.sendAllDataToRemote();
        player.inventoryMenu.sendAllDataToRemote();
        player.containerMenu.broadcastChanges();
    }

    /**
     * Forces a full inventory sync with additional player screen handler notification.
     * Use this when modifying equipment slots or cursor items that may need special handling.
     *
     * @param player The player whose inventory should be synced
     */
    public static void syncInventoryFull(ServerPlayer player) {
        syncInventory(player);
        player.inventoryMenu.slotsChanged(player.getInventory());
    }
}

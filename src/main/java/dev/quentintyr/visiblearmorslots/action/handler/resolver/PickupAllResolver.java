package dev.quentintyr.visiblearmorslots.action.handler.resolver;

import dev.quentintyr.visiblearmorslots.network.SlotActionPayload;
import dev.quentintyr.visiblearmorslots.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Handles double-click collecting: gathers matching stacks from the open
 * container menu into the carried stack, like vanilla PICKUP_ALL.
 */
public class PickupAllResolver {

    public static void resolve(SlotActionPayload action, ServerPlayer player) {
        if (player == null || player.containerMenu == null) {
            return;
        }

        ItemStack carried = player.containerMenu.getCarried();
        if (carried.isEmpty()) {
            return;
        }

        for (int pass = 0; pass < 2; pass++) {
            for (Slot target : player.containerMenu.slots) {
                if (carried.getCount() >= carried.getMaxStackSize()) {
                    break;
                }
                if (!target.hasItem() || !target.mayPickup(player)) {
                    continue;
                }
                ItemStack itemStack = target.getItem();
                if (!ItemStack.isSameItemSameComponents(itemStack, carried)) {
                    continue;
                }
                if (pass != 0 || itemStack.getCount() != itemStack.getMaxStackSize()) {
                    ItemStack removed = target.safeTake(itemStack.getCount(),
                            carried.getMaxStackSize() - carried.getCount(), player);
                    carried.grow(removed.getCount());
                }
            }
        }

        // Force inventory sync to client
        InventoryUtil.syncInventory(player);
    }
}

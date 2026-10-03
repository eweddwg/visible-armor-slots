package dev.quentintyr.visiblearmorslots.network;

import dev.quentintyr.visiblearmorslots.action.ActionType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Typed payload for slot actions used with the new payload registry API.
 */
public record SlotActionPayload(ActionType actionType,
        EquipmentSlot targetSlot,
        int hotbarSlot,
        boolean isShiftPressed,
        boolean isCtrlPressed,
        boolean isCreativeMode) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SlotActionPayload> ID = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath("visiblearmorslots", "slot_action"));

    // Encode/decode all fields so client and server agree on the payload shape.
    // No StreamCodec.of factory in 26.3 — plain anonymous implementation instead.
    public static final StreamCodec<RegistryFriendlyByteBuf, SlotActionPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, SlotActionPayload payload) {
            buf.writeVarInt(payload.actionType().ordinal());
            buf.writeBoolean(payload.targetSlot() != null);
            if (payload.targetSlot() != null) {
                buf.writeVarInt(payload.targetSlot().ordinal());
            }
            buf.writeVarInt(payload.hotbarSlot());
            buf.writeBoolean(payload.isShiftPressed());
            buf.writeBoolean(payload.isCtrlPressed());
            buf.writeBoolean(payload.isCreativeMode());
        }

        @Override
        public SlotActionPayload decode(RegistryFriendlyByteBuf buf) {
            ActionType actionType = ActionType.values()[buf.readVarInt()];
            EquipmentSlot targetSlot = null;
            if (buf.readBoolean()) {
                targetSlot = EquipmentSlot.values()[buf.readVarInt()];
            }
            int hotbarSlot = buf.readVarInt();
            boolean isShiftPressed = buf.readBoolean();
            boolean isCtrlPressed = buf.readBoolean();
            boolean isCreativeMode = buf.readBoolean();
            return new SlotActionPayload(actionType, targetSlot, hotbarSlot, isShiftPressed, isCtrlPressed,
                    isCreativeMode);
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

package dev.quentintyr.visiblearmorslots.network;

import dev.quentintyr.visiblearmorslots.action.ActionType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

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
    public static final StreamCodec<RegistryFriendlyByteBuf, SlotActionPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeEnum(payload.actionType());
                buf.writeBoolean(payload.targetSlot() != null);
                if (payload.targetSlot() != null) {
                    buf.writeEnum(payload.targetSlot());
                }
                buf.writeVarInt(payload.hotbarSlot());
                buf.writeBoolean(payload.isShiftPressed());
                buf.writeBoolean(payload.isCtrlPressed());
                buf.writeBoolean(payload.isCreativeMode());
            },
            buf -> {
                ActionType actionType = buf.readEnum(ActionType.class);
                EquipmentSlot targetSlot = null;
                if (buf.readBoolean()) {
                    targetSlot = buf.readEnum(EquipmentSlot.class);
                }
                int hotbarSlot = buf.readVarInt();
                boolean isShiftPressed = buf.readBoolean();
                boolean isCtrlPressed = buf.readBoolean();
                boolean isCreativeMode = buf.readBoolean();
                return new SlotActionPayload(actionType, targetSlot, hotbarSlot, isShiftPressed, isCtrlPressed,
                        isCreativeMode);
            });

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

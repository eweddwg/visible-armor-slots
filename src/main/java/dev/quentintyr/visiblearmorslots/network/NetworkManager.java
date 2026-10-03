package dev.quentintyr.visiblearmorslots.network;

import dev.quentintyr.visiblearmorslots.action.handler.SlotActionHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Manages network communication between client and server using the typed
 * payload registry API.
 */
public class NetworkManager {

    public static void initialize() {
        try {
            // Register the payload type (server-side C2S)
            PayloadTypeRegistry.serverboundPlay().register(SlotActionPayload.ID, SlotActionPayload.CODEC);

            // Register receiver using the typed payload handler
            ServerPlayNetworking.registerGlobalReceiver(SlotActionPayload.ID, (payload, context) -> {
                context.server().execute(() -> {
                    try {
                        SlotActionHandler.handleAction(payload, context.player());
                    } catch (Exception e) {
                        dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.error(
                            "Error handling slot action {} for player {}: {}",
                            payload.actionType(), context.player().getName().getString(), e.getMessage(), e
                        );
                    }
                });
            });
            
            dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.info("Network handlers registered successfully");
        } catch (Exception e) {
            dev.quentintyr.visiblearmorslots.Visiblearmorslots.LOGGER.error("Failed to register network handlers", e);
            throw new RuntimeException("Failed to initialize network communication", e);
        }
    }
}

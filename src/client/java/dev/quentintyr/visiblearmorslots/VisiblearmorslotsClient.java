package dev.quentintyr.visiblearmorslots;

import dev.quentintyr.visiblearmorslots.config.ModConfig;
import dev.quentintyr.visiblearmorslots.gui.ArmorSlotsOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class VisiblearmorslotsClient implements ClientModInitializer {
    private static ArmorSlotsOverlay armorSlotsOverlay;

    @Override
    public void onInitializeClient() {
        Visiblearmorslots.LOGGER.info("Visible Armor Slots Client initializing...");

        // Load (or create) JSON config
        ModConfig.load();
        ModConfig.save(); // ensure file exists with defaults if first run

        // Initialize overlay
        armorSlotsOverlay = new ArmorSlotsOverlay();

        // Register screen events
        registerScreenEvents();

        Visiblearmorslots.LOGGER.info("Visible Armor Slots Client initialized!");
    }

    private void registerScreenEvents() {
        // Initialize overlay when screen opens
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> handledScreen) {
                armorSlotsOverlay.initialize(handledScreen);
            }
        });
    }

    public static ArmorSlotsOverlay getArmorSlotsOverlay() {
        return armorSlotsOverlay;
    }
}
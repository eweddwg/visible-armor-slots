package dev.quentintyr.visiblearmorslots.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

import dev.quentintyr.visiblearmorslots.config.ModConfig;


public class ContainerListWidget extends ContainerObjectSelectionList<ContainerListWidget.BaseEntry> {

    // Base entry class to satisfy generic type bounds
    public abstract static class BaseEntry extends ContainerObjectSelectionList.Entry<BaseEntry> {
        @Override
        public List<? extends net.minecraft.client.gui.narration.NarratableEntry> narratables() {
            return List.of();
        }

        @Override
        public List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
            return List.of();
        }
    }

    private final ModConfig config;

    private record ContainerDescriptor(Identifier id, String title, List<String> sources) {}

    public ContainerListWidget(Minecraft client, int width, int height, int y, int itemHeight, ModConfig config) {
        super(client, width, height, y, itemHeight);
        this.config = config;
        refreshEntries();
    }

    public void refreshEntries() {
        clearEntries();

        // Vanilla screens
        addEntry(new SectionTitleEntry(Component.literal("Vanilla Containers")));
        for (ContainerDescriptor d : getVanillaContainers()) {
            addEntry(new ContainerEntry(d.id(), d.title(), d.sources()));
        }

        // Modded Screens with (modname) container_name
        addEntry(new SectionTitleEntry(Component.literal("Modded Containers")));
        BuiltInRegistries.MENU.forEach(handler -> {
            Identifier id = BuiltInRegistries.MENU.getKey(handler);
            if (id != null && !"minecraft".equals(id.getNamespace())) {
                addEntry(new ContainerEntry(
                        id,
                        formatModdedTitle(id),
                        List.of("Modded Container")
                ));
            }
        });
    }

    public void setAllEnabled(boolean enabled) {
        children().forEach(entry -> {
            if (entry instanceof ContainerEntry c) c.setEnabled(enabled);
        });
    }

    @Override
    public int getRowWidth() {
        return Math.min(400, getWidth() - 50);
    }

    @Override
    protected int scrollBarX() {
        return getX() + getWidth() - 6;
    }

    private class SectionTitleEntry extends BaseEntry {
        private final Component title;
        public SectionTitleEntry(Component title) { this.title = title; }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   boolean hovered, float tickDelta) {
            int x = getX();
            int y = getY();
            int w = getWidth();
            int h = getHeight();
            graphics.centeredText(minecraft.font, title, x + w / 2, y + (h - 8) / 2, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
            return false; // not clickable
        }
    }

    private class ContainerEntry extends BaseEntry {
        private final Identifier id;
        private boolean enabled;
        private final String title;

        private int lastToggleX, lastToggleY, lastToggleWidth, lastToggleHeight;

        public ContainerEntry(Identifier id, String title, List<String> sources) {
            this.id = id;
            this.title = title;
            this.enabled = config.isContainerInList(id.toString());
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   boolean hovered, float tickDelta) {
            int x = getX();
            int y = getY();
            int h = getHeight();

            // Toggle switch dimensions
            int toggleWidth = 32;
            int toggleHeight = 14;
            int toggleX = x + 5;
            int toggleY = y + (h - toggleHeight) / 2;

            // Store for click detection
            lastToggleX = toggleX;
            lastToggleY = toggleY;
            lastToggleWidth = toggleWidth;
            lastToggleHeight = toggleHeight;

            // Draw toggle background
            int bgColor = enabled ? 0xFF00AA00 : 0xFF555555;
            graphics.fill(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, bgColor);

            // Draw toggle knob
            int knobSize = 10;
            int knobY = toggleY + 2;
            int knobX = enabled ? toggleX + toggleWidth - knobSize - 2 : toggleX + 2;
            graphics.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);

            graphics.text(minecraft.font, title, x + 45, y + (h - 8) / 2, enabled ? 0xFFFFFF : 0x888888, false);
        }

        private void toggle() {
            enabled = !enabled;
            config.setContainerEnabled(id.toString(), enabled);
        }

        public void setEnabled(boolean value) {
            this.enabled = value;
            config.setContainerEnabled(id.toString(), value);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
            double mx = event.x();
            double my = event.y();
            if (mx >= lastToggleX && mx < lastToggleX + lastToggleWidth &&
                my >= lastToggleY && my < lastToggleY + lastToggleHeight) {
                toggle();
                return true;
            }
            return false;
        }
    }

    private static String formatPath(String path) {
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    private static String formatModdedTitle(Identifier id) {
        String modName = formatPath(id.getNamespace());
        String containerName = formatPath(id.getPath());
        return "(" + modName + ") " + containerName;
    }

    private static List<ContainerDescriptor> getVanillaContainers() {
        List<ContainerDescriptor> list = new ArrayList<>();
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:generic_9x3"),
                "Chest / Barrel",
                List.of("Chest", "Trapped Chest", "Barrel")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:generic_9x6"),
                "Large Chest",
                List.of("Large Chest")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:shulker_box"),
                "Shulker Box",
                List.of("Shulker Box")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:crafting"),
                "Crafting Table",
                List.of("Crafting Table")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:anvil"),
                "Anvil",
                List.of("Anvil", "Chipped Anvil", "Damaged Anvil")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:smithing"),
                "Smithing Table",
                List.of("Smithing Table")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:furnace"),
                "Furnace",
                List.of("Furnace")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:blast_furnace"),
                "Blast Furnace",
                List.of("Blast Furnace")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:smoker"),
                "Smoker",
                List.of("Smoker")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:brewing_stand"),
                "Brewing Stand",
                List.of("Brewing Stand")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:enchantment"),
                "Enchanting Table",
                List.of("Enchanting Table")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:grindstone"),
                "Grindstone",
                List.of("Grindstone")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:loom"),
                "Loom",
                List.of("Loom")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:cartography_table"),
                "Cartography Table",
                List.of("Cartography Table")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:stonecutter"),
                "Stonecutter",
                List.of("Stonecutter")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:hopper"),
                "Hopper",
                List.of("Hopper")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:lectern"),
                "Lectern",
                List.of("Lectern")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:beacon"),
                "Beacon",
                List.of("Beacon")
        ));
        list.add(new ContainerDescriptor(
                Identifier.parse("minecraft:merchant"),
                "Trading",
                List.of("Villagers", "Wandering Trader")
        ));
        return list;
    }
}

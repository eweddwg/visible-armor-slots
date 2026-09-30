package dev.quentintyr.visiblearmorslots.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import dev.quentintyr.visiblearmorslots.config.ModConfig;
import dev.quentintyr.visiblearmorslots.config.ContainerListWidget;


public class ConfigScreen extends Screen {

    private final Screen parent;
    private final ModConfig config;

    private enum Category { SETTINGS, CONTAINERS }
    private Category currentCategory = Category.SETTINGS;

    private ContainerListWidget containerListWidget;
    private Button toggleAllButton;
    private boolean allEnabled = true;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("config.visiblearmorslots.title"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }

    @Override
    protected void init() {
        int topBarY = 20;
        int buttonHeight = 20;
        int buttonWidth = 100;
        int gap = 5;
        int startX = (this.width - (buttonWidth * 2 + gap)) / 2;

        Button settingsBtn = Button.builder(
                Component.translatable("config.visiblearmorslots.tab.settings"),
                b -> switchCategory(Category.SETTINGS)
        ).bounds(startX, topBarY, buttonWidth, buttonHeight).build();
        settingsBtn.active = currentCategory != Category.SETTINGS;
        addRenderableWidget(settingsBtn);

        Button containersBtn = Button.builder(
                Component.translatable("config.visiblearmorslots.tab.containers"),
                b -> switchCategory(Category.CONTAINERS)
        ).bounds(startX + buttonWidth + gap, topBarY, buttonWidth, buttonHeight).build();
        containersBtn.active = currentCategory != Category.CONTAINERS;
        addRenderableWidget(containersBtn);

        if (currentCategory == Category.SETTINGS) {
            initSettingsContent();
            addRenderableWidget(
                    Button.builder(Component.translatable("gui.done"), b -> onClose())
                            .bounds(width / 2 - 100, height - 30, 200, 20)
                            .build()
            );
        } else {
            initContainersContent();
        }
    }

    private void switchCategory(Category newCategory) {
        currentCategory = newCategory;
        resize(width, height);
    }

    private void initSettingsContent() {
        GridLayout grid = new GridLayout();
        grid.columnSpacing(10);
        grid.rowSpacing(4);
        GridLayout.RowHelper adder = grid.createRowHelper(1);

        int w = 220;
        int h = 20;

        adder.addChild(CycleButton.onOffBuilder(config.isEnabled())
                .create(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.enabled"),
                        (b, v) -> config.setEnabled(v)));

        adder.addChild(CycleButton.onOffBuilder(config.shouldShowTooltips())
                .create(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.showTooltips"),
                        (b, v) -> config.setShowTooltips(v)));

        adder.addChild(CycleButton.onOffBuilder(config.isShowOffhandSlot())
                .create(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.showOffhand"),
                        (b, v) -> config.setShowOffhandSlot(v)));

        adder.addChild(CycleButton.onOffBuilder(config.isDarkMode())
                .create(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.darkMode"),
                        (b, v) -> config.setDarkMode(v)));

        adder.addChild(CycleButton.onOffBuilder(config.isAutoPositioning())
                .create(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.autoPosition"),
                        (b, v) -> config.setAutoPositioning(v)));

        adder.addChild(CycleButton.builder(s -> Component.literal(s.toString()), config.getPositioning())
                .withValues(ModConfig.Side.LEFT, ModConfig.Side.RIGHT)
                .create(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.side"),
                        (b, s) -> config.setPositioning(s)));

        adder.addChild(new AbstractSliderButton(0, 0, w, h,
                Component.translatable("config.visiblearmorslots.marginX", config.getMarginX()),
                config.getMarginX() / 31.0) {
            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("config.visiblearmorslots.marginX", (int)(value * 31)));
            }
            @Override
            protected void applyValue() {
                config.setMarginX((int)(value * 31));
            }
        });

        adder.addChild(new AbstractSliderButton(0, 0, w, h,
                Component.translatable("config.visiblearmorslots.marginY", config.getMarginY()),
                (config.getMarginY() + 64) / 127.0) {
            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("config.visiblearmorslots.marginY", (int)(value * 127) - 64));
            }
            @Override
            protected void applyValue() {
                config.setMarginY((int)(value * 127) - 64);
            }
        });

        grid.arrangeElements();
        FrameLayout.centerInRectangle(grid, 0, 50, width, height - 80);
        grid.visitChildren(element -> {
            if (element instanceof AbstractWidget widget) {
                addRenderableWidget(widget);
            }
        });
    }

    private void initContainersContent() {
        int listTop = 50;
        int listBottom = height - 60;

        containerListWidget = new ContainerListWidget(
                minecraft, width, listBottom - listTop, listTop, 25, config
        );
        addRenderableWidget(containerListWidget);

        int bw = 100;
        int spacing = 5;
        int startX = (width - (bw * 3 + spacing * 2)) / 2;
        int y = height - 28;

        toggleAllButton = Button.builder(
                Component.empty(),
                b -> {
                    allEnabled = !allEnabled;
                    containerListWidget.setAllEnabled(allEnabled);
                    updateToggleAllButton();
                }).bounds(startX, y, bw, 20).build();
        addRenderableWidget(toggleAllButton);
        updateToggleAllButton();

        addRenderableWidget(Button.builder(
                Component.translatable("config.visiblearmorslots.containers.reset"),
                b -> {
                    config.resetContainersToDefault();
                    containerListWidget.refreshEntries();
                }).bounds(startX + bw + spacing, y, bw, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                b -> onClose()
        ).bounds(startX + (bw + spacing) * 2, y, bw, 20).build());
    }

    private void updateToggleAllButton() {
        toggleAllButton.setMessage(Component.translatable(
                allEnabled
                        ? "config.visiblearmorslots.containers.disableAll"
                        : "config.visiblearmorslots.containers.enableAll"
        ));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(font, title, width / 2, 8, 0xFFFFFF);

        if (currentCategory == Category.CONTAINERS) {
            graphics.centeredText(
                    font,
                    Component.translatable("config.visiblearmorslots.containers.help").getString(),
                    width / 2,
                    height - 45,
                    0x808080
            );
        }
    }

    @Override
    public void onClose() {
        ModConfig.save();
        if (minecraft != null) {
            minecraft.gui.setScreen(parent);
        }
    }
}

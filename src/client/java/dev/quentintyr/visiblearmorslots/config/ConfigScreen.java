package dev.quentintyr.visiblearmorslots.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSlider;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.GridLayout;
import net.minecraft.client.gui.components.SimpleLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
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
        ).pos(startX, topBarY).size(buttonWidth, buttonHeight).build();
        settingsBtn.active = currentCategory != Category.SETTINGS;
        addRenderableWidget(settingsBtn);

        Button containersBtn = Button.builder(
                Component.translatable("config.visiblearmorslots.tab.containers"),
                b -> switchCategory(Category.CONTAINERS)
        ).pos(startX + buttonWidth + gap, topBarY).size(buttonWidth, buttonHeight).build();
        containersBtn.active = currentCategory != Category.CONTAINERS;
        addRenderableWidget(containersBtn);

        if (currentCategory == Category.SETTINGS) {
            initSettingsContent();
            addRenderableWidget(
                    Button.builder(CommonComponents.GUI_DONE, b -> close())
                            .pos(width / 2 - 100, height - 30).size(200, 20)
                            .build()
            );
        } else {
            initContainersContent();
        }
    }

    private void switchCategory(Category newCategory) {
        currentCategory = newCategory;
        resize(minecraft, width, height);
    }

    private void initSettingsContent() {
        GridLayout grid = new GridLayout();
        grid.getMainPositioner().marginX(10).marginY(4);
        GridLayout.Adder adder = grid.createAdder(1);

        int w = 220;
        int h = 20;

        adder.add(CycleButton.onOffBuilder(config.isEnabled())
                .build(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.enabled"),
                        (b, v) -> config.setEnabled(v)));

        adder.add(CycleButton.onOffBuilder(config.shouldShowTooltips())
                .build(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.showTooltips"),
                        (b, v) -> config.setShowTooltips(v)));

        adder.add(CycleButton.onOffBuilder(config.isShowOffhandSlot())
                .build(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.showOffhand"),
                        (b, v) -> config.setShowOffhandSlot(v)));

        adder.add(CycleButton.onOffBuilder(config.isDarkMode())
                .build(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.darkMode"),
                        (b, v) -> config.setDarkMode(v)));

        adder.add(CycleButton.onOffBuilder(config.isAutoPositioning())
                .build(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.autoPosition"),
                        (b, v) -> config.setAutoPositioning(v)));

        adder.add(CycleButton.<ModConfig.Side>builder(s -> Component.literal(s.toString()))
                .withValues(ModConfig.Side.LEFT, ModConfig.Side.RIGHT)
                .withInitialValue(config.getPositioning())
                .build(0, 0, w, h,
                        Component.translatable("config.visiblearmorslots.side"),
                        (b, s) -> config.setPositioning(s)));

        adder.add(new AbstractSlider(0, 0, w, h,
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

        adder.add(new AbstractSlider(0, 0, w, h,
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

        grid.refreshPositions();
        SimpleLayout.setPosition(grid, 0, 50, width, height - 80, 0.5f, 0f);
        grid.forEachChild(this::addRenderableWidget);
    }

    private void initContainersContent() {
        int listTop = 50;
        int listBottom = height - 60;

        containerListWidget = new ContainerListWidget(
                minecraft, width, listBottom - listTop, listTop, 25, config
        );
        addDrawableChild(containerListWidget);

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
                }).pos(startX, y).size(bw, 20).build();
        addRenderableWidget(toggleAllButton);
        updateToggleAllButton();

        addRenderableWidget(Button.builder(
                Component.translatable("config.visiblearmorslots.containers.reset"),
                b -> {
                    config.resetContainersToDefault();
                    containerListWidget.refreshEntries();
                }).pos(startX + bw + spacing, y).size(bw, 20).build());

        addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                b -> close()
        ).pos(startX + (bw + spacing) * 2, y).size(bw, 20).build());
    }

    private void updateToggleAllButton() {
        toggleAllButton.setMessage(Component.translatable(
                allEnabled
                        ? "config.visiblearmorslots.containers.disableAll"
                        : "config.visiblearmorslots.containers.enableAll"
        ));
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        super.renderBackground(ctx, mx, my, delta);
        super.render(ctx, mx, my, delta);
        
        ctx.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        
        if (currentCategory == Category.CONTAINERS) {
            ctx.drawCenteredString(
                    font,
                    Component.translatable("config.visiblearmorslots.containers.help").getString(),
                    width / 2,
                    height - 45,
                    0x808080
            );
        }
    }

    @Override
    public void close() {
        ModConfig.save();
        if (minecraft != null) {
            minecraft.gui.setScreen(parent);
        }
    }
}
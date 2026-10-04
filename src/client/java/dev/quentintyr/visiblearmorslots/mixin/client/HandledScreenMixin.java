package dev.quentintyr.visiblearmorslots.mixin.client;

import dev.quentintyr.visiblearmorslots.VisiblearmorslotsClient;
import dev.quentintyr.visiblearmorslots.gui.ArmorSlotsOverlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class HandledScreenMixin {

    @Unique
    private Boolean vas$lastRecipeOpen = null;

    @Unique
    private boolean vas$isRecipeBookOpen() {
        Object self = this;
        // 26.3: AbstractRecipeBookScreen holds private recipeBookComponent with isVisible().
        try {
            java.lang.reflect.Field field = findRecipeBookField(self.getClass());
            if (field != null) {
                field.setAccessible(true);
                Object widget = field.get(self);
                if (widget != null) {
                    java.lang.reflect.Method isVisible = widget.getClass().getMethod("isVisible");
                    Object result = isVisible.invoke(widget);
                    if (result instanceof Boolean b)
                        return b;
                }
            }
        } catch (Throwable ignored) {
        }
        // Legacy: mapped getter with isOpen().
        // Mojang name is getRecipeBookComponent, Yarn-era name getRecipeBookWidget.
        for (String getter : new String[] { "getRecipeBookComponent", "getRecipeBookWidget" }) {
            try {
                java.lang.reflect.Method getWidget = self.getClass().getMethod(getter);
                Object widget = getWidget.invoke(self);
                if (widget != null) {
                    java.lang.reflect.Method isOpen = widget.getClass().getMethod("isOpen");
                    Object result = isOpen.invoke(widget);
                    if (result instanceof Boolean b)
                        return b;
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    @Unique
    private static java.lang.reflect.Field findRecipeBookField(Class<?> cls) {
        for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField("recipeBookComponent");
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("RETURN"))
    private void onExtract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if ((Object) this instanceof InventoryScreen) {
            return;
        }

        // Refresh overlay when recipe book toggles
        boolean openNow = vas$isRecipeBookOpen();
        if (vas$lastRecipeOpen == null || vas$lastRecipeOpen != openNow) {
            vas$lastRecipeOpen = openNow;
            ArmorSlotsOverlay overlayRef = VisiblearmorslotsClient.getArmorSlotsOverlay();
            if (overlayRef != null) {
                overlayRef.initialize((AbstractContainerScreen<?>) (Object) this);
            }
        }

        // Do not render overlay while recipe book is open
        if (openNow) {
            return;
        }

        ArmorSlotsOverlay overlay = VisiblearmorslotsClient.getArmorSlotsOverlay();
        if (overlay != null && overlay.isVisible()) {
            overlay.render(graphics, mouseX, mouseY, delta);
            overlay.renderTooltips(graphics, mouseX, mouseY);
        }
    }

    @Inject(method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof InventoryScreen) {
            return;
        }

        // While recipe book is open, ignore overlay interactions
        if (vas$isRecipeBookOpen()) {
            return;
        }

        ArmorSlotsOverlay overlay = VisiblearmorslotsClient.getArmorSlotsOverlay();
        if (overlay != null && overlay.mouseClicked(event.x(), event.y(), event.button(), event.hasShiftDown(), event.hasControlDown())) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "mouseReleased(Lnet/minecraft/client/input/MouseButtonEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void onMouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof InventoryScreen) {
            return;
        }

        if (vas$isRecipeBookOpen()) {
            return;
        }

        ArmorSlotsOverlay overlay = VisiblearmorslotsClient.getArmorSlotsOverlay();
        if (overlay != null && overlay.isVisible()) {
            // Block mouse release events over overlay to prevent drops
            if (event.x() >= overlay.getBaseX() && event.x() < overlay.getBaseX() + 24 &&
                    event.y() >= overlay.getBaseY() && event.y() < overlay.getBaseY() + overlay.getColumnHeight()) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }

    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof InventoryScreen) {
            return;
        }

        if (vas$isRecipeBookOpen()) {
            return;
        }

        ArmorSlotsOverlay overlay = VisiblearmorslotsClient.getArmorSlotsOverlay();
        if (overlay != null && overlay.keyPressed(event.key(), event.hasShiftDown(), event.hasControlDown())) {
            cir.setReturnValue(true);
        }
    }
}

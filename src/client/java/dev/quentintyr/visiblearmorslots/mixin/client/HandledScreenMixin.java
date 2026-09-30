package dev.quentintyr.visiblearmorslots.mixin.client;

import dev.quentintyr.visiblearmorslots.VisiblearmorslotsClient;
import dev.quentintyr.visiblearmorslots.gui.ArmorSlotsOverlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
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
        if (self instanceof AbstractRecipeBookScreen<?> recipeBookScreen) {
            // Preferred: accessor mixin (stable under remap)
            try {
                RecipeBookComponent<?> component = ((RecipeBookScreenAccessor) recipeBookScreen)
                        .visiblearmorslots$getRecipeBookComponent();
                if (component != null) {
                    return component.isVisible();
                }
            } catch (Throwable ignored) {
            }
            // Fallback: reflection for environments where the accessor isn't registered
            try {
                java.lang.reflect.Field field = AbstractRecipeBookScreen.class
                        .getDeclaredField("recipeBookComponent");
                field.setAccessible(true);
                Object component = field.get(recipeBookScreen);
                if (component instanceof RecipeBookComponent<?> recipeBook) {
                    return recipeBook.isVisible();
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("RETURN"))
    private void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
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
    private void onMouseClicked(MouseButtonEvent event, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof InventoryScreen) {
            return;
        }

        // While recipe book is open, ignore overlay interactions
        if (vas$isRecipeBookOpen()) {
            return;
        }

        ArmorSlotsOverlay overlay = VisiblearmorslotsClient.getArmorSlotsOverlay();
        if (overlay != null && overlay.mouseClicked(event.x(), event.y(), event.button())) {
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
            double mouseX = event.x();
            double mouseY = event.y();
            if (mouseX >= overlay.getBaseX() && mouseX < overlay.getBaseX() + 24 &&
                    mouseY >= overlay.getBaseY() && mouseY < overlay.getBaseY() + overlay.getColumnHeight()) {
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
        if (overlay != null && overlay.keyPressed(event.key(), 0, event.modifiers())) {
            cir.setReturnValue(true);
        }
    }
}

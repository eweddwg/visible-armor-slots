package dev.quentintyr.visiblearmorslots.util;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * Key constants for hotkeys. SDL scancodes via vanilla InputConstants
 * (GLFW values are dead since 26.3).
 */
public class KeyCodes {
    // Number keys (positional scancodes, layout-independent)
    public static final int KEY_1 = InputConstants.KEY_1;
    public static final int KEY_2 = InputConstants.KEY_2;
    public static final int KEY_3 = InputConstants.KEY_3;
    public static final int KEY_4 = InputConstants.KEY_4;
    public static final int KEY_5 = InputConstants.KEY_5;
    public static final int KEY_6 = InputConstants.KEY_6;
    public static final int KEY_7 = InputConstants.KEY_7;
    public static final int KEY_8 = InputConstants.KEY_8;
    public static final int KEY_9 = InputConstants.KEY_9;

    // Action keys (positional scancodes)
    public static final int KEY_Q = InputConstants.KEY_Q;
    public static final int KEY_F = InputConstants.KEY_F;

    private KeyCodes() {
        // Utility class, no instantiation
    }
}

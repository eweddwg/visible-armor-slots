package dev.quentintyr.visiblearmorslots.action;

/**
 * Represents different types of slot actions
 */
public enum ActionType {
    MOUSE_SWAP,
    QUICK_TRANSFER, // Shift-click
    HOTBAR_SWAP, // Number key
    DROP, // Q key
    PICKUP_ALL // Double-click, appended last to keep old ordinals stable
}

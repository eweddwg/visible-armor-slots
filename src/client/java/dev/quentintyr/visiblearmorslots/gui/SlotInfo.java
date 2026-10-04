package dev.quentintyr.visiblearmorslots.gui;

import net.minecraft.world.entity.EquipmentSlot;

/**
 * Armor and offhand slot types
 */
public class SlotInfo {

    public enum SlotType {
        HELMET(EquipmentSlot.HEAD),
        CHESTPLATE(EquipmentSlot.CHEST),
        LEGGINGS(EquipmentSlot.LEGS),
        BOOTS(EquipmentSlot.FEET),
        OFFHAND(EquipmentSlot.OFFHAND);

        private final EquipmentSlot equipmentSlot;

        SlotType(EquipmentSlot equipmentSlot) {
            this.equipmentSlot = equipmentSlot;
        }

        public EquipmentSlot getEquipmentSlot() {
            return equipmentSlot;
        }
    }
}

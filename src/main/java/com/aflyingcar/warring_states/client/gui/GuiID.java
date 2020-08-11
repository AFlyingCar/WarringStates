package com.aflyingcar.warring_states.client.gui;

public enum GuiID {
    INVALID,
    STATE_CREATION_GUI,
    STATE_MANAGER_GUI,
    CONFIRM_APPLY_CITIZENSHIP,
    DIPLOMACY_GUI
    ;

    private final static GuiID[] values = GuiID.values();

    public static GuiID fromInt(int id) {
        // 0 == INVALID
        return (id > 0 && id < values.length) ? values[id] : INVALID;
    }
}

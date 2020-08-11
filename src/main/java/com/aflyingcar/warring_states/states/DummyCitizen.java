package com.aflyingcar.warring_states.states;

import com.aflyingcar.warring_states.util.PlayerUtils;

import java.util.UUID;

public class DummyCitizen {
    private final UUID citizenID;
    private int privileges;
    private final String name;

    public DummyCitizen(UUID citizenID, int privileges) {
        this(citizenID, privileges, PlayerUtils.getPlayerNameFromUUID(citizenID));
    }

    public DummyCitizen(UUID citizenID, int privileges, String name) {
        this.citizenID = citizenID;
        this.privileges = privileges;
        this.name = name;
    }

    public int getPrivileges() {
        return privileges;
    }

    public String getName() {
        return name;
    }

    public UUID getCitizenID() {
        return citizenID;
    }

    public void setPrivileges(int privilegeValue) {
        privileges = privilegeValue;
    }
}

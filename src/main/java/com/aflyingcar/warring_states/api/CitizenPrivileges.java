package com.aflyingcar.warring_states.api;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The privileges that a citizen can have
 */
public class CitizenPrivileges {
    public final static int NONE            = 0b0000000;
    public final static int CLAIM_TERRITORY = 0b0000001;
    public final static int DECLARE_WAR     = 0b0000010;
    public final static int STEAL_TERRITORY = 0b0000100;
    public final static int MODIFY_BLOCKS   = 0b0001000;
    public final static int INTERACT        = 0b0010000;
    public final static int MANAGEMENT      = 0b0100000; // Renaming, changing flags, etc...
    public final static int RECRUITMENT     = 0b1000000;

    public final static int ALL = CLAIM_TERRITORY | DECLARE_WAR | STEAL_TERRITORY | MODIFY_BLOCKS | INTERACT | MANAGEMENT | RECRUITMENT;

    private final static int[] values = {
            CLAIM_TERRITORY, DECLARE_WAR, STEAL_TERRITORY, MODIFY_BLOCKS, INTERACT, MANAGEMENT, RECRUITMENT
    };

    public static int[] values() {
        return values;
    }

    private static String translate(String privilegeString) {
        return I18n.format("warring_states.privilege_level." + privilegeString);
    }

    @SideOnly(Side.CLIENT)
    public static String toString(int privilege) {
        String s = "";

        if(privilege == 0)
            return translate("none");
        else if((privilege & ALL) == ALL)
            return translate("full");

        boolean appended = false;
        if((privilege & CLAIM_TERRITORY) != 0) {
            s += translate("claim_territory");
            appended = true;
        }
        if((privilege & DECLARE_WAR) != 0) {
            if(appended) s += ", ";
            s += translate("declare_war");
            appended = true;
        }
        if((privilege & STEAL_TERRITORY) != 0) {
            if(appended) s += ", ";
            s += translate("steal_territory");
            appended = true;
        }
        if((privilege & MODIFY_BLOCKS) != 0) {
            if(appended) s += ", ";
            s += translate("modify_blocks");
            appended = true;
        }
        if((privilege & INTERACT) != 0) {
            if(appended) s += ", ";
            s += translate("interact");
            appended = true;
        }
        if((privilege & MANAGEMENT) != 0) {
            if(appended) s += ", ";
            s += translate("management");
            appended = true;
        }
        if((privilege & RECRUITMENT) != 0) {
            if(appended) s += ", ";
            s += translate("recruitment");
            appended = true;
        }

        return s;
    }
}

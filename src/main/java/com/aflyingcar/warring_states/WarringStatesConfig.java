package com.aflyingcar.warring_states;

import com.aflyingcar.warring_states.api.CitizenPrivileges;
import net.minecraftforge.common.config.Config;

@SuppressWarnings("CanBeFinal")
@Config(modid = WarringStatesMod.MOD_ID)
public class WarringStatesConfig {
    @Config.Comment("Should flight be disabled during a war?")
    public static boolean disableFlightDuringWar = true;

    @Config.Comment("Amount of time the defenders in a war have to wait before automatically winning in minutes.")
    public static int defenderWaitVictoryMaxTime = 20;

    @Config.Comment("Should creative players be allowed to ignore territory protections?")
    public static boolean allowCreativeToIgnoreProtections = false;

    @Config.Comment("Should block placement actually be blocked. DEBUGGING FEATURE! LEAVE ALONE UNLESS YOU KNOW WHAT YOU ARE DOING.")
    public static boolean shouldBlockPlacementBeBlocked = true;

    @Config.Comment("Should block breakage actually be blocked. DEBUGGING FEATURE! LEAVE ALONE UNLESS YOU KNOW WHAT YOU ARE DOING.")
    public static boolean shouldBlockBreakageBeBlocked = true;

    @Config.Comment("Should block interaction actually be blocked. DEBUGGING FEATURE! LEAVE ALONE UNLESS YOU KNOW WHAT YOU ARE DOING.")
    public static boolean shouldBlockInteractionBeBlocked = true;

    @Config.Comment("How much health does the defender's claimer have.")
    public static int defenderClaimerHealth = 50;

    @Config.Comment("How much health must the belligerents provide to the newly captured claimer to finish the claiming process.")
    public static int belligerentStealHealth = 50;

    @Config.Comment("Should claimers attempt to self-correct when a disparity is discovered between which State owns a given chunk of territory?")
    public static boolean correctClaimerManagerOwnershipDisparity = true;

    @Config.Comment("Should people be prevented from unclaiming their capitals?")
    public static boolean preventUnclaimingCapital = true;

    @Config.Comment("Should we ignore the breaking and placing of blocks with tile entities during a war?")
    public static boolean shouldBlockModificationOfTileEntitiesBeIgnoredDuringWar = true;

    @Config.Comment("Should changes during a war be rolled back?")
    public static boolean shouldChangesBeRolledBack = true;

    @Config.Comment("Should explosions be protected against?")
    public static boolean shouldProtectAgainstExplosions = true;

    @Config.Comment("Should tile entity interactions be protected against during war?")
    public static boolean shouldTileEntityInteractionsBeIgnoredDuringWar = false;

    @Config.Comment("Should mobs be allowed to harm things inside of claimed territory?")
    public static boolean canMobsHarmInClaimedTerritory = true;

    @Config.Comment("Should debug functionality be enabled so that claimers can be harvested manually.")
    public static boolean enableDebugClaimerHarvesting = false;

    @Config.Comment("Should support for rolling back trampled crops be enabled?")
    public static boolean enableExperimentalCropRollbackSupport = false;

    @Config.Comment("Should crops be protected even during a war? Will be ignored if enableExperimentalCropRollback is set to true.")
    public static boolean shouldCropsBeProtectedEvenDuringWar = false;

    @Config.Comment("Should State leaders with sufficient privileges be allowed to dissolve states with members.")
    public static boolean allowDissolvingStatesWithMembers = true;

    @Config.Comment("What privileges should be allowed for newly applied citizens?")
    public static int defaultNewlyAppliedPrivileges = CitizenPrivileges.MODIFY_BLOCKS | CitizenPrivileges.INTERACT;

    @Config.Comment("How many days should from the last login before a State's claims start to deteriorate.")
    public static int numberOfDaysBeforeClaimDecayBegins = 14; // 2 weeks

    @Config.Comment("How many hours should pass between each chunk decay?")
    public static int numberOfHoursBetweenEachChunkDecay = 48; // 2 days

    @Config.Comment("Should territory be sanity checked on startup?")
    public static boolean performTerritorySanityCheckOnStartup = false;

    @Config.Comment("Should problems be fixed during the startup sanity check. Does nothing if performTerritorySanityCheckOnStartup is false.")
    public static boolean shouldProblemsBeFixedDuringSanityCheck = true;

    @Config.Comment("How long should should you have to wait in between war attempts.")
    public static int numHoursBetweenWarAttempts = 48;

    @Config.Comment("Minimum amount of experience required for declaring a steal chunk wargoal.")
    public static float minimumExperienceRequiredForStealingChunks = 0;
}

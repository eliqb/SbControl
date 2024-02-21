package me.eliab.sbcontrol.enums;

/**
 * Enum representing options for team behavior that can be enabled or disabled.
 * <p>
 * The {@code FriendlyFlags} define various aspects of team interaction and visibility among team members.
 * </p>
 */
public enum FriendlyFlags {

    /**
     * No special team options are enabled.
     */
    NONE,

    /**
     * Enables friendly fire, allowing players on the same team to inflict damage on each other.
     * Does not affect some non-player entities in a team.
     * <p><strong>Note:</strong> Players can still inflict status effects on each other.</p>
     */
    ALLOW_FRIENDLY_FIRE,

    /**
     * Enables the visibility of invisible players on the same team, rendering them semi-transparent.
     */
    CAN_SEE_INVISIBLE_FRIENDS,

    /**
     * Combines all team-related flags: {@code ALLOW_FRIENDLY_FIRE}, {@code CAN_SEE_INVISIBLE_FRIENDS}.
     */
    ALL

}

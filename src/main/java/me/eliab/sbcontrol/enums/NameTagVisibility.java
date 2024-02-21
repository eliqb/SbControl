package me.eliab.sbcontrol.enums;

/**
 * A team option that decides whose name tags above their heads can be seen.
 */
public enum NameTagVisibility {

    /**
     * <p><strong>(Default)</strong></p>
     * Name above player's head can be seen by all the players.
     */
    ALWAYS("always"),

    /**
     * Name above player's head can be seen only by players in the same team.
     */
    HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),

    /**
     * Name above player's head cannot be seen by all the players in the same team.
     */
    HIDE_FOR_OWN_TEAM("hideForOwnTeam"),

    /**
     * Name above player's head cannot be seen by any players.
     */
    NEVER("never");

    private final String value;

    NameTagVisibility(String value) {
        this.value = value;
    }

    /**
     * Returns the string enum for this NameTagVisibility constant.
     * @return the string enum
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the NameTagVisibility constant by its string enum.
     * @param value the string enum
     * @return the NameTagVisibility constant
     * @throws IllegalArgumentException if the value is null, or if it does not belong to any NameTagVisibility constant
     */
    public static NameTagVisibility byValue(String value) {

        if (value == null) {
            throw new IllegalArgumentException("Cannot get NameTagVisibility from null value");
        }

        switch (value) {
            case "always": return ALWAYS;
            case "hideForOtherTeams": return HIDE_FOR_OTHER_TEAMS;
            case "hideForOwnTeam": return HIDE_FOR_OWN_TEAM;
            case "never": return NEVER;
            default: throw new IllegalArgumentException("Invalid NameTagVisibility value");
        }

    }

}

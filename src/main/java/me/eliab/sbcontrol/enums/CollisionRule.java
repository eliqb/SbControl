package me.eliab.sbcontrol.enums;

/**
 * A team option that controls the way the entities on the team collide with other entities.
 */
public enum CollisionRule {

    /**
     * <p><strong>(Default)</strong></p>
     * Normal collision.
     */
    ALWAYS("always"),

    /**
     * Entities in this team can be pushed only by other entities in the same team.
     */
    PUSH_OTHER_TEAMS("pushOtherTeams"),

    /**
     * Entities in this team cannot be pushed by another entity in this team.
     */
    PUSH_OWN_TEAM("pushOwnTeam"),

    /**
     * No entities can push entities in this team.
     */
    NEVER("never");

    private final String value;

    CollisionRule(String value) {
        this.value = value;
    }

    /**
     * Returns the string enum for this CollisionRule constant.
     * @return the string enum
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the CollisionRule constant by its string enum.
     * @param value the string enum
     * @return the CollisionRule constant
     * @throws IllegalArgumentException if the value is null, or if it does not belong to any CollisionRule constant
     */
    public static CollisionRule byValue(String value) {

        if (value == null) {
            throw new IllegalArgumentException("Cannot get CollisionRule from null value");
        }

        switch (value) {
            case "always": return ALWAYS;
            case "pushOtherTeams": return PUSH_OTHER_TEAMS;
            case "pushOwnTeam": return PUSH_OWN_TEAM;
            case "never": return NEVER;
            default: throw new IllegalArgumentException("Invalid CollisionRule value");
        }

    }

}

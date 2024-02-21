package me.eliab.sbcontrol.network.packets;

import com.google.common.collect.ImmutableList;
import me.eliab.sbcontrol.enums.CollisionRule;
import me.eliab.sbcontrol.enums.FriendlyFlags;
import me.eliab.sbcontrol.enums.NameTagVisibility;
import me.eliab.sbcontrol.enums.PacketSbType;
import me.eliab.sbcontrol.network.Packet;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.Collections;

/**
 * This packet is sent when it should create and update teams.
 */
public abstract class PacketTeam implements Packet {

    protected String teamName;
    protected Mode mode;
    protected String teamDisplayName = "";
    protected FriendlyFlags friendlyFlags = FriendlyFlags.NONE;
    protected NameTagVisibility nameTagVisibility = NameTagVisibility.ALWAYS;
    protected CollisionRule collisionRule = CollisionRule.ALWAYS;
    protected ChatColor teamColor = ChatColor.WHITE;
    protected String teamPrefix = "";
    protected String teamSuffix = "";
    protected Collection<String> entities = Collections.emptyList();

    /**
     * Sets the unique name for the team, which is shared with the scoreboard.
     * @param teamName A unique name for the team.
     */
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    /**
     * Sets the mode to be performed by the packet.
     * @param mode The mode of the packet.
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Sets the display name for the team.
     * @param teamDisplayName The display name for the team.
     */
    public void setTeamDisplayName(String teamDisplayName) {
        this.teamDisplayName = teamDisplayName;
    }

    /**
     * Sets the friendly flags for the team.
     * @param friendlyFlags The flags for the team.
     */
    public void setFriendlyFlags(FriendlyFlags friendlyFlags) {
        this.friendlyFlags = friendlyFlags;
    }

    /**
     * Sets the name tag visibility for players on this team.
     * @param nameTagVisibility The name tag visibility for the team.
     */
    public void setNameTagVisibility(NameTagVisibility nameTagVisibility) {
        this.nameTagVisibility = nameTagVisibility;
    }

    /**
     * Sets the collision rule for players on this team.
     * @param collisionRule The collision rule for the team.
     */
    public void setCollisionRule(CollisionRule collisionRule) {
        this.collisionRule = collisionRule;
    }

    /**
     * Sets the color for the team, used to color the names of players on the team.
     * @param teamColor The color for the team.
     */
    public void setTeamColor(ChatColor teamColor) {
        this.teamColor = (teamColor == ChatColor.RESET) ? ChatColor.WHITE : teamColor;
    }

    /**
     * Sets the prefix displayed before the names of players on this team.
     * @param teamPrefix The prefix for the team.
     */
    public void setTeamPrefix(String teamPrefix) {
        this.teamPrefix = teamPrefix;
    }

    /**
     * Sets the suffix displayed after the names of players on this team.
     * @param teamSuffix The suffix for the team.
     */
    public void setTeamSuffix(String teamSuffix) {
        this.teamSuffix = teamSuffix;
    }

    /**
     * Sets the identifiers of entities to be added or removed for this team.
     * For players, use their usernames; for other entities, use their UUIDs.
     *
     * @param entities The identifiers of entities to be used.
     */
    public void setEntities(Collection<String> entities) {
        this.entities = ImmutableList.copyOf(entities);
    }

    /**
     * Retrieves the unique name for the team, shared with the scoreboard.
     * @return The unique name of the team.
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Retrieves the mode to be performed by the packet.
     * @return The mode of the packet.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Retrieves the display name for the team.
     * @return The display name of the team.
     */
    public String getTeamDisplayName() {
        return teamDisplayName;
    }

    /**
     * Retrieves the friendly flags for the team.
     * @return The flags for the team.
     */
    public FriendlyFlags getFriendlyFlags() {
        return friendlyFlags;
    }

    /**
     * Retrieves the name tag visibility for players on this team.
     * @return The name tag visibility for the team.
     */
    public NameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    /**
     * Retrieves the collision rule for players on this team.
     * @return The collision rule for the team.
     */
    public CollisionRule getCollisionRule() {
        return collisionRule;
    }

    /**
     * Retrieves the color for the team, used to color the names of players on the team.
     * @return The color of the team.
     */
    public ChatColor getTeamColor() {
        return teamColor;
    }

    /**
     * Retrieves the prefix displayed before the names of players on this team.
     * @return The prefix of the team.
     */
    public String getTeamPrefix() {
        return teamPrefix;
    }

    /**
     * Retrieves the suffix displayed after the names of players on this team.
     * @return The suffix of the team.
     */
    public String getTeamSuffix() {
        return teamSuffix;
    }

    /**
     * Retrieves the identifiers of entities to be added or removed for this team.
     * For players, use their usernames; for other entities, use their UUIDs.
     *
     * @return The identifiers of entities to be used.
     */
    public Collection<String> getEntities() {
        return entities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PacketSbType getPacketSbType() {
        return PacketSbType.TEAM;
    }

    public enum Mode {

        CREATE,
        REMOVE,
        UPDATE,
        ADD_ENTITIES,
        REMOVE_ENTITIES

    }

}

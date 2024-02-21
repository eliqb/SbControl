package me.eliab.sbcontrol;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import me.eliab.sbcontrol.enums.CollisionRule;
import me.eliab.sbcontrol.enums.NameTagVisibility;
import me.eliab.sbcontrol.network.versions.Version;
import me.eliab.sbcontrol.util.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a team in a scoreboard.
 * <p><strong>Note:</strong> It can be safely used asynchronously as everything is at packet level.</p>
 */
public class Team extends BoardComponent {

    private final String name;
    private String displayName;
    private boolean allowFriendlyFire = false;
    private boolean canSeeInvisibleFriends = false;
    private NameTagVisibility nameTagVisibility = NameTagVisibility.ALWAYS;
    private CollisionRule collisionRule = CollisionRule.ALWAYS;
    private ChatColor color = ChatColor.WHITE;
    private String prefix = "";
    private String suffix = "";
    private final Set<String> entities = new HashSet<>();

    Team(Board board, String name) {

        super(board);
        Preconditions.checkArgument(name != null, "Team cannot be created with null name");
        checkLengthForVersion(Version.V1_20, name, 16, "Team name cannot be larger than 16 characters");

        this.name = name;
        displayName = name;
        board.onTeamCreate(name, this);

    }

    /**
     * Destroys this team, removing it from the associated board. After calling this method
     * the team becomes unusable and calling any method will throw {@code IllegalStateException}.
     *
     * @throws IllegalStateException If the team has already been destroyed.
     */
    @Override
    public synchronized void destroy() {
        checkState();
        board.onTeamRemove(name, this);
    }

    /**
     * Sets the display name of the team.
     *
     * <p>
     * It automatically converts the color codes and hex colors, found in the string using the color code '&amp;',
     * to its chat format.
     * </p>
     *
     * @param displayName The new display name.
     * @throws IllegalArgumentException If the display name is null.
     *                                  If the display name is larger than 32 characters
     *                                  <strong>(only in 1.12 version)</strong>.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public void setDisplayName(String displayName) {

        checkState();
        Preconditions.checkArgument(displayName != null, "Team " + name + " cannot have null display name");
        checkLengthForVersion(Version.V1_12, displayName, 32, "Team " + name + " cannot have display name larger than 32 characters");

        this.displayName = ChatUtils.setColors(displayName);
        board.onTeamUpdate(this);

    }

    /**
     * Enables or disables friendly fire. When {@code true} allows players on the same team to inflict damage on each other.
     * Does not affect some non-player entities in a team.
     * <p><strong>Note:</strong> Players can still inflict status effects on each other.</p>
     *
     * @param flag True to allow friendly fire, false otherwise.
     * @throws IllegalStateException If the team has been destroyed.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public void allowFriendlyFire(boolean flag) {
        checkState();
        this.allowFriendlyFire = flag;
    }

    /**
     * Enables or disables the visibility of invisible players on the same team, rendering them semi-transparent.
     *
     * @param flag True to allow visibility of invisible friends, false otherwise.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public void canSeeInvisibleFriends(boolean flag) {
        checkState();
        this.canSeeInvisibleFriends = flag;
    }

    /**
     * Sets the name tag visibility for the team.
     *
     * @param nameTagVisibility The name tag visibility setting.
     * @throws IllegalArgumentException If the nameTagVisibility is null.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public void setNameTagVisibility(NameTagVisibility nameTagVisibility) {

        checkState();
        Preconditions.checkArgument(nameTagVisibility != null, "Team " + name + " cannot have null name tag visibility");

        this.nameTagVisibility = nameTagVisibility;
        board.onTeamUpdate(this);

    }

    /**
     * Sets the collision rule for the team.
     *
     * @param collisionRule The collision rule setting.
     * @throws IllegalArgumentException If the collisionRule is null.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public void setCollisionRule(CollisionRule collisionRule) {

        checkState();
        Preconditions.checkArgument(collisionRule != null, "Team " + name + " cannot have null collision rule");

        this.collisionRule = collisionRule;
        board.onTeamUpdate(this);

    }

    /**
     * Sets the color of the team used to color the names of players on the team.
     * The {@link ChatColor#RESET} will reset the color back to white.
     *
     * @param color The ChatColor representing the team color.
     * @throws IllegalArgumentException If the color is null.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public void setColor(ChatColor color) {
        checkState();
        Preconditions.checkArgument(color != null, "Team " + name + " cannot have null color");

        this.color = (color == ChatColor.RESET) ? ChatColor.WHITE : color;
        board.onTeamUpdate(this);
    }

    /**
     * Sets the prefix displayed before the names of players on this team.
     *
     * <p>
     * It automatically converts the color codes and hex colors, found in the string using the color code '&amp;',
     * to its chat format.
     * </p>
     *
     * @param prefix The prefix for the team.
     * @throws IllegalArgumentException If the prefix is null.
     *                                  If the prefix is larger than 16 characters
     *                                  <strong>(only in 1.12 version)</strong>.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public void setPrefix(String prefix) {

        checkState();
        Preconditions.checkArgument(prefix != null, "Team " + name + " cannot have null prefix");
        checkLengthForVersion(Version.V1_12, prefix, 16, "Team '" + name + "' cannot have a prefix larger than 16 characters");

        this.prefix = ChatUtils.setColors(prefix);
        board.onTeamUpdate(this);

    }

    /**
     * Sets the suffix displayed after the names of players on this team.
     *
     * <p>
     * It automatically converts the color codes and hex colors, found in the string using the color code '&amp;',
     * to its chat format.
     * </p>
     *
     * @param suffix The suffix for the team.
     * @throws IllegalArgumentException If the suffix is null.
     *                                  If the suffix is larger than 16 characters
     *                                  <strong>(only in 1.12 version)</strong>.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public void setSuffix(String suffix) {

        checkState();
        Preconditions.checkArgument(suffix != null, "Team " + name + " cannot have null suffix");
        checkLengthForVersion(Version.V1_12, suffix, 16, "Team '" + name + "' cannot have a suffix larger than 16 characters");

        this.suffix = ChatUtils.setColors(suffix);
        board.onTeamUpdate(this);

    }

    /**
     * Adds a player to the team.
     *
     * @param player The player to add.
     * @throws IllegalArgumentException If the player is null.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public void addPlayer(Player player) {
        checkState();
        Preconditions.checkArgument(player != null, "Team " + name + " cannot add null player");
        addEntity(player.getName());
    }

    /**
     * Adds an entity to the team. For players, use their usernames; for other entities, use their UUIDs.
     *
     * @param entityName The name of the entity to be added.
     * @throws IllegalArgumentException If the entity name is null.
     *                                  If the entity is already in the team.
     *                                  If the entity name is larger than 40 characters
     *                                  <strong>(only in 1.20 or lower versions)</strong>.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public void addEntity(String entityName) {

        checkState();
        Preconditions.checkArgument(entityName != null, "Team " + name + " cannot add null entity name");
        checkLengthForVersion(Version.V1_20, entityName, 40, "Team '" + name + "' cannot add entity with name larger than 40 characters");
        Preconditions.checkArgument(board.getEntityTeam(entityName) == null, "Team '" + name + "' cannot add '" + entityName + "' because entity is in another team");

        entities.add(entityName);
        board.onTeamAddEntity(this, entityName);

    }

    /**
     * Removes a player from the team.
     *
     * @param player The player to remove.
     * @throws IllegalArgumentException If the player is null.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public void removePlayer(Player player) {
        checkState();
        Preconditions.checkArgument(player != null, "Team " + name + " cannot remove null player");
        removeEntity(player.getName());
    }

    /**
     * Removes an entity from the team. For players, use their usernames; for other entities, use their UUIDs.
     *
     * @param entityName The name of the entity to be removed.
     * @throws IllegalArgumentException If the entity name is null.
     *                                  If the entity is not in the team.
     *                                  If the entity name is larger than 40 characters
     *                                  <strong>(only in 1.20 or lower versions)</strong>.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public void removeEntity(String entityName) {

        checkState();
        Preconditions.checkArgument(entityName != null, "Team " + name + " cannot remove null entity name");
        checkLengthForVersion(Version.V1_20, entityName, 40, "Team '" + name + "' cannot remove entity with name larger than 40 characters");
        Preconditions.checkArgument(board.getEntityTeam(entityName) == this, "Team '" + name + "' cannot remove '" + entityName + "' because entity is not in this team");

        entities.remove(entityName);
        board.onTeamRemoveEntity(this, entityName);

    }

    /**
     * Checks if the team has a specific player.
     *
     * @param player The player to check.
     * @return True if the entity is in the team, false otherwise.
     * @throws IllegalArgumentException If the player is null.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public boolean hasPlayer(Player player) {
        checkState();
        Preconditions.checkArgument(player != null, "Team  " + name + " cannot have null player");
        return entities.contains(player.getName());
    }

    /**
     * Checks if the team has a specific entity.
     *
     * @param entityName The name of the entity to check.
     * @return True if the entity is in the team, false otherwise.
     * @throws IllegalArgumentException If the entity name is null.
     * @throws IllegalStateException    If the team has been destroyed.
     */
    public boolean hasEntity(String entityName) {
        checkState();
        Preconditions.checkArgument(entityName != null, "Team  " + name + " cannot have null entity");
        return entities.contains(entityName);
    }

    /**
     * Retrieves the name of the team.
     *
     * @return The name of the team.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public String getName() {
        checkState();
        return name;
    }

    /**
     * Retrieves the display name of the team.
     *
     * @return The display name of the team.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public String getDisplayName() {
        checkState();
        return displayName;
    }

    /**
     * Checks if friendly fire is allowed for the team.
     *
     * @return True if friendly fire is allowed, false otherwise.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public boolean allowFriendlyFire() {
        checkState();
        return allowFriendlyFire;
    }

    /**
     * Checks if team members can see invisible friends.
     *
     * @return True if members can see invisible friends, false otherwise.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public boolean canSeeInvisibleFriends() {
        checkState();
        return canSeeInvisibleFriends;
    }

    /**
     * Retrieves the name tag visibility setting for the team.
     *
     * @return The name tag visibility setting.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public NameTagVisibility getNameTagVisibility() {
        checkState();
        return nameTagVisibility;
    }

    /**
     * Retrieves the collision rule setting for the team.
     *
     * @return The collision rule setting.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public CollisionRule getCollisionRule() {
        checkState();
        return collisionRule;
    }

    /**
     * Retrieves the color of the team used to color the names of players on the team.
     *
     * @return The ChatColor representing the team color.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public ChatColor getColor() {
        checkState();
        return color;
    }

    /**
     * Retrieves the prefix displayed before the names of players on this team.
     *
     * @return The prefix of the team.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public String getPrefix() {
        checkState();
        return prefix;
    }

    /**
     * Retrieves the suffix displayed after the names of players on this team.
     *
     * @return The suffix of the team.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public String getSuffix() {
        checkState();
        return suffix;
    }

    /**
     * Retrieves an immutable set with the entities that are part of the team.
     *
     * @return A set of the entity names in the team.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public Set<String> getEntities() {
        checkState();
        return ImmutableSet.copyOf(entities);
    }

    /**
     * Checks if the team is empty (has no entities).
     *
     * @return True if the team is empty, false otherwise.
     * @throws IllegalStateException If the team has been destroyed.
     */
    public boolean isEmpty() {
        checkState();
        return entities.isEmpty();
    }

    @Override
    protected void checkState() {
        if (board.getTeam(name) == null) {
            throw new IllegalStateException("Unregistered board component");
        }
    }

}

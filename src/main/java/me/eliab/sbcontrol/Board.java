package me.eliab.sbcontrol;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import me.eliab.sbcontrol.enums.FriendlyFlags;
import me.eliab.sbcontrol.enums.Position;
import me.eliab.sbcontrol.network.Packet;
import me.eliab.sbcontrol.network.PacketManager;
import me.eliab.sbcontrol.network.packets.*;
import me.eliab.sbcontrol.network.versions.Version;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a scoreboard manager for objectives, teams, and scores for a group of players.
 * This class facilitates the creation, modification, and removal of scoreboard elements.
 * The integration with SbControl ensures version compatibility and smooth functionality across
 * different Minecraft versions.
 *
 * <p><strong>Note:</strong> It can be safely used asynchronously as everything is at packet level.</p>
 */
public class Board {

    private static final PacketManager PACKET_MANAGER = SbControl.getPacketManager();
    private static final Version VERSION = SbControl.getVersion();

    private final Set<UUID> players = new HashSet<>();
    private final Map<String, Objective> objectivesByName = new HashMap<>();
    private final Map<String, Map<String, Score>> scoresByObjective = new HashMap<>();
    private final Map<Position, Objective> displaying = new EnumMap<>(Position.class);
    private final Map<String, Team> teamsByName = new HashMap<>();
    private final Map<String, Team> teamsByEntity = new HashMap<>();

    /**
     * Creates a new scoreboard manager with no initial players.
     */
    public Board() {}

    /**
     * Creates a new scoreboard manager with the specified players.
     * @param players A varargs array of players to initialize the scoreboard with.
     */
    public Board(Player... players) {
        this(Arrays.asList(players));
    }

    /**
     * Creates a new scoreboard manager with the specified players.
     * @param players A collection of players to initialize the scoreboard with.
     */
    public Board(Collection<Player> players) {

        this.players.addAll(players.stream()
                .map(Entity::getUniqueId)
                .collect(Collectors.toSet()));

        players.forEach(player -> SbControl.setPlayerBoard(player, this));

    }

    /**
     * Adds a player to the scoreboard, initializing the scoreboard elements.
     *
     * @param player The player to be added to the scoreboard.
     * @throws IllegalArgumentException If the player is null, or if it has already been added to the scoreboard.
     */
    public void addPlayer(Player player) {

        Preconditions.checkArgument(player != null, "Board cannot add null player");
        Preconditions.checkArgument(players.add(player.getUniqueId()), "Board cannot add an already added player");
        SbControl.setPlayerBoard(player, this);

        List<Packet> packets = new ArrayList<>();

        objectivesByName.values().forEach(
                objective -> packets.add(createPacketObjective(objective, PacketObjective.Mode.CREATE))
        );

        teamsByName.values().forEach(
                team -> packets.add(createPacketTeam(team, PacketTeam.Mode.CREATE, team.getEntities()))
        );

        scoresByObjective.values().forEach(
                scores -> scores.values().forEach(
                        score -> packets.add(createPacketScore(score, PacketScore.Action.UPDATE))
                )
        );

        displaying.forEach(
                (position, objective) -> packets.add(createPacketDisplayObjective(position, objective.getName()))
        );

        if (!packets.isEmpty()) {
            PACKET_MANAGER.sendPacket(player, packets.toArray(new Packet[0]));
        }

    }

    /**
     * Removes a player from the scoreboard, clearing the associated elements.
     *
     * @param player The player to be removed from the scoreboard.
     * @throws IllegalArgumentException If the player is null, or if it has not been added to the scoreboard.
     */
    public void removePlayer(Player player) {

        Preconditions.checkArgument(player != null, "Board cannot remove null player");
        Preconditions.checkArgument(players.remove(player.getUniqueId()), "Board cannot remove a player that has not been added");
        SbControl.removePlayerBoard(player, this);

        List<Packet> packets = new ArrayList<>();

        displaying.keySet().forEach(position -> createPacketDisplayObjective(position, ""));

        scoresByObjective.values().forEach(
                scores -> scores.values().forEach(
                        score -> createPacketScore(score, PacketScore.Action.REMOVE)
                )
        );

        objectivesByName.values().forEach(
                objective -> packets.add(createPacketObjective(objective, PacketObjective.Mode.REMOVE))
        );

        teamsByName.values().forEach(

                team -> {
                    if (!team.getEntities().isEmpty()) {
                        packets.add( createPacketTeam(team, PacketTeam.Mode.REMOVE_ENTITIES, team.getEntities()) );
                    }
                    packets.add(createPacketTeam(team, PacketTeam.Mode.REMOVE));
                }

        );

        if (!packets.isEmpty()) {
            PACKET_MANAGER.sendPacket(player, packets.toArray(new Packet[0]));
        }

    }

    /**
     * Creates a new objective with the specified name.
     *
     * @param name The name of the objective.
     * @return The created Objective instance.
     * @throws IllegalArgumentException If the objective name is null.
     *                                  If another objective with the same name already exists.
     *                                  If the objective name is larger than 16 characters
     *                                  <strong>(only in 1.20 or lower versions)</strong>.
     */
    public Objective createObjective(String name) {
        Preconditions.checkArgument(name != null, "Objective name cannot be null");
        Preconditions.checkArgument(!objectivesByName.containsKey(name), "Board cannot created objective with an already used name '" + name + "'");
        return new Objective(this, name);
    }

    /**
     * Creates a new team with the specified name.
     *
     * @param name The name of the team.
     * @return The created Team instance.
     * @throws IllegalArgumentException If the team name is null.
     *                                  If another team with the same name already exists.
     *                                  If the team name is larger than 16 characters
     *                                  <strong>(only in 1.20 or lower versions)</strong>.
     */
    public Team createTeam(String name) {
        Preconditions.checkArgument(name != null, "Team name cannot be null");
        Preconditions.checkArgument(!teamsByName.containsKey(name), "Board cannot created team with an already used name '" + name + "'");
        return new Team(this, name);
    }

    /**
     * Clears the objective displayed at the specified position.
     *
     * @param position The position for which to clear the objective.
     * @throws IllegalArgumentException If the position is null.
     */
    public void clearPosition(Position position) {

        Preconditions.checkArgument(position != null, "Board cannot clear null position");

        if (getObjectiveByPosition(position) != null) {
            onObjectiveDisplay(position, null);
        }

    }

    /**
     * Retrieves the Objective instance with the specified name.
     *
     * @param name The name of the objective to retrieve.
     * @return The Objective instance associated with the provided name, or null if not found.
     */
    public Objective getObjective(String name) {
        return objectivesByName.get(name);
    }

    /**
     * Retrieves the Objective instance displayed at the specified position.
     *
     * @param position The position for which to retrieve the displayed objective.
     * @return The Objective instance displayed at the specified position, or null if not set.
     */
    public Objective getObjectiveByPosition(Position position) {
        return displaying.get(position);
    }

    /**
     * Retrieves the Team instance with the specified name.
     *
     * @param name The name of the team to retrieve.
     * @return The Team instance associated with the provided name, or null if not found.
     */
    public Team getTeam(String name) {
        return teamsByName.get(name);
    }

    /**
     * Retrieves the Team instance associated with the specified entity name.
     *
     * @param entityName The name of the entity for which to retrieve the associated team.
     * @return The Team instance associated with the specified entity name, or null if not found.
     */
    public Team getEntityTeam(String entityName) {
        return teamsByEntity.get(entityName);
    }

    /**
     * Retrieves an immutable set of unique player UUIDs associated with this scoreboard.
     * @return An immutable set of player UUIDs.
     */
    public Set<UUID> getPlayers() {
        return ImmutableSet.copyOf(players);
    }

    /**
     * Retrieves an immutable set of Objective instances associated with this scoreboard.
     * @return An immutable set of Objective instances.
     */
    public Set<Objective> getObjectives() {
        return ImmutableSet.copyOf(objectivesByName.values());
    }

    /**
     * Retrieves an immutable set of Team instances associated with this scoreboard.
     * @return An immutable set of Team instances.
     */
    public Set<Team> getTeams() {
        return ImmutableSet.copyOf(teamsByName.values());
    }

    private void sendPacketAll(Packet packet) {

        for (UUID uuid : players) {
            // checks if player is online
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                PACKET_MANAGER.sendPacket(player, packet);
            }
        }

    }

    void onObjectiveCreate(String objectiveName, Objective objective, Map<String, Score> scores) {
        objectivesByName.put(objectiveName, objective);
        scoresByObjective.put(objectiveName, scores);
        sendPacketAll(createPacketObjective(objective, PacketObjective.Mode.CREATE));
    }

    void onObjectiveUpdate(Objective objective) {
        sendPacketAll(createPacketObjective(objective, PacketObjective.Mode.UPDATE));
    }

    void onObjectiveDisplay(Position position, Objective objective) {

        String objectiveName;

        if (objective != null) {
            displaying.put(position, objective);
            objectiveName = objective.getName();
        } else {
            displaying.remove(position);
            objectiveName = "";
        }

        sendPacketAll(createPacketDisplayObjective(position, objectiveName));

    }

    void onObjectiveRemove(String objectiveName, Objective objective) {
        objectivesByName.remove(objectiveName);
        scoresByObjective.remove(objectiveName);
        sendPacketAll(createPacketObjective(objective, PacketObjective.Mode.REMOVE));
    }

    void onTeamCreate(String teamName, Team team) {
        teamsByName.put(teamName, team);
        sendPacketAll(createPacketTeam(team, PacketTeam.Mode.CREATE));
    }

    void onTeamUpdate(Team team) {
        sendPacketAll(createPacketTeam(team, PacketTeam.Mode.UPDATE));
    }

    void onTeamAddEntity(Team team, String entityName) {
        teamsByEntity.put(entityName, team);
        sendPacketAll( createPacketTeam(team, PacketTeam.Mode.ADD_ENTITIES, Collections.singleton(entityName)) );
    }

    void onTeamRemoveEntity(Team team, String entityName) {
        teamsByEntity.remove(entityName);
        sendPacketAll( createPacketTeam(team, PacketTeam.Mode.REMOVE_ENTITIES, Collections.singleton(entityName)) );
    }

    void onTeamRemove(String teamName, Team team) {
        teamsByName.remove(teamName);
        if (!team.isEmpty()) {
            sendPacketAll( createPacketTeam(team, PacketTeam.Mode.REMOVE_ENTITIES, team.getEntities()) );
        }
        sendPacketAll(createPacketTeam(team, PacketTeam.Mode.REMOVE));
    }

    void onScoreCreate(Objective objective, String entityName, Score score) {
        scoresByObjective.get(objective.getName()).put(entityName, score);
        onScoreUpdate(score);
    }

    void onScoreUpdate(Score score) {
        sendPacketAll(createPacketScore(score, PacketScore.Action.UPDATE));
    }

    void onScoreRemove(Objective objective, String entityName, Score score) {
        scoresByObjective.get(objective.getName()).remove(entityName);
        sendPacketAll(createPacketScore(score, PacketScore.Action.REMOVE));
    }

    private static PacketDisplayObjective createPacketDisplayObjective(Position position, String objectiveName) {
        PacketDisplayObjective packet = PACKET_MANAGER.createPacketDisplayObjective();
        packet.setPosition(position);
        packet.setScoreName(objectiveName);
        return packet;
    }

    private static PacketObjective createPacketObjective(Objective objective, PacketObjective.Mode mode) {

        PacketObjective packet = PACKET_MANAGER.createPacketObjective();
        packet.setObjectiveName(objective.getName());
        packet.setMode(mode);

        if (mode != PacketObjective.Mode.REMOVE) {

            packet.setObjectiveValue(objective.getDisplayName());
            packet.setType(objective.getRenderType());

            if (VERSION.isHigherOrEqualThan(Version.V1_20_3)) {
                packet.setNumberFormat(objective.getNumberFormat());
            }

        }
        return packet;

    }

    private static PacketTeam createPacketTeam(Team team, PacketTeam.Mode mode) {
        return createPacketTeam(team, mode, null);
    }

    private static PacketTeam createPacketTeam(Team team, PacketTeam.Mode mode, Collection<String> entities) {

        PacketTeam packet = PACKET_MANAGER.createPacketTeam();
        packet.setTeamName(team.getName());
        packet.setMode(mode);

        if (mode == PacketTeam.Mode.CREATE || mode == PacketTeam.Mode.UPDATE) {

            packet.setTeamDisplayName(team.getDisplayName());

            int flags = 0;
            if (team.allowFriendlyFire()) flags |= 1;
            if (team.canSeeInvisibleFriends()) flags |= 2;
            packet.setFriendlyFlags(FriendlyFlags.values()[flags]);

            packet.setNameTagVisibility(team.getNameTagVisibility());
            packet.setCollisionRule(team.getCollisionRule());
            packet.setTeamColor(team.getColor());
            packet.setTeamPrefix(team.getPrefix());
            packet.setTeamSuffix(team.getSuffix());

            if (mode == PacketTeam.Mode.CREATE && entities != null) {
                packet.setEntities(entities);
            }

        } else if ((mode == PacketTeam.Mode.ADD_ENTITIES || mode == PacketTeam.Mode.REMOVE_ENTITIES) && entities != null) {
            packet.setEntities(entities);
        }

        return packet;

    }

    private static Packet createPacketScore(Score score, PacketScore.Action action) {

        if (action == PacketScore.Action.UPDATE) {

            PacketScore packet = createPacketScoreCommon(score);
            packet.setValue(score.getScore());

            if (VERSION.isHigherOrEqualThan(Version.V1_20_3)) {
                packet.setDisplayName(score.getDisplayName());
                packet.setNumberFormat(score.getNumberFormat());
            } else {
                packet.setAction(action);
            }

            return packet;

        } else {

            if (VERSION.isHigherOrEqualThan(Version.V1_20_3 )) {
                return createPacketResetScore(score);
            } else {
                PacketScore packet = createPacketScoreCommon(score);
                packet.setAction(action);
                return packet;
            }

        }

    }

    private static PacketScore createPacketScoreCommon(Score score) {
        PacketScore packet = PACKET_MANAGER.createPacketScore();
        packet.setEntityName(score.getEntityName());
        packet.setObjectiveName(score.getObjective().getName());
        return packet;
    }

    private static PacketResetScore createPacketResetScore(Score score) {
        PacketResetScore packet = PACKET_MANAGER.createPacketResetScore();
        packet.setEntityName(score.getEntityName());
        packet.setObjectiveName(score.getObjective().getName());
        return packet;
    }

}

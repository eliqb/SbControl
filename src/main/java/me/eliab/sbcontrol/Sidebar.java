package me.eliab.sbcontrol;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import me.eliab.sbcontrol.enums.Position;
import me.eliab.sbcontrol.enums.RenderType;
import me.eliab.sbcontrol.network.PacketManager;
import me.eliab.sbcontrol.network.packets.*;
import me.eliab.sbcontrol.network.versions.Version;
import me.eliab.sbcontrol.numbers.BlankFormat;
import me.eliab.sbcontrol.util.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * The {@code Sidebar} class facilitates the management of Minecraft scoreboards for players, supporting up to 15 lines.
 * It provides an intuitive set of methods for creating, updating, and removing sidebars, helping developers to effortlessly
 * present information within a player's sidebar.
 *
 * <p>
 * Usage of this class involves creating an instance for a specific player, setting the title and lines as needed. You can toggle displaying
 * or hiding the sidebar dynamically. The integration with SbControl ensures version compatibility and smooth functionality across
 * different Minecraft protocol versions.
 * </p>
 *
 * <p><strong>Note:</strong> It can be safely used asynchronously as everything is at packet level.</p>
 */
public class Sidebar {

    private static final int MAX_LINES = 15;
    private static final PacketManager PACKET_MANAGER = SbControl.getPacketManager();
    private static final SidebarManager SIDEBAR_MANAGER;

    static {

        Version version = SbControl.getVersion();

        if (version.isHigherOrEqualThan(Version.V1_20_3)) {
            SIDEBAR_MANAGER = new SidebarManager_v1_20_3();
        } else if (version.isHigherOrEqualThan(Version.V1_13)) {
            SIDEBAR_MANAGER = new SidebarManager_v1_13();
        } else {
            SIDEBAR_MANAGER = new SidebarManager_v1_12();
        }

    }

    private final UUID uuid;
    private final Channel channel;
    private String title = "";
    private final String[] lines = new String[MAX_LINES];
    private boolean deleted = false;

    /**
     * Constructs a Sidebar for the specified player.
     *
     * @param player The Player for which the sidebar is created.
     * @throws IllegalArgumentException If the player is null.
     */
    public Sidebar(Player player) {

        Preconditions.checkArgument(player != null, "Sidebar cannot have null player");

        uuid = player.getUniqueId();
        channel = PACKET_MANAGER.getPlayerChannel(player);

        SIDEBAR_MANAGER.sendObjectivePacket(channel, PacketObjective.Mode.CREATE, title);
        SIDEBAR_MANAGER.sendDisplayPacket(channel, true);

    }

    /**
     * Removes the sidebar and all its elements from the player. After calling this method
     * the sidebar becomes unusable and calling any of methods will throw {@code IllegalStateException}.
     *
     * @throws IllegalStateException If the sidebar has been deleted.
     */
    public synchronized void destroy() {

        checkState();

        removeLines();
        SIDEBAR_MANAGER.sendObjectivePacket(channel, PacketObjective.Mode.REMOVE, null);

        deleted = true;

    }

    /**
     * Sets the title of the sidebar.
     *
     * <p>
     * It automatically converts the color codes and hex colors, found in the string using the color code '&amp;',
     * to its chat format.
     * </p>
     *
     * @param title The display name.
     * @throws IllegalArgumentException If the title value is null.
     *                                  If the title is larger than 32 characters
     *                                  <strong>(only in 1.12 version)</strong>.
     * @throws IllegalStateException    If the sidebar has been deleted.
     */
    public void setTitle(String title) {

        checkState();
        Preconditions.checkArgument(title != null, "Sidebar set title with null value");

        this.title = ChatUtils.setColors(title);
        SIDEBAR_MANAGER.sendObjectivePacket(channel, PacketObjective.Mode.UPDATE, this.title);

    }

    /**
     * Sets the lines for the sidebar in the order they are given from top to bottom.
     * If there were lines already set, it will remove them. If a value is null, it will not display that line,
     * but you can later display it using {@link #setLine(int, String)}
     *
     * <p>
     * It automatically converts the color codes and hex colors, found in the strings using the color code '&amp;',
     * to its chat format.
     * </p>
     *
     * @param lines A varargs array of Strings representing the lines to display.
     * @throws IllegalArgumentException If the array is null.
     *                                  If the array length is higher than 14.
     *                                  If a line value is larger than 32 characters
     *                                  <strong>(only in 1.12 version)</strong>.
     * @throws IllegalStateException    If the sidebar has been deleted.
     */
    public void setLines(String... lines) {
        Preconditions.checkArgument(lines != null, "Sidebar cannot set lines from null array");
        setLines(Arrays.asList(lines));
    }

    /**
     * Sets the lines for the sidebar from a Collection in the same order they are from top to bottom.
     * If there were lines already set, it will remove them. If a value is null, it will not display that line,
     * but you can later display it using {@link #setLine(int, String)}
     *
     * <p>
     * It automatically converts the color codes and hex colors, found in the strings using the color code '&amp;',
     * to its chat format.
     * </p>
     *
     * @param lines A Collection of Strings representing the lines to display.
     * @throws IllegalArgumentException If the Collection is null.
     *                                  If the Collection size is higher than 14.
     *                                  If a line value is larger than 32 characters
     *                                  <strong>(only in 1.12 version)</strong>.
     * @throws IllegalStateException    If the sidebar has been deleted.
     */
    public synchronized void setLines(Collection<String> lines) {

        checkState();
        Preconditions.checkArgument(lines != null, "Sidebar cannot set lines from null Collection");

        int index = lines.size() - 1;
        checkIndex(index);

        removeLines();

        for (String value : lines) {

            if (value != null) {
                setLine(index, value);
            }

            index--;

        }

    }


    /**
     * Sets the value of a line at the specified index on the sidebar. If the line does not exist, it will be created.
     *
     * <p>
     * It automatically converts the color codes and hex colors, found in the string using the color code '&amp;',
     * to its chat format.
     * </p>
     *
     * @param index The index at which to set the line. Must be within the valid range of lines.
     * @param line  The value to be displayed on the specified line.
     * @throws IllegalArgumentException  If the line value is null.
     *                                   If the index is negative.
     *                                   If the line value is larger than 32 characters
     *                                   <strong>(only in 1.12 version)</strong>.
     * @throws IndexOutOfBoundsException If the index is higher than 14.
     * @throws IllegalStateException     If the sidebar has been deleted.
     */
    public void setLine(int index, String line) {

        checkState();
        checkIndex(index);
        Preconditions.checkArgument(line != null, "Sidebar cannot set line with null value");

        LineAction action = (lines[index] != null) ? LineAction.UPDATE : LineAction.CREATE;

        lines[index] = ChatUtils.setColors(line);
        SIDEBAR_MANAGER.sendLinePacket(channel, action, index, lines[index]);

    }

    /**
     * Removes all the lines of the sidebar.
     * @throws IllegalStateException If the sidebar has been deleted.
     */
    public void removeLines() {

        checkState();

        for (int i = 0; i < MAX_LINES; i++) {
            removeLine(i);
        }

    }

    /**
     * Removes the line at the given index of the sidebar.
     *
     * @param index The line index to remove.
     * @throws IllegalArgumentException  If the index is negative.
     * @throws IndexOutOfBoundsException If the index is higher than 14.
     * @throws IllegalStateException     If the sidebar has been deleted.
     */
    public void removeLine(int index) {

        checkState();
        checkIndex(index);

        if (lines[index] != null) {
            SIDEBAR_MANAGER.sendLinePacket(channel, LineAction.REMOVE, index, null);
            lines[index] = null;
        }

    }

    /**
     * Displays or removes the sidebar for the associated player.
     *
     * <p>
     * This method allows developers to control the visibility of the sidebar for a specific player.
     * If the {@code flag} parameter is set to {@code true}, the sidebar will be displayed;
     * if set to {@code false}, the sidebar will be removed from view.
     * </p>
     *
     * @param flag A boolean value indicating whether to display ({@code true}) or remove ({@code false}) the sidebar.
     * @throws IllegalStateException If the sidebar has been deleted.
     */
    public void display(boolean flag) {
        checkState();
        SIDEBAR_MANAGER.sendDisplayPacket(channel, flag);
    }

    /**
     * Retrieves the associated player to this sidebar.
     *
     * @return The associated player.
     * @throws IllegalStateException If the sidebar has been deleted.
     */
    public Player getPlayer() {
        checkState();
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Retrieves the current title of the sidebar.
     *
     * <p>
     * This method returns the display name set for the sidebar. The title represents
     * the header of the scoreboard that is visible to the player.
     * </p>
     *
     * @return The display name of the sidebar.
     * @throws IllegalStateException If the sidebar has been deleted.
     */
    public String getTitle() {
        checkState();
        return title;
    }

    /**
     * Retrieves the current content of the lines in the sidebar.
     * The returned array represents the content of each line from top to bottom.
     *
     * @return An array containing the content of each line in the sidebar.
     * @throws IllegalStateException If the sidebar has been deleted.
     */
    public String[] getLines() {
        checkState();
        return Arrays.copyOf(lines, MAX_LINES);
    }

    /**
     * Retrieves the value of a specific line in the sidebar. The index parameter represents the position of the line
     * in the sidebar, and the method returns the content associated with that line.
     *
     * @param index The index of the line whose content is to be retrieved.
     *              It must be a non-negative integer and less than the maximum number of lines.
     * @return The content of the line at the specified index.
     * @throws IllegalArgumentException  If the index is negative.
     * @throws IndexOutOfBoundsException If the index is higher than 14.
     * @throws IllegalStateException     If the sidebar has been deleted.
     */
    public String getLineValue(int index) {
        checkState();
        checkIndex(index);
        return lines[index];
    }

    /**
     * Checks whether the sidebar has been deleted.
     *
     * <p>
     * This method returns the current state of the sidebar, indicating whether it has been
     * deleted or is still active. A deleted sidebar implies that it has been removed, and
     * attempting further operations on a deleted sidebar will throw {@code IllegalStateException}.
     * </p>
     *
     * @return {@code true} if the sidebar has been deleted, {@code false} otherwise.
     */
    public boolean isDeleted() {
        return deleted;
    }

    private void checkState() {
        if (deleted) {
            throw new UnsupportedOperationException("Sidebar has been already deleted");
        }
    }

    private void checkIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Sidebar cannot have negative lines");
        } else if (index >= MAX_LINES) {
            throw new IndexOutOfBoundsException("Sidebar cannot have more than " + MAX_LINES + " lines");
        }
    }

    private enum LineAction {

        CREATE,
        REMOVE,
        UPDATE

    }

    private interface SidebarManager {

        String OBJECTIVE_NAME = "{sidebar}";
        String LINE_NAME = "{line_%2d}";

        void sendObjectivePacket(Channel channel, PacketObjective.Mode mode, String title);

        void sendLinePacket(Channel channel, LineAction action, int index, String line);

        default void sendDisplayPacket(Channel channel, boolean flag) {

            PacketDisplayObjective packet = PACKET_MANAGER.createPacketDisplayObjective();
            packet.setPosition(Position.SIDEBAR);
            packet.setScoreName(flag ? OBJECTIVE_NAME : "");

            PACKET_MANAGER.sendPacket(channel, packet);

        }

        default String getEntity(int index) {
            return ChatUtils.colorByOrdinal(MAX_LINES - index).toString();
        }

    }

    private static class SidebarManager_v1_12 extends SidebarManager_v1_13 {

        @Override
        public void sendObjectivePacket(Channel channel, PacketObjective.Mode mode, String title) {
            checkLength(title, "Sidebar title cannot be larger than 32 characters");
            super.sendObjectivePacket(channel, mode, title);
        }

        @Override
        public String getEntity(int index) {
            return super.getEntity(index) + ChatColor.RESET;
        }

        @Override
        PacketTeam createPacketTeam(PacketTeam.Mode mode, int index, String line) {

            PacketTeam packet = PACKET_MANAGER.createPacketTeam();
            packet.setTeamName(String.format(LINE_NAME, index));
            packet.setMode(mode);

            if (mode == PacketTeam.Mode.UPDATE || mode == PacketTeam.Mode.CREATE) {

                // in 1.12 if the line value is larger than 16 characters we have to divide it
                // due to team prefix and suffix having a max of 16 character,
                // so we will have to use both to be able to display a max of 32 characters
                checkLength(line, "Sidebar line cannot be larger than 32 characters");

                // if the line value is less or equal than 16 characters, there is no problem, so we just assigned it to the prefix
                if (line.length() <= 16) {
                    packet.setTeamPrefix(line);

                } else {

                    // if the line value is larger than 16 characters we will have to divide it
                    int maxLength = 32;
                    int midPoint = 16;

                    // checks if at the middle point there is a color code to avoid chopping it, and if there is,
                    // moves the middle point by one losing one character having 31 in total now
                    if (line.charAt(midPoint - 1) == ChatColor.COLOR_CHAR) {
                        midPoint--;
                        maxLength--;
                    }

                    // assigns the first characters of the line value to the prefix
                    String prefix = line.substring(0, midPoint);
                    // checks what colors were used at the end of the first part
                    String lastColors = ChatColor.getLastColors(prefix);

                    // check if there were colors found
                    if (!lastColors.isEmpty()) {
                        // if there were colors found we modify the max length since we will have to add the colors to the second part as well
                        maxLength -= lastColors.length();
                    }

                    // assigns the colors that were found and adds the second characters of the line value to the suffix
                    String suffix = lastColors + line.substring(midPoint, Math.min(line.length(), maxLength));

                    // uses the team prefix and suffix to be able to display the full line value
                    packet.setTeamPrefix(prefix);
                    packet.setTeamSuffix(suffix);

                }

                if (mode == PacketTeam.Mode.CREATE) {
                    packet.setEntities( Collections.singletonList(getEntity(index)) );
                }

            }

            return packet;

        }

        private void checkLength(String value, String message) {
            Preconditions.checkArgument(value.length() <= 32, message);
        }

    }

    private static class SidebarManager_v1_13 implements SidebarManager {

        private static final PacketTeam.Mode[] TEAM_MODES = PacketTeam.Mode.values();
        private static final PacketScore.Action[] SCORE_ACTIONS = PacketScore.Action.values();

        @Override
        public void sendObjectivePacket(Channel channel, PacketObjective.Mode mode, String title) {

            PacketObjective packet = PACKET_MANAGER.createPacketObjective();
            packet.setObjectiveName(OBJECTIVE_NAME);
            packet.setMode(mode);

            if (mode != PacketObjective.Mode.REMOVE) {
                packet.setObjectiveValue(title);
                packet.setType(RenderType.INTEGER);
            }

            PACKET_MANAGER.sendPacket(channel, packet);

        }

        @Override
        public void sendLinePacket(Channel channel, LineAction action, int index, String line) {

            PACKET_MANAGER.sendPacket(channel, createPacketTeam(TEAM_MODES[action.ordinal()], index, line));

            if (action != LineAction.UPDATE) {
                PACKET_MANAGER.sendPacket(channel, createPacketScore(SCORE_ACTIONS[action.ordinal()], index));
            }

        }

        PacketTeam createPacketTeam(PacketTeam.Mode mode, int index, String line) {

            PacketTeam packet = PACKET_MANAGER.createPacketTeam();
            packet.setTeamName(java.lang.String.format(LINE_NAME, index));
            packet.setMode(mode);

            if (mode == PacketTeam.Mode.UPDATE || mode == PacketTeam.Mode.CREATE) {

                packet.setTeamPrefix(line);

                if (mode == PacketTeam.Mode.CREATE) {
                    packet.setEntities(Collections.singleton(getEntity(index)));
                }

            }

            return packet;

        }

        PacketScore createPacketScore(PacketScore.Action action, int index) {

            PacketScore packet = PACKET_MANAGER.createPacketScore();
            packet.setEntityName(getEntity(index));
            packet.setAction(action);
            packet.setObjectiveName(OBJECTIVE_NAME);

            return packet;

        }

    }

    private static class SidebarManager_v1_20_3 implements SidebarManager {

        @Override
        public void sendObjectivePacket(Channel channel, PacketObjective.Mode mode, String title) {

            PacketObjective packet = PACKET_MANAGER.createPacketObjective();
            packet.setObjectiveName(OBJECTIVE_NAME);
            packet.setMode(mode);

            if (mode != PacketObjective.Mode.REMOVE) {
                packet.setObjectiveValue(title);
                packet.setType(RenderType.INTEGER);
                packet.setNumberFormat(BlankFormat.getInstance());
            }

            PACKET_MANAGER.sendPacket(channel, packet);

        }

        @Override
        public void sendLinePacket(Channel channel, LineAction action, int index, String line) {

            if (action == LineAction.UPDATE || action == LineAction.CREATE) {
                PACKET_MANAGER.sendPacket(channel, createPacketScore(index, line));
            } else if (action == LineAction.REMOVE){
                PACKET_MANAGER.sendPacket(channel, createPacketResetScore(index));
            }

        }

        PacketScore createPacketScore(int index, String line) {

            PacketScore packet = PACKET_MANAGER.createPacketScore();
            packet.setEntityName(getEntity(index));
            packet.setObjectiveName(OBJECTIVE_NAME);
            packet.setDisplayName(line);

            return packet;

        }

        PacketResetScore createPacketResetScore(int index) {

            PacketResetScore packet = PACKET_MANAGER.createPacketResetScore();
            packet.setEntityName(getEntity(index));
            packet.setObjectiveName(OBJECTIVE_NAME);

            return packet;

        }

    }

}

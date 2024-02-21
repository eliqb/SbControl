package me.eliab.sbcontrol.enums;

import me.eliab.sbcontrol.util.Reflection;

/**
 * Enum representing various Minecraft packet scoreboard types.
 * Each enum constant encapsulates the name of the corresponding NMS (net.minecraft.server) class.
 */
public enum PacketSbType {

    DISPLAY_OBJECTIVE("PacketPlayOutScoreboardDisplayObjective"),
    OBJECTIVE("PacketPlayOutScoreboardObjective"),
    TEAM("PacketPlayOutScoreboardTeam"),
    SCORE("PacketPlayOutScoreboardScore"),
    RESET_SCORE("ClientboundResetScorePacket");

    private final String className;

    PacketSbType(String className) {
        this.className = className;
    }

    /**
     * Retrieves the corresponding NMS (net.minecraft.server) class for the scoreboard packet type.
     *
     * @return The Class object representing the NMS class.
     * @throws ClassNotFoundException If the NMS class is not found.
     */
    public Class<?> getNmsClass() throws ClassNotFoundException {
        return Reflection.getNmsClass("network.protocol.game", className);
    }

}

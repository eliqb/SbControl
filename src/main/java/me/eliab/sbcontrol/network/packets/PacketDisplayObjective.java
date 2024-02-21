package me.eliab.sbcontrol.network.packets;

import me.eliab.sbcontrol.enums.PacketSbType;
import me.eliab.sbcontrol.enums.Position;
import me.eliab.sbcontrol.network.Packet;

/**
 * This packet is sent to the client when it should display a scoreboard objective.
 */
public abstract class PacketDisplayObjective implements Packet {

    protected Position position;
    protected String scoreName;

    /**
     * Sets the position at which the scoreboard will be displayed.
     * @param position The desired position for displaying the scoreboard.
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Sets the unique name for the scoreboard.
     * @param scoreName The unique name to be set for the scoreboard.
     */
    public void setScoreName(String scoreName) {
        this.scoreName = scoreName;
    }

    /**
     * Retrieves the position at which the scoreboard will be displayed.
     * @return The given position for displaying the scoreboard.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Retrieves the unique name for the scoreboard.
     * @return The unique name given for the scoreboard.
     */
    public String getScoreName() {
        return scoreName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PacketSbType getPacketSbType() {
        return PacketSbType.DISPLAY_OBJECTIVE;
    }

}

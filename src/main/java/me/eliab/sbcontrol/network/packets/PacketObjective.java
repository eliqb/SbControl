package me.eliab.sbcontrol.network.packets;

import me.eliab.sbcontrol.enums.PacketSbType;
import me.eliab.sbcontrol.enums.RenderType;
import me.eliab.sbcontrol.network.Packet;
import me.eliab.sbcontrol.numbers.NumberFormat;

/**
 * This packet is sent to the client when it should create a new scoreboard objective or remove one.
 */
public abstract class PacketObjective implements Packet {

    protected String objectiveName;
    protected Mode mode;
    protected String objectiveValue;
    protected RenderType type;
    protected NumberFormat numberFormat;

    /**
     * Sets the unique name of the objective.
     * @param objectiveName A unique name for the objective.
     */
    public void setObjectiveName(String objectiveName) {
        this.objectiveName = objectiveName;
    }

    /**
     * Sets the mode to be performed by the packet.
     * @param mode A packet mode indicating the desired operation.
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Sets the text to be displayed for the score.
     * @param objectiveValue A display name for the objective's score.
     */
    public void setObjectiveValue(String objectiveValue) {
        this.objectiveValue = objectiveValue;
    }

    /**
     * Sets how the objective scores should be rendered.
     * @param type A rendering type determining the visual presentation of scores.
     */
    public void setType(RenderType type) {
        this.type = type;
    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Sets how the objective scores' number should be formatted.
     *
     * @param numberFormat A number format specifying how scores should be formatted.
     * @throws UnsupportedOperationException If the versioned {@code PacketObjective} does not support this feature.
     */
    public void setNumberFormat(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    /**
     * Retrieves the unique name of the objective.
     * @return The unique name of the objective.
     */
    public String getObjectiveName() {
        return objectiveName;
    }

    /**
     * Retrieves the mode to be performed by the packet.
     * @return The packet mode of this packet.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Retrieves the text to be displayed for the score.
     * @return The display name of the objective's score.
     */
    public String getObjectiveValue() {
        return objectiveValue;
    }

    /**
     * Retrieves how the objective scores should be rendered.
     * @return The render type for the scores.
     */
    public RenderType getType() {
        return type;
    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Retrieves how the objective scores' number should be formatted.
     *
     * @return The number format specifying how scores are formatted.
     * @throws UnsupportedOperationException If the versioned {@code PacketObjective} does not support this feature.
     */
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PacketSbType getPacketSbType() {
        return PacketSbType.OBJECTIVE;
    }

    public enum Mode {

        CREATE,
        REMOVE,
        UPDATE

    }
}

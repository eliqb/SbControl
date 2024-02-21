package me.eliab.sbcontrol.network.packets;

import me.eliab.sbcontrol.enums.PacketSbType;
import me.eliab.sbcontrol.network.Packet;
import me.eliab.sbcontrol.numbers.NumberFormat;

/**
 * This packet is sent to the client when it should update a scoreboard item.
 */
public abstract class PacketScore implements Packet {

    protected String entityName;
    protected Action action;
    protected String objectiveName;
    protected int value;
    protected String displayName;
    protected NumberFormat numberFormat;

    /**
     * Sets the entity name whose score this packet represents. For players, use their username; for other entities, use their UUID.
     * @param entityName The name or UUID of the entity.
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * <p><strong>(Available in 1.20.2 or lower versions)</strong></p>
     * Sets the action to be performed by the packet.
     *
     * @param action The action to be performed.
     * @throws UnsupportedOperationException If the versioned PacketScore does not support this feature.
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Sets the name of the objective to which the score belongs.
     * @param objectiveName The name of the objective.
     */
    public void setObjectiveName(String objectiveName) {
        this.objectiveName = objectiveName;
    }

    /**
     * Sets the score value to be displayed next to the entry.
     * @param value The score for the entity.
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Sets a custom display name for the score.
     *
     * @param displayName The custom display name for the score.
     * @throws UnsupportedOperationException If the versioned {@code PacketScore} does not support this feature.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Sets how the scores number should be formatted.
     *
     * @param numberFormat The number format.
     * @throws UnsupportedOperationException If the versioned {@code PacketScore} does not support this feature.
     */
    public void setNumberFormat(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    /**
     * Retrieves the entity name whose score this packet represents. For players, use their username; for other entities, use their UUID.
     * @return The entity name or UUID of the score.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * <p><strong>(Available in 1.20.2 or lower versions)</strong></p>
     * Retrieves the action to be performed by the packet.
     *
     * @return The packet action of this packet.
     * @throws UnsupportedOperationException If the versioned {@code PacketScore} does not support this feature.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Retrieves the name of the objective to which the score belongs.
     * @return The name of the objective.
     */
    public String getObjectiveName() {
        return objectiveName;
    }

    /**
     * Retrieves the score value to be displayed next to the entry.
     * @return The score of the entity.
     */
    public int getValue() {
        return value;
    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Retrieves the custom display name for the score.
     *
     * @return The display name for the score.
     * @throws UnsupportedOperationException If the versioned {@code PacketScore} does not support this feature.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Retrieves how the scores number should be formatted.
     *
     * @return The number format.
     * @throws UnsupportedOperationException If the versioned {@code PacketScore} does not support this feature.
     */
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PacketSbType getPacketSbType() {
        return PacketSbType.SCORE;
    }

    public enum Action {

        UPDATE,
        REMOVE

    }

}

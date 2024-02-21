package me.eliab.sbcontrol.network.packets;

import me.eliab.sbcontrol.enums.PacketSbType;
import me.eliab.sbcontrol.network.Packet;

/**
 * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
 * This packet is sent to the client when it should remove a scoreboard item.
 */
public abstract class PacketResetScore implements Packet {

    protected String entityName;
    protected String objectiveName;

    /**
     * Sets the entity associated with this score. For players, use their username; for other entities, use their UUID.
     * @param entityName The name or UUID of the entity.
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Sets the name of the objective to which this score belongs, or null to remove it from all objectives.
     * @param objectiveName The name of the objective or null to remove association from any objective.
     */
    public void setObjectiveName(String objectiveName) {
        this.objectiveName = objectiveName;
    }

    /**
     * Retrieves the entity associated with this score. For players, retrieves their username; for other entities, retrieves their UUID.
     * @return The name or UUID of the entity.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Retrieves the name of the objective to which this score belongs, if present.
     * @return The name of the associated objective or null if not associated.
     */
    public String getObjectiveName() {
        return objectiveName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PacketSbType getPacketSbType() {
        return PacketSbType.RESET_SCORE;
    }

}

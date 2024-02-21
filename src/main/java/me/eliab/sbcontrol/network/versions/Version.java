package me.eliab.sbcontrol.network.versions;

import me.eliab.sbcontrol.network.PacketManager;

/**
 * Enum representing the different versions of minecraft, each associated with a specific {@link PacketManager} implementation.
 */
public enum Version {

    V1_12(PacketManager_v1_12.class),
    V1_13(PacketManager_v1_13.class),
    V1_16(PacketManager_v1_13.class),
    V1_20(PacketManager_v1_13.class),
    V1_20_2(PacketManager_v1_20_2.class),
    V1_20_3(PacketManager_v1_20_3.class);

    private final Class<? extends PacketManager> packetManagerClass;

    Version(Class<? extends PacketManager> packetManagerClass) {
        this.packetManagerClass = packetManagerClass;
    }

    /**
     * Checks if the current version is lower than or equal to the specified version.
     *
     * @param version The version to compare against.
     * @return True if the current version is lower than or equal to the specified version.
     */
    public boolean isLowerOrEqualThan(Version version) {
        return this.ordinal() <= version.ordinal();
    }

    /**
     * Checks if the current version is higher than or equal to the specified version.
     *
     * @param version The version to compare against.
     * @return True if the current version is higher than or equal to the specified version.
     */
    public boolean isHigherOrEqualThan(Version version) {
        return this.ordinal() >= version.ordinal();
    }

    /**
     * Gets the associated {@link PacketManager} class for the current version.
     * @return The PacketManager class for the current version.
     */
    public Class<? extends PacketManager> getPacketManagerClass() {
        return packetManagerClass;
    }

}

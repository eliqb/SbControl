package me.eliab.sbcontrol.network;

import me.eliab.sbcontrol.enums.PacketSbType;

/**
 * Represents a packet in the context of Minecraft designed for scoreboard communication.
 *
 * <p>
 * Minecraft scoreboard packets encapsulate information related to scoring, objectives, and teams.
 * Implementations of this interface define the structure and content of specific packet types.
 * </p>
 *
 * <p>
 * The {@link #write(PacketSerializer)} method is responsible for serializing the packet data into a {@link PacketSerializer}.
 * This serialization process is crucial for transmitting packet information over the network.
 * </p>
 *
 * <p>
 * Each packet is associated with a specific {@link PacketSbType}, indicating its purpose or category.
 * The type is used for identification and proper handling of the packet.
 * </p>
 */
public interface Packet {

    /**
     * Writes the packet data into the provided {@link PacketSerializer}.
     *
     * <p>
     * This method is responsible for serializing the packet's content into a binary format.
     * It allows the packet to be transmitted over the network.
     * </p>
     *
     * @param serializer The serializer used for encoding packet data.
     */
    void write(PacketSerializer serializer);

    /**
     * Retrieves the type of scoreboard packet.
     *
     * <p>
     * The {@link PacketSbType} provides information about the purpose or category of the packet.
     * It is used for identification and handling of the packet on both the client and server sides.
     * </p>
     *
     * @return The scoreboard packet type.
     */
    PacketSbType getPacketSbType();

}

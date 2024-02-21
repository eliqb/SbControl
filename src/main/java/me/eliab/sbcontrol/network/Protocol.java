package me.eliab.sbcontrol.network;

import me.eliab.sbcontrol.enums.PacketSbType;
import me.eliab.sbcontrol.network.versions.Version;
import me.eliab.sbcontrol.util.Reflection;

import java.lang.invoke.MethodHandle;
import java.util.EnumMap;
import java.util.Map;

/**
 * The {@code Protocol} class provides functionality to retrieve Minecraft packet IDs for specific packet types.
 * It is designed to work with different Minecraft versions and dynamically determine packet IDs based on reflection.
 */
public class Protocol {

    // Mapping of packet IDs for each ScoreboardType.
    private final Map<PacketSbType, Integer> packetIDs = new EnumMap<>(PacketSbType.class);

    Protocol(Version version) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {

        Codec codec = getCodec(version);

        registerPacket(codec, PacketSbType.DISPLAY_OBJECTIVE);
        registerPacket(codec, PacketSbType.OBJECTIVE);
        registerPacket(codec, PacketSbType.TEAM);
        registerPacket(codec, PacketSbType.SCORE);

        if (version.isHigherOrEqualThan(Version.V1_20_3)) {
            registerPacket(codec, PacketSbType.RESET_SCORE);
        }

    }

    /**
     * Retrieves the packet ID for the given packet.
     *
     * @param packet The packet for which to retrieve the packet ID.
     * @return The corresponding packet ID.
     */
    public int getPacketID(Packet packet) {
        return getPacketID(packet.getPacketSbType());
    }

    /**
     * Retrieves the packet ID for the given ScoreboardType.
     *
     * @param packetSbType The PacketSbType for which to retrieve the packet ID.
     * @return The corresponding packet ID.
     * @throws IllegalArgumentException If the packet ID for the specified ScoreboardType is not registered.
     */
    public int getPacketID(PacketSbType packetSbType) {

        Integer id = packetIDs.get(packetSbType);
        if (id == null) {
            throw new IllegalArgumentException("Unregister packet id for ScoreboardType " + packetSbType);
        }
        return id;

    }

    private void registerPacket(Codec codec, PacketSbType packetSbType) {

        try {
            int id = codec.getPacketID(Type.PLAY, Direction.CLIENTBOUND, packetSbType.getNmsClass());
            packetIDs.put(packetSbType, id);
        } catch (Throwable t) {
            throw new RuntimeException("Protocol could not register ScoreboardType " + packetSbType);
        }

    }

    private static Codec getCodec(Version version) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {

        Class<?> enumProtocolClass = Reflection.getNmsClass("network", "EnumProtocol");
        Class<?> enumProtocolDirectionClass = Reflection.getNmsClass("network.protocol", "EnumProtocolDirection");
        Class<?> packetClass = Reflection.getNmsClass("network.protocol", "Packet");

        Enum<?>[] protocolEnum = Reflection.getEnumConstants(enumProtocolClass);
        Enum<?>[] protocolDirectionEnum = Reflection.getEnumConstants(enumProtocolDirectionClass);

        if (version.isHigherOrEqualThan(Version.V1_20_2)) {

            Class<?> codecClass = enumProtocolClass.getDeclaredClasses()[1];

            MethodHandle getCodecMethod = Reflection.findMethod(enumProtocolClass, codecClass, enumProtocolDirectionClass);
            MethodHandle getPacketIDMethod = Reflection.findMethod(codecClass, int.class, packetClass);

            return (type, direction, nmsClass) -> {

                Enum<?> protocol = protocolEnum[type.ordinal()];
                Enum<?> protocolDirection = protocolDirectionEnum[direction.ordinal()];
                Object codec = getCodecMethod.invoke(protocol, protocolDirection);

                return (int) getPacketIDMethod.invoke(codec, Reflection.createInstance(nmsClass));

            };

        } else {

            Class<?> returnType = version.isHigherOrEqualThan(Version.V1_20) ? int.class : Integer.class;
            MethodHandle getPacketIDMethod = Reflection.findMethod(enumProtocolClass, returnType, enumProtocolDirectionClass, packetClass);

            return (type, direction, nmsClass) -> {

                Enum<?> protocol = protocolEnum[type.ordinal()];
                Enum<?> protocolDirection = protocolDirectionEnum[direction.ordinal()];

                return (int) getPacketIDMethod.invoke(protocol, protocolDirection, Reflection.createInstance(nmsClass));

            };

        }

    }

    private enum Type {

        HANDSHAKING,
        PLAY,
        STATUS,
        LOGIN,
        CONFIGURATION

    }

    private enum Direction {

        SERVERBOUND,
        CLIENTBOUND

    }

    @FunctionalInterface
    private interface Codec {
        int getPacketID(Type type, Direction direction, Class<?> nmsClass) throws Throwable;
    }

}

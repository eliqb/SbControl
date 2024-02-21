package me.eliab.sbcontrol.network;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import me.eliab.sbcontrol.SbControl;
import me.eliab.sbcontrol.network.packets.*;
import me.eliab.sbcontrol.network.versions.Version;
import me.eliab.sbcontrol.util.Reflection;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.UUID;

/**
 * The {@code PacketManager} class orchestrates the creation, serialization, and transmission of scoreboard packets
 * using the Netty network framework, closely emulating the methodology employed by Minecraft.
 *
 * <p>
 * This class serves as a central hub for handling various aspects of packet management within the context of
 * a scoreboard system. It encapsulates the logic for packet creation, serialization, and dispatch, providing
 * a streamlined interface for working with scoreboard-related network communication.
 * </p>
 *
 * <p>
 * Developers can leverage the methods offered by this class to integrate scoreboard packet functionality
 * into their applications, ensuring compatibility and consistency with Minecraft networking standards.
 * </p>
 *
 * <p>To get an instance of the versioned {@code PacketManager} use {@link SbControl#getPacketManager()}</p>
 */
public abstract class PacketManager {

    private final Version version;
    private final Protocol protocol;

    private final MethodHandle getEntityPlayerMethod;
    private final MethodHandle playerConnectionGetter;
    private final MethodHandle networkManagerGetter;
    private final MethodHandle channelGetter;

    private final Map<UUID, Channel> channels = new MapMaker().weakValues().makeMap();

    public PacketManager(Version version) throws ReflectiveOperationException {

        this.version = version;
        protocol = new Protocol(version);

        // nms classes
        Class<?> craftPlayerClass = Reflection.getBukkitClass("entity.CraftPlayer");
        Class<?> entityPlayerClass = Reflection.getNmsClass("server.level", "EntityPlayer");
        Class<?> playerConnectionClass = Reflection.getNmsClass("server.network", "PlayerConnection");
        Class<?> networkManagerClass = Reflection.getNmsClass("network", "NetworkManager");

        // methods
        getEntityPlayerMethod = Reflection.getMethod(craftPlayerClass, "getHandle", entityPlayerClass);
        playerConnectionGetter = Reflection.findFieldGetter(entityPlayerClass, playerConnectionClass);

        if (version.isHigherOrEqualThan(Version.V1_20_2)) {
            Class<?> serverCommonPacketListenerImplClass = Reflection.getNmsClass("server.network", "ServerCommonPacketListenerImpl");
            networkManagerGetter = Reflection.findFieldGetter(serverCommonPacketListenerImplClass, networkManagerClass);
        } else {
            networkManagerGetter = Reflection.findFieldGetter(playerConnectionClass, networkManagerClass);
        }

        channelGetter = Reflection.findFieldGetter(networkManagerClass, Channel.class);

    }

    /**
     * Sends one or more packets to a specific player.
     *
     * <p>
     * This method simplifies the process of sending packets to a particular player. It internally retrieves the channel associated
     * with the player and then invokes {@link #sendPacket(Channel, Packet...)} to handle the packet serialization and transmission.
     * </p>
     *
     * @param player  The player to send the packets to.
     * @param packets The packets to be sent.
     * @see #sendPacket(Channel, Packet...)
     * @throws IllegalArgumentException If the player is null.
     */
    public void sendPacket(Player player, Packet... packets) {
        sendPacket(getPlayerChannel(player), packets);
    }

    /**
     * Sends one or more packets to a specified channel.
     *
     * <p>
     * This method facilitates the sending of one or more packets to a given channel. Each packet is serialized using
     * a dynamically created {@link PacketSerializer}, and the resulting byte data is sent through the channel.
     * </p>
     *
     * @param channel The channel to which packets will be sent.
     * @param packets The packets to be sent.
     * @throws IllegalArgumentException If the channel is null or closed.
     */
    public void sendPacket(Channel channel, Packet... packets) {

        Preconditions.checkArgument(channel != null, "PacketManager cannot send packet to null channel");
        Preconditions.checkArgument(channel.isOpen(), "PacketManager cannot send packet to closed channel");

        for (Packet packet : packets) {
            PacketSerializer packetSerializer = createPacketSerializer();
            packetSerializer.writeByte(protocol.getPacketID(packet));
            packet.write(packetSerializer);
            channel.writeAndFlush(packetSerializer.getHandle());
        }

    }

    /**
     * Retrieves the communication channel associated with the specified player.
     *
     * <p>
     * This method is used to obtain the network communication channel (Netty Channel) linked to a particular player.
     * The player's unique identifier is used to retrieve the associated channel from the cache. If the channel is not
     * cached or is no longer open, the method attempts to extract the channel from the player's internal network
     * components using reflection.
     * </p>
     *
     * <p>
     * Note: Reflection is utilized to access internal Minecraft server components, and the method may throw a
     * {@code RuntimeException} if there are unexpected issues during the reflection process.
     * </p>
     *
     * @param player The player for whom to retrieve the communication channel.
     * @return The Netty Channel associated with the given player.
     * @throws IllegalArgumentException If the player is null.
     * @throws RuntimeException If there are issues accessing or invoking internal components via reflection.
     */
    public Channel getPlayerChannel(Player player) {

        Preconditions.checkArgument(player != null, "PacketManager cannot get channel from null player");

        Channel channel = channels.get(player.getUniqueId());

        if (channel == null || !channel.isOpen()) {

            try {

                // same as

                // EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                Object entityPlayer = getEntityPlayerMethod.invoke(player);

                // PlayerConnection playerConnection = entityPlayer.playerConnection;
                Object playerConnection = playerConnectionGetter.invoke(entityPlayer);

                // NetworkManager networkManager = playerConnection.networkManager;
                Object networkManager = networkManagerGetter.invoke(playerConnection);

                // Channel channel = networkManager.channel;
                channels.put(player.getUniqueId(), channel = (Channel) channelGetter.invoke(networkManager));

            } catch (Throwable t) {
                throw new RuntimeException("PacketManager encounter an error retrieving player channel via reflection.", t);
            }

        }

        return channel;

    }

    /**
     * Retrieves the version of the {@code PacketManager}.
     *
     * <p>
     * This method returns the {@link Version} currently in use by the {@code PacketManager}.
     * The version represents the protocol or format used for packet communication.
     * </p>
     *
     * @return The {@link Version} currently configured for the {@code PacketManager}.
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Retrieves the underlying {@link Protocol} instance used by the {@code PacketManager}.
     *
     * <p>This method returns the Protocol instance currently employed by the PacketManager.</p>
     *
     * @return The {@link Protocol} instance associated with the PacketManager.
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * Creates a new {@link PacketSerializer} with an empty {@link ByteBuf}.
     *
     * <p>This method provides a convenient way to obtain a new PacketSerializer instance with an initially empty ByteBuf.</p>
     *
     * @return A new {@code PacketSerializer} instance.
     */
    public PacketSerializer createPacketSerializer() {
        return createPacketSerializer(Unpooled.buffer());
    }

    /**
     * Creates a new {@link PacketSerializer} that uses the given {@link ByteBuf}.
     *
     * <p>
     * This method allows the creation of a PacketSerializer instance with a specified ByteBuf, offering flexibility
     * in handling different buffer configurations.
     * </p>
     *
     * @param byteBuf The ByteBuf to be used by the new PacketSerializer.
     * @return A new {@code PacketSerializer} instance.
     */
    public abstract PacketSerializer createPacketSerializer(ByteBuf byteBuf);

    /**
     * Creates a new instance of {@link PacketDisplayObjective}, representing an empty display objective.
     *
     * <p>
     * This method is used to generate a fresh {@code PacketDisplayObjective} instance that can be customized
     * to define the display properties of an objective in a packet.
     * </p>
     *
     * @return A new {@code PacketDisplayObjective} instance, ready for customization.
     */
    public abstract PacketDisplayObjective createPacketDisplayObjective();

    /**
     * Creates a new instance of {@link PacketObjective}.
     *
     * <p>
     * This method is designed to produce an empty {@code PacketObjective} instance, which can be further customized
     * by setting its properties as needed.
     * </p>
     *
     * @return A new {@code PacketObjective} instance, ready for customization.
     */
    public abstract PacketObjective createPacketObjective();

    /**
     * Creates a new instance of {@link PacketTeam}.
     *
     * <p>
     * This method is designed to produce an empty {@code PacketTeam} instance, which can be further customized
     * by setting its properties as needed.
     * </p>
     *
     * @return A new {@code PacketTeam} instance, ready for customization.
     */
    public abstract PacketTeam createPacketTeam();

    /**
     * Creates a new instance of {@link PacketScore}.
     *
     * <p>
     * This method is designed to produce an empty {@code PacketScore} instance, which can be further customized
     * by setting its properties as needed.
     * </p>
     *
     * @return A new {@code PacketScore} instance, ready for customization.
     */
    public abstract PacketScore createPacketScore();

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Creates a new instance of {@link PacketScore}.
     *
     * <p>
     * This method is designed to produce an empty {@code PacketScore} instance, which can be further customized
     * by setting its properties as needed.
     * </p>
     *
     * @return A new {@code PacketScore} instance, ready for customization.
     * @throws UnsupportedOperationException if the versioned {@code PacketManager} does not support this feature.
     */
    public abstract PacketResetScore createPacketResetScore();

}

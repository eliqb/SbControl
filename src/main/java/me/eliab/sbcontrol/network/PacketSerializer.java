package me.eliab.sbcontrol.network;

import com.google.common.base.Preconditions;
import dev.dewy.nbt.api.Tag;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import me.eliab.sbcontrol.numbers.NumberFormat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * The {@code PacketSerializer} class is designed to work easier with a {@link ByteBuf},
 * providing simplified methods to help serialize minecraft packet data.
 * This class aims to replicate the {@code PacketDataSerializer} class that minecraft uses to achieve the same.
 */
public abstract class PacketSerializer {

    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    protected final ByteBuf byteBuf;

    public PacketSerializer(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    /**
     * Writes a {@code boolean} value into the provided {@code ByteBuf}.
     * @param value The {@code boolean} value to be written.
     */
    public void writeBoolean(boolean value) {
        byteBuf.writeBoolean(value);
    }

    /**
     * Writes a {@code byte} value into the provided {@code ByteBuf}.
     * @param value The {@code byte} value to be written.
     */
    public void writeByte(int value) {
        byteBuf.writeByte(value);
    }

    /**
     * Writes a {@code byte} array into the provided {@code ByteBuf}.
     * @param value The {@code byte} array to be written.
     */
    public void writeBytes(byte[] value) {
        byteBuf.writeBytes(value);
    }

    /**
     * Writes an int value into the provided {@code ByteBuf}.
     * @param value The integer value to be written into the ByteBuf.
     */
    public void writeInt(int value) {
        byteBuf.writeInt(value);
    }

    /**
     * Writes a {@code long} value into the provided {@code ByteBuf}.
     * @param value The {@code long} value to be written.
     */
    public void writeLong(long value) {
        byteBuf.writeLong(value);
    }

    /**
     * Writes a {@code int} value using a Variable-Length format (VarInt) into the given {@code ByteBuf}.
     *
     * <p>
     * VarInts are a compact encoding scheme for integers where smaller numbers use fewer bytes.
     * Each VarInt is encoded using 7 bits of the value, and the most significant bit indicates whether
     * there's another byte following for the next part of the number. The encoding is little-endian,
     * meaning the least significant group is written first, followed by each more significant group.
     * Note that each group is 7 bits, not 8. VarInts are guaranteed to never exceed 5 bytes in length.
     * </p>
     *
     * @param value The {@code int} value to be encoded and written as a VarInt.
     */
    public void writeVarInt(int value) {

        while (true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                writeByte(value);
                return;
            }

            writeByte((value & SEGMENT_BITS) | CONTINUE_BIT);
            value >>>= 7;
        }

    }

    /**
     * Writes a {@code long} value using a Variable-Length format (VarLong) into the given {@code ByteBuf}.
     *
     * <p>
     * VarLongs are encoded in a variable-length format, minimizing the number of bytes used for smaller numbers.
     * The 7 least significant bits of each byte encode the value, and the most significant bit indicates whether
     * there's another byte for the next part of the number. The encoding is little-endian, with the least significant
     * group written first, followed by each of the more significant groups. VarLongs are limited to a maximum length
     * of 10 bytes.
     * </p>
     *
     * @param value The {@code long} value to be encoded and written.
     */
    public void writeVarLong(long value) {

        while (true) {
            if ((value & ~((long) SEGMENT_BITS)) == 0) {
                writeByte((int) value);
                return;
            }

            writeByte((int) ((value & SEGMENT_BITS) | CONTINUE_BIT));
            value >>>= 7;
        }

    }

    /**
     * Writes a UTF-8 encoded {@code String} into the provided {@code ByteBuf}.
     * The process involves encoding the string into a sequence of bytes, followed by writing the length of the byte sequence
     * as a VarInt and then writing the byte sequence itself.
     *
     * <p>
     * This method ensures proper serialization of a String by encoding it in UTF-8, allowing for a variable-length
     * representation that includes the length information for decoding.
     * </p>
     *
     * @param value The {@code String} to be serialized into the ByteBuf.
     * @throws IllegalArgumentException If the provided {@code String} is {@code null}.
     */
    public void writeString(String value) {

        Preconditions.checkArgument(value != null, "PacketSerializer cannot write null string");

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        writeVarInt(bytes.length);
        writeBytes(bytes);

    }

    /**
     * Writes a {@code Collection} of elements into the {@code ByteBuf} using a provided operation.
     *
     * <p>
     * This method facilitates the serialization of a collection by applying a specified operation to each element.
     * It writes the size of the collection as a VarInt, followed by invoking the provided operation for each element.
     * </p>
     *
     * <pre>
     * {@code
     * List<String> values = Arrays.asList("alex", "john");
     * writeCollection(values, PacketSerializer::writeString);
     * }
     * </pre>
     *
     * @param values   The {@code Collection} to be serialized.
     * @param consumer The operation to be applied to each element for serialization.
     * @param <T>      The type of elements in the {@code Collection}.
     * @throws IllegalArgumentException If the provided {@code Collection} or the {@code BiConsumer} is {@code null}.
     */
    public <T> void writeCollection(Collection<T> values, BiConsumer<PacketSerializer, T> consumer) {

        Preconditions.checkArgument(values != null, "PacketSerializer cannot write null collection");
        Preconditions.checkArgument(consumer != null, "PacketSerializer cannot write Collection without a BiConsumer");

        writeVarInt(values.size());

        for (T value : values) {
            consumer.accept(this, value);
        }

    }

    /**
     * Writes an {@code Enum} constant into the provided {@code ByteBuf}.
     *
     * <p>
     * This method serializes the {@code Enum} constant by writing its ordinal value as a VarInt.
     * </p>
     *
     * @param value The {@code Enum} constant to be serialized.
     * @throws IllegalArgumentException If the provided {@code Enum} is {@code null}.
     */
    public void writeEnum(Enum<?> value) {
        Preconditions.checkArgument(value != null, "PacketSerializer cannot write null enum");
        writeVarInt(value.ordinal());
    }

    /**
     * Writes a nullable element into the {@code ByteBuf} using a provided operation.
     *
     * <p>
     * This method facilitates the serialization of an element that may be null. It writes a boolean indicating
     * the presence of a non-null value, and if the value is non-null, it applies the specified operation to serialize it.
     * </p>
     *
     * <pre>
     * {@code
     * String value = null;
     * writeNullable(value, PacketSerializer::writeString);
     * }
     * </pre>
     *
     * @param value    The nullable element to be serialized. It may be {@code null}.
     * @param consumer The operation to be applied for serializing the non-null element.
     * @param <T>      The type of the nullable element.
     * @throws IllegalArgumentException If the provided {@code BiConsumer} is {@code null}.
     */
    public <T> void writeNullable(T value, BiConsumer<PacketSerializer, T> consumer) {

        Preconditions.checkArgument(consumer != null, "PacketSerializer cannot write nullable value without a BiConsumer");

        if (value != null) {
            writeBoolean(true);
            consumer.accept(this, value);
        } else {
            writeBoolean(false);
        }

    }

    /**
     * Writes the content of a named binary tag (NBT) into the provided {@code ByteBuf}.
     *
     * <p>
     * The provided NBT tag is written in a format compatible with Minecraft's NBT serialization
     * format. It begins by writing a single byte representing the type ID of the tag, followed by
     * the serialized data representing the tag itself.
     * </p>
     *
     * @param value The NBT Tag to be serialized and written into the ByteBuf.
     * @throws IllegalArgumentException If the provided NBT tag is {@code null}.
     * @throws RuntimeException If an IOException occurs while writing the NBT data into the {@code ByteBuf}.
     */
    public void writeNBTTag(Tag value) {

        Preconditions.checkArgument(value != null, "PacketSerializer cannot write null NBT tag");

        try (ByteBufOutputStream dataOutput = new ByteBufOutputStream(byteBuf)) {

            // Write the type ID of the tag
            dataOutput.writeByte(value.getTypeId());
            // Serialize and write the tag data
            value.write(dataOutput, 0, null);

        } catch (IOException e) {
            // If an IOException occurs during the serialization process, wrap it in a RuntimeException
            // and throw it to indicate a failure in writing the NBT data.
            throw new RuntimeException("Error while writing NBT data into ByteBuf", e);
        }

    }

    /**
     * Converts a plain text {@code String} into a ChatComponent and writes it into the {@code ByteBuf}.
     *
     * <p>
     * This method allows converts a plain text string into a ChatComponent, a formatted text element commonly used
     * in chat-related functionalities. The resulting ChatComponent is then serialized and written into the provided {@code ByteBuf}.
     * </p>
     *
     * @param value The plain text {@code String} to convert and write as a ChatComponent.
     * @throws IllegalArgumentException If the provided {@code String} is {@code null}.
     */
    public abstract void writeAsComponent(String value);

    /**
     * Writes a {@code NumberFormat} into the {@code ByteBuf}.
     *
     * @param value The {@code NumberFormat} instance to be serialized into the {@code ByteBuf}.
     * @throws UnsupportedOperationException If the versioned {@code PacketSerializer} does not support this.
     * @throws IllegalArgumentException If the provided {@code NumberFormat} is {@code null}.
     */
    public abstract void writeNumberFormat(NumberFormat value);

    /**
     * Retrieves the underlying {@code ByteBuf} instance associated with this {@code PacketSerializer}.
     *
     * <p>
     * This method provides access to the raw {@code ByteBuf} used by the {@code PacketSerializer} for efficient
     * reading and writing operations. Modifying the returned {@code ByteBuf} directly may affect the internal state
     * of the serializer, so caution is advised.
     * </p>
     *
     * @return The {@code ByteBuf} instance used by this {@code PacketSerializer}.
     */
    public ByteBuf getHandle() {
        return byteBuf;
    }

}

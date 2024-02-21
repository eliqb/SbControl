package me.eliab.sbcontrol.network.versions;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import me.eliab.sbcontrol.enums.NumberFormatType;
import me.eliab.sbcontrol.network.PacketSerializer;
import me.eliab.sbcontrol.network.packets.PacketObjective;
import me.eliab.sbcontrol.network.packets.PacketResetScore;
import me.eliab.sbcontrol.network.packets.PacketScore;
import me.eliab.sbcontrol.numbers.NumberFormat;
import me.eliab.sbcontrol.util.ChatUtils;

class PacketManager_v1_20_3 extends PacketManager_v1_20_2 {

    PacketManager_v1_20_3(Version version) throws ReflectiveOperationException {
        super(version);
    }

    @Override
    public PacketSerializer createPacketSerializer(ByteBuf byteBuf) {
        return new PacketSerializer_v1_20_3(byteBuf);
    }

    @Override
    public PacketObjective createPacketObjective() {
        return new PacketObjective_1_20_3();
    }

    @Override
    public PacketScore createPacketScore() {
        return new PacketScore_1_20_3();
    }

    @Override
    public PacketResetScore createPacketResetScore() {
        return new PacketResetScore_v1_20_3();
    }

    static class PacketSerializer_v1_20_3 extends PacketSerializer {

        PacketSerializer_v1_20_3(ByteBuf byteBuf) {
            super(byteBuf);
        }

        @Override
        public void writeAsComponent(String value) {
            Preconditions.checkArgument(value != null, "PacketSerializer cannot write null string as ChatComponent");
            writeNBTTag(ChatUtils.nbtFromLegacyText(value));
        }

        @Override
        public void writeNumberFormat(NumberFormat value) {

            Preconditions.checkArgument(value != null, "PacketSerializer cannot write null NumberFormat");

            writeEnum(value.getType());

            if (value.getType() != NumberFormatType.BLANK) {
                writeNBTTag(value.getFormat());
            }

        }

    }

    static class PacketObjective_1_20_3 extends PacketObjective {

        @Override
        public void write(PacketSerializer serializer) {

            serializer.writeString(objectiveName);
            serializer.writeByte(mode.ordinal());

            if (mode != Mode.REMOVE) {
                serializer.writeAsComponent(objectiveValue);
                serializer.writeEnum(type);
                serializer.writeNullable(numberFormat, PacketSerializer::writeNumberFormat);
            }

        }

    }

    static class PacketScore_1_20_3 extends PacketScore {

        @Override
        public void setAction(Action action) {
            throw new UnsupportedOperationException("PacketScore cannot have Action because it is not available for this version");
        }

        @Override
        public Action getAction() {
            throw new UnsupportedOperationException("PacketScore cannot have Action because it is not available for this version");
        }

        @Override
        public void write(PacketSerializer serializer) {

            serializer.writeString(entityName);
            serializer.writeString(objectiveName);
            serializer.writeVarInt(value);
            serializer.writeNullable(displayName, PacketSerializer::writeAsComponent);
            serializer.writeNullable(numberFormat, PacketSerializer::writeNumberFormat);

        }

    }

    static class PacketResetScore_v1_20_3 extends PacketResetScore {

        @Override
        public void write(PacketSerializer serializer) {

            serializer.writeString(entityName);
            serializer.writeNullable(objectiveName, PacketSerializer::writeString);

        }

    }

}

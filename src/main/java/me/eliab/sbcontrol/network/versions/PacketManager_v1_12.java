package me.eliab.sbcontrol.network.versions;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import me.eliab.sbcontrol.network.PacketManager;
import me.eliab.sbcontrol.network.PacketSerializer;
import me.eliab.sbcontrol.network.packets.*;
import me.eliab.sbcontrol.numbers.NumberFormat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

class PacketManager_v1_12 extends PacketManager {

    PacketManager_v1_12(Version version) throws ReflectiveOperationException {
        super(version);
    }

    @Override
    public PacketSerializer createPacketSerializer(ByteBuf byteBuf) {
        return new PacketSerializer_v1_12(byteBuf);
    }

    @Override
    public PacketDisplayObjective createPacketDisplayObjective() {
        return new PacketDisplayObjective_v1_12();
    }

    @Override
    public PacketObjective createPacketObjective() {
        return new PacketObjective_v1_12();
    }

    @Override
    public PacketTeam createPacketTeam() {
        return new PacketTeam_v1_12();
    }

    @Override
    public PacketScore createPacketScore() {
        return new PacketScore_v1_12();
    }

    @Override
    public PacketResetScore createPacketResetScore() {
        throw new UnsupportedOperationException("PacketManager cannot create PacketResetScore because it is not available for this version");
    }

    static class PacketSerializer_v1_12 extends PacketSerializer {

        PacketSerializer_v1_12(ByteBuf byteBuf) {
            super(byteBuf);
        }

        @Override
        public void writeAsComponent(String value) {

            Preconditions.checkArgument(value != null, "PacketSerializer cannot write null string as ChatComponent");

            BaseComponent[] components = TextComponent.fromLegacyText(value);
            writeString(ComponentSerializer.toString(components));

        }

        @Override
        public void writeNumberFormat(NumberFormat value) {
            throw new UnsupportedOperationException("PacketSerializer cannot serialize NumberFormat because it is not available for this version");
        }

    }

    static class PacketDisplayObjective_v1_12 extends PacketDisplayObjective {

        @Override
        public void write(PacketSerializer serializer) {
            serializer.writeByte(position.ordinal());
            serializer.writeString(scoreName);
        }

    }

    static class PacketObjective_v1_12 extends PacketObjective {

        @Override
        public void setNumberFormat(NumberFormat numberFormat) {
            throw new UnsupportedOperationException("PacketObjective cannot have NumberFormat because it is not available for this version");
        }

        @Override
        public NumberFormat getNumberFormat() {
            throw new UnsupportedOperationException("PacketObjective cannot have NumberFormat because it is not available for this version");
        }

        @Override
        public void write(PacketSerializer serializer) {

            serializer.writeString(objectiveName);
            serializer.writeByte(mode.ordinal());

            if (mode != Mode.REMOVE) {
                serializer.writeString(objectiveValue);
                serializer.writeString(type.getValue());
            }

        }

    }

    static class PacketTeam_v1_12 extends PacketTeam {

        @Override
        public void write(PacketSerializer serializer) {

            serializer.writeString(teamName);
            serializer.writeByte(mode.ordinal());

            if (mode == Mode.CREATE || mode == Mode.UPDATE) {

                serializer.writeString(teamDisplayName);
                serializer.writeString(teamPrefix);
                serializer.writeString(teamSuffix);
                serializer.writeByte(friendlyFlags.ordinal());
                serializer.writeString(nameTagVisibility.getValue());
                serializer.writeString(collisionRule.getValue());
                serializer.writeByte(teamColor.ordinal());

                if (mode == Mode.CREATE) {
                    serializer.writeCollection(entities, PacketSerializer::writeString);
                }

            } else if (mode == Mode.ADD_ENTITIES || mode == Mode.REMOVE_ENTITIES) {
                serializer.writeCollection(entities, PacketSerializer::writeString);
            }

        }

    }

    static class PacketScore_v1_12 extends PacketScore {

        @Override
        public void setDisplayName(String displayName) {
            throw new UnsupportedOperationException("PacketScore cannot have display name because it is not available for this version");
        }

        @Override
        public void setNumberFormat(NumberFormat numberFormat) {
            throw new UnsupportedOperationException("PacketScore cannot have NumberFormat because it is not available for this version");
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("PacketScore cannot have display name because it is not available for this version");
        }

        @Override
        public NumberFormat getNumberFormat() {
            throw new UnsupportedOperationException("PacketScore cannot have NumberFormat because it is not available for this version");
        }

        @Override
        public void write(PacketSerializer serializer) {

            serializer.writeString(entityName);
            serializer.writeByte(action.ordinal());

            if (action == Action.UPDATE) {
                serializer.writeString(objectiveName);
                serializer.writeVarInt(value);
            }

        }

    }

}

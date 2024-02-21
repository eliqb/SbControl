package me.eliab.sbcontrol.network.versions;

import me.eliab.sbcontrol.network.PacketSerializer;
import me.eliab.sbcontrol.network.packets.PacketObjective;
import me.eliab.sbcontrol.network.packets.PacketTeam;

class PacketManager_v1_13 extends PacketManager_v1_12 {

    PacketManager_v1_13(Version version) throws ReflectiveOperationException {
        super(version);
    }

    @Override
    public PacketObjective createPacketObjective() {
        return new PacketObjective_v1_13();
    }

    @Override
    public PacketTeam createPacketTeam() {
        return new PacketTeam_v1_13();
    }

    static class PacketObjective_v1_13 extends PacketObjective_v1_12 {

        @Override
        public void write(PacketSerializer serializer) {

            serializer.writeString(objectiveName);
            serializer.writeByte(mode.ordinal());

            if (mode != Mode.REMOVE) {
                serializer.writeAsComponent(objectiveValue);
                serializer.writeEnum(type);
            }

        }

    }

    static class PacketTeam_v1_13 extends PacketTeam {

        @Override
        public void write(PacketSerializer serializer) {

            serializer.writeString(teamName);
            serializer.writeByte(mode.ordinal());

            if (mode == Mode.CREATE || mode == Mode.UPDATE) {

                serializer.writeAsComponent(teamDisplayName);
                serializer.writeByte(friendlyFlags.ordinal());
                serializer.writeString(nameTagVisibility.getValue());
                serializer.writeString(collisionRule.getValue());
                serializer.writeEnum(teamColor);
                serializer.writeAsComponent(teamPrefix);
                serializer.writeAsComponent(teamSuffix);

                if (mode == Mode.CREATE) {
                    serializer.writeCollection(entities, PacketSerializer::writeString);
                }

            } else if (mode == Mode.ADD_ENTITIES || mode == Mode.REMOVE_ENTITIES) {
                serializer.writeCollection(entities, PacketSerializer::writeString);
            }

        }

    }

}

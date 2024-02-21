package me.eliab.sbcontrol.network.versions;

import me.eliab.sbcontrol.network.PacketSerializer;
import me.eliab.sbcontrol.network.packets.PacketDisplayObjective;

class PacketManager_v1_20_2 extends PacketManager_v1_13 {

    PacketManager_v1_20_2(Version version) throws ReflectiveOperationException {
        super(version);
    }

    @Override
    public PacketDisplayObjective createPacketDisplayObjective() {
        return new PacketDisplayObjective_1_20_2();
    }

    static class PacketDisplayObjective_1_20_2 extends PacketDisplayObjective {

        @Override
        public void write(PacketSerializer serializer) {
            serializer.writeEnum(position);
            serializer.writeString(scoreName);
        }

    }

}

package dao;

import dto.PacketsInfo;
import entities.Packet;

import java.sql.Timestamp;
import java.util.List;

public interface PacketDaoCustom {

    void insertPackets(List<Packet> packets);
    int getMaxPacketId();

    List<PacketsInfo> findPacketCounts(Timestamp start, Timestamp end, Integer increment);
    void flushEntityManager();
}

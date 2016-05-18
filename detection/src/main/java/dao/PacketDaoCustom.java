package dao;

import dto.PacketsInfo;
import entities.Packet;

import java.sql.Timestamp;
import java.util.List;

public interface PacketDaoCustom {

    void insertPackets(List<Packet> packets);
    int getMaxPacketId();
    List<String> getPacketSources(int amountOfSources);
    void deleteDDoSPackets();

    List<PacketsInfo> findPacketCounts(Timestamp start, Timestamp end, Integer increment);
    void flushEntityManager();
}

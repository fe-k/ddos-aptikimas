package dao;

import dao.impl.PacketDaoImpl;
import entities.Packet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacketDao extends JpaRepository<Packet, Integer>, PacketDaoCustom {


}

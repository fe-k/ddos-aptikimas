package dao;

import entities.Packet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacketDao extends JpaRepository<Packet, Integer>, PacketDaoCustom {


}

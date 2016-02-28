package dao.impl;

import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;

public class PacketDaoImpl {

    @Autowired
    EntityManager entityManager;
}

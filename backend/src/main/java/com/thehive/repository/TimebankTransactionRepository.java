package com.thehive.repository;

import com.thehive.model.entity.TimebankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimebankTransactionRepository extends JpaRepository<TimebankTransaction, Integer> {
    
    List<TimebankTransaction> findBySenderId(Integer senderId);
    
    List<TimebankTransaction> findByReceiverId(Integer receiverId);
    
    List<TimebankTransaction> findByHandshakeId(Integer handshakeId);
    
    List<TimebankTransaction> findBySenderIdOrReceiverId(Integer senderId, Integer receiverId);
}


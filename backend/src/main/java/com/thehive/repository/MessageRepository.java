package com.thehive.repository;

import com.thehive.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    
    List<Message> findBySenderId(Integer senderId);
    
    List<Message> findByReceiverId(Integer receiverId);
    
    List<Message> findByOfferId(Integer offerId);
    
    List<Message> findByRequestId(Integer requestId);
    
    List<Message> findByHandshakeId(Integer handshakeId);
    
    List<Message> findBySenderIdOrReceiverId(Integer senderId, Integer receiverId);
}


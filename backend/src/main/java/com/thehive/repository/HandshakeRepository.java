package com.thehive.repository;

import com.thehive.model.entity.Handshake;
import com.thehive.model.enums.HandshakeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HandshakeRepository extends JpaRepository<Handshake, Integer> {
    
    List<Handshake> findByOfferId(Integer offerId);
    
    List<Handshake> findBySeekerId(Integer seekerId);
    
    List<Handshake> findByProviderId(Integer providerId);
    
    List<Handshake> findByStatus(HandshakeStatus status);
    
    List<Handshake> findBySeekerIdOrProviderId(Integer seekerId, Integer providerId);
}


package com.thehive.repository;

import com.thehive.model.entity.Handshake;
import com.thehive.model.enums.HandshakeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HandshakeRepository extends JpaRepository<Handshake, Integer> {
    
    List<Handshake> findByOfferId(Integer offerId);
    
    List<Handshake> findBySeekerId(Integer seekerId);
    
    List<Handshake> findByProviderId(Integer providerId);
    
    List<Handshake> findByStatus(HandshakeStatus status);
    
    List<Handshake> findBySeekerIdOrProviderId(Integer seekerId, Integer providerId);
    
    @Query("SELECT h FROM Handshake h WHERE h.offer.id = :offerId AND h.seeker.id = :seekerId")
    Optional<Handshake> findByOfferIdAndSeekerId(@Param("offerId") Integer offerId, @Param("seekerId") Integer seekerId);
    
    @Query("SELECT h FROM Handshake h WHERE (h.seeker.id = :userId OR h.provider.id = :userId) AND h.status = :status")
    List<Handshake> findByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") HandshakeStatus status);
}


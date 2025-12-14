package com.thehive.repository;

import com.thehive.model.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
    
    List<Rating> findByHandshakeId(Integer handshakeId);
    
    List<Rating> findByRaterId(Integer raterId);
    
    List<Rating> findByRateeId(Integer rateeId);

    List<Rating> findByHandshakeOfferId(Integer offerId);
    
    List<Rating> findByHandshakeRequestId(Integer requestId);
    
    Optional<Rating> findByHandshakeIdAndRaterId(Integer handshakeId, Integer raterId);
    
    boolean existsByHandshakeIdAndRaterId(Integer handshakeId, Integer raterId);
}


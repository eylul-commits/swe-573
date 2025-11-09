package com.thehive.repository;

import com.thehive.model.entity.UserBadge;
import com.thehive.model.entity.UserBadgeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadgeId> {
    
    List<UserBadge> findByIdUserId(Integer userId);
    
    List<UserBadge> findByIdBadgeId(Integer badgeId);
}


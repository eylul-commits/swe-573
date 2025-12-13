package com.thehive.repository;

import com.thehive.model.entity.UserAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Integer> {
    
    List<UserAction> findByUserIdOrderByCreatedAtDesc(Integer userId);
}


package com.thehive.repository;

import com.thehive.model.entity.SemanticTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SemanticTagRepository extends JpaRepository<SemanticTag, Integer> {
    
    Optional<SemanticTag> findByName(String name);
    
    Optional<SemanticTag> findByWikidataId(String wikidataId);
}


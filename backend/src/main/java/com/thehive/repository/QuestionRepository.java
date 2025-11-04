package com.thehive.repository;

import com.thehive.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    
    List<Question> findByOfferId(Integer offerId);
    
    List<Question> findByRequestId(Integer requestId);
    
    List<Question> findByAskerId(Integer askerId);
}


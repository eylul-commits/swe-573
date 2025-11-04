package com.thehive.repository;

import com.thehive.model.entity.Request;
import com.thehive.model.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {
    
    List<Request> findBySeekerId(Integer seekerId);
    
    List<Request> findByStatus(ItemStatus status);
    
    List<Request> findByProvince(String province);
    
    List<Request> findByProvinceAndDistrict(String province, String district);
}


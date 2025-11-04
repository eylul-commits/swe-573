package com.thehive.repository;

import com.thehive.model.entity.Offer;
import com.thehive.model.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Integer> {
    
    List<Offer> findByProviderId(Integer providerId);
    
    List<Offer> findByStatus(ItemStatus status);
    
    List<Offer> findByProvince(String province);
    
    List<Offer> findByProvinceAndDistrict(String province, String district);
}


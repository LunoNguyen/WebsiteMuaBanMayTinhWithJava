package com.nhom8.DoAnJava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom8.DoAnJava.model.NhaSanXuat;

@Repository
public interface NhaSanXuatRepository extends JpaRepository<NhaSanXuat, String> {
    
}
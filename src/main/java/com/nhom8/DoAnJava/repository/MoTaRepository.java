package com.nhom8.DoAnJava.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom8.DoAnJava.model.MoTa;

@Repository
public interface MoTaRepository extends JpaRepository<MoTa, String> {
    List<MoTa> findByMaSP(String maSP);
    MoTa findTopByOrderByMaMTDesc();
    void deleteByMaSP(String maSP);
}

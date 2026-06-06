package com.nhom8.DoAnJava.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom8.DoAnJava.model.CapNhatGia;

@Repository
public interface CapNhatGiaRepository extends JpaRepository<CapNhatGia, LocalDateTime> {
}
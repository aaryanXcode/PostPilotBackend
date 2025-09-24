package com.back.postpilot.repository;

import com.back.postpilot.entity.ContentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentImageRepository extends JpaRepository<ContentImage, Long> {
}



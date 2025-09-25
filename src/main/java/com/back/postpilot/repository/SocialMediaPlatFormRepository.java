package com.back.postpilot.repository;

import com.back.postpilot.entity.SocialMediaPlatForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialMediaPlatFormRepository extends JpaRepository<SocialMediaPlatForm, Long> {
}

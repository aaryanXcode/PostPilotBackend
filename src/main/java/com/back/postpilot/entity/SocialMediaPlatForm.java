package com.back.postpilot.entity;

import com.back.postpilot.EnumTypeConstants.ContentPlatForms;
import jakarta.persistence.*;

@Entity
@Table(name = "p_social_media_platform")
public class SocialMediaPlatForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform")
    private ContentPlatForms platform;
}

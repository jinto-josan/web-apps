package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.infrastructure.persistence.entity.InboxMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InboxRepository extends JpaRepository<InboxMessageEntity, String> { }

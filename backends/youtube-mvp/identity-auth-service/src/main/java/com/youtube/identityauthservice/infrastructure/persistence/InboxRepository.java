package com.youtube.identityauthservice.domain.repositories;

import com.youtube.identityauthservice.domain.model.InboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InboxRepository extends JpaRepository<InboxMessage, String> { }

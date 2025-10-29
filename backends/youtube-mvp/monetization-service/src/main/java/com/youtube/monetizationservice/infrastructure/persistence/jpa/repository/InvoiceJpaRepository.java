package com.youtube.monetizationservice.infrastructure.persistence.jpa.repository;

import com.youtube.monetizationservice.infrastructure.persistence.jpa.entity.InvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, String> {
	List<InvoiceJpaEntity> findByMembershipId(String membershipId);
	List<InvoiceJpaEntity> findByChannelId(String channelId);
	List<InvoiceJpaEntity> findBySubscriberId(String subscriberId);
}

package com.youtube.monetizationservice.infrastructure.persistence.jpa.repository;

import com.youtube.monetizationservice.infrastructure.persistence.jpa.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, String> {
	Optional<PaymentJpaEntity> findByExternalPaymentId(String externalPaymentId);
	List<PaymentJpaEntity> findByInvoiceId(String invoiceId);
	List<PaymentJpaEntity> findByMembershipId(String membershipId);
}

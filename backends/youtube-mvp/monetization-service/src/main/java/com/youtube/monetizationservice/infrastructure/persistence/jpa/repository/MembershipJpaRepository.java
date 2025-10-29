package com.youtube.monetizationservice.infrastructure.persistence.jpa.repository;

import com.youtube.monetizationservice.infrastructure.persistence.jpa.entity.MembershipJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipJpaRepository extends JpaRepository<MembershipJpaEntity, String> {
	List<MembershipJpaEntity> findByChannelId(String channelId);
	List<MembershipJpaEntity> findBySubscriberId(String subscriberId);
	Optional<MembershipJpaEntity> findByChannelIdAndSubscriberId(String channelId, String subscriberId);
}

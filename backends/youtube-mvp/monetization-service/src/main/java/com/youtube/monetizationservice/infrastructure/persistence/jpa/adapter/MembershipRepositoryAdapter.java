package com.youtube.monetizationservice.infrastructure.persistence.jpa.adapter;

import com.youtube.monetizationservice.domain.models.Membership;
import com.youtube.monetizationservice.domain.repository.MembershipRepository;
import com.youtube.monetizationservice.infrastructure.persistence.jpa.entity.MembershipJpaEntity;
import com.youtube.monetizationservice.infrastructure.persistence.jpa.mapper.MembershipMapper;
import com.youtube.monetizationservice.infrastructure.persistence.jpa.repository.MembershipJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MembershipRepositoryAdapter implements MembershipRepository {

	private final MembershipJpaRepository jpaRepository;

	@Override
	public Membership save(Membership membership) {
		MembershipJpaEntity saved = jpaRepository.save(MembershipMapper.toEntity(membership));
		return MembershipMapper.toDomain(saved);
	}

	@Override
	public Optional<Membership> findById(String membershipId) {
		return jpaRepository.findById(membershipId).map(MembershipMapper::toDomain);
	}

	@Override
	public List<Membership> findByChannelId(String channelId) {
		return jpaRepository.findByChannelId(channelId).stream().map(MembershipMapper::toDomain).collect(Collectors.toList());
	}

	@Override
	public List<Membership> findBySubscriberId(String subscriberId) {
		return jpaRepository.findBySubscriberId(subscriberId).stream().map(MembershipMapper::toDomain).collect(Collectors.toList());
	}

	@Override
	public Optional<Membership> findByChannelIdAndSubscriberId(String channelId, String subscriberId) {
		return jpaRepository.findByChannelIdAndSubscriberId(channelId, subscriberId).map(MembershipMapper::toDomain);
	}

	@Override
	public boolean existsById(String membershipId) {
		return jpaRepository.existsById(membershipId);
	}

	@Override
	public void deleteById(String membershipId) {
		jpaRepository.deleteById(membershipId);
	}
}

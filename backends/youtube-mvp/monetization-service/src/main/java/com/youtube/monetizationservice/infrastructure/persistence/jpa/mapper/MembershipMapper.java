package com.youtube.monetizationservice.infrastructure.persistence.jpa.mapper;

import com.youtube.monetizationservice.domain.models.Membership;
import com.youtube.monetizationservice.domain.valueobjects.MembershipTier;
import com.youtube.monetizationservice.domain.valueobjects.Money;
import com.youtube.monetizationservice.infrastructure.persistence.jpa.entity.MembershipJpaEntity;

public final class MembershipMapper {
	private MembershipMapper() {}

	public static MembershipJpaEntity toEntity(Membership domain) {
		return MembershipJpaEntity.builder()
			.id(domain.getId())
			.channelId(domain.getChannelId())
			.subscriberId(domain.getSubscriberId())
			.tier(domain.getTier().name())
			.monthlyFeeAmount(domain.getMonthlyFee().getAmount())
			.monthlyFeeCurrency(domain.getMonthlyFee().getCurrency().getCurrencyCode())
			.status(domain.getStatus().name())
			.startDate(domain.getStartDate())
			.endDate(domain.getEndDate())
			.nextBillingDate(domain.getNextBillingDate())
			.paymentMethodId(domain.getPaymentMethodId())
			.externalSubscriptionId(domain.getExternalSubscriptionId())
			.version(domain.getVersion())
			.createdAt(domain.getCreatedAt())
			.updatedAt(domain.getUpdatedAt())
			.etag(domain.getEtag())
			.build();
	}

	public static Membership toDomain(MembershipJpaEntity entity) {
		return Membership.builder()
			.id(entity.getId())
			.channelId(entity.getChannelId())
			.subscriberId(entity.getSubscriberId())
			.tier(MembershipTier.valueOf(entity.getTier()))
			.monthlyFee(new Money(entity.getMonthlyFeeAmount(), entity.getMonthlyFeeCurrency()))
			.status(Membership.MembershipStatus.valueOf(entity.getStatus()))
			.startDate(entity.getStartDate())
			.endDate(entity.getEndDate())
			.nextBillingDate(entity.getNextBillingDate())
			.paymentMethodId(entity.getPaymentMethodId())
			.externalSubscriptionId(entity.getExternalSubscriptionId())
			.version(entity.getVersion() == null ? 1 : entity.getVersion())
			.createdAt(entity.getCreatedAt())
			.updatedAt(entity.getUpdatedAt())
			.etag(entity.getEtag())
			.build();
	}
}

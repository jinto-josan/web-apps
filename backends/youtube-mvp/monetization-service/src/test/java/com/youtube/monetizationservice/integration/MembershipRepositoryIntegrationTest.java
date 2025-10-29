package com.youtube.monetizationservice.integration;

import com.youtube.monetizationservice.domain.models.Membership;
import com.youtube.monetizationservice.domain.repository.MembershipRepository;
import com.youtube.monetizationservice.domain.valueobjects.MembershipTier;
import com.youtube.monetizationservice.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class MembershipRepositoryIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
			.withDatabaseName("monetization")
			.withUsername("postgres")
			.withPassword("postgres");

	@DynamicPropertySource
	static void registerProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
		registry.add("spring.liquibase.enabled", () -> true);
	}

	@Autowired
	MembershipRepository membershipRepository;

	@Test
	void saveAndLoadMembership() {
		Membership m = Membership.builder()
			.id(UUID.randomUUID().toString())
			.channelId("channel-1")
			.subscriberId("user-1")
			.tier(MembershipTier.BASIC)
			.monthlyFee(new Money(new BigDecimal("4.99"), Currency.getInstance("USD")))
			.status(Membership.MembershipStatus.ACTIVE)
			.startDate(Instant.now())
			.nextBillingDate(Instant.now().plusSeconds(30*24*3600))
			.version(1)
			.createdAt(Instant.now())
			.updatedAt(Instant.now())
			.build();

		Membership saved = membershipRepository.save(m);
		assertThat(saved.getId()).isNotNull();

		Membership loaded = membershipRepository.findById(saved.getId()).orElseThrow();
		assertThat(loaded.getSubscriberId()).isEqualTo("user-1");
	}
}

package com.youtube.monetizationservice.presentation.rest;

import com.youtube.monetizationservice.application.usecase.PaymentUseCase;
import com.youtube.monetizationservice.domain.valueobjects.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Payments", description = "Payment webhook processing")
public class PaymentController {

	private final PaymentUseCase paymentUseCase;

	@PostMapping("/webhook")
	@Operation(summary = "Payment provider webhook",
			description = "Processes payment webhook and updates invoice, ledger, and events")
	public ResponseEntity<Void> webhook(
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@RequestHeader(value = "X-Signature", required = false) String signature,
			@Valid @RequestBody WebhookPayload payload) {
		log.info("Received payment webhook: {}", payload);
		Money amount = new Money(payload.getAmount(), payload.getCurrency());
		paymentUseCase.processWebhook(payload.getInvoiceId(), payload.getExternalPaymentId(), amount, signature, payload.toString());
		return ResponseEntity.status(HttpStatus.ACCEPTED).build();
	}

	@Data
	public static class WebhookPayload {
		@NotBlank
		private String invoiceId;
		@NotBlank
		private String externalPaymentId;
		@NotBlank
		private String currency;
		@NotBlank
		private BigDecimal amount;
	}
}

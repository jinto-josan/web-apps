package com.youtube.monetizationservice.presentation.rest;

import com.youtube.monetizationservice.application.dto.InvoiceResponse;
import com.youtube.monetizationservice.domain.models.Invoice;
import com.youtube.monetizationservice.domain.repository.InvoiceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice queries")
public class InvoiceController {

	private final InvoiceRepository invoiceRepository;

	@GetMapping
	@Operation(summary = "List invoices", description = "List invoices by membershipId or channelId with pagination")
	public ResponseEntity<Paged<InvoiceResponse>> list(
			@RequestParam(required = false) String membershipId,
			@RequestParam(required = false) String channelId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

		List<Invoice> invoices;
		if (membershipId != null) {
			invoices = invoiceRepository.findByMembershipId(membershipId);
		} else if (channelId != null) {
			invoices = invoiceRepository.findByChannelId(channelId);
		} else {
			return ResponseEntity.badRequest().build();
		}

		int start = Math.max(0, page * size);
		int end = Math.min(invoices.size(), start + size);
		List<Invoice> pageItems = start >= invoices.size() ? List.of() : invoices.subList(start, end);

		List<InvoiceResponse> items = pageItems.stream().map(inv -> InvoiceResponse.builder()
			.id(inv.getId())
			.membershipId(inv.getMembershipId())
			.channelId(inv.getChannelId())
			.subscriberId(inv.getSubscriberId())
			.amount(inv.getAmount().getAmount())
			.currency(inv.getAmount().getCurrency().getCurrencyCode())
			.status(inv.getStatus())
			.dueDate(inv.getDueDate())
			.paidDate(inv.getPaidDate())
			.createdAt(inv.getCreatedAt())
			.metadata(inv.getMetadata())
			.build()).collect(Collectors.toList());

		Paged<InvoiceResponse> response = new Paged<>(items, page, size, invoices.size(), (int)Math.ceil((double) invoices.size() / size));

		String etag = computeEtag(response);
		if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		}

		return ResponseEntity.ok()
			.header(HttpHeaders.ETAG, etag)
			.body(response);
	}

	private String computeEtag(Object obj) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(obj.toString().getBytes(StandardCharsets.UTF_8));
			return '"' + Base64.getEncoder().encodeToString(digest) + '"';
		} catch (NoSuchAlgorithmException e) {
			return '"' + Integer.toHexString(obj.hashCode()) + '"';
		}
	}

	public static class Paged<T> {
		public final List<T> items;
		public final int page;
		public final int size;
		public final int totalElements;
		public final int totalPages;
		public Paged(List<T> items, int page, int size, int totalElements, int totalPages) {
			this.items = items; this.page = page; this.size = size; this.totalElements = totalElements; this.totalPages = totalPages;
		}
		@Override public String toString() { return String.valueOf(totalElements) + ':' + String.valueOf(page) + ':' + String.valueOf(size); }
	}
}

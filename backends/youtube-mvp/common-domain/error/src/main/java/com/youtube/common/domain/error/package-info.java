/**
 * Common error handling infrastructure.
 * 
 * <p>This package provides:
 * <ul>
 *   <li>{@link com.youtube.common.domain.error.DomainException} - Base exception for domain errors</li>
 *   <li>{@link com.youtube.common.domain.error.ValidationException} - Validation failures</li>
 *   <li>{@link com.youtube.common.domain.error.NotFoundException} - Resource not found</li>
 *   <li>{@link com.youtube.common.domain.error.ConflictException} - Conflict errors</li>
 *   <li>{@link com.youtube.common.domain.error.UnauthorizedException} - Unauthorized access</li>
 *   <li>{@link com.youtube.common.domain.error.ForbiddenException} - Forbidden access</li>
 *   <li>{@link com.youtube.common.domain.error.ErrorCodes} - Common error code constants</li>
 *   <li>{@link com.youtube.common.domain.error.ProblemDetailBuilder} - Utility for building RFC 7807 Problem Details</li>
 *   <li>{@link com.youtube.common.domain.error.GlobalExceptionHandler} - Base exception handler for REST controllers</li>
 *   <li>{@link com.youtube.common.domain.error.Result} - Functional result type for error handling</li>
 * </ul>
 */
package com.youtube.common.domain.error;


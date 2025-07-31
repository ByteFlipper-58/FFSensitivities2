package com.byteflipper.ffsensitivities.data.remote

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * Basic unit tests for BugReportApiClient
 * 
 * Note: These are basic tests. In a real project, you would want to:
 * 1. Use a mocking framework like MockK
 * 2. Mock the HTTP client responses
 * 3. Test network errors and timeouts
 * 4. Test validation scenarios
 */
class BugReportApiClientTest {
    
    @Test
    fun `test validation - empty tag should fail`() {
        val result = ValidationUtils.validateBugReport("", "test message")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        assertEquals("Tag cannot be empty", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `test validation - message too long should fail`() {
        val longMessage = "a".repeat(ApiConfig.MAX_MESSAGE_LENGTH + 1)
        val result = ValidationUtils.validateBugReport("test-tag", longMessage)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
        assertTrue(result.exceptionOrNull()?.message?.contains("too long") == true)
    }
    
    @Test
    fun `test validation - valid input should succeed`() {
        val result = ValidationUtils.validateBugReport("test-tag", "test message")
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `test feedback validation - empty name should fail`() {
        val result = ValidationUtils.validateFeedback("", "test@example.com", "test text")
        assertTrue(result.isFailure)
        assertEquals("Name cannot be empty", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `test feedback validation - invalid email should fail`() {
        val result = ValidationUtils.validateFeedback("John", "invalid-email", "test text")
        assertTrue(result.isFailure)
        assertEquals("Invalid email format", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `test feedback validation - valid input should succeed`() {
        val result = ValidationUtils.validateFeedback("John", "john@example.com", "test text")
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `test ApiResult sealed class`() {
        val success = ApiResult.Success(ApiResponse("ok", "test"))
        val error = ApiResult.Error("test error")
        
        assertTrue(success is ApiResult.Success)
        assertTrue(error is ApiResult.Error)
        
        when (success) {
            is ApiResult.Success -> assertEquals("ok", success.data.status)
            is ApiResult.Error -> fail("Should not be error")
        }
        
        when (error) {
            is ApiResult.Success -> fail("Should not be success")
            is ApiResult.Error -> assertEquals("test error", error.message)
        }
    }
} 
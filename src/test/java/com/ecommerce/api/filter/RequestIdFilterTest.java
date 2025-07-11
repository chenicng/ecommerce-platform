package com.ecommerce.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class RequestIdFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private FilterConfig filterConfig;

    private RequestIdFilter requestIdFilter;

    @BeforeEach
    void setUp() {
        requestIdFilter = new RequestIdFilter();
        MDC.clear(); // Clear MDC before each test
    }

    @Test
    void shouldGenerateRequestIdWhenNotProvided() throws IOException, ServletException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn(null);

        // When
        requestIdFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setHeader(eq("X-Request-ID"), anyString());
        verify(filterChain).doFilter(request, response);
        
        // Verify MDC was cleared after processing
        assertNull(MDC.get("requestId"));
    }

    @Test
    void shouldUseProvidedRequestId() throws IOException, ServletException {
        // Given
        String providedRequestId = "test-request-id-123";
        when(request.getHeader("X-Request-ID")).thenReturn(providedRequestId);

        // When
        requestIdFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setHeader("X-Request-ID", providedRequestId);
        verify(filterChain).doFilter(request, response);
        
        // Verify MDC was cleared after processing
        assertNull(MDC.get("requestId"));
    }

    @Test
    void shouldGenerateRequestIdWhenEmptyStringProvided() throws IOException, ServletException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn("");

        // When
        requestIdFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setHeader(eq("X-Request-ID"), anyString());
        verify(filterChain).doFilter(request, response);
        
        // Verify MDC was cleared after processing
        assertNull(MDC.get("requestId"));
    }

    @Test
    void shouldGenerateRequestIdWhenWhitespaceOnlyProvided() throws IOException, ServletException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn("   ");

        // When
        requestIdFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setHeader(eq("X-Request-ID"), anyString());
        verify(filterChain).doFilter(request, response);
        
        // Verify MDC was cleared after processing
        assertNull(MDC.get("requestId"));
    }

    @Test
    void shouldClearMDCEvenWhenFilterChainThrowsException() throws IOException, ServletException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn("test-id");
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

        // When/Then
        assertThrows(ServletException.class, () -> {
            requestIdFilter.doFilter(request, response, filterChain);
        });

        // Verify MDC was still cleared despite exception
        assertNull(MDC.get("requestId"));
    }

    @Test
    void shouldClearMDCEvenWhenFilterChainThrowsIOException() throws IOException, ServletException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn("test-id");
        doThrow(new IOException("Test IO exception")).when(filterChain).doFilter(request, response);

        // When/Then
        assertThrows(IOException.class, () -> {
            requestIdFilter.doFilter(request, response, filterChain);
        });

        // Verify MDC was still cleared despite exception
        assertNull(MDC.get("requestId"));
    }

    @Test
    void shouldGenerateUniqueRequestIds() throws IOException, ServletException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn(null);

        // When
        requestIdFilter.doFilter(request, response, filterChain);
        
        // Capture first request ID
        ArgumentCaptor<String> firstCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq("X-Request-ID"), firstCaptor.capture());
        String firstRequestId = firstCaptor.getValue();

        reset(response);
        requestIdFilter.doFilter(request, response, filterChain);
        
        // Capture second request ID
        ArgumentCaptor<String> secondCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq("X-Request-ID"), secondCaptor.capture());
        String secondRequestId = secondCaptor.getValue();

        // Then
        assertNotNull(firstRequestId);
        assertNotNull(secondRequestId);
        assertNotEquals(firstRequestId, secondRequestId);
        assertEquals(16, firstRequestId.length());
        assertEquals(16, secondRequestId.length());
    }

    @Test
    void shouldGenerateRequestIdWithCorrectFormat() throws IOException, ServletException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn(null);

        // When
        requestIdFilter.doFilter(request, response, filterChain);

        // Then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq("X-Request-ID"), captor.capture());
        String requestId = captor.getValue();
        
        assertNotNull(requestId);
        assertEquals(16, requestId.length());
        assertTrue(requestId.matches("[a-f0-9]+"), "Request ID should only contain hex characters");
    }

    @Test
    void shouldInitializeWithoutException() throws ServletException {
        // When/Then
        assertDoesNotThrow(() -> requestIdFilter.init(filterConfig));
    }

    @Test
    void shouldDestroyWithoutException() {
        // When/Then
        assertDoesNotThrow(() -> requestIdFilter.destroy());
    }

    @Test
    void shouldHandleNullFilterConfig() throws ServletException {
        // When/Then
        assertDoesNotThrow(() -> requestIdFilter.init(null));
    }

    @Test
    void shouldCreateFilterInstance() {
        // When
        RequestIdFilter filter = new RequestIdFilter();

        // Then
        assertNotNull(filter);
    }
} 
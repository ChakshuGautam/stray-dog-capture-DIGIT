# Zuul Gateway Configuration for CCRS (PGR-Based Option)

## Overview

This document describes how to configure the DIGIT Zuul Gateway to support the **CCRS (Citizen Complaint Registry Service)** implementation that uses PGR as its backend. The configuration enables:

1. **URL Aliasing** - Map `/ccrs/v1/*` endpoints to PGR service endpoints
2. **Public Tracking API** - Expose `/_track` without authentication
3. **Request Transformation** - Convert CCRS request format to PGR format
4. **Response Sanitization** - Remove PII from public tracking responses

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              Zuul Gateway                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐              │
│  │  PRE Filters │ → │    Routing    │ → │ POST Filters │              │
│  └──────────────┘    └──────────────┘    └──────────────┘              │
│         │                   │                   │                       │
│         ▼                   ▼                   ▼                       │
│  ┌─────────────┐     ┌─────────────┐    ┌─────────────┐               │
│  │ AuthFilter  │     │ Route Config │    │ Response    │               │
│  │ (skip for   │     │ ccrs → pgr   │    │ Transform   │               │
│  │  _track)    │     │              │    │ (for _track)│               │
│  └─────────────┘     └─────────────┘    └─────────────┘               │
│         │                   │                   │                       │
│         ▼                   ▼                   ▼                       │
│  ┌─────────────┐     ┌─────────────┐    ┌─────────────┐               │
│  │ Request     │     │             │    │ PII         │               │
│  │ Transform   │     │             │    │ Sanitizer   │               │
│  │ (for _track)│     │             │    │             │               │
│  └─────────────┘     └─────────────┘    └─────────────┘               │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                          ┌─────────────────┐
                          │   PGR Service   │
                          │  (pgr-services) │
                          └─────────────────┘
```

---

## 1. Route Configuration

### 1.1 Helm Values Configuration

Add CCRS routes to the gateway's Helm values:

```yaml
# values.yaml for egov-gateway
ingress:
  context: "gateway"

zuul:
  routes:
    # CCRS Create - Maps to PGR create
    ccrs-create:
      path: /ccrs/v1/report/_create
      serviceId: pgr-services
      stripPrefix: true
      url: /pgr-services/v2/request/_create

    # CCRS Update - Maps to PGR update
    ccrs-update:
      path: /ccrs/v1/report/_update
      serviceId: pgr-services
      stripPrefix: true
      url: /pgr-services/v2/request/_update

    # CCRS Search - Maps to PGR search
    ccrs-search:
      path: /ccrs/v1/report/_search
      serviceId: pgr-services
      stripPrefix: true
      url: /pgr-services/v2/request/_search

    # CCRS Track - Public endpoint (no auth)
    ccrs-track:
      path: /ccrs/v1/report/_track
      serviceId: pgr-services
      stripPrefix: true
      url: /pgr-services/v2/request/_search

    # PGR passthrough (for direct access if needed)
    pgr-services:
      path: /pgr-services/**
      serviceId: pgr-services
      stripPrefix: false

# Public endpoints (no authentication required)
publicEndpoints:
  - /ccrs/v1/report/_track
  - /user/oauth/token
  - /user-otp/v1/_send
  - /localization/messages/v1/_search
```

### 1.2 Application Properties

```properties
# application.properties for egov-gateway

# CCRS Route Mappings
zuul.routes.ccrs-create.path=/ccrs/v1/report/_create
zuul.routes.ccrs-create.url=http://pgr-services:8080/pgr-services/v2/request/_create
zuul.routes.ccrs-create.stripPrefix=true

zuul.routes.ccrs-update.path=/ccrs/v1/report/_update
zuul.routes.ccrs-update.url=http://pgr-services:8080/pgr-services/v2/request/_update
zuul.routes.ccrs-update.stripPrefix=true

zuul.routes.ccrs-search.path=/ccrs/v1/report/_search
zuul.routes.ccrs-search.url=http://pgr-services:8080/pgr-services/v2/request/_search
zuul.routes.ccrs-search.stripPrefix=true

zuul.routes.ccrs-track.path=/ccrs/v1/report/_track
zuul.routes.ccrs-track.url=http://pgr-services:8080/pgr-services/v2/request/_search
zuul.routes.ccrs-track.stripPrefix=true

# Sensitive headers to not forward
zuul.routes.ccrs-track.sensitiveHeaders=Cookie,Set-Cookie

# Public endpoints list
egov.gateway.public.endpoints=/ccrs/v1/report/_track,/user/oauth/token
```

---

## 2. Gateway Filters

### 2.1 Filter Registration

Create a configuration class to register CCRS filters:

```java
package org.egov.gateway.filters.ccrs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for CCRS gateway filters.
 * Registers request/response transformation filters for the public tracking API.
 */
@Configuration
public class CCRSFilterConfiguration {

    @Bean
    public CCRSTrackRequestFilter ccrsTrackRequestFilter() {
        return new CCRSTrackRequestFilter();
    }

    @Bean
    public CCRSTrackResponseFilter ccrsTrackResponseFilter() {
        return new CCRSTrackResponseFilter();
    }

    @Bean
    public CCRSAuthBypassFilter ccrsAuthBypassFilter() {
        return new CCRSAuthBypassFilter();
    }
}
```

### 2.2 Authentication Bypass Filter

```java
package org.egov.gateway.filters.ccrs;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * CCRSAuthBypassFilter - Marks public CCRS endpoints to skip authentication.
 *
 * This filter runs early in the PRE chain and sets a context flag that
 * the main AuthFilter checks before enforcing authentication.
 */
@Slf4j
@Component
public class CCRSAuthBypassFilter extends ZuulFilter {

    @Value("${egov.gateway.public.endpoints:/ccrs/v1/report/_track}")
    private String publicEndpointsConfig;

    private Set<String> publicEndpoints;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        // Run before AuthFilter (which is typically order 1-2)
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String uri = ctx.getRequest().getRequestURI();

        if (isPublicEndpoint(uri)) {
            log.debug("Marking {} as public endpoint - skipping auth", uri);
            ctx.set("skipAuth", true);
            ctx.set("isPublicEndpoint", true);
        }

        return null;
    }

    private boolean isPublicEndpoint(String uri) {
        if (publicEndpoints == null) {
            publicEndpoints = new HashSet<>(
                Arrays.asList(publicEndpointsConfig.split(","))
            );
        }

        return publicEndpoints.stream()
                .anyMatch(endpoint -> uri.contains(endpoint.trim()));
    }
}
```

### 2.3 Request Transformation Filter

```java
package org.egov.gateway.filters.ccrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * CCRSTrackRequestFilter - Transforms public tracking requests to PGR format.
 *
 * Input (CCRS Track Request):
 * {
 *   "trackingId": "ABC123",
 *   "tenantId": "ncr"
 * }
 *
 * Output (PGR Search Request):
 * {
 *   "RequestInfo": { ... system request info ... },
 *   "criteria": {
 *     "serviceRequestId": "ABC123",
 *     "tenantId": "ncr"
 *   }
 * }
 */
@Slf4j
@Component
public class CCRSTrackRequestFilter extends ZuulFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${egov.default.tenant:ncr}")
    private String defaultTenant;

    private static final String TRACK_ENDPOINT = "/ccrs/v1/report/_track";

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        // Run after auth bypass but before routing
        return 5;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String uri = ctx.getRequest().getRequestURI();
        return uri.contains(TRACK_ENDPOINT);
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();

        try {
            // Read original request body
            InputStream inputStream = ctx.getRequest().getInputStream();
            String requestBody = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

            log.debug("Original track request: {}", requestBody);

            // Parse and transform
            JsonNode inputNode = objectMapper.readTree(requestBody);
            ObjectNode pgrRequest = buildPGRSearchRequest(inputNode);

            String transformedBody = objectMapper.writeValueAsString(pgrRequest);
            log.debug("Transformed PGR request: {}", transformedBody);

            // Replace request body
            byte[] bytes = transformedBody.getBytes(StandardCharsets.UTF_8);
            ctx.setRequest(new HttpServletRequestWrapper(ctx.getRequest()) {
                @Override
                public ServletInputStream getInputStream() throws IOException {
                    return new DelegatingServletInputStream(
                        new ByteArrayInputStream(bytes)
                    );
                }

                @Override
                public int getContentLength() {
                    return bytes.length;
                }

                @Override
                public long getContentLengthLong() {
                    return bytes.length;
                }
            });

        } catch (Exception e) {
            log.error("Error transforming CCRS track request", e);
            // Let it pass through - PGR will return error
        }

        return null;
    }

    /**
     * Build PGR search request from CCRS track request.
     */
    private ObjectNode buildPGRSearchRequest(JsonNode input) {
        ObjectNode request = objectMapper.createObjectNode();

        // Create minimal RequestInfo for system call
        ObjectNode requestInfo = objectMapper.createObjectNode();
        requestInfo.put("apiId", "ccrs-track");
        requestInfo.put("ver", "1.0");
        requestInfo.put("ts", System.currentTimeMillis());
        requestInfo.put("action", "_search");
        requestInfo.put("did", "1");
        requestInfo.put("key", "");
        requestInfo.put("msgId", "ccrs-track-" + System.currentTimeMillis());
        requestInfo.put("authToken", ""); // No auth for public endpoint
        request.set("RequestInfo", requestInfo);

        // Build search criteria
        ObjectNode criteria = objectMapper.createObjectNode();

        // Handle different input formats
        if (input.has("trackingId")) {
            criteria.put("serviceRequestId", input.get("trackingId").asText());
        } else if (input.has("reportNumber")) {
            criteria.put("serviceRequestId", input.get("reportNumber").asText());
        } else if (input.has("serviceRequestId")) {
            criteria.put("serviceRequestId", input.get("serviceRequestId").asText());
        }

        // Tenant ID (required for PGR)
        String tenantId = input.has("tenantId")
            ? input.get("tenantId").asText()
            : defaultTenant;
        criteria.put("tenantId", tenantId);

        request.set("criteria", criteria);

        return request;
    }
}

/**
 * Helper class to wrap InputStream as ServletInputStream.
 */
class DelegatingServletInputStream extends ServletInputStream {
    private final InputStream sourceStream;

    public DelegatingServletInputStream(InputStream sourceStream) {
        this.sourceStream = sourceStream;
    }

    @Override
    public int read() throws IOException {
        return sourceStream.read();
    }

    @Override
    public boolean isFinished() {
        try {
            return sourceStream.available() == 0;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(javax.servlet.ReadListener readListener) {
        throw new UnsupportedOperationException();
    }
}
```

### 2.4 Response Sanitization Filter

```java
package org.egov.gateway.filters.ccrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * CCRSTrackResponseFilter - Sanitizes PGR response for public tracking.
 *
 * Removes PII:
 * - Reporter phone number
 * - Reporter name (partially masked)
 * - Full address details
 * - Internal IDs
 * - Assignee details
 *
 * Keeps:
 * - Report number / tracking ID
 * - Current status with localized description
 * - Timeline of status changes
 * - Locality/ward (without full address)
 * - Resolution information (if resolved)
 */
@Slf4j
@Component
public class CCRSTrackResponseFilter extends ZuulFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${egov.localisation.default.locale:en_IN}")
    private String defaultLocale;

    private static final String TRACK_ENDPOINT = "/ccrs/v1/report/_track";

    // Status to user-friendly message mapping
    private static final Map<String, String> STATUS_MESSAGES = new HashMap<>() {{
        put("PENDING_VALIDATION", "Your report has been received and is being validated");
        put("PENDING_VERIFICATION", "Your report is under review by our team");
        put("VERIFIED", "Your report has been verified and approved");
        put("ASSIGNED", "A Municipal Corporation officer has been assigned to your report");
        put("IN_PROGRESS", "Field visit is in progress");
        put("CAPTURED", "The dog has been captured successfully");
        put("RESOLVED", "Your report has been resolved. Thank you for reporting!");
        put("REJECTED", "Your report could not be verified");
        put("AUTO_REJECTED", "Your report did not pass automated validation");
        put("DUPLICATE", "This appears to be a duplicate report");
        put("UNABLE_TO_LOCATE", "The dog could not be located at the reported location");
    }};

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        // Run after response is received
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String uri = ctx.getRequest().getRequestURI();
        return uri.contains(TRACK_ENDPOINT);
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();

        try {
            InputStream stream = ctx.getResponseDataStream();
            if (stream == null) {
                return null;
            }

            String responseBody = IOUtils.toString(stream, StandardCharsets.UTF_8);
            log.debug("Original PGR response: {}", responseBody);

            JsonNode pgrResponse = objectMapper.readTree(responseBody);
            ObjectNode trackResponse = buildTrackResponse(pgrResponse);

            String sanitizedResponse = objectMapper.writeValueAsString(trackResponse);
            log.debug("Sanitized track response: {}", sanitizedResponse);

            ctx.setResponseBody(sanitizedResponse);
            ctx.getResponse().setContentType("application/json;charset=UTF-8");

        } catch (Exception e) {
            log.error("Error transforming CCRS track response", e);
            setErrorResponse(ctx, "Error processing tracking request");
        }

        return null;
    }

    /**
     * Build sanitized tracking response from PGR response.
     */
    private ObjectNode buildTrackResponse(JsonNode pgrResponse) {
        ObjectNode response = objectMapper.createObjectNode();

        // Copy ResponseInfo
        if (pgrResponse.has("ResponseInfo")) {
            response.set("ResponseInfo", pgrResponse.get("ResponseInfo"));
        }

        // Process ServiceWrappers (PGR response format)
        JsonNode serviceWrappers = pgrResponse.path("ServiceWrappers");

        if (serviceWrappers.isArray() && serviceWrappers.size() > 0) {
            JsonNode wrapper = serviceWrappers.get(0);
            JsonNode service = wrapper.path("service");

            ObjectNode tracking = objectMapper.createObjectNode();

            // Basic tracking info
            tracking.put("reportNumber", service.path("serviceRequestId").asText());
            tracking.put("trackingId", extractTrackingId(service));

            // Status
            String status = service.path("applicationStatus").asText();
            tracking.put("status", status);
            tracking.put("statusDescription", getStatusMessage(status));
            tracking.put("isTerminal", isTerminalStatus(status));

            // Dates
            JsonNode auditDetails = service.path("auditDetails");
            tracking.put("reportedDate", auditDetails.path("createdTime").asLong());
            if (service.has("resolvedTime")) {
                tracking.put("resolvedDate", service.path("resolvedTime").asLong());
            }

            // Location (sanitized - no full address)
            ObjectNode location = buildSanitizedLocation(service.path("address"));
            tracking.set("location", location);

            // Service type
            tracking.put("serviceType", "Stray Dog Report");
            tracking.put("serviceCode", service.path("serviceCode").asText());

            // Resolution info (if resolved)
            if (isTerminalStatus(status)) {
                ObjectNode resolution = buildResolutionInfo(service);
                tracking.set("resolution", resolution);
            }

            // Timeline
            JsonNode workflow = wrapper.path("workflow");
            if (workflow.isArray()) {
                ArrayNode timeline = buildTimeline(workflow);
                tracking.set("timeline", timeline);
            }

            response.set("tracking", tracking);
            response.put("found", true);

        } else {
            response.putNull("tracking");
            response.put("found", false);
            response.put("message", "No report found with the provided tracking ID");
        }

        return response;
    }

    /**
     * Build sanitized location (no PII).
     */
    private ObjectNode buildSanitizedLocation(JsonNode address) {
        ObjectNode location = objectMapper.createObjectNode();

        if (!address.isMissingNode()) {
            // Only include locality/ward level - not full address
            if (address.has("locality")) {
                JsonNode locality = address.get("locality");
                if (locality.isObject()) {
                    location.put("localityName", locality.path("name").asText());
                    location.put("localityCode", locality.path("code").asText());
                } else {
                    location.put("localityName", locality.asText());
                }
            }

            if (address.has("ward")) {
                JsonNode ward = address.get("ward");
                if (ward.isObject()) {
                    location.put("wardName", ward.path("name").asText());
                } else {
                    location.put("wardName", ward.asText());
                }
            }

            if (address.has("city")) {
                location.put("city", address.get("city").asText());
            }

            // Include landmark if provided (usually not PII)
            if (address.has("landmark") && !address.get("landmark").asText().isEmpty()) {
                location.put("landmark", address.get("landmark").asText());
            }
        }

        return location;
    }

    /**
     * Build resolution information.
     */
    private ObjectNode buildResolutionInfo(JsonNode service) {
        ObjectNode resolution = objectMapper.createObjectNode();

        String status = service.path("applicationStatus").asText();

        switch (status) {
            case "RESOLVED":
            case "CAPTURED":
                resolution.put("outcome", "CAPTURED");
                resolution.put("description", "The stray dog was successfully captured");
                resolution.put("payoutEligible", true);
                break;
            case "UNABLE_TO_LOCATE":
                resolution.put("outcome", "NOT_FOUND");
                resolution.put("description", "The dog could not be found at the reported location");
                resolution.put("payoutEligible", false);
                break;
            case "REJECTED":
            case "AUTO_REJECTED":
                resolution.put("outcome", "REJECTED");
                resolution.put("description", "The report could not be verified");
                resolution.put("payoutEligible", false);
                break;
            case "DUPLICATE":
                resolution.put("outcome", "DUPLICATE");
                resolution.put("description", "This was identified as a duplicate report");
                resolution.put("payoutEligible", false);
                break;
            default:
                resolution.put("outcome", status);
                resolution.put("payoutEligible", false);
        }

        return resolution;
    }

    /**
     * Build timeline from workflow history.
     */
    private ArrayNode buildTimeline(JsonNode workflow) {
        ArrayNode timeline = objectMapper.createArrayNode();

        for (JsonNode step : workflow) {
            ObjectNode event = objectMapper.createObjectNode();

            String state = step.path("state").path("state").asText();
            if (state.isEmpty()) {
                state = step.path("state").asText();
            }

            event.put("status", state);
            event.put("description", getStatusMessage(state));

            JsonNode auditDetails = step.path("auditDetails");
            event.put("timestamp", auditDetails.path("createdTime").asLong());

            // Include comments if not containing PII
            String comments = step.path("comments").asText();
            if (comments != null && !comments.isEmpty() && !containsPII(comments)) {
                event.put("comments", comments);
            }

            timeline.add(event);
        }

        return timeline;
    }

    /**
     * Extract tracking ID from additional details or generate from service request ID.
     */
    private String extractTrackingId(JsonNode service) {
        JsonNode additionalDetails = service.path("additionalDetails");
        if (additionalDetails.has("trackingId")) {
            return additionalDetails.get("trackingId").asText();
        }
        // Fallback: use last 6 chars of service request ID
        String serviceRequestId = service.path("serviceRequestId").asText();
        if (serviceRequestId.length() > 6) {
            return serviceRequestId.substring(serviceRequestId.length() - 6).toUpperCase();
        }
        return serviceRequestId;
    }

    /**
     * Get user-friendly status message.
     */
    private String getStatusMessage(String status) {
        return STATUS_MESSAGES.getOrDefault(status,
            "Your report is being processed");
    }

    /**
     * Check if status is terminal (no further updates expected).
     */
    private boolean isTerminalStatus(String status) {
        return status != null && (
            status.equals("RESOLVED") ||
            status.equals("CAPTURED") ||
            status.equals("REJECTED") ||
            status.equals("AUTO_REJECTED") ||
            status.equals("DUPLICATE") ||
            status.equals("UNABLE_TO_LOCATE")
        );
    }

    /**
     * Basic PII detection in comments.
     */
    private boolean containsPII(String text) {
        if (text == null) return false;
        // Check for phone number patterns
        if (text.matches(".*\\d{10}.*")) return true;
        // Check for email patterns
        if (text.matches(".*@.*\\..*")) return true;
        return false;
    }

    /**
     * Set error response.
     */
    private void setErrorResponse(RequestContext ctx, String message) {
        try {
            ObjectNode error = objectMapper.createObjectNode();
            error.putNull("tracking");
            error.put("found", false);
            error.put("error", message);
            ctx.setResponseBody(objectMapper.writeValueAsString(error));
            ctx.getResponse().setContentType("application/json;charset=UTF-8");
        } catch (Exception e) {
            log.error("Error setting error response", e);
        }
    }
}
```

---

## 3. Modifying Existing Auth Filter

Update the existing `AuthFilter` to respect the `skipAuth` flag:

```java
// In AuthFilter.java (existing DIGIT gateway filter)

@Override
public boolean shouldFilter() {
    RequestContext ctx = RequestContext.getCurrentContext();

    // Check if auth bypass flag is set
    Boolean skipAuth = (Boolean) ctx.get("skipAuth");
    if (Boolean.TRUE.equals(skipAuth)) {
        log.debug("Skipping auth for public endpoint");
        return false;
    }

    // Existing logic...
    return true;
}
```

---

## 4. MDMS Configuration for Gateway

Add CCRS routes to MDMS gateway configuration:

### 4.1 Route Configuration Master

```json
{
  "tenantId": "ncr",
  "moduleName": "gateway",
  "RouteConfiguration": [
    {
      "serviceName": "ccrs-services",
      "routes": [
        {
          "path": "/ccrs/v1/report/_create",
          "target": "/pgr-services/v2/request/_create",
          "methods": ["POST"],
          "authRequired": true,
          "roles": ["TEACHER", "CITIZEN"]
        },
        {
          "path": "/ccrs/v1/report/_update",
          "target": "/pgr-services/v2/request/_update",
          "methods": ["POST"],
          "authRequired": true,
          "roles": ["TEACHER", "VERIFIER", "MC_OFFICER", "MC_SUPERVISOR"]
        },
        {
          "path": "/ccrs/v1/report/_search",
          "target": "/pgr-services/v2/request/_search",
          "methods": ["POST"],
          "authRequired": true,
          "roles": ["TEACHER", "VERIFIER", "MC_OFFICER", "MC_SUPERVISOR", "DISTRICT_ADMIN"]
        },
        {
          "path": "/ccrs/v1/report/_track",
          "target": "/pgr-services/v2/request/_search",
          "methods": ["POST"],
          "authRequired": false,
          "public": true,
          "filters": ["CCRSTrackRequestFilter", "CCRSTrackResponseFilter"]
        }
      ]
    }
  ]
}
```

### 4.2 Public Endpoints Master

```json
{
  "tenantId": "ncr",
  "moduleName": "gateway",
  "PublicEndpoints": [
    {
      "endpoint": "/ccrs/v1/report/_track",
      "methods": ["POST"],
      "description": "Public tracking API for stray dog reports"
    },
    {
      "endpoint": "/user/oauth/token",
      "methods": ["POST"],
      "description": "OAuth token endpoint"
    },
    {
      "endpoint": "/user-otp/v1/_send",
      "methods": ["POST"],
      "description": "OTP sending endpoint"
    }
  ]
}
```

---

## 5. Deployment Configuration

### 5.1 Kubernetes ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: egov-gateway-ccrs-config
  namespace: egov
data:
  ccrs-routes.properties: |
    # CCRS Route Mappings
    zuul.routes.ccrs-create.path=/ccrs/v1/report/_create
    zuul.routes.ccrs-create.url=http://pgr-services.egov:8080/pgr-services/v2/request/_create
    zuul.routes.ccrs-update.path=/ccrs/v1/report/_update
    zuul.routes.ccrs-update.url=http://pgr-services.egov:8080/pgr-services/v2/request/_update
    zuul.routes.ccrs-search.path=/ccrs/v1/report/_search
    zuul.routes.ccrs-search.url=http://pgr-services.egov:8080/pgr-services/v2/request/_search
    zuul.routes.ccrs-track.path=/ccrs/v1/report/_track
    zuul.routes.ccrs-track.url=http://pgr-services.egov:8080/pgr-services/v2/request/_search

    # Public endpoints
    egov.gateway.public.endpoints=/ccrs/v1/report/_track
```

### 5.2 Helm Values

```yaml
# values.yaml for egov-gateway deployment

extraEnv:
  - name: CCRS_ENABLED
    value: "true"
  - name: EGOV_GATEWAY_PUBLIC_ENDPOINTS
    value: "/ccrs/v1/report/_track,/user/oauth/token"

extraVolumes:
  - name: ccrs-config
    configMap:
      name: egov-gateway-ccrs-config

extraVolumeMounts:
  - name: ccrs-config
    mountPath: /config/ccrs
    readOnly: true
```

---

## 6. Testing

### 6.1 Test Public Track Endpoint

```bash
# Test tracking with tracking ID
curl -X POST "https://gateway.ncr.digit.org/ccrs/v1/report/_track" \
  -H "Content-Type: application/json" \
  -d '{
    "trackingId": "ABC123",
    "tenantId": "ncr"
  }'

# Test tracking with report number
curl -X POST "https://gateway.ncr.digit.org/ccrs/v1/report/_track" \
  -H "Content-Type: application/json" \
  -d '{
    "reportNumber": "DJ-CCRS-2024-000001",
    "tenantId": "ncr"
  }'
```

### 6.2 Expected Response

```json
{
  "ResponseInfo": {
    "apiId": "ccrs-track",
    "ver": "1.0",
    "ts": 1702000000000,
    "status": "successful"
  },
  "tracking": {
    "reportNumber": "DJ-CCRS-2024-000001",
    "trackingId": "ABC123",
    "status": "ASSIGNED",
    "statusDescription": "A Municipal Corporation officer has been assigned to your report",
    "isTerminal": false,
    "reportedDate": 1701900000000,
    "serviceType": "Stray Dog Report",
    "location": {
      "localityName": "Sector 15",
      "wardName": "Ward 7",
      "city": "Delhi"
    },
    "timeline": [
      {
        "status": "PENDING_VALIDATION",
        "description": "Your report has been received and is being validated",
        "timestamp": 1701900000000
      },
      {
        "status": "PENDING_VERIFICATION",
        "description": "Your report is under review by our team",
        "timestamp": 1701900300000
      },
      {
        "status": "VERIFIED",
        "description": "Your report has been verified and approved",
        "timestamp": 1701910000000
      },
      {
        "status": "ASSIGNED",
        "description": "A Municipal Corporation officer has been assigned to your report",
        "timestamp": 1701920000000
      }
    ]
  },
  "found": true
}
```

### 6.3 Verify Auth is Not Required

```bash
# Should work WITHOUT auth token
curl -X POST "https://gateway.ncr.digit.org/ccrs/v1/report/_track" \
  -H "Content-Type: application/json" \
  -d '{"trackingId": "ABC123", "tenantId": "ncr"}'

# Should return 401 for authenticated endpoints
curl -X POST "https://gateway.ncr.digit.org/ccrs/v1/report/_create" \
  -H "Content-Type: application/json" \
  -d '{...}'
# Expected: 401 Unauthorized
```

---

## 7. Monitoring

### 7.1 Logging

Add logging configuration for CCRS filters:

```properties
# application.properties
logging.level.org.egov.gateway.filters.ccrs=DEBUG
```

### 7.2 Metrics

Track CCRS-specific metrics:

```java
// In filter classes
@Autowired
private MeterRegistry meterRegistry;

// In run() method
meterRegistry.counter("ccrs.track.requests", "status", "success").increment();
```

---

## 8. Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| 401 on `_track` | Auth bypass not working | Check `skipAuth` flag and filter order |
| Empty response | PGR returned no results | Verify tracking ID exists in PGR |
| PII in response | Response filter not running | Check filter registration and order |
| Timeout | PGR service slow | Check PGR health, increase timeout |
| Transform error | JSON parsing failed | Check request/response format, enable debug logs |

---

## 9. Migration to Spring Cloud Gateway

When DIGIT migrates from Zuul to Spring Cloud Gateway, convert filters to:

```java
@Component
public class CCRSTrackGatewayFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Request modification
        return DataBufferUtils.join(exchange.getRequest().getBody())
            .flatMap(dataBuffer -> {
                // Transform request
                // ...
                return chain.filter(exchange.mutate()
                    .request(modifiedRequest)
                    .build());
            })
            .then(Mono.defer(() -> {
                // Response modification
                // ...
            }));
    }
}
```

---

## Summary

This configuration enables the CCRS public tracking API by:

1. **Route aliasing** - Maps `/ccrs/v1/report/_track` to PGR's `/_search`
2. **Auth bypass** - Skips authentication for the tracking endpoint
3. **Request transformation** - Converts simple tracking requests to PGR format
4. **Response sanitization** - Removes PII and formats user-friendly response

The approach keeps PGR as the backend while providing a clean, public-facing tracking API specifically designed for the stray dog reporting use case.

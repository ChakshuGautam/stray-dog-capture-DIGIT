package org.digit.fraud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestWrapper<T> {

    private RequestInfo requestInfo;
    private T request;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RequestInfo {
        private String apiId;
        private String ver;
        private Long ts;
        private String action;
        private String did;
        private String key;
        private String msgId;
        private String authToken;
        private String correlationId;
        private UserInfo userInfo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserInfo {
        private String uuid;
        private String userName;
        private String name;
        private String mobileNumber;
        private String emailId;
        private String tenantId;
    }
}

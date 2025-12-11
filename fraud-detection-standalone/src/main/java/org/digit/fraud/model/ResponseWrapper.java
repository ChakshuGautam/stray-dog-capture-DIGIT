package org.digit.fraud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseWrapper<T> {

    private ResponseInfo responseInfo;
    private T response;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseInfo {
        private String apiId;
        private String ver;
        private Long ts;
        private String resMsgId;
        private String msgId;
        private String status;
    }

    public static <T> ResponseWrapper<T> success(T data, String msgId) {
        return ResponseWrapper.<T>builder()
                .responseInfo(ResponseInfo.builder()
                        .apiId("fraud-detection")
                        .ver("1.0")
                        .ts(System.currentTimeMillis())
                        .msgId(msgId)
                        .status("successful")
                        .build())
                .response(data)
                .build();
    }

    public static <T> ResponseWrapper<T> error(String status, String msgId) {
        return ResponseWrapper.<T>builder()
                .responseInfo(ResponseInfo.builder()
                        .apiId("fraud-detection")
                        .ver("1.0")
                        .ts(System.currentTimeMillis())
                        .msgId(msgId)
                        .status(status)
                        .build())
                .build();
    }
}

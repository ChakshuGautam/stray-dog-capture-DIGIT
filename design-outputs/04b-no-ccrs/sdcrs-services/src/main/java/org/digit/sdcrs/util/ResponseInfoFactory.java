package org.digit.sdcrs.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.stereotype.Component;

/**
 * ResponseInfoFactory - Utility for creating DIGIT ResponseInfo objects.
 * Used in all API responses to maintain consistency with DIGIT standards.
 */
@Component
public class ResponseInfoFactory {

    /**
     * Create ResponseInfo from RequestInfo for successful responses.
     *
     * @param requestInfo The request info from incoming request
     * @param success Whether the operation was successful
     * @return ResponseInfo object
     */
    public ResponseInfo createResponseInfoFromRequestInfo(RequestInfo requestInfo, Boolean success) {
        String apiId = "";
        String ver = "";
        Long ts = 0L;
        String resMsgId = "";
        String msgId = "";

        if (requestInfo != null) {
            apiId = requestInfo.getApiId();
            ver = requestInfo.getVer();
            ts = requestInfo.getTs();
            msgId = requestInfo.getMsgId();
        }

        String status = success ? "successful" : "failed";

        return ResponseInfo.builder()
                .apiId(apiId)
                .ver(ver)
                .ts(ts)
                .resMsgId(resMsgId)
                .msgId(msgId)
                .status(status)
                .build();
    }

    /**
     * Create success ResponseInfo.
     *
     * @param requestInfo The request info from incoming request
     * @return ResponseInfo for successful operation
     */
    public ResponseInfo createSuccessResponseInfo(RequestInfo requestInfo) {
        return createResponseInfoFromRequestInfo(requestInfo, true);
    }

    /**
     * Create failure ResponseInfo.
     *
     * @param requestInfo The request info from incoming request
     * @return ResponseInfo for failed operation
     */
    public ResponseInfo createFailureResponseInfo(RequestInfo requestInfo) {
        return createResponseInfoFromRequestInfo(requestInfo, false);
    }
}

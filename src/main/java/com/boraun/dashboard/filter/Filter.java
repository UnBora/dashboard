package com.boraun.dashboard.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.thymeleaf.util.StringUtils;


import java.text.MessageFormat;
import java.util.HashMap;

@Data
public class Filter {
    private int status;
    @Setter(AccessLevel.NONE)
    private String latency;

    public String getLatency() {
        return MessageFormat.format("{0}ms", (end - start));
    }

    private String method;
    private String requestUrl;
    private String clientIP;
    private String requestId;
    private String responseId;
    private String clientTimestamp;
    private String serverTimestamp;
    private HashMap<String, String> requestHeaders;
    private HashMap<String, String> responseHeaders;
    private String requestBody;
    private String responseBody;

    public String getResponseBody() {
        if (responseHeaders.containsKey("Content-Type")) {
            String contentType = responseHeaders.get("Content-Type");
            if (StringUtils.contains(contentType, "application/json")) {
                return this.responseBody;
            }
            return contentType;
        }
        int dotIndex = this.requestUrl.lastIndexOf('.');
        // handle cases with no extension or multiple dots
        if (dotIndex == -1 || dotIndex == this.requestUrl.length() - 1) {
            return "";
        } else {
            return this.requestUrl.substring(dotIndex + 1);
        }
    }

    @JsonIgnore
    private long start;
    @JsonIgnore
    private long end;
}

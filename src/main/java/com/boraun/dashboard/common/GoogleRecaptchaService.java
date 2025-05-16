package com.boraun.dashboard.common;

import com.boraun.dashboard.admin.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GoogleRecaptchaService {
    private static final Map<String, String> RECAPTCHA_ERROR_CODE = new HashMap<>();
    private final ConfigurationService configurationService;

    static {
        RECAPTCHA_ERROR_CODE.put("missing-input-secret", "The secret parameter is missing");
        RECAPTCHA_ERROR_CODE.put("invalid-input-secret", "The secret parameter is invalid or malformed");
        RECAPTCHA_ERROR_CODE.put("missing-input-response", "The response parameter is missing");
        RECAPTCHA_ERROR_CODE.put("invalid-input-response", "The response parameter is invalid or malformed");
        RECAPTCHA_ERROR_CODE.put("bad-request", "The request is invalid or malformed");
    }




    private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RestTemplateBuilder restTemplateBuilder;

    @Autowired
    public GoogleRecaptchaService(ConfigurationService configurationService, RestTemplateBuilder restTemplateBuilder) {
        this.configurationService = configurationService;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    public boolean verifyRecaptcha(String ip, String recaptchaResponse) {
        Map<String, String> body = new HashMap<>();
        body.put("secret", configurationService.getGoogleRecaptchaSecretKey());
        body.put("response", recaptchaResponse);
        body.put("remoteip", ip);
        log.info("Request body for recaptcha: {}", body);

        ResponseEntity<Map> recaptchaResponseEntity =
                restTemplateBuilder.build()
                        .postForEntity(GOOGLE_RECAPTCHA_VERIFY_URL + "?secret={secret}&response={response}&remoteip={remoteip}", body, Map.class, body);

        log.info("Response from recaptcha: {}", recaptchaResponseEntity);
        Map<String, Object> responseBody = recaptchaResponseEntity.getBody();
        boolean recaptchaSuccess = (Boolean) responseBody.get("success");
        if (!recaptchaSuccess) {
            List<String> errorCodes = (List) responseBody.get("error-codes");
            String errorMessage = errorCodes.stream()
                    .map(s -> RECAPTCHA_ERROR_CODE.get(s))
                    .collect(Collectors.joining(", "));
            log.info(errorMessage);
            return false;
        } else {
            return true;
        }
    }
}

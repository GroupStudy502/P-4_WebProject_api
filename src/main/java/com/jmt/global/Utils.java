package com.jmt.global;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Utils { // 빈의 이름 - utils

    private final MessageSource messageSource;
    private final HttpServletRequest request;
    private final DiscoveryClient discoveryClient;


    public Map<String, List<String>> getErrorMessages(Errors errors) { //JSON 받을 때는 에러를 직접
        // FieldErrors


        Map<String, List<String>> messages = errors.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, e -> getCodeMessages(e.getCodes()), (p1, p2) -> p1));

        // GlobalErrors
        List<String> gMessages = errors.getGlobalErrors()
                .stream()
                .flatMap(e -> getCodeMessages(e.getCodes()).stream()).toList();

        if (!gMessages.isEmpty()) {
            messages.put("global", gMessages);
        }
        return messages;
    }


    public List<String> getCodeMessages(String[] codes) {
        ResourceBundleMessageSource ms = (ResourceBundleMessageSource) messageSource;
        ms.setUseCodeAsDefaultMessage(false);

        List<String> messages = Arrays.stream(codes)
                .map(c -> {
                    try {
                        return ms.getMessage(c, null, request.getLocale());
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(s -> !s.isBlank())
                .toList();

        ms.setUseCodeAsDefaultMessage(true);
        return messages;
    }

    public String getMessage(String code) {
        List<String> messages = getCodeMessages(new String[] {code});

        return messages.isEmpty() ? code : messages.get(0);
    }

    public String url(String url) {
        List<ServiceInstance> instances = discoveryClient.getInstances("api-service");

        try {
            return String.format("%s%s", instances.get(0).getUri().toString(), url);
        } catch (Exception e) {
            return String.format("%s://%s:%d%s%s", request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath(), url);
        }
    }

    public String redirectUrl(String url) {
        String _fromGateway = Objects.requireNonNullElse(request.getHeader("from-gateway"), "false");
        String gatewayHost = Objects.requireNonNullElse(request.getHeader("gateway-host"), "");
        boolean fromGateway = _fromGateway.equals("true");

        return fromGateway ? request.getScheme() + "://" + gatewayHost + "/app" + url : request.getContextPath() + url;
    }

    public String adminUrl(String url) {
        List<ServiceInstance> instances = discoveryClient.getInstances("admin-service");
        return String.format("%s%s", instances.get(0).getUri().toString(), url);
    }

    /**
     * 비회원을 구분하는 Unique ID
     *   IP + User-Agent
     * @return
     */
    public int guestUid() {
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");

        return Objects.hash(ip, ua);
    }

}
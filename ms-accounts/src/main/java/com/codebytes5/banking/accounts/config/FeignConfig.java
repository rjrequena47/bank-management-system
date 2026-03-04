package com.codebytes5.banking.accounts.config;

import com.codebytes5.banking.accounts.client.CustomerClientErrorDecoder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@org.springframework.context.annotation.Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null) {
                        template.header("Authorization", authHeader);
                    }
                    template.header("X-Internal-Service", "ms-accounts");
                }
            }
        };
    }

    /**
     * Registra el ErrorDecoder personalizado para manejar respuestas de error de
     * ms-customers.
     * Convierte códigos HTTP remotos en excepciones de dominio con mensajes en
     * español.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomerClientErrorDecoder();
    }
}

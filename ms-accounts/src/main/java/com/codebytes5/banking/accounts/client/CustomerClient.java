package com.codebytes5.banking.accounts.client;

import com.codebytes5.banking.accounts.config.FeignConfig;
import com.codebytes5.banking.accounts.dto.CustomerValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Cliente Feign para comunicarse con el microservicio ms-customers.
 */
@FeignClient(name = "ms-customers", url = "${customers.service.url}", configuration = FeignConfig.class)
public interface CustomerClient {

    @GetMapping("/api/customers/{customerId}/validate")
    CustomerValidationResponse validateCustomer(@PathVariable("customerId") UUID customerId);
}

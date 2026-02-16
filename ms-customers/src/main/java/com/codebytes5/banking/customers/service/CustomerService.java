package com.codebytes5.banking.customers.service;

import com.codebytes5.banking.customers.dto.CustomerResponse;
import com.codebytes5.banking.customers.dto.CustomerValidationResponse;
import com.codebytes5.banking.customers.enums.CustomerStatus;
import com.codebytes5.banking.customers.mapper.CustomerMapper;
import com.codebytes5.banking.customers.model.Customer;
import com.codebytes5.banking.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    /**
     * Obtiene el perfil del cliente basado en su email.
     * 
     * @param email El email del cliente autenticado.
     * @return CustomerResponse con la información del perfil.
     */
    public CustomerResponse getProfile(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
        return customerMapper.toResponse(customer);
    }

    /**
     * Valida la existencia y el estado de un cliente por su ID.
     * 
     * @param id UUID del cliente.
     * @return CustomerValidationResponse con flags de existencia y estado activo.
     */
    public CustomerValidationResponse validateCustomer(UUID id) {
        return customerRepository.findById(id)
                .map(customer -> CustomerValidationResponse.builder()
                        .customerId(customer.getId())
                        .exists(true)
                        .isActive(customer.getStatus() == CustomerStatus.ACTIVE)
                        .build())
                .orElse(CustomerValidationResponse.builder()
                        .customerId(id)
                        .exists(false)
                        .isActive(false)
                        .build());
    }

    /**
     * Obtiene información básica de un cliente por su ID.
     * 
     * @param id UUID del cliente.
     * @return CustomerResponse mapeado.
     */
    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
        return customerMapper.toResponse(customer);
    }
}

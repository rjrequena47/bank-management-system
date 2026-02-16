package com.codebytes5.banking.customers.service;

import com.codebytes5.banking.customers.dto.CustomerResponse;
import com.codebytes5.banking.customers.mapper.CustomerMapper;
import com.codebytes5.banking.customers.model.Customer;
import com.codebytes5.banking.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}

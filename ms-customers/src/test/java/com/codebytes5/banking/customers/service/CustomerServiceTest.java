package com.codebytes5.banking.customers.service;

import com.codebytes5.banking.customers.dto.CustomerResponse;
import com.codebytes5.banking.customers.dto.CustomerValidationResponse;
import com.codebytes5.banking.customers.dto.UpdateCustomerRequest;
import com.codebytes5.banking.customers.enums.CustomerStatus;
import com.codebytes5.banking.customers.exception.ResourceNotFoundException;
import com.codebytes5.banking.customers.mapper.CustomerMapper;
import com.codebytes5.banking.customers.model.Customer;
import com.codebytes5.banking.customers.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void getProfile_success() {
        String email = "test@test.com";
        Customer customer = Customer.builder().email(email).build();
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(new CustomerResponse());

        CustomerResponse response = customerService.getProfile(email);

        assertNotNull(response);
    }

    @Test
    void getProfile_notFound() {
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getProfile("notfound@test.com"));
    }

    @Test
    void validateCustomer_active() {
        UUID id = UUID.randomUUID();
        Customer customer = Customer.builder().id(id).status(CustomerStatus.ACTIVE).build();
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        CustomerValidationResponse response = customerService.validateCustomer(id);

        assertTrue(response.isExists());
        assertTrue(response.isActive());
    }

    @Test
    void updateProfile_success() {
        String email = "test@test.com";
        UpdateCustomerRequest request = UpdateCustomerRequest.builder()
                .firstName("New")
                .lastName("Name")
                .address("New Address")
                .phone("123456")
                .build();
        Customer customer = Customer.builder().email(email).build();

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(new CustomerResponse());

        CustomerResponse response = customerService.updateProfile(email, request);

        assertNotNull(response);
        assertEquals("New", customer.getFirstName());
        assertEquals("New Address", customer.getAddress());
        verify(customerRepository).save(any(Customer.class));
    }
}

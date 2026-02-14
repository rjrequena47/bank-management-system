package com.codebytes5.banking.customers.repository;

import com.codebytes5.banking.customers.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByDni(String dni);

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);
}

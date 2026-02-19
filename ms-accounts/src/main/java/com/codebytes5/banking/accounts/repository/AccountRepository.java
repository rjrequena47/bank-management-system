package com.codebytes5.banking.accounts.repository;

import com.codebytes5.banking.accounts.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    long countByCustomerId(UUID customerId);

    boolean existsByAccountNumber(String accountNumber);
}

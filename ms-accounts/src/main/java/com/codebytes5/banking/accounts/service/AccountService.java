package com.codebytes5.banking.accounts.service;

import com.codebytes5.banking.accounts.client.CustomerClient;
import com.codebytes5.banking.accounts.dto.AccountResponse;
import com.codebytes5.banking.accounts.dto.CreateAccountRequest;
import com.codebytes5.banking.accounts.dto.CustomerValidationResponse;
import com.codebytes5.banking.accounts.enums.AccountStatus;
import com.codebytes5.banking.accounts.exception.CustomerNotActiveException;
import com.codebytes5.banking.accounts.exception.CustomerNotFoundException;
import com.codebytes5.banking.accounts.exception.MaxAccountsReachedException;
import com.codebytes5.banking.accounts.mapper.AccountMapper;
import com.codebytes5.banking.accounts.model.Account;
import com.codebytes5.banking.accounts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que implementa el caso de uso de creación de cuentas bancarias.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private static final int MAX_ACCOUNTS_PER_CUSTOMER = 3;

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final CustomerClient customerClient;
    private final IbanGeneratorService ibanGeneratorService;

    /**
     * Crea una nueva cuenta bancaria para el cliente autenticado.
     *
     * @param customerId UUID del cliente extraído del token JWT.
     * @param request    Datos de la cuenta a crear (sin customerId).
     * @return AccountResponse con los datos de la cuenta creada.
     */
    @Transactional
    public AccountResponse createAccount(UUID customerId, CreateAccountRequest request) {
        // 1. Validar existencia y estado del cliente
        CustomerValidationResponse validation = customerClient.validateCustomer(customerId);

        if (!validation.isExists()) {
            throw new CustomerNotFoundException(customerId);
        }

        if (!validation.isActive()) {
            throw new CustomerNotActiveException(customerId);
        }

        // 2. Validar límite máximo de cuentas
        long accountCount = accountRepository.countByCustomerId(customerId);
        if (accountCount >= MAX_ACCOUNTS_PER_CUSTOMER) {
            throw new MaxAccountsReachedException();
        }

        // 3. Generar IBAN único
        String iban = ibanGeneratorService.generateUniqueIban();

        // 4. Crear la entidad Account
        Instant now = Instant.now();
        Account account = Account.builder()
                .customerId(customerId)
                .accountNumber(iban)
                .accountType(request.getAccountType())
                .currency(request.getCurrency())
                .alias(request.getAlias())
                .dailyWithdrawalLimit(request.getDailyWithdrawalLimit())
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 5. Persistir la cuenta
        Account savedAccount = accountRepository.save(account);

        // 6. Retornar el DTO de respuesta
        return accountMapper.toResponse(savedAccount);
    }

    public List<AccountResponse> getAccountsByCustomerId(UUID customerId) {
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }
}

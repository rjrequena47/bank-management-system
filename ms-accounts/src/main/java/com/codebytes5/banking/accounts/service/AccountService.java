package com.codebytes5.banking.accounts.service;

import com.codebytes5.banking.accounts.client.CustomerClient;
import com.codebytes5.banking.accounts.dto.AccountResponse;
import com.codebytes5.banking.accounts.dto.CreateAccountRequest;
import com.codebytes5.banking.accounts.dto.CustomerValidationResponse;
import com.codebytes5.banking.accounts.enums.AccountStatus;
import com.codebytes5.banking.accounts.exception.CustomerNotActiveException;
import com.codebytes5.banking.accounts.exception.CustomerNotFoundException;
import com.codebytes5.banking.accounts.exception.MaxAccountsReachedException;
import com.codebytes5.banking.accounts.exception.AccountNotFoundException;
import com.codebytes5.banking.accounts.exception.UnauthorizedAccountAccessException;
import com.codebytes5.banking.accounts.mapper.AccountMapper;
import com.codebytes5.banking.accounts.model.Account;
import com.codebytes5.banking.accounts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("[AccountService] Iniciando creación de cuenta. customerId={}, tipo={}", customerId,
                request.getAccountType());

        // 1. Validar existencia y estado del cliente
        CustomerValidationResponse validation = customerClient.validateCustomer(customerId);

        if (!validation.isExists()) {
            log.warn("[AccountService] Cliente no encontrado en ms-customers. customerId={}", customerId);
            throw new CustomerNotFoundException(customerId);
        }

        if (!validation.isActive()) {
            log.warn("[AccountService] Cliente inactivo en ms-customers. customerId={}", customerId);
            throw new CustomerNotActiveException(customerId);
        }

        // 2. Validar límite máximo de cuentas
        long accountCount = accountRepository.countByCustomerId(customerId);
        if (accountCount >= MAX_ACCOUNTS_PER_CUSTOMER) {
            log.warn("[AccountService] Límite de cuentas alcanzado. customerId={}, cuentasActuales={}", customerId,
                    accountCount);
            throw new MaxAccountsReachedException();
        }

        // 3. Generar IBAN único
        String iban = ibanGeneratorService.generateUniqueIban();

        // 4. Crear la entidad Account
        Instant now = Instant.now();
        BigDecimal dailyLimit = request.getDailyWithdrawalLimit() != null
                ? request.getDailyWithdrawalLimit()
                : BigDecimal.valueOf(1000.00);

        Account account = Account.builder()
                .customerId(customerId)
                .accountNumber(iban)
                .accountType(request.getAccountType())
                .currency(request.getCurrency())
                .alias(request.getAlias())
                .dailyWithdrawalLimit(dailyLimit)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 5. Persistir la cuenta
        Account savedAccount = accountRepository.save(account);
        log.info("[AccountService] Cuenta creada exitosamente. accountId={}, customerId={}", savedAccount.getId(),
                customerId);

        // 6. Retornar el DTO de respuesta
        return accountMapper.toResponse(savedAccount);
    }

    public List<AccountResponse> getAccountsByCustomerId(UUID customerId) {
        log.info("[AccountService] Consultando cuentas por cliente. customerId={}", customerId);
        List<AccountResponse> accounts = accountRepository.findByCustomerId(customerId)
                .stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
        log.info("[AccountService] Cuentas encontradas. customerId={}, total={}", customerId, accounts.size());
        return accounts;
    }

    public AccountResponse getAccountByIdAndCustomerId(UUID accountId, UUID customerId) {
        log.info("[AccountService] Consultando cuenta por ID. accountId={}, customerId={}", accountId, customerId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("[AccountService] Cuenta no encontrada. accountId={}", accountId);
                    return new AccountNotFoundException("Cuenta no encontrada");
                });

        if (!account.getCustomerId().equals(customerId)) {
            log.warn(
                    "[AccountService] Intento de acceso no autorizado. accountId={}, clienteSolicitante={}, dueñoReal={}",
                    accountId, customerId, account.getCustomerId());
            throw new UnauthorizedAccountAccessException("La cuenta no pertenece al cliente autenticado");
        }

        return accountMapper.toResponse(account);
    }
}

package com.codebytes5.banking.accounts.service;

import com.codebytes5.banking.accounts.exception.IbanGenerationException;
import com.codebytes5.banking.accounts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Servicio encargado de generar IBANs únicos para las cuentas bancarias.
 * Formato: {countryCode} + 16 dígitos numéricos (ISO 3166-1 alpha-2).
 * Reintenta hasta 5 veces antes de lanzar IbanGenerationException.
 */
@Service
@RequiredArgsConstructor
public class IbanGeneratorService {

    private static final int NUMERIC_LENGTH = 16;
    private static final int MAX_ATTEMPTS = 5;

    /**
     * ISO 3166-1 alpha-2 country code used as IBAN prefix.
     * Configured via 'iban.country-code' in application.properties (e.g. BO, US,
     * DE, ES).
     */
    @Value("${iban.country-code:BO}")
    private String countryCode;

    private final AccountRepository accountRepository;
    private final Random random = new Random();

    public String generateUniqueIban() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String iban = generateIban();
            if (!accountRepository.existsByAccountNumber(iban)) {
                return iban;
            }
        }
        throw new IbanGenerationException();
    }

    private String generateIban() {
        StringBuilder sb = new StringBuilder(countryCode.toUpperCase());
        for (int i = 0; i < NUMERIC_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}

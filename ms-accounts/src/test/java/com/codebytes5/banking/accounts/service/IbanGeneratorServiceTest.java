package com.codebytes5.banking.accounts.service;

import com.codebytes5.banking.accounts.exception.IbanGenerationException;
import com.codebytes5.banking.accounts.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IbanGeneratorServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private IbanGeneratorService ibanGeneratorService;

    @Test
    void generateUniqueIban_successOnFirstAttempt() {
        ReflectionTestUtils.setField(ibanGeneratorService, "countryCode", "BO");
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);

        String iban = ibanGeneratorService.generateUniqueIban();

        assertNotNull(iban);
        assertTrue(iban.startsWith("BO"));
        assertEquals(18, iban.length()); // BO + 16 numeric
        verify(accountRepository, times(1)).existsByAccountNumber(anyString());
    }

    @Test
    void generateUniqueIban_successOnThirdAttempt() {
        ReflectionTestUtils.setField(ibanGeneratorService, "countryCode", "BO");
        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(true) // 1st
                .thenReturn(true) // 2nd
                .thenReturn(false); // 3rd

        String iban = ibanGeneratorService.generateUniqueIban();

        assertNotNull(iban);
        verify(accountRepository, times(3)).existsByAccountNumber(anyString());
    }

    @Test
    void generateUniqueIban_throwsExceptionAfterMaxAttempts() {
        ReflectionTestUtils.setField(ibanGeneratorService, "countryCode", "BO");
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(true);

        assertThrows(IbanGenerationException.class, () -> ibanGeneratorService.generateUniqueIban());
    }
}

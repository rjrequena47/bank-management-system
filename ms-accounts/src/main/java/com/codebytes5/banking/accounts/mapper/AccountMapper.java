package com.codebytes5.banking.accounts.mapper;

import com.codebytes5.banking.accounts.dto.AccountResponse;
import com.codebytes5.banking.accounts.model.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponse toResponse(Account account);
}

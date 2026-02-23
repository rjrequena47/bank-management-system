package com.codebytes5.banking.accounts.mapper;

import com.codebytes5.banking.accounts.dto.TransactionResponse;
import com.codebytes5.banking.accounts.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "concept", target = "description")
    @Mapping(source = "createdAt", target = "timestamp")
    @Mapping(target = "balanceBefore", expression = "java(transaction.getType() == com.codebytes5.banking.accounts.enums.TransactionType.DEPOSIT ? transaction.getBalanceAfter().subtract(transaction.getAmount()) : transaction.getBalanceAfter().add(transaction.getAmount()))")
    TransactionResponse toResponse(Transaction transaction);
}

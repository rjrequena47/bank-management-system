package com.codebytes5.banking.customers.mapper;

import com.codebytes5.banking.customers.dto.CustomerRegistrationRequest;
import com.codebytes5.banking.customers.dto.CustomerResponse;
import com.codebytes5.banking.customers.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerRegistrationRequest request);

    @Mapping(target = "fullName", source = "customer", qualifiedByName = "mapFullName")
    CustomerResponse toResponse(Customer customer);

    @Named("mapFullName")
    default String mapFullName(Customer customer) {
        if (customer == null)
            return null;
        return customer.getFirstName() + " " + customer.getLastName();
    }
}

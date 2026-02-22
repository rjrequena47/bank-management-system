package com.codebytes5.banking.customers.controller;

import com.codebytes5.banking.customers.dto.CustomerResponse;
import com.codebytes5.banking.customers.dto.CustomerValidationResponse;
import com.codebytes5.banking.customers.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import com.codebytes5.banking.customers.exception.UnauthorizedAccessException;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Endpoints para la gestión de datos de clientes")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Endpoint para obtener el perfil del cliente autenticado.
     * Requiere un token JWT válido.
     */
    @Operation(summary = "Obtener perfil del cliente", description = "Devuelve la información del cliente autenticado basándose en el token JWT", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile(@AuthenticationPrincipal String email) {
        CustomerResponse response = customerService.getProfile(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint interno para obtener información de un cliente por su ID.
     * Acceso: Interno o ADMIN.
     */
    @Operation(summary = "Obtener cliente por ID (Interno/Admin)", description = "Devuelve información del cliente por su ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @Parameter(description = "ID del cliente") @PathVariable UUID customerId,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalService,
            Authentication authentication) {

        boolean isInternal = "ms-accounts".equals(internalService);
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        if (!isInternal && !isAdmin) {
            throw new UnauthorizedAccessException("Access denied. Internal or ADMIN only.");
        }

        CustomerResponse response = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint interno para validar la existencia y estado de un cliente con ID.
     */
    @Operation(summary = "Validar cliente (Interno)", description = "Verifica si un cliente existe y está activo.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{customerId}/validate")
    public ResponseEntity<CustomerValidationResponse> validateCustomer(
            @Parameter(description = "ID del cliente a validar") @PathVariable UUID customerId,
            @RequestHeader(value = "X-Internal-Service", required = false) String internalService) {

        if (!"ms-accounts".equals(internalService)) {
            throw new UnauthorizedAccessException("Access denied. Internal only.");
        }

        CustomerValidationResponse response = customerService.validateCustomer(customerId);
        return ResponseEntity.ok(response);
    }
}

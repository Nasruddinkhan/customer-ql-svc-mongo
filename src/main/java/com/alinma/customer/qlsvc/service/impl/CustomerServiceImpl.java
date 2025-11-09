package com.alinma.customer.qlsvc.service.impl;

import com.alinma.customer.qlsvc.exception.CustomerNotFoundException;
import com.alinma.customer.qlsvc.exception.GenericException;
import com.alinma.customer.qlsvc.mapper.CustomerMapper;
import com.alinma.customer.qlsvc.model.documents.Customer;
import com.alinma.customer.qlsvc.model.dto.CustomerDto;
import com.alinma.customer.qlsvc.model.dto.CustomerEvent;
import com.alinma.customer.qlsvc.repository.CustomerRepository;
import com.alinma.customer.qlsvc.service.CustomerService;
import com.alinma.customer.qlsvc.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper customerMapper;
    private final EventService eventService;

    @Override
    public Flux<CustomerDto> getAll() {
        return repository.findAll()
                .map(customerMapper::toDto)
                .doOnError(ex -> log.error("Error fetching all customers", ex))
                .onErrorMap(DataAccessException.class, ex ->
                        new GenericException("Database error while fetching all customers"));
    }

    @Override
    public Mono<CustomerDto> getById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer not found with id: " + id)))
                .map(customerMapper::toDto)
                .doOnError(ex -> log.error("Error fetching customer by id {}", id, ex))
                .onErrorMap(DataAccessException.class, ex ->
                        new RuntimeException("Database error while fetching customer", ex));
    }

    @Override
    public Mono<CustomerDto> createCustomer(CustomerDto dto) {
        log.info("Creating customer: {}", dto);
        Customer customer = customerMapper.toEntity(dto);
        customer.setCreatedAt(LocalDateTime.now());
        return repository.save(customer)
                .map(customerMapper::toDto)
                .flatMap(savedDto -> emitEventAndReturn(savedDto, "New customer created successfully"))
                .doOnSuccess(c -> log.info("Customer created successfully with id: {}", c.id()))
                .doOnError(ex -> log.error("Error creating customer", ex))
                .onErrorMap(ex -> new GenericException("Failed to create customer: " + ex.getMessage()));
    }

    @Override
    public Mono<CustomerDto> update(CustomerDto input) {
        return repository.findById(input.id())
                .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer not found with id: " + input.id())))
                .flatMap(existing -> getCustomerUpdate(input, existing))
                .doOnSuccess(c -> log.info("Customer updated successfully with id: {}", c.id()))
                .doOnError(ex -> log.error("Error updating customer", ex))
                .onErrorMap(ex -> new GenericException("Failed to update customer: " + ex.getMessage()));
    }

    private Mono<CustomerDto> getCustomerUpdate(CustomerDto input, Customer existing) {
        existing.setFirstName(Optional.ofNullable(input.firstName()).orElse(existing.getFirstName()));
        existing.setLastName(Optional.ofNullable(input.lastName()).orElse(existing.getLastName()));
        existing.setPhone(Optional.ofNullable(input.phone()).orElse(existing.getPhone()));
        return repository.save(existing)
                .map(customerMapper::toDto)
                .flatMap(updated -> emitEventAndReturn(updated, "Customer updated successfully"));

    }

    @Override
    public Mono<Boolean> delete(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer not found with id: " + id)))
                .flatMap(this::getDeleteCustomer)
                .doOnSuccess(result -> log.info("Customer deleted successfully: {}", id))
                .doOnError(ex -> log.error("Error deleting customer with id {}", id, ex))
                .onErrorMap(ex -> new GenericException("Failed to delete customer: " + ex.getMessage()));
    }

    private Mono<Boolean> getDeleteCustomer(Customer existing) {
        return repository.delete(existing)
                .then(emitEventAndReturn(existing, "Customer deleted successfully"))
                .thenReturn(true);
    }

    private <T> Mono<T> emitEventAndReturn(T value, String message) {
        return Mono.fromRunnable(() ->
                eventService.emitCustomerEvent(new CustomerEvent(getId(value), message))
        ).thenReturn(value);
    }

    private String getId(Object obj) {
        if (obj instanceof CustomerDto dto) return dto.id();
        if (obj instanceof Customer c) return c.getId();
        return "UNKNOWN";
    }
}

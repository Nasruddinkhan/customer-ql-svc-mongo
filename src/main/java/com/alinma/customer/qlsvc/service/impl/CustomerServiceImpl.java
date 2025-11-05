package com.alinma.customer.qlsvc.service.impl;

import com.alinma.customer.qlsvc.exception.CustomerNotFoundException;
import com.alinma.customer.qlsvc.mapper.CustomerMapper;
import com.alinma.customer.qlsvc.model.documents.Customer;
import com.alinma.customer.qlsvc.model.dto.CustomerDto;
import com.alinma.customer.qlsvc.model.dto.CustomerEvent;
import com.alinma.customer.qlsvc.repository.CustomerRepository;
import com.alinma.customer.qlsvc.service.CustomerService;
import com.alinma.customer.qlsvc.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        return customerMapper
                .toDtoFlux(repository.findAll());
    }

    @Override
    public Mono<CustomerDto> getById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer not found with id: " + id)))
                .map(customerMapper::toDto);
    }

    @Override
    public Mono<CustomerDto> createCustomer(CustomerDto dto) {
        log.info("dto = {}", dto);
        Customer customer = customerMapper.toEntity(dto);
        customer.setCreatedAt(LocalDateTime.now());
        return repository.save(customer)
                .map(customerMapper::toDto)
                .doOnNext(e->
                        eventService.emitCustomerEvent(new CustomerEvent(e.id(), "new customer created successfully")));
    }

    @Override
    public Mono<CustomerDto> update(CustomerDto input) {
        return repository.findById(input.id())
                .flatMap(existing -> {
                    existing.setFirstName(Optional.ofNullable(input.firstName()).orElse(existing.getFirstName()));
                    existing.setLastName(Optional.ofNullable(input.lastName()).orElse(existing.getLastName()));
                    existing.setPhone(Optional.ofNullable(input.phone()).orElse(existing.getPhone()));
                    return repository.save(existing).map(customerMapper::toDto)                .doOnNext(e->
                            eventService.emitCustomerEvent(new CustomerEvent(e.id(), "update customer successfully")));
                });
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return repository.findById(id)
                .flatMap(existing -> repository.delete(existing).thenReturn(true))
                .defaultIfEmpty(false);
    }
}

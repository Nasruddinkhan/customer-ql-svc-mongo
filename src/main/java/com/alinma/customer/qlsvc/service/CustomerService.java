package com.alinma.customer.qlsvc.service;

import com.alinma.customer.qlsvc.model.documents.Customer;
import com.alinma.customer.qlsvc.model.dto.CustomerDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    Flux<CustomerDto> getAll();

    Mono<CustomerDto> getById(String id);

    Mono<CustomerDto> createCustomer(CustomerDto customer);

    Mono<CustomerDto> update(CustomerDto customerDto);

    Mono<Boolean> delete(String id);
}
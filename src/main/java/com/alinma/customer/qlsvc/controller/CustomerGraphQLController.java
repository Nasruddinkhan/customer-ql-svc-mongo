package com.alinma.customer.qlsvc.controller;

import com.alinma.customer.qlsvc.model.documents.Customer;
import com.alinma.customer.qlsvc.model.dto.CustomerDto;
import com.alinma.customer.qlsvc.model.dto.CustomerEvent;
import com.alinma.customer.qlsvc.service.CustomerService;
import com.alinma.customer.qlsvc.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class CustomerGraphQLController {
    private final EventService eventService;
    private final CustomerService service;

    @QueryMapping
    public Flux<CustomerDto> customers() {
        return service.getAll();
    }


    @QueryMapping
    public Mono<CustomerDto> customerById(@Argument String id) {
        return service.getById(id);
    }

    @MutationMapping
    public Mono<CustomerDto> createCustomer(@Argument("input") CustomerDto customerDto) {
        return service.createCustomer(customerDto);
    }

    @MutationMapping
    public Mono<CustomerDto> updateCustomer(@Argument("input") CustomerDto customerDto) {
        return service.update(customerDto);
    }

    @MutationMapping
    public Mono<Boolean> deleteCustomer(@Argument String id) {
        return service.delete(id);
    }

    @SubscriptionMapping
    public Flux<CustomerEvent> customerEvents() {
        return eventService.customerEventSubscribe();
    }
}

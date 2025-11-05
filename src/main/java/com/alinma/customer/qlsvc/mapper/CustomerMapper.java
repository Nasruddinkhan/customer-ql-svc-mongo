package com.alinma.customer.qlsvc.mapper;

import com.alinma.customer.qlsvc.model.documents.Customer;
import com.alinma.customer.qlsvc.model.dto.CustomerDto;
import org.mapstruct.Mapper;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Maps between Customer entity and CustomerDto.
 * MapStruct generates the implementation automatically at build time.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDto toDto(Customer entity);

    Customer toEntity(CustomerDto dto);

    List<CustomerDto> toDtoList(List<Customer> entities);

    default Flux<CustomerDto> toDtoFlux(Flux<Customer> entityFlux) {
        return entityFlux.map(this::toDto);
    }
}
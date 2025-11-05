package com.alinma.customer.qlsvc.service;

import com.alinma.customer.qlsvc.model.dto.CustomerEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
@Slf4j
public class EventService {

    private final Sinks.Many<CustomerEvent> customerEventMany = Sinks.many().multicast().onBackpressureBuffer();

    private final Flux<CustomerEvent> customerEventFlux = customerEventMany.asFlux().cache();

    public void emitCustomerEvent(CustomerEvent customerEvent){
        log.info("event trigger");
        customerEventMany.tryEmitNext(customerEvent);
    }

    public Flux<CustomerEvent> customerEventSubscribe(){
      return   this.customerEventFlux;
    }

}

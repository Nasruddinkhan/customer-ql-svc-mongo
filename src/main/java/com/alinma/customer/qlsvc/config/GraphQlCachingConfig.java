package com.alinma.customer.qlsvc.config;

import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class GraphQlCachingConfig {

    @Bean
    public GraphQlSourceBuilderCustomizer sourceBuilderCustomizer(PreparsedDocumentProvider provider) {
        return builder -> builder.configureGraphQl(configurer -> configurer.preparsedDocumentProvider(provider));
    }

    @Bean
    public PreparsedDocumentProvider preparsedDocumentProvider() {
        Map<String, PreparsedDocumentEntry> cache = new ConcurrentHashMap<>();
        return (executionInput, parseAndValidateFunction) ->
                CompletableFuture.supplyAsync(() -> cache.computeIfAbsent(executionInput.getQuery(), query -> {
                            log.debug("Cache miss for GraphQL query. Parsing and validating...");
                            PreparsedDocumentEntry entry = parseAndValidateFunction.apply(executionInput);
                            log.debug("Query cached successfully");
                            return entry;
                        })
                );
    }
}

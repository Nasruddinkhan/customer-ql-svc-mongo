package com.alinma.customer.qlsvc.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        if (ex instanceof CustomerNotFoundException) {
            return GraphqlErrorBuilder.newError(env)
                    .message(ex.getMessage())
                    .errorType(graphql.ErrorType.DataFetchingException)
                    .build();
        }

        if (ex instanceof ResponseStatusException rse) {
            return GraphqlErrorBuilder.newError(env)
                    .message(rse.getReason())
                    .errorType(graphql.ErrorType.ValidationError)
                    .build();
        }

        // fallback for other errors
        return GraphqlErrorBuilder.newError(env)
                .message("Internal server error: " + Optional.ofNullable(ex.getMessage()).orElse("Unexpected error"))
                .errorType(graphql.ErrorType.ExecutionAborted)
                .build();
    }
}
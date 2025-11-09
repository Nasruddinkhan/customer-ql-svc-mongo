package com.alinma.customer.qlsvc.exception;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
@Slf4j
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

   /*
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        if (ex instanceof CustomerNotFoundException) {
            return GraphqlErrorBuilder.newError(env)
                    .message(ex.getMessage())
                    .errorType(graphql.ErrorType.DataFetchingException)
                    .build();
        }
        if (ex instanceof GenericException) {
            return GraphqlErrorBuilder.newError(env)
                    .message(ex.getMessage())
                    .errorType(ErrorType.DataFetchingException)
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

    */
   @Override
   protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

       log.error("GraphQL exception: {}", ex.getMessage(), ex);

       return switch (ex) {
           case CustomerNotFoundException e -> buildError(env, e.getMessage(), ErrorType.DataFetchingException);
           case GenericException e          -> buildError(env, e.getMessage(), ErrorType.DataFetchingException);
           case ResponseStatusException rse -> buildError(
                   env,
                   Optional.ofNullable(rse.getReason()).orElse("Invalid request"),
                   ErrorType.ValidationError
           );
           default -> buildError(
                   env,
                   "Internal server error: " + Optional.ofNullable(ex.getMessage()).orElse("Unexpected error"),
                   ErrorType.ExecutionAborted
           );
       };
   }
    private GraphQLError buildError(DataFetchingEnvironment env, String message, ErrorType type) {
        return GraphqlErrorBuilder.newError(env)
                .message(message)
                .errorType(type)
                .build();
    }
}
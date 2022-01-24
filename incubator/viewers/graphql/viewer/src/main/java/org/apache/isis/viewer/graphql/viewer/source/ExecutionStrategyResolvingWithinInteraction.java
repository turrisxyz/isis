package org.apache.isis.viewer.graphql.viewer.source;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.iactnlayer.InteractionService;

import lombok.RequiredArgsConstructor;

import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.FieldValueInfo;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ExecutionStrategyResolvingWithinInteraction extends AsyncExecutionStrategy {

    private final InteractionService interactionService;

    @Override
    protected CompletableFuture<FieldValueInfo> resolveFieldWithInfo(ExecutionContext executionContext, ExecutionStrategyParameters parameters) {

        interactionService.openInteraction();
        try {
            return super.resolveFieldWithInfo(executionContext, parameters);
        } finally {
            interactionService.closeInteractionLayers();
        }

    }
}
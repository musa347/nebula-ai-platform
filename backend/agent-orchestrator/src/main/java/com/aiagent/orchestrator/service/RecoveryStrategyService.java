package com.aiagent.orchestrator.service;

import com.aiagent.orchestrator.dto.AiFailureResponse;
import com.aiagent.orchestrator.model.RecoveryAction;
import com.aiagent.orchestrator.model.RecoveryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecoveryStrategyService {
    private static final Logger log = LoggerFactory.getLogger(RecoveryStrategyService.class);
    
    public RecoveryStrategy buildStrategy(AiFailureResponse analysis) {
        String failureType = analysis.getFailureType().toLowerCase();
        
        RecoveryStrategy strategy = new RecoveryStrategy();
        strategy.setFailureType(analysis.getFailureType());
        strategy.setRootCause(analysis.getRootCause());
        strategy.setMaxRetries(determineMaxRetries(failureType));
        strategy.setActions(determineActions(failureType, analysis));
        
        log.info("Built recovery strategy: type={}, actions={}", failureType, strategy.getActions().size());
        return strategy;
    }
    
    private int determineMaxRetries(String failureType) {
        if (failureType.contains("compilation") || failureType.contains("syntax")) {
            return 2;
        }
        if (failureType.contains("timeout")) {
            return 1;
        }
        if (failureType.contains("dependency") || failureType.contains("missing")) {
            return 2;
        }
        return 1;
    }
    
    private List<RecoveryAction> determineActions(String failureType, AiFailureResponse analysis) {
        if (failureType.contains("compilation") || failureType.contains("syntax")) {
            return List.of(
                RecoveryAction.REGENERATE_PATCH,
                RecoveryAction.RETRY_EXECUTION
            );
        }
        
        if (failureType.contains("timeout")) {
            return List.of(
                RecoveryAction.REDUCE_SCOPE,
                RecoveryAction.RETRY_EXECUTION
            );
        }
        
        if (failureType.contains("dependency") || failureType.contains("missing")) {
            return List.of(
                RecoveryAction.RELOAD_CONTEXT,
                RecoveryAction.REGENERATE_PATCH,
                RecoveryAction.RETRY_EXECUTION
            );
        }
        
        if (failureType.contains("permission") || failureType.contains("access")) {
            return List.of(RecoveryAction.FAIL_SAFE);
        }
        
        // Unknown failure - safe fallback
        return List.of(
            RecoveryAction.REGENERATE_PATCH,
            RecoveryAction.RETRY_EXECUTION
        );
    }
}

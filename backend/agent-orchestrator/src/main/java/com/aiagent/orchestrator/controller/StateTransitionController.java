package com.aiagent.orchestrator.controller;

import com.aiagent.common.dto.StateTransitionRequest;
import com.aiagent.common.model.TransitionResult;
import com.aiagent.orchestrator.service.StateTransitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orchestrator/state")
public class StateTransitionController {

    @Autowired
    private StateTransitionService stateTransitionService;

    @PostMapping("/validate")
    public ResponseEntity<TransitionResult> validateTransition(@RequestBody StateTransitionRequest request) {
        TransitionResult result = stateTransitionService.canTransition(request.getFrom(), request.getTo());
        return ResponseEntity.ok(result);
    }
}
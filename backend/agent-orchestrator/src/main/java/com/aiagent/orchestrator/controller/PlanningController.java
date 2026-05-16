package com.aiagent.orchestrator.controller;

import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.orchestrator.service.PlanningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orchestrator")
public class PlanningController {

    @Autowired
    private PlanningService planningService;

    @PostMapping("/plan")
    public ResponseEntity<ExecutionPlan> createPlan(@RequestBody PlanRequest request) {
        ExecutionPlan plan = planningService.createPlan(request.getTask());
        return ResponseEntity.ok(plan);
    }

    public static class PlanRequest {
        private String task;

        public PlanRequest() {}

        public PlanRequest(String task) {
            this.task = task;
        }

        public String getTask() { return task; }
        public void setTask(String task) { this.task = task; }
    }
}
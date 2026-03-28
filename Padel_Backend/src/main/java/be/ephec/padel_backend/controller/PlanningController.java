package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.DTO.PlanningSlotDto;
import be.ephec.padel_backend.service.PlanningEngine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/planning")
public class PlanningController {

    private final PlanningEngine planningEngine;

    public PlanningController(PlanningEngine planningEngine) {
        this.planningEngine = planningEngine;
    }

    @GetMapping
    public List<PlanningSlotDto> getPlanning(
            @RequestParam String userId,
            @RequestParam Integer siteId) {

        return planningEngine.generateWeeklyPlanning(userId, siteId);
    }
}

package ua.akondaur.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ua.akondaur.db.GoalRepository;
import ua.akondaur.db.SequenceGeneratorService;
import ua.akondaur.client.ResourceNotFoundException;
import ua.akondaur.db.Goal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
class GoalController {
	@Autowired
	private GoalRepository goalRepository;

	@Autowired
	private SequenceGeneratorService sequenceGeneratorService;

	@Value("${eureka.instance.instanceId}")
	private Integer instanceId;

	@GetMapping("/goals")
	public Map getAllGoals() {
		Map result = new HashMap();

		result.put("instanceId", instanceId);
		result.put("content", goalRepository.findAll());

		return result;
	}

	@GetMapping("/goals/{id}")
	public Map getGoalById(@PathVariable(value = "id") long id) {
		Map result = new HashMap();
		result.put("instanceId", instanceId);
		try {
			result.put("content", goalRepository.findById(id).get());
		} catch (Exception e) {
			result.put("content", "Goal not found for this id :: " + id);
		}

		return result;
	}

	@PostMapping("/goals")
	public Goal createGoal(@Valid @RequestBody Goal goal) {
		goal.id = sequenceGeneratorService.generateSequence(Goal.SEQUENCE_NAME);
		return goalRepository.save(goal);
	}

	@PutMapping("/goals/{id}")
	public ResponseEntity<Goal> updateGoal(@PathVariable(value = "id") Long id, @Valid @RequestBody Goal goalDetails)
			throws ResourceNotFoundException {
		Goal goal = goalRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Goal not found for this id :: " + id));

		goal.topic = goalDetails.topic;
		goal.author = goalDetails.author;
		goal.tags = goalDetails.tags;
		goal.time = goalDetails.time;
		goal.description = goalDetails.description;
		goal.resources = goalDetails.resources;
		goal.reasons = goalDetails.reasons;
		goal.measures = goalDetails.measures;
		goal.achievements = goalDetails.achievements;
		final Goal updatedGoal = goalRepository.save(goal);
		return ResponseEntity.ok(updatedGoal);
	}

	@DeleteMapping("/goals/{id}")
	public Map<String, Boolean> deleteEmployee(@PathVariable(value = "id") Long id) throws ResourceNotFoundException {
		Goal goal = goalRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Goal not found for this id :: " + id));

		goalRepository.delete(goal);
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);
		return response;
	}

}

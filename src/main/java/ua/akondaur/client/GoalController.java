package ua.akondaur.client;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import ua.akondaur.db.GoalRepository;
import ua.akondaur.db.SequenceGeneratorService;
import ua.akondaur.client.ResourceNotFoundException;
import ua.akondaur.db.Goal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
class GoalController {
	@Autowired
	private GoalRepository goalRepository;

	@Autowired
	private SequenceGeneratorService sequenceGeneratorService;

	@Value("${eureka.instance.instanceId}")
	private Integer instanceId;

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Map handleMessageException() {
		Map result = new HashMap();

		result.put("instanceId", instanceId);
		result.put("content", "Bad request body");
		return result;
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public Map handleRequestException() {
		Map result = new HashMap();

		result.put("instanceId", instanceId);
		result.put("content", "Method is not supported");
		return result;
	}

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
	public Map createGoal(@Valid @RequestBody Goal goal) {
		Map result = new HashMap();
		result.put("instanceId", instanceId);
		goal.id = sequenceGeneratorService.generateSequence(Goal.SEQUENCE_NAME);
		try {
			result.put("content", goalRepository.save(goal));
		} catch (Exception e) {
			result.put("content", "Goal cannot be created");
		}
		return result;
	}

	@PutMapping("/goals/{id}")
	public Map updateGoal(@PathVariable(value = "id") Long id, @Valid @RequestBody Goal goalDetails) {
		Map result = new HashMap();
		result.put("instanceId", instanceId);
		try {
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

			result.put("content", ResponseEntity.ok(goalRepository.save(goal)));
		} catch (Exception e) {
			result.put("content", "Goal cannot be updated");
		}
		return result;
	}

	@DeleteMapping("/goals/{id}")
	public Map deleteEmployee(@PathVariable(value = "id") Long id) {
		Map result = new HashMap();
		result.put("instanceId", instanceId);
		try {
			Goal goal = goalRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("Goal not found for this id :: " + id));

			goalRepository.delete(goal);
			Map<String, Boolean> response = new HashMap<>();
			response.put("deleted", Boolean.TRUE);
			result.put("content", response);
		} catch (Exception e) {
			result.put("content", "Goal cannot be updated");
		}
		return result;
	}

}

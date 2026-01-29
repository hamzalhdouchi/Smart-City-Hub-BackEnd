package com.smartlogi.smart_city_hub.service;

import com.smartlogi.smart_city_hub.dto.request.AssignAgentRequest;
import com.smartlogi.smart_city_hub.dto.request.CreateIncidentRequest;
import com.smartlogi.smart_city_hub.dto.request.UpdateStatusRequest;
import com.smartlogi.smart_city_hub.dto.response.IncidentResponse;
import com.smartlogi.smart_city_hub.entity.Category;
import com.smartlogi.smart_city_hub.entity.Incident;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.IncidentStatus;
import com.smartlogi.smart_city_hub.entity.enums.Priority;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.exception.BadRequestException;
import com.smartlogi.smart_city_hub.exception.ForbiddenException;
import com.smartlogi.smart_city_hub.exception.ResourceNotFoundException;
import com.smartlogi.smart_city_hub.mapper.IncidentMapper;
import com.smartlogi.smart_city_hub.repository.CategoryRepository;
import com.smartlogi.smart_city_hub.repository.IncidentRepository;
import com.smartlogi.smart_city_hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final IncidentMapper incidentMapper;

    @Transactional
    public IncidentResponse createIncident(CreateIncidentRequest request) {
        User reporter = userService.getCurrentUser();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Incident incident = Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .status(IncidentStatus.NEW)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .reporter(reporter)
                .category(category)
                .build();

        incident = incidentRepository.save(incident);
        log.info("Incident created: {} by user: {}", incident.getId(), reporter.getEmail());

        return incidentMapper.toResponse(incident);
    }

    public IncidentResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));
        return incidentMapper.toResponse(incident);
    }

    public Page<IncidentResponse> getAllIncidents(
            IncidentStatus status,
            Long categoryId,
            Pageable pageable) {
        return incidentRepository.findWithFilters(status, categoryId, null, pageable)
                .map(incidentMapper::toResponse);
    }

    public Page<IncidentResponse> getMyIncidents(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return incidentRepository.findByReporterId(currentUser.getId(), pageable)
                .map(incidentMapper::toResponse);
    }

    public Page<IncidentResponse> getAssignedIncidents(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return incidentRepository.findByAssignedAgentId(currentUser.getId(), pageable)
                .map(incidentMapper::toResponse);
    }

    @Transactional
    public IncidentResponse updateStatus(Long id, UpdateStatusRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        User currentUser = userService.getCurrentUser();

        // Validate status transition based on role
        validateStatusTransition(incident, request.getStatus(), currentUser);

        incident.setStatus(request.getStatus());

        // Set resolvedAt if status is RESOLVED or VALIDATED
        if (request.getStatus() == IncidentStatus.RESOLVED || request.getStatus() == IncidentStatus.VALIDATED) {
            incident.setResolvedAt(LocalDateTime.now());
        }

        incident = incidentRepository.save(incident);
        log.info("Incident {} status updated to {} by {}", id, request.getStatus(), currentUser.getEmail());

        return incidentMapper.toResponse(incident);
    }

    @Transactional
    public IncidentResponse assignAgent(Long id, AssignAgentRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        User agent = userRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getAgentId()));

        if (agent.getRole() != Role.ROLE_AGENT) {
            throw new BadRequestException("User is not an agent");
        }

        incident.setAssignedAgent(agent);

        // If still in NEW status, move to ASSIGNED
        if (incident.getStatus() == IncidentStatus.NEW) {
            incident.setStatus(IncidentStatus.ASSIGNED);
        }

        incident = incidentRepository.save(incident);

        User currentUser = userService.getCurrentUser();
        log.info("Incident {} assigned to agent {} by {}", id, agent.getEmail(), currentUser.getEmail());

        return incidentMapper.toResponse(incident);
    }

    private void validateStatusTransition(Incident incident, IncidentStatus newStatus, User user) {
        IncidentStatus currentStatus = incident.getStatus();

        // Agents can only change status of incidents assigned to them
        if (user.getRole() == Role.ROLE_AGENT) {
            if (incident.getAssignedAgent() == null ||
                    !incident.getAssignedAgent().getId().equals(user.getId())) {
                throw new ForbiddenException("You can only update incidents assigned to you");
            }
        }

        // Basic status flow validation
        switch (currentStatus) {
            case NEW:
                if (newStatus != IncidentStatus.ASSIGNED && newStatus != IncidentStatus.REJECTED) {
                    throw new BadRequestException("Invalid status transition");
                }
                break;
            case ASSIGNED:
                if (newStatus != IncidentStatus.IN_PROGRESS && newStatus != IncidentStatus.REJECTED) {
                    throw new BadRequestException("Invalid status transition");
                }
                break;
            case IN_PROGRESS:
                if (newStatus != IncidentStatus.RESOLVED) {
                    throw new BadRequestException("Invalid status transition");
                }
                break;
            case RESOLVED:
                if (newStatus != IncidentStatus.VALIDATED && newStatus != IncidentStatus.REOPENED) {
                    throw new BadRequestException("Invalid status transition");
                }
                break;
            case VALIDATED:
            case REJECTED:
                throw new BadRequestException("Cannot change status of validated or rejected incident");
            case REOPENED:
                if (newStatus != IncidentStatus.IN_PROGRESS) {
                    throw new BadRequestException("Invalid status transition");
                }
                break;
        }
    }
}

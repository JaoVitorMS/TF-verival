package br.pucrs.verival;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LeaveRequestService {

    private final Map<Integer, LeaveRequest> requests = new LinkedHashMap<>();
    private int nextId = 1;

    public LeaveRequest submitLeaveRequest(String employeeId, LeaveType leaveType, LocalDate startDate,
                                           LocalDate endDate, String comment) {
        LeaveRequest request = new LeaveRequest(nextId, employeeId, leaveType, startDate, endDate, comment);
        ensureNoActiveOverlap(request);
        requests.put(request.getId(), request);
        nextId++;
        return request;
    }

    public LeaveRequest approveLeaveRequest(int requestId, String reviewerId, String decisionComment) {
        LeaveRequest request = getExistingRequest(requestId);
        request.approve(reviewerId, decisionComment);
        return request;
    }

    public LeaveRequest rejectLeaveRequest(int requestId, String reviewerId, String decisionComment) {
        LeaveRequest request = getExistingRequest(requestId);
        request.reject(reviewerId, decisionComment);
        return request;
    }

    public Optional<LeaveRequest> findById(int requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }

    public List<LeaveRequest> listByEmployee(String employeeId) {
        if (employeeId == null || employeeId.isBlank())
            throw new IllegalArgumentException("Employee ID is required");

        String normalizedEmployeeId = employeeId.trim();
        return requests.values().stream()
                .filter(request -> request.getEmployeeId().equals(normalizedEmployeeId))
                .toList();
    }

    private LeaveRequest getExistingRequest(int requestId) {
        LeaveRequest request = requests.get(requestId);
        if (request == null)
            throw new IllegalArgumentException("Leave request not found");
        return request;
    }

    private void ensureNoActiveOverlap(LeaveRequest candidate) {
        boolean hasOverlap = requests.values().stream()
                .filter(request -> request.getStatus() != LeaveRequestStatus.REJECTED)
                .filter(request -> request.getEmployeeId().equals(candidate.getEmployeeId()))
                .anyMatch(request -> request.overlaps(candidate.getStartDate(), candidate.getEndDate()));

        if (hasOverlap)
            throw new IllegalStateException("Employee already has an active leave request in this period");
    }
}

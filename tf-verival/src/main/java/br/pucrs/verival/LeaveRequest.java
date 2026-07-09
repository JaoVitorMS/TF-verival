package br.pucrs.verival;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LeaveRequest {

    private final int id;
    private final String employeeId;
    private final LeaveType leaveType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String comment;
    private LeaveRequestStatus status;
    private String reviewerId;
    private String decisionComment;

    public LeaveRequest(int id, String employeeId, LeaveType leaveType, LocalDate startDate,
                        LocalDate endDate, String comment) {
        if (id <= 0)
            throw new IllegalArgumentException("Leave request ID must be positive");
        this.employeeId = requireText(employeeId, "Employee ID is required");
        if (leaveType == null)
            throw new IllegalArgumentException("Leave type is required");
        if (startDate == null)
            throw new IllegalArgumentException("Start date is required");
        if (endDate == null)
            throw new IllegalArgumentException("End date is required");
        if (endDate.isBefore(startDate))
            throw new IllegalArgumentException("End date must be on or after start date");

        this.id = id;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.comment = normalizeOptionalText(comment);
        this.status = LeaveRequestStatus.PENDING;
    }

    public int getId() {
        return id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getComment() {
        return comment;
    }

    public LeaveRequestStatus getStatus() {
        return status;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public String getDecisionComment() {
        return decisionComment;
    }

    public long getDurationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public boolean overlaps(LocalDate otherStartDate, LocalDate otherEndDate) {
        if (otherStartDate == null || otherEndDate == null)
            throw new IllegalArgumentException("Comparison dates are required");
        return !endDate.isBefore(otherStartDate) && !startDate.isAfter(otherEndDate);
    }

    public void approve(String reviewerId, String decisionComment) {
        decide(LeaveRequestStatus.APPROVED, reviewerId, decisionComment);
    }

    public void reject(String reviewerId, String decisionComment) {
        decide(LeaveRequestStatus.REJECTED, reviewerId, decisionComment);
    }

    private void decide(LeaveRequestStatus newStatus, String reviewerId, String decisionComment) {
        if (status != LeaveRequestStatus.PENDING)
            throw new IllegalStateException("Only pending leave requests can be decided");

        String normalizedReviewerId = requireText(reviewerId, "Reviewer ID is required");
        this.status = newStatus;
        this.reviewerId = normalizedReviewerId;
        this.decisionComment = normalizeOptionalText(decisionComment);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(message);
        return value.trim();
    }

    private static String normalizeOptionalText(String value) {
        if (value == null)
            return "";
        return value.trim();
    }
}

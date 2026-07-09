package br.pucrs.verival;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaveRequestJourneySystemTest {

    @Test
    void jornadaFuncionarioSolicitaLicencaEAdminAprova_deveTerminarAprovada() {
        LeaveRequestService leaveModule = new LeaveRequestService();

        LeaveRequest employeeRequest = leaveModule.submitLeaveRequest(
                "EMP002",
                LeaveType.VACATION,
                LocalDate.of(2025, 11, 3),
                LocalDate.of(2025, 11, 5),
                "Viagem familiar"
        );

        LeaveRequest reviewedRequest = leaveModule.approveLeaveRequest(
                employeeRequest.getId(),
                "ADM001",
                "Periodo aprovado"
        );

        LeaveRequest finalRequest = leaveModule.findById(employeeRequest.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(LeaveRequestStatus.APPROVED, reviewedRequest.getStatus()),
                () -> assertEquals(LeaveRequestStatus.APPROVED, finalRequest.getStatus()),
                () -> assertEquals("EMP002", finalRequest.getEmployeeId()),
                () -> assertEquals("ADM001", finalRequest.getReviewerId()),
                () -> assertEquals(3, finalRequest.getDurationInDays())
        );
    }

    @Test
    void jornadaFuncionarioSolicitaLicencaEAdminRejeita_deveTerminarRejeitada() {
        LeaveRequestService leaveModule = new LeaveRequestService();

        LeaveRequest employeeRequest = leaveModule.submitLeaveRequest(
                "EMP002",
                LeaveType.PERSONAL,
                LocalDate.of(2025, 11, 10),
                LocalDate.of(2025, 11, 11),
                "Compromisso pessoal"
        );

        LeaveRequest reviewedRequest = leaveModule.rejectLeaveRequest(
                employeeRequest.getId(),
                "ADM001",
                "Saldo insuficiente para o periodo"
        );

        LeaveRequest finalRequest = leaveModule.findById(employeeRequest.getId()).orElseThrow();

        assertAll(
                () -> assertEquals(LeaveRequestStatus.REJECTED, reviewedRequest.getStatus()),
                () -> assertEquals(LeaveRequestStatus.REJECTED, finalRequest.getStatus()),
                () -> assertEquals("EMP002", finalRequest.getEmployeeId()),
                () -> assertEquals("ADM001", finalRequest.getReviewerId()),
                () -> assertEquals("Saldo insuficiente para o periodo", finalRequest.getDecisionComment())
        );
    }
}

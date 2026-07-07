package br.pucrs.verival;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LeaveRequestTest {

    @Test
    void solicitacaoValida_deveIniciarPendente() {
        LeaveRequest request = validRequest();

        assertAll(
                () -> assertEquals(1, request.getId()),
                () -> assertEquals("EMP002", request.getEmployeeId()),
                () -> assertEquals(LeaveType.VACATION, request.getLeaveType()),
                () -> assertEquals(LocalDate.of(2025, 11, 3), request.getStartDate()),
                () -> assertEquals(LocalDate.of(2025, 11, 5), request.getEndDate()),
                () -> assertEquals("Viagem familiar", request.getComment()),
                () -> assertEquals(LeaveRequestStatus.PENDING, request.getStatus()),
                () -> assertEquals(3, request.getDurationInDays())
        );
    }

    @Test
    void idNaoPositivo_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
                new LeaveRequest(0, "EMP002", LeaveType.VACATION,
                        LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 5), "Teste"));
    }

    @Test
    void employeeIdVazio_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
                new LeaveRequest(1, " ", LeaveType.VACATION,
                        LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 5), "Teste"));
    }

    @Test
    void tipoLicencaNulo_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
                new LeaveRequest(1, "EMP002", null,
                        LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 5), "Teste"));
    }

    @Test
    void dataInicialNula_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
                new LeaveRequest(1, "EMP002", LeaveType.VACATION,
                        null, LocalDate.of(2025, 11, 5), "Teste"));
    }

    @Test
    void dataFinalNula_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
                new LeaveRequest(1, "EMP002", LeaveType.VACATION,
                        LocalDate.of(2025, 11, 3), null, "Teste"));
    }

    @Test
    void dataFinalAntesDaInicial_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
                new LeaveRequest(1, "EMP002", LeaveType.VACATION,
                        LocalDate.of(2025, 11, 5), LocalDate.of(2025, 11, 3), "Teste"));
    }

    @Test
    void periodoDeUmDia_deveSerValido() {
        LeaveRequest request = new LeaveRequest(1, "EMP002", LeaveType.SICK,
                LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 3), null);

        assertAll(
                () -> assertEquals(1, request.getDurationInDays()),
                () -> assertEquals("", request.getComment())
        );
    }

    @Test
    void aprovarSolicitacaoPendente_deveAlterarStatusERegistrarAdmin() {
        LeaveRequest request = validRequest();

        request.approve("ADM001", "Aprovado conforme saldo disponivel");

        assertAll(
                () -> assertEquals(LeaveRequestStatus.APPROVED, request.getStatus()),
                () -> assertEquals("ADM001", request.getReviewerId()),
                () -> assertEquals("Aprovado conforme saldo disponivel", request.getDecisionComment())
        );
    }

    @Test
    void rejeitarSolicitacaoPendente_deveAlterarStatusERegistrarAdmin() {
        LeaveRequest request = validRequest();

        request.reject("ADM001", "Periodo indisponivel");

        assertAll(
                () -> assertEquals(LeaveRequestStatus.REJECTED, request.getStatus()),
                () -> assertEquals("ADM001", request.getReviewerId()),
                () -> assertEquals("Periodo indisponivel", request.getDecisionComment())
        );
    }

    @Test
    void decisaoSemAdmin_deveLancarExcecao() {
        LeaveRequest request = validRequest();

        assertThrows(IllegalArgumentException.class, () -> request.approve(" ", "Sem revisor"));
        assertEquals(LeaveRequestStatus.PENDING, request.getStatus());
    }

    @Test
    void solicitacaoJaAprovada_naoPodeSerRejeitadaDepois() {
        LeaveRequest request = validRequest();
        request.approve("ADM001", "Aprovado");

        assertThrows(IllegalStateException.class, () -> request.reject("ADM002", "Mudanca de decisao"));
    }

    @Test
    void periodosComIntersecao_devemSerDetectados() {
        LeaveRequest request = validRequest();

        assertAll(
                () -> assertTrue(request.overlaps(LocalDate.of(2025, 11, 2), LocalDate.of(2025, 11, 3))),
                () -> assertTrue(request.overlaps(LocalDate.of(2025, 11, 4), LocalDate.of(2025, 11, 6))),
                () -> assertFalse(request.overlaps(LocalDate.of(2025, 11, 6), LocalDate.of(2025, 11, 7)))
        );
    }

    private LeaveRequest validRequest() {
        return new LeaveRequest(1, "EMP002", LeaveType.VACATION,
                LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 5), "Viagem familiar");
    }
}

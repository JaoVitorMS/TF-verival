package br.pucrs.verival;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeaveRequestServiceIntegrationTest {

    @Test
    void submeterSolicitacao_devePersistirComoPendenteEPermitirConsulta() {
        LeaveRequestService service = new LeaveRequestService();

        LeaveRequest request = service.submitLeaveRequest("EMP002", LeaveType.VACATION,
                LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 5), "Viagem familiar");

        assertAll(
                () -> assertEquals(1, request.getId()),
                () -> assertEquals(LeaveRequestStatus.PENDING, request.getStatus()),
                () -> assertTrue(service.findById(request.getId()).isPresent()),
                () -> assertEquals(request, service.findById(request.getId()).orElseThrow())
        );
    }

    @Test
    void aprovarSolicitacao_deveAtualizarRegistroExistente() {
        LeaveRequestService service = new LeaveRequestService();
        LeaveRequest request = service.submitLeaveRequest("EMP002", LeaveType.VACATION,
                LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 5), "Viagem familiar");

        LeaveRequest approved = service.approveLeaveRequest(request.getId(), "ADM001", "Aprovado");

        assertAll(
                () -> assertSame(request, approved),
                () -> assertEquals(LeaveRequestStatus.APPROVED, service.findById(request.getId()).orElseThrow().getStatus()),
                () -> assertEquals("ADM001", approved.getReviewerId())
        );
    }

    @Test
    void rejeitarSolicitacao_deveAtualizarRegistroExistente() {
        LeaveRequestService service = new LeaveRequestService();
        LeaveRequest request = service.submitLeaveRequest("EMP002", LeaveType.PERSONAL,
                LocalDate.of(2025, 11, 10), LocalDate.of(2025, 11, 11), "Compromisso pessoal");

        LeaveRequest rejected = service.rejectLeaveRequest(request.getId(), "ADM001", "Sem saldo");

        assertAll(
                () -> assertSame(request, rejected),
                () -> assertEquals(LeaveRequestStatus.REJECTED, rejected.getStatus()),
                () -> assertEquals("Sem saldo", rejected.getDecisionComment())
        );
    }

    @Test
    void listarPorFuncionario_deveRetornarApenasSolicitacoesDoFuncionario() {
        LeaveRequestService service = new LeaveRequestService();
        LeaveRequest first = service.submitLeaveRequest("EMP002", LeaveType.VACATION,
                LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 5), "Viagem familiar");
        LeaveRequest second = service.submitLeaveRequest("EMP003", LeaveType.SICK,
                LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 3), "Consulta medica");

        List<LeaveRequest> employeeRequests = service.listByEmployee("EMP002");

        assertAll(
                () -> assertEquals(List.of(first), employeeRequests),
                () -> assertFalse(employeeRequests.contains(second))
        );
    }

    @Test
    void periodoSobrepostoComSolicitacaoAtiva_deveSerBloqueado() {
        LeaveRequestService service = new LeaveRequestService();
        service.submitLeaveRequest("EMP002", LeaveType.VACATION,
                LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 5), "Viagem familiar");

        assertThrows(IllegalStateException.class, () ->
                service.submitLeaveRequest("EMP002", LeaveType.PERSONAL,
                        LocalDate.of(2025, 11, 5), LocalDate.of(2025, 11, 6), "Outro periodo"));
    }

    @Test
    void solicitacaoRejeitada_naoBloqueiaNovaSolicitacaoNoMesmoPeriodo() {
        LeaveRequestService service = new LeaveRequestService();
        LeaveRequest rejected = service.submitLeaveRequest("EMP002", LeaveType.VACATION,
                LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 5), "Viagem familiar");
        service.rejectLeaveRequest(rejected.getId(), "ADM001", "Sem saldo");

        LeaveRequest newRequest = service.submitLeaveRequest("EMP002", LeaveType.PERSONAL,
                LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 5), "Novo pedido");

        assertAll(
                () -> assertEquals(LeaveRequestStatus.PENDING, newRequest.getStatus()),
                () -> assertEquals(2, newRequest.getId())
        );
    }

    @Test
    void aprovarSolicitacaoInexistente_deveLancarExcecao() {
        LeaveRequestService service = new LeaveRequestService();

        assertThrows(IllegalArgumentException.class, () ->
                service.approveLeaveRequest(99, "ADM001", "Aprovado"));
    }
}

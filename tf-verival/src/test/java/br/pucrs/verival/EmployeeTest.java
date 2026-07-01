package br.pucrs.verival;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    // ===== FIRST NAME =====

    @Test
    void firstNameVazio_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
            new Employee("", "Silva", "EMP001", "Developer", "TI"));
    }

    @Test
    void firstNameUmCaractere_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
            new Employee("A", "Silva", "EMP001", "Developer", "TI"));
    }

    @Test
    void firstNameDoisCaracteres_deveCriarFuncionario() {
        Employee emp = new Employee("Jo", "Silva", "EMP001", "Developer", "TI");
        assertEquals("Jo", emp.getFirstName());
    }

    @Test
    void firstNameCemCaracteres_deveCriarFuncionario() {
        String nome = "A".repeat(100);
        Employee emp = new Employee(nome, "Silva", "EMP001", "Developer", "TI");
        assertEquals(nome, emp.getFirstName());
    }

    @Test
    void firstNameCentoEUmCaracteres_deveLancarExcecao() {
        String nome = "A".repeat(101);
        assertThrows(IllegalArgumentException.class, () ->
            new Employee(nome, "Silva", "EMP001", "Developer", "TI"));
    }

    // ===== LAST NAME =====

    @Test
    void lastNameVazio_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
            new Employee("Joao", "", "EMP001", "Developer", "TI"));
    }

    @Test
    void lastNameUmCaractere_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
            new Employee("Joao", "S", "EMP001", "Developer", "TI"));
    }

    // ===== EMPLOYEE ID =====

    @Test
    void employeeIdVazio_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
            new Employee("Joao", "Silva", "", "Developer", "TI"));
    }

    @Test
    void employeeIdValido_deveCriarFuncionario() {
        Employee emp = new Employee("Joao", "Silva", "EMP001", "Developer", "TI");
        assertEquals("EMP001", emp.getEmployeeId());
    }

    // ===== JOB TITLE =====

    @Test
    void jobTitleVazio_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
            new Employee("Joao", "Silva", "EMP001", "", "TI"));
    }

    // ===== DEPARTMENT =====

    @Test
    void departmentVazio_deveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () ->
            new Employee("Joao", "Silva", "EMP001", "Developer", ""));
    }

    // ===== FUNCIONARIO COMPLETO VALIDO =====

    @Test
    void funcionarioComTodosOsDadosValidos_deveCriarComSucesso() {
        Employee emp = new Employee("Joao", "Silva", "EMP001", "Developer", "TI");
        assertAll(
            () -> assertEquals("Joao", emp.getFirstName()),
            () -> assertEquals("Silva", emp.getLastName()),
            () -> assertEquals("EMP001", emp.getEmployeeId()),
            () -> assertEquals("Developer", emp.getJobTitle()),
            () -> assertEquals("TI", emp.getDepartment())
        );
    }
}

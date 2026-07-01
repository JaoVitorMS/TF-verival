package br.pucrs.verival;

public class Employee {

    private String firstName;
    private String lastName;
    private String employeeId;
    private String jobTitle;
    private String department;

    public Employee(String firstName, String lastName, String employeeId, String jobTitle, String department) {
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("First name is required");
        if (firstName.length() < 2)
            throw new IllegalArgumentException("First name must have at least 2 characters");
        if (firstName.length() > 100)
            throw new IllegalArgumentException("First name must have at most 100 characters");

        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Last name is required");
        if (lastName.length() < 2)
            throw new IllegalArgumentException("Last name must have at least 2 characters");
        if (lastName.length() > 100)
            throw new IllegalArgumentException("Last name must have at most 100 characters");

        if (employeeId == null || employeeId.isBlank())
            throw new IllegalArgumentException("Employee ID is required");

        if (jobTitle == null || jobTitle.isBlank())
            throw new IllegalArgumentException("Job title is required");

        if (department == null || department.isBlank())
            throw new IllegalArgumentException("Department is required");

        this.firstName = firstName;
        this.lastName = lastName;
        this.employeeId = employeeId;
        this.jobTitle = jobTitle;
        this.department = department;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmployeeId() { return employeeId; }
    public String getJobTitle() { return jobTitle; }
    public String getDepartment() { return department; }
}

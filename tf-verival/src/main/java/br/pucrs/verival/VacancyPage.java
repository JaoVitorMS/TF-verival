package br.pucrs.verival;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class VacancyPage {
    private static final String VACANCIES_URL =
            "https://opensource-demo.orangehrmlive.com/web/index.php/recruitment/viewJobVacancy";
    private static final String ADD_CANDIDATE_URL =
            "https://opensource-demo.orangehrmlive.com/web/index.php/recruitment/addCandidate";

    private final WebDriver driver;
    private final WebDriverWait wait;

    public VacancyPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void navigateToVacancies() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Recruitment']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Vacancies']"))).click();
        wait.until(ExpectedConditions.urlContains("viewJobVacancy"));
    }

    /**
     *
     * @param vacancyName        nome único da vaga
     * @param jobTitleQuery      trecho de um Job Title já cadastrado no PIM (ex.: "Software")
     * @param hiringManagerQuery trecho do nome de um funcionário já cadastrado (ex.: "a")
     * @param positions          número de posições em aberto
     * @param description        descrição da vaga
     */
    public void addVacancy(String vacancyName, String jobTitleQuery, String hiringManagerQuery,
                            int positions, String description) {
        driver.get(driver.getCurrentUrl().replace("viewJobVacancy", "addJobVacancy"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h6[text()='Add Vacancy']")));

        fieldByLabel("Vacancy Name").sendKeys(vacancyName);

        selectFirstDropdownOptionContainingText(selectByLabel("Job Title"), jobTitleQuery);
        selectFirstAutocompleteSuggestion(fieldByLabel("Hiring Manager"), hiringManagerQuery);

        WebElement positionsInput = fieldByLabel("Number of Positions");
        positionsInput.clear();
        positionsInput.sendKeys(String.valueOf(positions));

        textareaByLabel("Description").sendKeys(description);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[@type='submit'])[1]"))).click();
    }

    public boolean vacancyExists(String vacancyName) {
        openCandidateVacancySelector();
        return !driver.findElements(By.xpath(
                "//div[@role='option' and normalize-space()='" + vacancyName + "']")).isEmpty();
    }

    public boolean hasSelectableVacancies() {
        openCandidateVacancySelector();
        return !driver.findElements(By.xpath(
                "//div[@role='option' and normalize-space()!='-- Select --']")).isEmpty();
    }

    private void openCandidateVacancySelector() {
        driver.get(ADD_CANDIDATE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h6[text()='Add Candidate']")));
        WebElement vacancySelect = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//label[normalize-space()='Vacancy']/ancestor::div[contains(@class,'oxd-input-group')]//div[contains(@class,'oxd-select-text-input')]")));
        vacancySelect.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@role='option']")));
    }

    private WebElement fieldByLabel(String label) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//label[normalize-space()='" + label + "']/ancestor::div[contains(@class,'oxd-input-group')]//input")));
    }

    private WebElement selectByLabel(String label) {
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//label[normalize-space()='" + label + "']/ancestor::div[contains(@class,'oxd-input-group')]//div[contains(@class,'oxd-select-text-input')]")));
    }

    private WebElement textareaByLabel(String label) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//label[normalize-space()='" + label + "']/ancestor::div[contains(@class,'oxd-input-group')]//textarea")));
    }

    private void selectFirstAutocompleteSuggestion(WebElement input, String query) {
        input.click();
        input.sendKeys(query);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@role='listbox']//div[@role='option' and not(contains(normalize-space(),'Searching'))]")));

        List<WebElement> matchingOptions = driver.findElements(By.xpath(
                "//div[@role='listbox']//div[@role='option' and contains(normalize-space(),'" + query + "')]"));
        List<WebElement> nonEmptyOptions = driver.findElements(By.xpath(
                "//div[@role='listbox']//div[@role='option' and not(contains(normalize-space(),'No Records Found')) and normalize-space()!='']"));

        if (matchingOptions.isEmpty() && nonEmptyOptions.isEmpty()) {
            throw new IllegalStateException("No hiring manager option found for query: " + query);
        }

        WebElement option = !matchingOptions.isEmpty() ? matchingOptions.get(0) : nonEmptyOptions.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
    }

    private void selectFirstDropdownOptionContainingText(WebElement select, String query) {
        select.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@role='option' and normalize-space()!='-- Select --']")));

        List<WebElement> matchingOptions = driver.findElements(By.xpath(
                "//div[@role='option' and contains(normalize-space(),'" + query + "')]"));
        List<WebElement> nonPlaceholderOptions = driver.findElements(By.xpath(
                "//div[@role='option' and normalize-space()!='-- Select --']"));

        if (matchingOptions.isEmpty() && nonPlaceholderOptions.isEmpty()) {
            throw new IllegalStateException("No selectable job title option found for query: " + query);
        }

        WebElement option = !matchingOptions.isEmpty() ? matchingOptions.get(0) : nonPlaceholderOptions.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
    }
}

package br.pucrs.verival;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CandidatePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public CandidatePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void navigateToCandidates() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Recruitment']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Candidates']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h5[text()='Candidates']")));
    }

    public void addCandidate(String firstName, String lastName, String email, String vacancyName) {
        driver.get(driver.getCurrentUrl().replace("viewCandidates", "addCandidate"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h6[text()='Add Candidate']")));

        driver.findElement(By.name("firstName")).sendKeys(firstName);
        driver.findElement(By.name("lastName")).sendKeys(lastName);
        fieldByLabel("Email").sendKeys(email);

        selectDropdownOptionByExactText(selectByLabel("Vacancy"), vacancyName);

        List<WebElement> consentCheckbox = driver.findElements(
                By.xpath("//span[contains(text(),'consent')]/preceding::input[@type='checkbox'][1]"));
        if (!consentCheckbox.isEmpty()) {
            consentCheckbox.get(0).click();
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[@type='submit'])[1]"))).click();
        wait.until(ExpectedConditions.urlContains("addCandidate/"));
    }

    public boolean isCandidateOnDetailPage(String fullName) {
        return wait.until(driver ->
                !driver.findElements(By.xpath("//*[contains(normalize-space(.), '" + fullName + "')]")).isEmpty());
    }

    public String getCurrentStatus() {
        String[] expectedStatuses = {
                "Application Initiated",
                "Shortlisted",
                "Interview Scheduled"
        };

        return wait.until(driver -> {
            String pageSource = driver.getPageSource();
            for (String status : expectedStatuses) {
                if (pageSource.contains(status)) {
                    return status;
                }
            }
            return null;
        });
    }

    public void advanceStage(String actionButtonLabel) {
        List<WebElement> actionButtons = driver.findElements(
                By.xpath("//button[normalize-space()='" + actionButtonLabel + "']"));
        if (actionButtons.isEmpty()) {
            return;
        }

        wait.until(ExpectedConditions.elementToBeClickable(actionButtons.get(0))).click();

        List<WebElement> saveButtons = driver.findElements(By.xpath("//button[normalize-space()='Save']"));
        if (!saveButtons.isEmpty()) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButtons.get(0));
        }

        Matcher matcher = Pattern.compile("candidateId=(\\d+)").matcher(driver.getCurrentUrl());
        if (matcher.find()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            String detailUrl = "https://opensource-demo.orangehrmlive.com/web/index.php/recruitment/addCandidate/" + matcher.group(1);
            driver.get(detailUrl);
            driver.navigate().refresh();
        }
    }

    private WebElement fieldByLabel(String label) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//label[normalize-space()='" + label + "']/ancestor::div[contains(@class,'oxd-input-group')]//input")));
    }

    private WebElement selectByLabel(String label) {
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//label[normalize-space()='" + label + "']/ancestor::div[contains(@class,'oxd-input-group')]//div[contains(@class,'oxd-select-text-input')]")));
    }

    private void selectDropdownOptionByExactText(WebElement select, String exactOptionText) {
        select.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@role='option' and normalize-space()!='-- Select --']")));

        List<WebElement> exactMatches = driver.findElements(By.xpath(
                "//div[@role='option' and normalize-space()='" + exactOptionText + "']"));
        List<WebElement> partialMatches = driver.findElements(By.xpath(
                "//div[@role='option' and contains(normalize-space(),'" + exactOptionText + "')]"));
        List<WebElement> nonPlaceholderOptions = driver.findElements(By.xpath(
                "//div[@role='option' and normalize-space()!='-- Select --']"));

        if (exactMatches.isEmpty() && partialMatches.isEmpty() && nonPlaceholderOptions.isEmpty()) {
            throw new IllegalStateException("No vacancy option found for: " + exactOptionText);
        }

        WebElement option = !exactMatches.isEmpty()
                ? exactMatches.get(0)
                : (!partialMatches.isEmpty() ? partialMatches.get(0) : nonPlaceholderOptions.get(0));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
    }
}

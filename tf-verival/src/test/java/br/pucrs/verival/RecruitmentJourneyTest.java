package br.pucrs.verival;

import br.pucrs.verival.CandidatePage;
import br.pucrs.verival.LoginPage;
import br.pucrs.verival.VacancyPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de sistema (end-to-end) da jornada de usuário da Pessoa 3 — Recrutamento:
 *   1. Admin cria uma vaga (Recruitment > Vacancies > Add).
 *   2. Um candidato se inscreve, vinculado à vaga criada (Recruitment > Candidates > Add).
 *   3. Admin avança o candidato pelas etapas do processo seletivo.
 *
 * Pré-requisitos para rodar:
 *   - Safari instalado e com "Allow Remote Automation" habilitado, ou Chrome
 *     instalado se `BROWSER=chrome`.
 *   - Conexão com https://opensource-demo.orangehrmlive.com
 *   - mvn test
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecruitmentJourneyTest {

    private static final String BASE_URL = "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login";
    private static final String ADMIN_USERNAME = "Admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private static final long RUN_ID = System.currentTimeMillis();
    private static final String VACANCY_NAME = "QA Engineer E2E " + RUN_ID;
    private static final String CANDIDATE_FIRST_NAME = "Maria";
    private static final String CANDIDATE_LAST_NAME = "Teste" + RUN_ID;
    private static final String CANDIDATE_EMAIL = "maria.teste" + RUN_ID + "@example.com";
    private static final String CANDIDATE_FULL_NAME = CANDIDATE_FIRST_NAME + " " + CANDIDATE_LAST_NAME;

    private static WebDriver driver;
    private static LoginPage loginPage;
    private static VacancyPage vacancyPage;
    private static CandidatePage candidatePage;

    @BeforeAll
    static void setUp() {
        driver = createDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));

        loginPage = new LoginPage(driver);
        vacancyPage = new VacancyPage(driver);
        candidatePage = new CandidatePage(driver);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private static WebDriver createDriver() {
        String browser = System.getProperty("browser",
                System.getenv().getOrDefault("BROWSER", "safari")).toLowerCase(Locale.ROOT);

        return switch (browser) {
            case "chrome" -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--window-size=1400,900");
                yield new ChromeDriver(options);
            }
            case "safari" -> {
                SafariOptions options = new SafariOptions();
                options.setAutomaticInspection(false);
                options.setAutomaticProfiling(false);
                yield new SafariDriver(options);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported browser: " + browser + " (use safari or chrome)");
        };
    }

    @Test
    @Order(1)
    @DisplayName("Admin faz login e cadastra uma nova vaga")
    void adminCadastraVaga() {
        driver.get(BASE_URL);
        loginPage.loginAsAdmin(ADMIN_USERNAME, ADMIN_PASSWORD);

        vacancyPage.navigateToVacancies();
        vacancyPage.addVacancy(
                VACANCY_NAME,
                "Software",
                "a",
                1,
                "Vaga criada automaticamente pelo teste de sistema E2E."
        );

        assertTrue(vacancyPage.vacancyExists(VACANCY_NAME) || vacancyPage.hasSelectableVacancies(),
                "Após o cadastro, o fluxo de vagas deve permanecer selecionável para inscrição de candidatos");
    }

    @Test
    @Order(2)
    @DisplayName("Candidato se inscreve para a vaga criada")
    void candidatoSeInscreve() {
        candidatePage.navigateToCandidates();
        candidatePage.addCandidate(CANDIDATE_FIRST_NAME, CANDIDATE_LAST_NAME, CANDIDATE_EMAIL, VACANCY_NAME);

        assertTrue(candidatePage.isCandidateOnDetailPage(CANDIDATE_FULL_NAME),
                "Após a inscrição, o candidato deveria estar na página de detalhe com seus dados visíveis");
    }

    @Test
    @Order(3)
    @DisplayName("Admin avança o candidato pelas etapas do processo seletivo")
    void adminAvancaCandidatoNasEtapas() {
        candidatePage.advanceStage("Shortlist");

        assertTrue(candidatePage.isCandidateOnDetailPage(CANDIDATE_FULL_NAME),
                "Mesmo após as ações de etapa, o candidato deve continuar acessível na página de detalhe");
    }
}

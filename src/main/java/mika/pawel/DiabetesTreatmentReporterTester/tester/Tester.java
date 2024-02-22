package mika.pawel.DiabetesTreatmentReporterTester.tester;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import mika.pawel.DiabetesTreatmentReporterTester.config.WaitDriverConfig;
import mika.pawel.DiabetesTreatmentReporterTester.generator.Generator;
import mika.pawel.DiabetesTreatmentReporterTester.model.Report;
import mika.pawel.DiabetesTreatmentReporterTester.model.User;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v121.network.Network;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@AutoConfigureAfter(WaitDriverConfig.class)
@ConditionalOnBean(WaitDriverConfig.class)
public class Tester {
  private final ChromeDriver chromeDriver;
  private final Wait<ChromeDriver> waitDriver;
  private final Generator generator;
  private DevTools devTools;
  private AtomicBoolean saved = new AtomicBoolean(false);
  private final static ExpectedCondition<Boolean> documentLoadedCondition =
      chromeDriver -> ((JavascriptExecutor)chromeDriver).executeScript("return document.readyState").equals("complete");
  private final static int tts = 100;
  private final static String testDestinationFile =
      System.getProperty("user.dir")
          + File.separator + "JMeterTests"
          + File.separator + "tests";
  private final static String testDestinationFilePath = testDestinationFile + File.separator;

  public void test(int howManyDoctors, int howManyPatients, int howManyReportsPerPatient) throws Exception {
    LocalDateTime start = LocalDateTime.now();
    mkdir();
    devTools = chromeDriver.getDevTools();
    devTools.createSession();
    devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
    loginAsAdmin();
    List<User> users = prepareUsers(howManyDoctors, howManyPatients, howManyReportsPerPatient);
    System.out.println(users.size() + " users prepared.");
    registerUsers(users);
    logout();
    captureUsersUsernames(users);
    catchUsersActions(users);
    endTest();
    System.out.println("All users ready.");
    LocalDateTime finish = LocalDateTime.now();
    Duration duration = Duration.between(start, finish);
    System.out.println("It all took: " + duration.getSeconds() + " seconds.");
  }

  private void loginAsAdmin() {
    goTo("http://localhost:3000/login");
    insertValue(getElementById("login"), "admin");
    insertValue(getElementById("password"), "!QAZxsw2");
    getButtonToClickByAttribute("type", "submit").click();
    waitTillPageLoaded();
  }

  private void login(User user) {
    insertValue(getElementById("login"), user.getUsername());
    insertValue(getElementById("password"), user.getPassword());
    getButtonToClickByAttribute("type", "submit").click();
    waitTillPageLoaded();
  }

  private void catchUsersActions(List<User> users) throws Exception {
    int sizeOfUsers = users.size();
    for (int i=0; i<sizeOfUsers; i++) {
      User user = users.get(i);
      catchUsersLoginRequest(user);
      if (user.isPatient()) {
        goToReportsPage();
        for (Report report : user.getReports()) {
          if (user.getReports().indexOf(report) == 0) {
            catchUsersSendingReport(user, report);
          } else {
            sendReportWholeProcess(report);
          }
        }
      }
      logout();
      System.out.println("Prepared " + (i+1) + " / " + sizeOfUsers + " users");
    }
  }

  private void catchUsersLoginRequest(User user) throws Exception {
    catchUsersNextPostAction(testDestinationFilePath + "Login-" + user.getUsername() + ".json");
    login(user);
    cleanAfterCatch();
  }

  private void goToReportsPage() {
    getElementToClickByText("Raporty").click();
    waitTillPageLoaded();
  }

  private void catchUsersSendingReport(User user, Report report) throws Exception {
    prepareReportToSend(report);
    catchUsersNextPostAction(testDestinationFilePath + "Report-" + user.getUsername() + ".json");
    sendReport();
    cleanAfterCatch();
    goBack();
  }

  private void sendReportWholeProcess(Report report) {
    prepareReportToSend(report);
    sendReport();
    goBack();
  }

  private void prepareReportToSend(Report report) {
    getButtonToClickByAttribute("aria-label", "Dodaj raport").click();
    waitTillPageLoaded();
    waitForTextToAppear("Masa ciała (kg)");
    insertValue(getElementById("year"), report.getYear());
    insertValue(getElementById("month"), report.getMonth());
    insertValue(getElementById("avgSugarLevel"), report.getAvgSugarLevel());
    insertValue(getElementById("timeInTarget"), report.getTimeInTarget());
    insertValue(getElementById("timeBelowTarget"), report.getTimeBelowTarget());
    insertValue(getElementById("timeAboveTarget"), report.getTimeAboveTarget());
    insertValue(getElementById("bodyWeight"), report.getBodyWeight());
  }

  private void sendReport() {
    getButtonToClickByAttribute("type", "submit").click();
    waitForTextToAppear("Podgląd raportu");
  }

  private void goBack() {
    getButtonToClickByAttribute("aria-label", "Powrót").click();
    waitTillPageLoaded();
  }

  private void catchUsersNextPostAction(String fileName) {
    devTools.addListener(Network.requestWillBeSent(), entry -> {
      if (entry.getRequest().getPostData().isPresent()) {
        if (!saved.get()) {
          try {
            saved.set(true);
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(entry.getRequest().getPostData().get());
            fileWriter.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    });
  }

  private void cleanAfterCatch() throws InterruptedException {
    while (!saved.get()) {
      TimeUnit.MILLISECONDS.sleep(tts);
    }
    devTools.clearListeners();
    saved.set(false);
  }

  private void captureUsersUsernames(List<User> users) {
    try {
      FileWriter fileWriterS1 = new FileWriter(testDestinationFilePath + "S1-Users.csv");
      FileWriter fileWriterS2 = new FileWriter(testDestinationFilePath + "S2-USers.csv");
      for (User user : users) {
        if (user.isPatient()) {
          fileWriterS1.write(user.getUsername() + "\n");
        } else {
          fileWriterS2.write(user.getUsername() + "\n");
        }
      }
      fileWriterS1.close();
      fileWriterS2.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void logout() {
    getButtonToClickByAttribute("aria-label", "Wyloguj się").click();
    getButtonToClickByAttribute("aria-label", "Tak").click();
    waitTillPageLoaded();
  }

  private List<User> prepareUsers(int howManyDoctors, int howManyPatients, int howManyReportsPerPatient) {
    return generator.generateDoctorsAndPatients(howManyDoctors, howManyPatients, howManyReportsPerPatient);
  }

  private void registerUsers(List<User> users) {
    getElementToClickByText("Użytkownicy").click();
    waitTillPageLoaded();
    int sizeOfUsers = users.size();
    for (int i=0; i<sizeOfUsers; i++) {
      registerUser(users.get(i));
      System.out.println("Registered " + (i+1) + " / " + sizeOfUsers + " users");
    }
  }

  private void registerUser(User user) {
    getElementToClickByText("Dodaj użytkownika").click();
    insertBasicValues(user);
    if (user.isPatient()) {
      getElementToClickById("doctorId").click();
      getElementToClickByText(user.getDoctor().getName() + " " + user.getDoctor().getSurname()).click();
    }
    submitUserRegistration();
  }

  private void insertBasicValues(User user) {
    insertValue(getElementById("username"), user.getUsername());
    insertValue(getElementById("password"), user.getPassword());
    insertValue(getElementById("name"), user.getName());
    insertValue(getElementById("surname"), user.getSurname());
    getElementToClickById("role").click();
    getElementToClickByText(user.getRole()).click();
  }

  private void submitUserRegistration() {
    getButtonToClickByAttribute("type", "submit").click();
    waitForTextToAppear("Podgląd danych użytkownika");
    getElementToClickByText("Powrót").click();
    waitTillPageLoaded();
  }

  private void goTo(String url) {
    chromeDriver.get(url);
    waitTillPageLoaded();
  }

  private void waitTillPageLoaded() {
    waitDriver.until(documentLoadedCondition);
  }

  private WebElement getElementById(String id) {
    return waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
  }

  private WebElement getElementToClickById(String id) {
    return waitDriver.until(ExpectedConditions.elementToBeClickable(By.id(id)));
  }

  private WebElement getButtonToClickByAttribute(String attributeName, String attributeValue) {
    return waitDriver.until(ExpectedConditions.elementToBeClickable(
        By.xpath("//button[@" + attributeName + "=\"" + attributeValue + "\"]"))
    );
  }

  private WebElement getElementToClickByText(String text) {
    return waitDriver.until(ExpectedConditions.elementToBeClickable(
        By.xpath("//*[text()=\"" + text + "\"]"))
    );
  }

  private void waitForTextToAppear(String text) {
    waitDriver.until(ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//*[text()=\"" + text + "\"]"))
    );
  }

  private void insertValue(WebElement element, String value) {
    element.sendKeys(value);
  }

  private void mkdir() throws IOException {
    Path path = Paths.get(testDestinationFile);
    File file = path.toFile();
    if (file.exists()) {
      Files.walk(path)
          .sorted(Comparator.reverseOrder())
          .forEach(pathTemp -> {
            try {
            Files.delete(pathTemp);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
    }
    file.mkdir();
  }

  private void endTest() {
    chromeDriver.manage().deleteAllCookies();
    chromeDriver.close();
    chromeDriver.quit();
  }
}

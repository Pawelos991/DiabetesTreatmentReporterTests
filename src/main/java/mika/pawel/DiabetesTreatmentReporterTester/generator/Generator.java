package mika.pawel.DiabetesTreatmentReporterTester.generator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mika.pawel.DiabetesTreatmentReporterTester.model.Report;
import mika.pawel.DiabetesTreatmentReporterTester.model.User;
import org.springframework.stereotype.Component;

@Component
public class Generator {
  private final Random random = new Random();
  private static String[] names = {
      "Krzysztof", "Dawid", "Gabriel", "Oskar", "Piotr", "Julian", "Igor", "Miłosz", "Tymoteusz", "Oliwier", "Wiktor",
      "Michał", "Maksymilian", "Marcel", "Tymon", "Kacper", "Anna", "Maria", "Katarzyna", "Małgorzata", "Agnieszka",
      "Barbara", "Ewa", "Krystyna", "Elżbieta", "Magdalena", "Joanna", "Zofia", "Aleksandra", "Monika", "Teresa"
  };
  private static String[] surnames = {
      "Nowak", "Kowalski", "Wiśniewski", "Wójcik", "Kowalczyk", "Kamiński", "Lewandowski", "Zieliński", "Szymański",
      "Woźniak", "Dąbrowski", "Kozłowski", "Mazur", "Jankowski", "Kwiatkowski", "Wojciechowski", "Krawczyk",
      "Kaczmarek", "Piotrowski", "Grabowski", "Pawłowski", "Zając", "Król", "Michalski", "Wróbel", "Wieczorek",
      "Jabłoński", "Nowakowski", "Majewski", "Olszewski", "Jaworski", "Malinowski", "Adamczyk", "Stępień",
      "Dudek", "Górski", "Nowicki", "Pawlak", "Sikora", "Witkowski", "Rutkowski", "Walczak", "Michalak", "Baran"
  };
  private static String[] years = {
      "2020", "2021", "2022", "2023"
  };
  private static String[] months = {
      "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"
  };
  private static String usernameAddition = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm"));
  private static String doctorUsername = "test_doctor-" + usernameAddition + "-";
  private static String patientUsername = "test_patient-" + usernameAddition + "-";
  private static String password = "!QAZxsw2";
  private static String doctorRole = "Doctor";
  private static String patientRole = "Patient";

  public List<User> generateDoctorsAndPatients(int howManyDoctors, int howManyPatients, int howManyReportsPerPatient) {
    List<User> doctors = generateDoctors(howManyDoctors);
    List<User> patients = generatePatients(howManyPatients, howManyReportsPerPatient, doctors);
    doctors.addAll(patients);
    return doctors;
  }

  private List<User> generateDoctors(int howMany) {
    List<User> doctors = new ArrayList<>();
    for (int i = 0; i < howMany; i++) {
      doctors.add(generateDoctor(i));
    }
    return doctors;
  }

  private List<User> generatePatients(int howMany, int howManyReportsPerPatient, List<User> doctors) {
    List<User> patients = new ArrayList<>();
    for (int i = 0; i < howMany; i++) {
      patients.add(generatePatient(i, howManyReportsPerPatient, doctors.get(random.nextInt(doctors.size()))));
    }
    return patients;
  }

  private User generateDoctor(int usernameOffset) {
    return new User(
        doctorUsername + usernameOffset,
        password,
        getRandomName(),
        getRandomSurname(),
        doctorRole,
        null,
        null
    );
  }

  private User generatePatient(int usernameOffset, int howManyReports, User doctor) {
    List<Report> reports = generateReports(howManyReports);
    return new User(
        patientUsername + usernameOffset,
        password,
        getRandomName(),
        getRandomSurname(),
        patientRole,
        doctor,
        reports
    );
  }

  private List<Report> generateReports(int howMany) {
    List<Report> reports = new ArrayList<>();
    for (String year : years) {
      for (String month : months) {
        if (reports.size() < howMany) {
          reports.add(generateReport(year, month));
        }
      }
    }
    return reports;
  }

  private Report generateReport(String year, String month) {
    int timeInTarget = random.nextInt(40) + 40;
    int timeBelowTarget = 100 - timeInTarget - random.nextInt(20);
    int timeAboveTarget = 100 - timeInTarget - timeBelowTarget;
    return new Report(
        year,
        month,
        String.valueOf(random.nextInt(100) + 100),
        String.valueOf(timeInTarget),
        String.valueOf(timeBelowTarget),
        String.valueOf(timeAboveTarget),
        String.valueOf(random.nextInt(60) + 60)
    );
  }

  private String getRandomName() {
    return names[random.nextInt(names.length)];
  }

  private String getRandomSurname() {
    return surnames[random.nextInt(surnames.length)];
  }
}

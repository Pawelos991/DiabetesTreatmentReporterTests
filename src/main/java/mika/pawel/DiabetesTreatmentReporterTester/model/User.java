package mika.pawel.DiabetesTreatmentReporterTester.model;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class User {
  private final String username;
  private final String password;
  private final String name;
  private final String surname;
  private final String role;
  private final User doctor;
  private final List<Report> reports;

  public boolean isPatient() {
    return "Patient".equals(role);
  }
}

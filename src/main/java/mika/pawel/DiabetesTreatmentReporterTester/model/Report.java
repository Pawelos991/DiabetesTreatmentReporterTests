package mika.pawel.DiabetesTreatmentReporterTester.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Report {
  private final String year;
  private final String month;
  private final String avgSugarLevel;
  private final String timeInTarget;
  private final String timeBelowTarget;
  private final String timeAboveTarget;
  private final String bodyWeight;
}

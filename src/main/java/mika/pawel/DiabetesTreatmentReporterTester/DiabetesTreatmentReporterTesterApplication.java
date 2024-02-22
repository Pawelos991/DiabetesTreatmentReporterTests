package mika.pawel.DiabetesTreatmentReporterTester;

import lombok.RequiredArgsConstructor;
import mika.pawel.DiabetesTreatmentReporterTester.tester.Tester;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class DiabetesTreatmentReporterTesterApplication implements CommandLineRunner {

	private final Tester tester;

	public static void main(String[] args) {
		SpringApplication.run(DiabetesTreatmentReporterTesterApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		int doctors = 0, patients= 0, howManyReportsPerPatient = 0;
		for(int i=0; i<args.length; i+=2) {
			switch (args[i]) {
				case "-doctors":
					doctors = Integer.valueOf(args[i+1]);
					break;
				case "-patients":
					patients = Integer.valueOf(args[i+1]);
					break;
				case "-reportsPerPatient":
					howManyReportsPerPatient = Integer.valueOf(args[i+1]);
					break;
				default:
					throw new RuntimeException();
			}
		}
		if (doctors == 0 || patients == 0 || howManyReportsPerPatient == 0) {
			throw new RuntimeException();
		}
		tester.test(doctors, patients, howManyReportsPerPatient);
	}

}

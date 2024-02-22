package mika.pawel.DiabetesTreatmentReporterTester.config;

import java.time.Duration;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfigureAfter(ChromeDriverConfig.class)
@ConditionalOnBean(ChromeDriverConfig.class)
@Configuration
public class WaitDriverConfig {
  @Bean(name = "WaitDriver")
  public Wait<ChromeDriver> configureWaitDriver(ChromeDriver chromeDriver) {
    return new FluentWait<>(chromeDriver)
        .withTimeout(Duration.ofMinutes(3))
        .pollingEvery(Duration.ofMillis(200))
        .ignoring(NoSuchElementException.class);
  }
}

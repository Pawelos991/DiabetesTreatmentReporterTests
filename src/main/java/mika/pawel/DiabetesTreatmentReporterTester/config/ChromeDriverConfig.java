package mika.pawel.DiabetesTreatmentReporterTester.config;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChromeDriverConfig {
  @Bean(name = "ChromeDriver")
  public ChromeDriver configureWebDriver() {
    System.setProperty("webdriver.chrome.driver",
        System.getProperty("user.dir") + File.separator + "Chrome-bin" + File.separator + "chromedriver.exe");

    HashMap<String, Object> chromePrefs = new HashMap<>();
    chromePrefs.put("download.default_directory", System.getProperty("user.dir") + File.separator + "downloads");

    ChromeOptions options = new ChromeOptions();
    options.setExperimentalOption("prefs", chromePrefs);
    options.addArguments("--lang=pl-PL");
    options.addArguments("--incognito");

    //Headless
    options.addArguments("user-agent=\"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.75 Safari/537.36\"");
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    //Headless

    File chromeBinary = new File(System.getProperty("user.dir")
        + File.separator + "Chrome-bin" + File.separator + "chrome.exe");
    options.setBinary(chromeBinary);
    ChromeDriver driver = new ChromeDriver(options);

    driver.manage().window().maximize();
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

    return driver;
  }

}

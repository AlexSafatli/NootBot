package net.dirtydeeds.discordsoundboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * This is the MainController for the SpringBoot application
 */
@SpringBootApplication
@EnableAsync
public class MainController {

  public MainController() {
  }

  public static void main(String[] args) {
    SpringApplication.run(MainController.class, args);
  }

}
package com.company;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

@Slf4j
public class Main {

  protected Configuration defaultConfig;

  public Main() {
    Configurations configs = new Configurations();
    try {
      Configuration config = configs.properties(new File("default.properties"));
      // access configuration properties
      {
        int alpha = config.getInt("alpha");
        String beta = config.getString("beta");

        log.info("Alpha {}", alpha);
        log.info("Beta {}", beta);
      }

      Configuration overrideConfig = configs.properties(new File("override.properties"));
      {
        int alpha = overrideConfig.getInt("alpha");
        String gamma = overrideConfig.getString("gamma");

        log.info("Alpha {}", alpha);
        log.info("Gamma {}", gamma);
      }

    } catch (ConfigurationException cex) {
      // Something went wrong
    }
  }

  public static void main(String[] args) {
    log.info("Welcome to Configuration01");

    Main main = new Main();

        /*
        User user1 = new User(1l, "Jack", "Jones");
        main.addUser(user1);

        User user2 = new User(1l, "John", "Smith");
        main.addUser(user2);

        User user3 = new User(1l, "Helen", "Underhill");
        main.addUser(user3);

        for (User user : main.getUsers()) {
            System.out.println(user);
        }
        */
  }
}

package com.company;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

@Slf4j
public class Main {

  protected Configuration defaultConfig;

  public Main() {
    Configurations configs = new Configurations();

    try {
      Configuration config = configs.properties(new File("default.properties"));
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

      CombinedConfiguration combinedConfig = configs.combined(new File("configDef.xml"));
      {
        int alpha = combinedConfig.getInt("alpha");
        String beta = combinedConfig.getString("beta");
        String gamma = combinedConfig.getString("gamma");

        log.info("Alpha {}", alpha);
        log.info("Beta {}", beta);
        log.info("Gamma {}", gamma);
      }

      Parameters params = new Parameters();
      FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
        new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
          .configure(params.properties()
            .setFileName("default.properties"));

      FileBasedConfiguration fbConfig = builder.getConfiguration();
      {
        int alpha = fbConfig.getInt("alpha");
        String beta = fbConfig.getString("beta");

        log.info("Alpha {}", alpha);
        log.info("Beta {}", beta);
      }
    } catch (ConfigurationException cex) {
      // Something went wrong
    }
  }

  public static void main(String[] args) {
    log.info("Welcome to Configuration01");

    Main main = new Main();
  }
}

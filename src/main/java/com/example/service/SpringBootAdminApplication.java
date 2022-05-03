package com.example.service;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

@Slf4j
@EnableAdminServer
@SpringBootApplication
public class SpringBootAdminApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringBootAdminApplication.class, args);
  }

  /**
   * Tag the metrics this app produces
   *
   * @return
   */
  @Value("${spring.application.name}")
  private String applicationName;

  @Autowired private Environment environment;

  @Bean
  MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {

    log.info("====== Environment and configuration ======");
    log.info("Active profiles: {}", Arrays.toString(environment.getActiveProfiles()));
    final MutablePropertySources sources = ((AbstractEnvironment) environment).getPropertySources();
    StreamSupport.stream(sources.spliterator(), false)
        .filter(ps -> ps instanceof EnumerablePropertySource)
        .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
        .flatMap(Arrays::stream)
        .distinct()
        .filter(
            prop ->
                !(prop.toLowerCase().contains("credentials")
                    || prop.toLowerCase().contains("password")
                    //                    || prop.toLowerCase().contains("vault")
                    || prop.toLowerCase().contains("certificate")
                    || prop.toLowerCase().contains("key")
                    || prop.toLowerCase().contains("token")
                    || prop.toLowerCase().contains("secret")))
        .forEach(prop -> log.info("{}: {}", prop, environment.getProperty(prop)));
    log.info("===========================================");

    String hostname = System.getenv("HOSTNAME");
    if (hostname == null) {
      try {
        hostname = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {
        hostname = "unknown";
      }
    }
    // probably better ways to do this, but its what idea recommended.
    String finalHostname = hostname;
    return registry ->
        registry
            .config()
            .commonTags("application", applicationName)
            .commonTags("instance", finalHostname)
            .commonTags("activeProfiles", String.join(", ", environment.getActiveProfiles()));
  }
}

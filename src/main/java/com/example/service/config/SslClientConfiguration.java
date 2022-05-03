package com.example.service.config;

import de.codecentric.boot.admin.server.config.AdminServerInstanceWebClientConfiguration;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import javax.net.ssl.SSLException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * With the containers themselves implementing TLS with self signed, ephemeral certs, we need to
 * allow invlaid certs.
 */
@Configuration
public class SslClientConfiguration {

  private AdminServerInstanceWebClientConfiguration adminServerInstanceWebClientConfiguration;

  public SslClientConfiguration(
      AdminServerInstanceWebClientConfiguration adminServerInstanceWebClientConfiguration) {
    this.adminServerInstanceWebClientConfiguration = adminServerInstanceWebClientConfiguration;
  }

  /**
   * Clone the webclient builder and replace its client connector with one that trusts all
   * certificate chains.
   *
   * @param webClientBuilder
   * @return
   * @throws SSLException
   */
  @Bean
  @Scope("prototype")
  public InstanceWebClient.Builder instanceWebClientBuilder(WebClient.Builder webClientBuilder)
      throws SSLException {
    InstanceWebClient.Builder instanceWebClientBuilder =
        this.adminServerInstanceWebClientConfiguration.instanceWebClientBuilder();
    SslContext sslContext =
        SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
    HttpClient httpClient =
        HttpClient.create().compress(true).secure(t -> t.sslContext(sslContext));

    WebClient.Builder clone = webClientBuilder.clone();
    clone.clientConnector(new ReactorClientHttpConnector(httpClient));
    return instanceWebClientBuilder.webClient(clone);
  }
}

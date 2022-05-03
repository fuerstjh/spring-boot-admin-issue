package com.example.service.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  private final AdminServerProperties adminServer;
  @Autowired private AdminServerProperties adminServerProperties;

  public WebSecurityConfig(AdminServerProperties adminServer) {
    this.adminServer = adminServer;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    SavedRequestAwareAuthenticationSuccessHandler successHandler =
        new SavedRequestAwareAuthenticationSuccessHandler();
    successHandler.setTargetUrlParameter("redirectTo");
    successHandler.setDefaultTargetUrl(this.adminServer.getContextPath() + "/");

    http.authorizeRequests()
        .antMatchers(this.adminServer.getContextPath() + "/login")
        .permitAll()
        .antMatchers(HttpMethod.POST, adminServerProperties.path("/logout"))
        .permitAll()
        .antMatchers(HttpMethod.POST, adminServerProperties.path("/error"))
        .permitAll()
        .antMatchers(HttpMethod.POST, adminServerProperties.path("/instances"))
        .permitAll()
        .antMatchers(HttpMethod.DELETE, adminServerProperties.path("/instances/*"))
        .permitAll()
        .antMatchers(adminServerProperties.path("/assets/**"))
        .permitAll()
        .antMatchers(adminServerProperties.path("/sw.js"))
        .permitAll()
        .antMatchers(HttpMethod.GET, "/actuator/health")
        .permitAll()
        .antMatchers(HttpMethod.HEAD, "/actuator/health")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .formLogin()
        .loginPage(this.adminServer.getContextPath() + "/login")
        .successHandler(successHandler)
        .and()
        .logout()
        .logoutUrl(this.adminServer.getContextPath() + "/logout")
        .and()
        .httpBasic()
        .and()
        .csrf()
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .ignoringRequestMatchers(
            new AntPathRequestMatcher(
                this.adminServer.getContextPath() + "/instances", HttpMethod.POST.toString()),
            new AntPathRequestMatcher(
                this.adminServer.getContextPath() + "/instances/*", HttpMethod.DELETE.toString()),
            new AntPathRequestMatcher(this.adminServer.getContextPath() + "/actuator/**"))
        .and()
        .rememberMe()
        .key(UUID.randomUUID().toString())
        .tokenValiditySeconds(1209600);
  }
}

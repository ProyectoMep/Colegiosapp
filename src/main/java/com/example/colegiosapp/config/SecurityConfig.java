package com.example.colegiosapp.config;

import com.example.colegiosapp.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(usuarioService).passwordEncoder(passwordEncoder);
        return builder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Recursos estáticos comunes (css, js, images, webjars, favicon)
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                // Carpetas públicas adicionales
                .requestMatchers("/img/**", "/assets/**", "/fonts/**", "/favicon.ico").permitAll()
                // Rutas públicas
                .requestMatchers("/", "/login", "/register", "/error", "/public/**").permitAll()
                // Rutas por rol
                .requestMatchers("/tutor/**").hasAuthority("Tutor")
                .requestMatchers("/admin/**").hasAuthority("Administrador")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form 
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("correo")
                .passwordParameter("contrasena")
                // Redirección según rol
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // manejo central de no autorizado / acceso denegado
            .exceptionHandling(exception -> exception
                // 403: Usuario AUTENTICADO pero sin permisos
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/login?denied");
                })
                // 401: Usuario NO autenticado intentando acceder a recurso protegido
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/login?denied");
                })
            );

        // Registrar explícitamente el UserDetailsService
        http.userDetailsService(usuarioService);

        return http.build();
    }

    /**
     * Redirección por rol luego de autenticarse.
     */
    @Bean
    @SuppressWarnings("Convert2Lambda")
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            @SuppressWarnings("ConvertToStringSwitch")
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication)
                    throws IOException, ServletException {
                String authority = authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority();

                if ("Tutor".equals(authority)) {
                    response.sendRedirect("/tutor/dashboard");
                } else if ("Administrador".equals(authority)) {
                    response.sendRedirect("/admin/dashboard");
                } else {
                    response.sendRedirect("/");
                }
            }
        };
    }
}

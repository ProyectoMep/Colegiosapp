package com.example.colegiosapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Controlador que maneja la página de inicio, el login, el registro y la
 * redirección posterior al inicio de sesión. El index muestra enlaces para
 * iniciar sesión o registrarse.
 */
@Controller
public class HomeController {

    private final UsuarioService usuarioService;

    public HomeController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Muestra la página principal con enlaces a login y registro. */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /** Devuelve la plantilla de login personalizada. */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Muestra el formulario de registro de usuarios. */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "register";
    }

    /**
     * Procesa el registro. Si la validación es correcta, guarda el nuevo
     * usuario como Tutor por defecto y redirige al login. Si hay errores,
     * vuelve a mostrar el formulario.
     */
    @PostMapping("/register")
    public String register(@Valid Usuario usuario, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        usuarioService.registerTutor(usuario);
        model.addAttribute("registrationSuccess", true);
        return "login";
    }

    /**
     * Redirige al usuario autenticado a su panel según el rol asignado.
     * Esta ruta se utiliza como defaultSuccessUrl en la configuración
     * de seguridad.
     */
    @RequestMapping("/postLogin")
    @SuppressWarnings("ConvertToStringSwitch")
    public String postLogin(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/";
        }
        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        if ("Tutor".equals(authority)) {
            return "redirect:/tutor/dashboard";
        } else if ("Administrador".equals(authority)) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/";
        }
    }
}

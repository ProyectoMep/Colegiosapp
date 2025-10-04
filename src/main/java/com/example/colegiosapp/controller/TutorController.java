package com.example.colegiosapp.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.example.colegiosapp.entity.Cita;
import com.example.colegiosapp.entity.Institucion;
import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.repository.CitaRepository;
import com.example.colegiosapp.repository.InstitucionRepository;
import com.example.colegiosapp.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

/**
* Gestiona páginas específicas del tutor, como el panel de control, la programación de citas
* y la visualización de citas programadas. El flujo de programación de citas está
* implementado en dos pasos: los usuarios primero completan el formulario y luego
* confirman los datos antes de guardarlos.
*/
@Controller
@RequestMapping("/tutor")
@SessionAttributes("citaPendiente")
public class TutorController {

    private final InstitucionRepository institucionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;

    public TutorController(InstitucionRepository institucionRepository,
                        UsuarioRepository usuarioRepository,
                        CitaRepository citaRepository) {
        this.institucionRepository = institucionRepository;
        this.usuarioRepository = usuarioRepository;
        this.citaRepository = citaRepository;
    }

    /**
     *Muestra el panel del tutor con botones de navegación.
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "tutor/dashboard";
    }

    /**
* Muestra el formulario de programación de citas. Si se proporciona un parámetro de
*localidad, solo se recuperan las instituciones de esa localidad; de lo contrario,
*no se muestran las instituciones.
*/
    @GetMapping("/agendar-cita")
    public String showAgendarCita(@RequestParam(value = "localidad", required = false) String localidad,
                            Model model) {
        List<String> localidades = institucionRepository.findAllLocalidades();
        model.addAttribute("localidades", localidades);
        if (localidad != null && !localidad.isEmpty()) {
            List<Institucion> instituciones = institucionRepository.findByLocalidad(localidad);
            model.addAttribute("instituciones", instituciones);
            model.addAttribute("selectedLocalidad", localidad);
        }
        // Proporcionar opciones de grados

        List<String> grados = Arrays.asList("Primero", "Segundo", "Tercero", "Cuarto", "Quinto", "Sexto", "Séptimo", "Octavo", "Noveno", "Décimo", "Undécimo");
        model.addAttribute("grados", grados);
        return "tutor/agendar_cita";
    }

    /**
* Gestiona el primer paso de la programación de citas. Los valores del
*formulario enviado se almacenan en la sesión y el usuario es
*redirigido a una página de confirmación.
     */
    @PostMapping("/agendar-cita")
    public String processAgendarCita(@RequestParam("institucionId") Long institucionId,
                                    @RequestParam("grado") String grado,
                                    @RequestParam("cantidad") Integer cantidad,
                                    @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                    @RequestParam("hora") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
                                    Authentication authentication,
                                    Model model) {
        
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName()).orElseThrow();
        Institucion institucion = institucionRepository.findById(institucionId).orElseThrow();

        Cita cita = new Cita();
        cita.setInstitucion(institucion);
        cita.setFechaCita(fecha);
        cita.setHoraCita(hora);
        cita.setCantidadCitas(cantidad);
        cita.setNombreAgenda(usuario.getNombre() + " " + usuario.getApellido());
        cita.setCorreoAgenda(usuario.getCorreo());
        cita.setTelefonoAgenda(usuario.getTelefono());
        cita.setEstado("Pendiente asistir");
        cita.setIdSede(1);

        model.addAttribute("citaPendiente", cita);
        model.addAttribute("grado", grado);
        return "tutor/confirmar_cita";
    }

    /**
     * Guarda la cita almacenada en la sesión. Tras guardarla, el usuario
* es redirigido a la lista de sus citas.
*/
    @PostMapping("/agendar-cita/confirmar")
    public String confirmarCita(@ModelAttribute("citaPendiente") Cita cita,
                                HttpSession session) {
        // Persistir en la cita
        citaRepository.save(cita);
        session.removeAttribute("citaPendiente");
        return "redirect:/tutor/citas";
    }

    /**
     * Muestra todas las citas del usuario autenticado. El filtro se realiza
     * mediante la coincidencia con la dirección de correo electrónico utilizada para programar la cita.
     */
    @GetMapping("/citas")
    public String listarCitas(Authentication authentication, Model model) {
        String correo = authentication.getName();
        List<Cita> citas = citaRepository.findByCorreoAgenda(correo);
        model.addAttribute("citas", citas);
        return "tutor/citas";
    }

    /**
     * Muestra un formulario para reprogramar una cita específica. El ID de la cita se proporciona mediante la variable de ruta.
     */
    @GetMapping("/citas/{id}/reprogramar")
    public String mostrarReprogramar(@PathVariable Long id, Model model) {
        Cita cita = citaRepository.findById(id).orElseThrow();
        model.addAttribute("cita", cita);
        return "tutor/reprogramar_cita";
    }

    /**
     * Gestiona la reprogramación de una cita. Solo se pueden modificar la fecha y
     * la hora. Tras guardar los cambios, el usuario es redirigido a su lista de citas.
     */
    @PostMapping("/citas/{id}/reprogramar")
    public String reprogramarCita(@PathVariable Long id,
                                @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                @RequestParam("hora") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {
        Cita cita = citaRepository.findById(id).orElseThrow();
        cita.setFechaCita(fecha);
        cita.setHoraCita(hora);
        cita.setEstado("Reprogramada");
        citaRepository.save(cita);
        return "redirect:/tutor/citas";
    }

    /**
     *Cancela una cita estableciendo su estado como "Cancelada". La cita no se elimina de la base de datos.
     */
    @PostMapping("/citas/{id}/cancelar")
    public String cancelarCita(@PathVariable Long id) {
        Cita cita = citaRepository.findById(id).orElseThrow();
        cita.setEstado("Cancelada");
        citaRepository.save(cita);
        return "redirect:/tutor/citas";
    }
}
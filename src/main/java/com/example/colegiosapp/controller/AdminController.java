package com.example.colegiosapp.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.colegiosapp.entity.Cita;
import com.example.colegiosapp.entity.Institucion;
import com.example.colegiosapp.entity.Rol;
import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.repository.CitaRepository;
import com.example.colegiosapp.repository.InstitucionRepository;
import com.example.colegiosapp.repository.RolRepository;
import com.example.colegiosapp.repository.UsuarioRepository;
import com.example.colegiosapp.util.ReportGenerator;

/**
 * Gestiona funciones administrativas específicas, como la gestión de roles de usuarios,
 * el registro de nuevas instituciones y la generación de informes.
 */
@SuppressWarnings("unused")
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final InstitucionRepository institucionRepository;
    private final CitaRepository citaRepository;
    private final ReportGenerator reportGenerator;

    public AdminController(UsuarioRepository usuarioRepository,
                        RolRepository rolRepository,
                        InstitucionRepository institucionRepository,
                        CitaRepository citaRepository,
                        ReportGenerator reportGenerator) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.institucionRepository = institucionRepository;
        this.citaRepository = citaRepository;
        this.reportGenerator = reportGenerator;
    }

    /*Muestra el panel de administración con botones para varias funciones de administración.*/
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    /** Enumera todos los usuarios y roles para modificar. Los administradores pueden cambiar el rol de un usuario enviando el formulario. */
    @GetMapping("/modificar-permisos")
    public String modificarPermisos(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Rol> roles = rolRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", roles);
        return "admin/modificar_permisos";
    }

    /** Actualiza el rol de un usuario específico. El ID de usuario y el ID del rol seleccionado se obtienen del formulario. */
    @PostMapping("/modificar-permisos")
    public String actualizarPermisos(@RequestParam("usuarioId") Long usuarioId,
                                    @RequestParam("rolId") Integer rolId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Rol rol = rolRepository.findById(rolId).orElseThrow();
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
        return "redirect:/admin/modificar-permisos";
    }

    /** Muestra el formulario de registro de la institución. */
    @GetMapping("/registrar-institucion")
    public String mostrarRegistrarInstitucion(Model model) {
        model.addAttribute("institucion", new Institucion());
        return "admin/registrar_institucion";
    }

    /** guarda nuevas instituciones */
    @PostMapping("/registrar-institucion")
    public String registrarInstitucion(Institucion institucion) {
        institucionRepository.save(institucion);
        return "redirect:/admin/dashboard";
    }

    /**
     * Reportes con filtros: institución (todas) + estado (todos) y previsualización.
     * - estadosDisponibles se pinta en el combo.
     * - estadoSeleccionado mantiene el valor elegido.
     * - citas contiene lo que se previsualiza.
     */
    @GetMapping("/reportes")
    public String mostrarReportes(
            @RequestParam(value = "institucionId", required = false) Long institucionId,
            @RequestParam(value = "estado", required = false) String estado,
            Model model) {

        // Combo de instituciones
        List<Institucion> instituciones = institucionRepository.findAll();
        model.addAttribute("instituciones", instituciones);

        // Combo de estados
        List<String> estadosDisponibles = List.of("Pendiente asistir", "Reprogramada", "Cancelada", "Asistió");
        String estadoFiltro = (estado == null || estado.isBlank()) ? null : estado.trim();
        model.addAttribute("estadosDisponibles", estadosDisponibles);
        model.addAttribute("estadoSeleccionado", estadoFiltro);

        // KPIs (si institucionId es null, el util devuelve el resumen global)
        Map<String, Long> stats = reportGenerator.generateCitaStatusSummary(institucionId);
        model.addAttribute("stats", stats);

        // Previsualización (mismos datos que el reporte)
        List<Cita> citas = (institucionId != null)
                ? citaRepository.findByInstitucionId(institucionId)
                : citaRepository.findAll();

        if (estadoFiltro != null) {
            citas = citas.stream()
                    .filter(c -> estadoFiltro.equalsIgnoreCase(c.getEstado() == null ? "" : c.getEstado()))
                    .toList();
        }

        model.addAttribute("citas", citas);
        model.addAttribute("totalCitas", citas.size());

        // Marca de institución seleccionada (solo si aplica)
        if (institucionId != null) {
            model.addAttribute("selectedInstitucion",
                    institucionRepository.findById(institucionId).orElse(null));
        }

        return "admin/reportes";
    }

    /** Excel */
    @GetMapping("/reportes/download")
    public ResponseEntity<InputStreamResource> descargarReporte(
            @RequestParam(value = "institucionId", required = false) Long institucionId) throws IOException {
        ByteArrayOutputStream out;
        try (Workbook workbook = reportGenerator.generateCitasWorkbook(institucionId)) {
            out = new ByteArrayOutputStream();
            workbook.write(out);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        String filename = (institucionId != null ? "reporte_citas_" + institucionId : "reporte_citas") + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    /** PDF */
    @GetMapping(value = "/reportes/download-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarReportePdf(
            @RequestParam(value = "institucionId", required = false) Long institucionId) {

        byte[] pdf = reportGenerator.generateCitasPdf(institucionId);
        String filename = (institucionId != null ? "reporte_citas_" + institucionId : "reporte_citas") + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

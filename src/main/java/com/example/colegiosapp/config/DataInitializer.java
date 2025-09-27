package com.example.colegiosapp.config;

import com.example.colegiosapp.entity.Rol;
import com.example.colegiosapp.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes essential database records such as roles on application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {
    private final RolRepository rolRepository;

    public DataInitializer(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExists("Administrador");
        createRoleIfNotExists("Gestor");
        createRoleIfNotExists("Tutor");
    }

    private void createRoleIfNotExists(String nombre) {
        rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            return rolRepository.save(rol);
        });
    }
}
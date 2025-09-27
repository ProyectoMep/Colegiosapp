package com.example.colegiosapp.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.colegiosapp.entity.Rol;
import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.repository.RolRepository;
import com.example.colegiosapp.repository.UsuarioRepository;


@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                    RolRepository rolRepository,
                    PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public Usuario registerTutor(Usuario usuario) {
        // Hash encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Asignación del rol 3 por defecto
        Rol tutorRole = rolRepository.findByNombre("Tutor")
                .orElseGet(() -> {
                    Rol newRole = new Rol("Tutor");
                    return rolRepository.save(newRole);
                });
        usuario.setRol(tutorRole);

        return usuarioRepository.save(usuario);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Usuario> optional = usuarioRepository.findByCorreo(username);
        Usuario usuario = optional.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        GrantedAuthority authority = new SimpleGrantedAuthority(usuario.getRol().getNombre());
        return new User(usuario.getCorreo(), usuario.getPassword(), Collections.singleton(authority));
    }
}
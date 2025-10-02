package com.example.colegiosapp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.colegiosapp.entity.Cita;

/**
 * Es una interfaz de Spring Data JPA que hereda de JpaRepository, lo cual permite gestionar
 *  la entidad Cita con operaciones CRUD sin necesidad de escribir código SQL
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByCorreoAgenda(String correoAgenda);
    List<Cita> findByInstitucionId(Long institucionId);
    List<Cita> findByInstitucionIdAndEstado(Long institucionId, String estado);
    List<Cita> findByInstitucionIdAndFechaCitaBetween(Long institucionId, LocalDate start, LocalDate end);
}
package com.example.colegiosapp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.colegiosapp.entity.Cita;

/**
 * Repository for managing {@link Cita} entities.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByCorreoAgenda(String correoAgenda);
    List<Cita> findByInstitucionId(Long institucionId);
    List<Cita> findByInstitucionIdAndEstado(Long institucionId, String estado);
    List<Cita> findByInstitucionIdAndFechaCitaBetween(Long institucionId, LocalDate start, LocalDate end);
}
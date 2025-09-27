package com.example.colegiosapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.colegiosapp.entity.Institucion;

/**
Buscar colegios por localidad
 */
@Repository
public interface InstitucionRepository extends JpaRepository<Institucion, Long> {
    List<Institucion> findByLocalidad(String localidad);

    @Query("SELECT DISTINCT i.localidad FROM Institucion i ORDER BY i.localidad")
    List<String> findAllLocalidades();
}
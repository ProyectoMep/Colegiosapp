package com.example.colegiosapp.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long id;

    @Column(name = "fecha_cita")
    private LocalDate fechaCita;

    @Column(name = "hora_cita")
    private LocalTime horaCita;

    @Column(name = "nombre_agenda")
    private String nombreAgenda;

    @Column(name = "correo_agenda")
    private String correoAgenda;

    @Column(name = "telefono_agenda")
    private String telefonoAgenda;

    @Column(name = "cantidad_citas")
    private Integer cantidadCitas;

    @ManyToOne
    @JoinColumn(name = "id_colegio")
    private Institucion institucion;

    @Column(name = "id_sede")
    private Integer idSede;

    @Column(name = "estado")
    private String estado;

    public Cita() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaCita() {
        return fechaCita;
    }

    public void setFechaCita(LocalDate fechaCita) {
        this.fechaCita = fechaCita;
    }

    public LocalTime getHoraCita() {
        return horaCita;
    }

    public void setHoraCita(LocalTime horaCita) {
        this.horaCita = horaCita;
    }

    public String getNombreAgenda() {
        return nombreAgenda;
    }

    public void setNombreAgenda(String nombreAgenda) {
        this.nombreAgenda = nombreAgenda;
    }

    public String getCorreoAgenda() {
        return correoAgenda;
    }

    public void setCorreoAgenda(String correoAgenda) {
        this.correoAgenda = correoAgenda;
    }

    public String getTelefonoAgenda() {
        return telefonoAgenda;
    }

    public void setTelefonoAgenda(String telefonoAgenda) {
        this.telefonoAgenda = telefonoAgenda;
    }

    public Integer getCantidadCitas() {
        return cantidadCitas;
    }

    public void setCantidadCitas(Integer cantidadCitas) {
        this.cantidadCitas = cantidadCitas;
    }

    public Institucion getInstitucion() {
        return institucion;
    }

    public void setInstitucion(Institucion institucion) {
        this.institucion = institucion;
    }

    public Integer getIdSede() {
        return idSede;
    }

    public void setIdSede(Integer idSede) {
        this.idSede = idSede;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
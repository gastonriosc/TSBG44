package com.example.tsbhashtabledaapp;

public class Registro {

    private String sexo;
    private String grupo_etario;
    private String jurisdiccion_residencia;
    private String jurisdiccion_residencia_id;
    private String depto_residencia;
    private String depto_residencia_id;
    private String jurisdiccion_aplicacion;
    private String jurisdiccion_aplicacion_id;
    private String depto_aplicacion;
    private String depto_aplicacion_id;
    private String fecha_aplicacion;
    private String vacuna;
    private String condicion_aplicacion;
    private String orden_dosis;
    private String lote_vacuna;

    public Registro(String sexo, String grupo_etario, String jurisdiccion_residencia,
                    String jurisdiccion_residencia_id, String depto_residencia,
                    String depto_residencia_id, String jurisdiccion_aplicacion,
                    String jurisdiccion_aplicacion_id, String depto_aplicacion,
                    String depto_aplicacion_id, String fecha_aplicacion, String vacuna,
                    String condicion_aplicacion, String orden_dosis, String lote_vacuna)
    {
        this.sexo = sexo;
        this.grupo_etario = grupo_etario;
        this.jurisdiccion_residencia = jurisdiccion_residencia;
        this.jurisdiccion_residencia_id = jurisdiccion_residencia_id;
        this.depto_residencia = depto_residencia;
        this.depto_residencia_id = depto_residencia_id;
        this.jurisdiccion_aplicacion = jurisdiccion_aplicacion;
        this.jurisdiccion_aplicacion_id = jurisdiccion_aplicacion_id;
        this.depto_aplicacion = depto_aplicacion;
        this.depto_aplicacion_id = depto_aplicacion_id;
        this.fecha_aplicacion = fecha_aplicacion;
        this.vacuna = vacuna;
        this.condicion_aplicacion = condicion_aplicacion;
        this.orden_dosis = orden_dosis;
        this.lote_vacuna = lote_vacuna;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getJurisdiccion_residencia() {
        return jurisdiccion_residencia;
    }

    public void setJurisdiccion_residencia(String jurisdiccion_residencia) {
        this.jurisdiccion_residencia = jurisdiccion_residencia;
    }

    public String getGrupo_etario() {
        return grupo_etario;
    }

    public void setGrupo_etario(String grupo_etario) {
        this.grupo_etario = grupo_etario;
    }

    public String getJurisdiccion_residencia_id() {
        return jurisdiccion_residencia_id;
    }

    public void setJurisdiccion_residencia_id(String jurisdiccion_residencia_id) {
        this.jurisdiccion_residencia_id = jurisdiccion_residencia_id;
    }

    public String getDepto_residencia() {
        return depto_residencia;
    }

    public void setDepto_residencia(String depto_residencia) {
        this.depto_residencia = depto_residencia;
    }

    public String getDepto_residencia_id() {
        return depto_residencia_id;
    }

    public void setDepto_residencia_id(String depto_residencia_id) {
        this.depto_residencia_id = depto_residencia_id;
    }

    public String getJurisdiccion_aplicacion() {
        return jurisdiccion_aplicacion;
    }

    public void setJurisdiccion_aplicacion(String jurisdiccion_aplicacion) {
        this.jurisdiccion_aplicacion = jurisdiccion_aplicacion;
    }

    public String getJurisdiccion_aplicacion_id() {
        return jurisdiccion_aplicacion_id;
    }

    public void setJurisdiccion_aplicacion_id(String jurisdiccion_aplicacion_id) {
        this.jurisdiccion_aplicacion_id = jurisdiccion_aplicacion_id;
    }

    public String getDepto_aplicacion() {
        return depto_aplicacion;
    }

    public void setDepto_aplicacion(String depto_aplicacion) {
        this.depto_aplicacion = depto_aplicacion;
    }

    public String getDepto_aplicacion_id() {
        return depto_aplicacion_id;
    }

    public void setDepto_aplicacion_id(String depto_aplicacion_id) {
        this.depto_aplicacion_id = depto_aplicacion_id;
    }

    public String getFecha_aplicacion() {
        return fecha_aplicacion;
    }

    public void setFecha_aplicacion(String fecha_aplicacion) {
        this.fecha_aplicacion = fecha_aplicacion;
    }

    public String getVacuna() {
        return vacuna;
    }

    public void setVacuna(String vacuna) {
        this.vacuna = vacuna;
    }

    public String getCondicion_aplicacion() {
        return condicion_aplicacion;
    }

    public void setCondicion_aplicacion(String condicion_aplicacion) {
        this.condicion_aplicacion = condicion_aplicacion;
    }

    public String getOrden_dosis() {
        return orden_dosis;
    }

    public void setOrden_dosis(String orden_dosis) {
        this.orden_dosis = orden_dosis;
    }

    public String getLote_vacuna() {
        return lote_vacuna;
    }

    public void setLote_vacuna(String lote_vacuna) {
        this.lote_vacuna = lote_vacuna;
    }
}


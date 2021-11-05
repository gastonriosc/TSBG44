package clases;

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

    public String toString()
    {
        return grupo_etario;
    }
}


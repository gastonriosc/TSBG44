package business;

import java.util.ArrayList;
import java.util.Objects;

public class Depto
{
    private String nombre;
    private int cantVacunasHombre;
    private int cantVacunasMujer;
    private int cantPrimeraVac;
    private int cantSegundaVac;
    private ArrayList<Vacuna> vacunas;

    public Depto(String nombre)
    {
        this.nombre = nombre;
        cantVacunasHombre = 0;
        cantVacunasMujer = 0;
        cantPrimeraVac = 0;
        cantSegundaVac = 0;
        vacunas = new ArrayList<>();
    }

    /**
     * Suma 1 al contador de géneros de acuerdo al valor del parámetro
     * sexo, que es el género asociado en el registro de la vacuna.
     * Si el género es "M", suma 1 al cantVacunasHombre, y si es "F",
     * suma 1 al cantVacunasMujer.
     * @param sexo parámetro que representa el género de la persona vacunada.
     */
    public void addSexo(String sexo)
    {
        if (Objects.equals(sexo, "\"M\""))
        {
            cantVacunasHombre++;
        }
        else
        {
            cantVacunasMujer++;
        }
    }

    /**
     * Suma 1 al contador de ordenes de dosis de acuerdo al valor del parámetro
     * numero, que representa el orden de la dosis aplicada.
     * Si el orden de la dosis es 1, suma 1 al cantPrimeraVac, y si es "2",
     * suma 1 al cantSegundaVac.
     * @param numero parámetro que representa el orden de la dosis aplicada.
     */
    public void addNroDosis(String numero)
    {
        if (Objects.equals(numero, "1"))
        {
            cantPrimeraVac++;
        }
        else
        {
            cantSegundaVac++;
        }
    }

    /**
     * agrega una nueva vacuna al vector de vacunas en caso de no haberse registrado antes,
     * y aumenta en 1 la cantidad de registros donde se aplicaron dicha vacuna.
     * @param nombre parámetro que representa el nombre de la vacuna aplicada.
     */
    public void addVacuna(String nombre)
    {
        for (Vacuna vac : vacunas)
        {
            if (Objects.equals(vac.getNombre(), nombre))
            {
                // si la vacuna ya se encontraba registrada, aumenta en 1 su cantidad de registros.

                int cant = vac.getCant();
                vac.setCant(cant+1);
                return;
            }
        }
        // si no la encuentra, agrega la vacuna al vector de vacunas, con cant = 1.

        Vacuna vac = new Vacuna(nombre);
        this.vacunas.add(vac);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantVacunasHombre() {
        return cantVacunasHombre;
    }

    public void setCantVacunasHombre(int cantVacunasHombre) {
        this.cantVacunasHombre = cantVacunasHombre;
    }

    public int getCantVacunasMujer() {
        return cantVacunasMujer;
    }

    public void setCantVacunasMujer(int cantVacunasMujer) {
        this.cantVacunasMujer = cantVacunasMujer;
    }

    public int getCantPrimeraVac() {
        return cantPrimeraVac;
    }

    public void setCantPrimeraVac(int cantPrimeraVac) {
        this.cantPrimeraVac = cantPrimeraVac;
    }

    public int getCantSegundaVac() {
        return cantSegundaVac;
    }

    public void setCantSegundaVac(int cantSegundaVac) {
        this.cantSegundaVac = cantSegundaVac;
    }

    public String toString()
    {
        return nombre;
    }

    public ArrayList<Vacuna> getVacunas()
    {
        return vacunas;
    }
}


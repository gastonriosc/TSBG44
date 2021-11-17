package business;

public class Vacuna
{
    private String nombre;
    private int cant;

    public Vacuna(String nombre)
    {
        this.nombre = nombre;
        this.cant = 1;
    }

    public String getNombre()
    {
        return nombre;
    }

    public int getCant()
    {
        return cant;
    }

    public void setCant(int cant)
    {
        this.cant = cant;
    }

    public String toString()
    {
        return nombre + ": " + cant;
    }
}

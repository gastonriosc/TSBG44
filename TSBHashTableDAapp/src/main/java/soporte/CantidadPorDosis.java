package soporte;

public class CantidadPorDosis {

    private String valor;
    private int cantidad;

    public CantidadPorDosis(String valor, int cant)
    {
        this.valor = valor;
        this.cantidad = cant;
    }


    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}

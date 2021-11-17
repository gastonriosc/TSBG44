package business;

import support.TSBHashTableDA;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Scanner;

public class TablaDeptos {
    private TSBHashTableDA<Integer, Depto> tabla;

    public TablaDeptos() {
        tabla = new TSBHashTableDA<>();
    }

    public void cargar(File f) {
        int counter = 0;

        try {
            Scanner miEscaner = new Scanner(f);
            while (miEscaner.hasNextLine()) {
                String[] linea = miEscaner.nextLine().split(",");

                if (counter == 0) // esto se hace para no contar la primera vuelta donde están los nombres de cada campo.
                {
                    counter++;
                    continue;
                }

                String nom_jurid = linea[6];    // nombre de la jurisdicción donde fue aplicada la vacuna.
                int id_depto = Integer.parseInt(linea[9].substring(1, linea[9].length() - 1));     // id del departamento donde fue aplicada la vacuna.
                String nom_depto = linea[8];    // nombre del departamento donde fue aplicada la vacuna.
                String sexo = linea[0];         // Género de la persona a la cual se le aplicó la vacuna, M o F.
                String numDosis = linea[13];    // orden de la dosis aplicada.
                String vacuna = linea[11];      // nombre de la vacuna aplicada.

                if (Objects.equals(nom_jurid, "\"Córdoba\"")) // pregunta si el registro fue en Córdoba.
                {
                    if (tabla.get(id_depto) == null) // pregunta si la tabla ya tiene ese depto registrado.
                    {
                        // el departamento actual no se encuentra en la tabla, lo agrega.

                        Depto departamento = new Depto(nom_depto);  // crea el departamento con su nombre.
                        departamento.addSexo(sexo);                 // suma +1 al contador de sexo de acuerdo al género del registro.
                        departamento.addNroDosis(numDosis);         // suma +1 al contador de orden de la dosis de acuerdo al orden del registro.
                        departamento.addVacuna(vacuna);             // suma +1 al contador de vacunas de acuerdo a la vacuna aplicada en el registro.

                        tabla.put(id_depto, departamento);          // agrega el departamento a la tablaDeptos.
                    } else {
                        // el departamento ya se encuentra en la tabla.

                        Depto departamento = (Depto) tabla.get(id_depto); // recupera el departamento.
                        departamento.addSexo(sexo);                 // suma +1 al contador de sexo de acuerdo al género del registro.
                        departamento.addNroDosis(numDosis);         // suma +1 al contador de orden de la dosis de acuerdo al orden del registro.
                        departamento.addVacuna(vacuna);             // suma +1 al contador de vacunas de acuerdo a la vacuna aplicada en el registro.
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("El archivo no existe");
        }
    }

    public String toString() {
        return tabla.values().toString();
    }


    // la idea es que este metodo recupere el depto de la tabla a partir del nombre pasado por paramentro,
    // pero el key es el id y el value es un objeto Depto, nose como resolverlo
//    public Depto getDepto(String nombre)
//    {
//        return tabla.get()
//    }
}

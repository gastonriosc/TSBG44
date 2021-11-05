package clases;

import java.io.FileNotFoundException;

import java.io.*;
import java.util.*;

/**
 * Una clase con un main() simple para probar la clase TSBHashtable.
 * @author Ing. Valerio Frittelli.
 * @version Octubre de 2017.
 */
public class Test {
    public static void main(String args[]) {

          TSBHashTableDA<Integer, String> tabla = new TSBHashTableDA<>();
//        int cont = 0;
//        File archivo = new File("prueba.csv");
//
//        try
//        {
//            Scanner miEscaner = new Scanner(archivo);
//            while (miEscaner.hasNextLine())
//            {
//                String linea = miEscaner.nextLine();
//                String []line = linea.split(",");
//                if (Objects.equals(line[6], "\"Córdoba\"")) {
//                    Registro reg = new Registro(line[0], line[1], line[2], line[3],
//                                                line[4], line[5], line[6], line[7],
//                                                line[8], line[9], line[10], line[11],
//                                                line[12], line[13], line[14]);
//                    tabla.put(cont, reg);
//
//                    cont += 1;
//                }
//            }
//        }
//        catch (FileNotFoundException e)
//        {
//            System.err.println("El arch no existe");
//        }

        tabla.put(1,"uno");
        tabla.put(2,"dos");
        tabla.put(3,"tres");
        tabla.put(4,"cuatro");

        for(String s : tabla.values())
            System.out.println(s);
    }
}

package clases;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Una clase con un main() simple para probar la clase TSBHashtable.
 * @author Ing. Valerio Frittelli.
 * @version Octubre de 2017.
 */
public class Test 
{
    public static void main(String args[])
    {
        // una tabla "corta" con factor de carga pequeño...
//        TSBHashTableDA<Integer, String> ht1 = new TSBHashTableDA<>(5);
        //TSBHashtable<Integer, String> ht1 = new TSBHashtable<>(8);
//        System.out.println("Contenido inicial: " + ht1);

//        // algunas inserciones...
//        ht1.put(1, "Argentina");
//        ht1.put(2, "Brasil");
//        ht1.put(3, "Chile");
//        ht1.put(4, "Peru");
//
//        System.out.println("\nAntes del rehash: " + ht1);
//
//        ht1.put(4, "Venezuela");
//        ht1.put(5, "Estados Unidos");
//        System.out.println("\nLuego de algunas inserciones: " + ht1);

//        boolean valor = ht1.containsValue("Argenti");
//        System.out.println(valor);

        TSBHashTableDA<String, Integer> ht2 = new TSBHashTableDA<>();
        ht2.put("a", 1);
        ht2.put("b", 2);
        System.out.println("Segunda tabla: " + ht2);

        ht2.remove("a");
        boolean valor = ht2.containsValue(1);

        System.out.println("\nSegunda tabla: " + ht2);
        System.out.println(valor);






//        System.out.println("\nTabla 1 recorrida a partir de una vista: ");
//        Set<Map.Entry<String, Integer>> se = ht2.entrySet(); // una coleccion Set que representa un conjunto con los keys.
//        Iterator<Map.Entry<String, Integer>> it = se.iterator(); // el iterador que implementa la coleccion Set en su interface.
//        while(it.hasNext())
//        {
//            Map.Entry<String, Integer> entry = it.next();
//            System.out.println("Par: " + entry);
//        }


//        System.out.println("keySet: " + ht2.keySet());
//        System.out.println("values: " + ht2.values());
//        boolean valor = ht2.values().contains(2);
//        System.out.println(valor);
//
//        TSBHashTableDA<String, Integer> ht3 = (TSBHashTableDA<String, Integer>) ht2.clone();
//        System.out.println("ht2: " + ht2);
//        System.out.println("ht3: " + ht3);
//
//        ht3.put("e", 7);
//        System.out.println("\nht2: " + ht2);
//        System.out.println("ht3: " + ht3);
//
//        System.out.println(ht2.hashCode());
//        System.out.println(ht3.hashCode());

    }
}

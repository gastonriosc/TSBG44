package clases;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Clase para emular la funcionalidad de la clase java.util.Hashtable provista
 * en forma nativa por Java. Una TSBHashtable usa un arreglo de la clase entry
 * que implementa la interfaz map, y la estrategia de direccionamiento abierto
 * para resolver las colisiones que pudieran presentarse.
 * 
 * Se almacenan en la tabla pares de objetos (key, value), en donde el objeto 
 * key actúa como clave para identificar al objeto value. La tabla no admite 
 * repetición de claves (no se almacenarán dos pares de objetos con la misma
 * clave). Tampoco acepta referencias nulas (tanto para las key como para los
 * values): no será insertado un par (key, value) si alguno de ambos objetos es
 * null. 
 * 
 * Se ha emulado tanto como ha sido posible el comportamiento de la clase ya 
 * indicada java.util.Hashtable. En esa clase, el parámetro loadFactor se usa
 * para determinar qué tan llena está la tabla antes de lanzar un proceso de 
 * rehash: si loadFactor es 0.75, entonces se hará un rehash cuando la cantidad 
 * de casillas ocupadas en el arreglo de soporte sea un 75% del tamaño de ese 
 * arreglo. En nuestra clase TSBHashtable, mantuvimos el concepto de loadFactor
 * (ahora llamado load_factor).
 * 
 * @author Ardiles Micaela, Avila Pilar, Ríos Cardona Gastón, Targón Juan Cruz
 * @version Octubre del 2021.
 * @param <K> el tipo de los objetos que serán usados como clave en la tabla.
 * @param <V> el tipo de los objetos que serán los valores de la tabla.
 */
public class TSBHashTableDA<K,V> implements Map<K,V>, Cloneable, Serializable
{
    //************************ Constantes (privadas o públicas).    
    
    // el tamaño máximo que podrá tener el arreglo de soporte...
    private final static int MAX_SIZE = Integer.MAX_VALUE; // el MAX_VALUE de Integer también es primo.


    //************************ Atributos privados (estructurales).
    
    // la tabla hash: el arreglo que contiene las listas de desborde...
    private Object []table;
    
    // el tamaño inicial de la tabla (tamaño con el que fue creada)...
    private int initial_capacity;
    
    // la cantidad de objetos que contiene la tabla en TODAS sus listas...
    private int count;
    
    // el factor de carga para calcular si hace falta un rehashing...
    private float load_factor;
      


    //************************ Atributos privados (para gestionar las vistas).

    /**
     * (Tal cual están definidos en la clase java.util.Hashtable)
     * Cada uno de estos campos se inicializa para contener una instancia de la
     * vista que sea más apropiada, la primera vez que esa vista es requerida. 
     * Las vistas son objetos stateless (no se requiere que almacenen datos, sino
     * que solo soportan operaciones), y por lo tanto no es necesario crear más
     * de una de cada una.
     */
    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K,V>> entrySet = null;
    private transient Collection<V> values = null;



    //************************ Atributos protegidos (control de iteración).
    
    // conteo de operaciones de cambio de tamaño (fail-fast iterator).
    protected transient int modCount;
    


    //************************ Constructores.

    /**
     * Crea una tabla vacía, con la capacidad inicial igual a 11 y con factor 
     * de carga igual a 0.5f.
     */    
    public TSBHashTableDA()
    {
        this(11, 0.5f);
    }
    
    /**
     * Crea una tabla vacía, con la capacidad inicial indicada y con factor 
     * de carga igual a 0.5f.
     * @param initial_capacity la capacidad inicial de la tabla.
     */    
    public TSBHashTableDA(int initial_capacity)
    {
        this(initial_capacity, 0.5f);
    }

    /**
     * Crea una tabla vacía, con la capacidad inicial indicada y con el factor 
     * de carga indicado. Si la capacidad inicial indicada por initial_capacity 
     * es menor o igual a 2, la tabla será creada de tamaño 11. Si el factor de
     * carga indicado es negativo o cero, se ajustará a 0.5f.
     * Con un array mayor a 3 y largo igual a un número primo, junto a una ocupacion
     * de maximo un 50%, se puede garantizar una iteración por todos los casilleros de la tabla
     * sin dejar alguno sin visitar.
     * @param initial_capacity la capacidad inicial de la tabla.
     * @param load_factor el factor de carga de la tabla.
     */
    public TSBHashTableDA(int initial_capacity, float load_factor)
    {
        if(load_factor <= 0) { load_factor = 0.5f; }
        if(initial_capacity <= 2) { initial_capacity = 11; }
        else if (!esPrimo(initial_capacity))
        {
            initial_capacity = siguientePrimo(initial_capacity);
        }
        if(initial_capacity > TSBHashTableDA.MAX_SIZE)
            {
                initial_capacity = TSBHashTableDA.MAX_SIZE;
            }


        this.table = new Object[initial_capacity];
        for (int i = 0; i < table.length; i++)
        {
            table[i] = new Entry<>();   // todo puede ser null?
        }
        
        this.initial_capacity = initial_capacity;
        this.load_factor = load_factor;
        this.count = 0;
        this.modCount = 0;
    }
    
    /**
     * Crea una tabla a partir del contenido del Map especificado.
     * @param t el Map a partir del cual se creará la tabla.
     */
    /*
     * "? extends K" significa que, no importa con que tipo de clave creaste el Map.Entry, tiene que poder ser casteable
     * al tipo de clave K de este Map de la TSBHashtable. Lo mismo con el value.
    */
    public TSBHashTableDA(Map<? extends K,? extends V> t)
    {
        this(11, 0.8f);
        this.putAll(t);
    }
    




    //************************ Implementación de métodos especificados por Map.
    
    /**
     * Retorna la cantidad de elementos contenidos en la tabla.
     * @return la cantidad de elementos de la tabla.
     */
    @Override
    public int size() 
    {
        return this.count;
    }

    /**
     * Determina si la tabla está vacía (no contiene ningún elemento).
     * @return true si la tabla está vacía.
     */
    @Override
    public boolean isEmpty() 
    {
        return (this.count == 0);
    }

    /**
     * Determina si la clave key está en la tabla. 
     * @param key la clave a verificar.
     * @return true si la clave está en la tabla.
     * @throws NullPointerException si la clave es null.
     */
    @Override
    public boolean containsKey(Object key) 
    {
        return (this.get((K)key) != null);
    }

    /**
     * Determina si alguna clave de la tabla está asociada al objeto value que
     * entra como parámetro. Equivale a contains().
     * @param value el objeto a buscar en la tabla.
     * @return true si alguna clave está asociada efectivamente a ese value.
     */    
    @Override
    public boolean containsValue(Object value)
    {
        return this.contains(value);
    }

    /**
     * Retorna el objeto al cual está asociada la clave key en la tabla, o null 
     * si la tabla no contiene ningún objeto asociado a esa clave.
     * @param key la clave que será buscada en la tabla.
     * @return el objeto asociado a la clave especificada (si existe la clave) o 
     *         null (si no existe la clave en esta tabla).
     * @throws NullPointerException si key es null.
     * @throws ClassCastException si la clase de key no es compatible con la 
     *         tabla.
     */
    @Override
    public V get(Object key) 
    {
       if(key == null) throw new NullPointerException("get(): parámetro null");

       int index = this.search_for_entry_index((K)key);
       Entry<K,V> entry =(Entry<K,V>) table[index];
       return (entry.getEstado() == 1) ? entry.getValue() : null; // se evalua que sea un entry ocupado y no una tumba.
    }

    /**
     * Asocia el valor (value) especificado, con la clave (key) especificada en
     * esta tabla. Si la tabla contenía previamente un valor asociado para la 
     * clave, entonces el valor anterior es reemplazado por el nuevo (y en este
     * caso el tamaño de la tabla no cambia). 
     * @param key la clave del objeto que se quiere agregar a la tabla.
     * @param value el objeto que se quiere agregar a la tabla.
     * @return el objeto anteriormente asociado a la clave si la clave ya 
     *         estaba asociada con alguno, o null si la clave no estaba antes 
     *         asociada a ningún objeto.
     * @throws NullPointerException si key es null o value es null.
     */
    @Override
    public V put(K key, V value) 
    {
        if(key == null || value == null) throw new NullPointerException("put(): parámetro null");

        if(count >= this.load_factor * table.length) this.rehash();

        int index;
        V old = null;
        index = this.search_for_entry_index(key);
        Entry<K,V> entry = (Entry<K,V>) table[index];
        if(entry.getEstado() != 0) // es un entry ocupado
        {
            if (entry.getEstado() == 1) // si es entry, no es tumba
           {
               old = entry.getValue();
           }
           entry.setValue(value);
           entry.setEstado(1);
        }
        else // no está ocupado
        {
            table[index] = new Entry<>(key, value);
            this.count++;
            this.modCount++; // aca se hace modCount++ por si no se hizo rehash, para mostrar que se modificó la tabla
        }
       
       return old;
    }

    /**
     * Elimina de la tabla la clave key (y su correspondiente valor asociado).  
     * Utiliza el metodo search_for_entry_index para obtener el índice del lugar
     * donde debería encontrarse el objeto.A partir de ahí se evalúa si ya existe
     * la key ingresada por parámetro en la tabla.
     * @param key la clave a eliminar.
     * @return El objeto al cual la clave estaba asociada, o null si la clave no
     *         estaba en la tabla.
     * @throws NullPointerException - if the key is null.
     */
    @Override
    public V remove(Object key) 
    {
       if(key == null) throw new NullPointerException("remove(): parámetro null");

       int index = this.search_for_entry_index((K)key);
       V old = null;
       Entry<K,V> entry =(Entry<K,V>) table[index];
       if(entry.getEstado() == 1)
       {
           old = entry.getValue();
           entry.setEstado(2);
           this.count--;
           this.modCount++;
       }
       return old;        
    }

    /**
     * Copia en esta tabla, todos los objetos contenidos en el map especificado.
     * Los nuevos objetos reemplazarán a los que ya existan en la tabla 
     * asociados a las mismas claves (si se repitiese alguna).
     * @param m el map cuyos objetos serán copiados en esta tabla. 
     * @throws NullPointerException si m es null.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) 
    {
        for(Map.Entry<? extends K, ? extends V> e : m.entrySet())
        {
            this.put(e.getKey(), e.getValue());
        }
    }

    /**
     * Elimina todo el contenido de la tabla, de forma de dejarla vacía. En esta
     * implementación además, el arreglo de soporte vuelve a tener el tamaño que
     * inicialmente tuvo al ser creado el objeto (inicial_capacity).
     */
    @Override
    public void clear() 
    {
        this.table = new Object[this.initial_capacity];
        for (int i = 0; i < table.length; i++)
        {
            table[i] = new Entry<>();
        }
        this.count = 0;
        this.modCount++;
    }





    //************************ Métodos de creación de cada una de las vistas.

    /**
     * Retorna un Set (conjunto) a modo de vista de todas las claves (key)
     * contenidas en la tabla. El conjunto está respaldado por la tabla, por lo 
     * que los cambios realizados en la tabla serán reflejados en el conjunto, y
     * viceversa. Si la tabla es modificada mientras un iterador está actuando 
     * sobre el conjunto vista, el resultado de la iteración será indefinido 
     * (salvo que la modificación sea realizada por la operación remove() propia
     * del iterador, o por la operación setValue() realizada sobre una entrada 
     * de la tabla que haya sido retornada por el iterador). El conjunto vista 
     * provee métodos para eliminar elementos, y esos métodos a su vez 
     * eliminan el correspondiente par (key, value) de la tabla (a través de las
     * operaciones Iterator.remove(), Set.remove(), removeAll(), retainAll() 
     * y clear()). El conjunto vista no soporta las operaciones add() y 
     * addAll() (si se las invoca, se lanzará una UnsuportedOperationException).
     * @return un conjunto (un Set) a modo de vista de todas las claves
     *         mapeadas en la tabla.
     */
    @Override
    public Set<K> keySet()
    {
        if(keySet == null)
        {
            // keySet = Collections.synchronizedSet(new KeySet());
            keySet = new KeySet();
        }
        return keySet;
    }
        
    /**
     * Retorna una Collection (colección) a modo de vista de todos los valores
     * (values) contenidos en la tabla. La colección está respaldada por la 
     * tabla, por lo que los cambios realizados en la tabla serán reflejados en 
     * la colección, y viceversa. Si la tabla es modificada mientras un iterador 
     * está actuando sobre la colección vista, el resultado de la iteración será 
     * indefinido (salvo que la modificación sea realizada por la operación 
     * remove() propia del iterador, o por la operación setValue() realizada 
     * sobre una entrada de la tabla que haya sido retornada por el iterador). 
     * La colección vista provee métodos para eliminar elementos, y esos métodos 
     * a su vez eliminan el correspondiente par (key, value) de la tabla (a 
     * través de las operaciones Iterator.remove(), Collection.remove(), 
     * removeAll(), removeAll(), retainAll() y clear()). La colección vista no 
     * soporta las operaciones add() y addAll() (si se las invoca, se lanzará 
     * una UnsuportedOperationException).
     * @return una colección (un Collection) a modo de vista de todas los 
     *         valores mapeados en la tabla.
     */
    @Override
    public Collection<V> values()
    {
        if(values==null)
        {
            // values = Collections.synchronizedCollection(new ValueCollection());
            values = new ValueCollection();
        }
        return values;
    }

    /**
     * Retorna un Set (conjunto) a modo de vista de todos los pares (key, value)
     * contenidos en la tabla. El conjunto está respaldado por la tabla, por lo 
     * que los cambios realizados en la tabla serán reflejados en el conjunto, y
     * viceversa. Si la tabla es modificada mientras un iterador está actuando 
     * sobre el conjunto vista, el resultado de la iteración será indefinido 
     * (salvo que la modificación sea realizada por la operación remove() propia
     * del iterador, o por la operación setValue() realizada sobre una entrada 
     * de la tabla que haya sido retornada por el iterador). El conjunto vista 
     * provee métodos para eliminar elementos, y esos métodos a su vez 
     * eliminan el correspondiente par (key, value) de la tabla (a través de las
     * operaciones Iterator.remove(), Set.remove(), removeAll(), retainAll() 
     * and clear()). El conjunto vista no soporta las operaciones add() y 
     * addAll() (si se las invoca, se lanzará una UnsuportedOperationException).
     * @return un conjunto (un Set) a modo de vista de todos los objetos 
     *         mapeados en la tabla.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        if(entrySet == null)
        {
            // entrySet = Collections.synchronizedSet(new EntrySet());
            entrySet = new EntrySet();
        }
        return entrySet;
    }





    //************************ Redefinición de métodos heredados desde Object.
    
    /**
     * Retorna una copia superficial de la tabla. En cada casillero del vector
     * tabla se almacenan las direcciones de los mismos objetos que contiene
     * la original. Se utiliza el metodo putAll para hacer la copia.
     * @return una copia superficial de la tabla.
     * @throws java.lang.CloneNotSupportedException si la clase no implementa la
     *         interface Cloneable.    
     */ 
    @Override
    protected Object clone() //throws CloneNotSupportedException
    {

        TSBHashTableDA<K, V> t = new TSBHashTableDA<>();
        t.putAll(this);
        return t;
    }


    /**
     * Determina si esta tabla es igual al objeto espeficicado.
     * Si el objeto a comparar no es de la clase Map o sus
     * tamaños no son iguales, retorna false.
     * @param obj el objeto a comparar con esta tabla.
     * @return true si los objetos son iguales.
     */
    @Override
    public boolean equals(Object obj) 
    {
        if(!(obj instanceof Map)) { return false; } // pregunta si el objeto es instancia de map sino retorna False

        Map<K, V> t = (Map<K, V>) obj;
        if(t.size() != this.size()) { return false; } // si los size son distintos ya es false

        return t.hashCode() == this.hashCode();
    }

    /**
     * Retorna un hash code para la tabla completa. El cual
     * es sumatoria de todos los hash code de cada objeto Entry
     * dentro de la tabla.
     * @return un acumulador que representa el hash code para la tabla.
     */
    @Override
    public int hashCode() // deberia ser la suma de todos los hashcodes...
    {
        int hc = 0;
        for (int i = 0; i < table.length-1; i++)
        {
            Entry<K,V> entry =(Entry<K,V>) table[i];
            if (entry.getEstado() == 1) { hc += entry.hashCode(); }
        }
        return hc;
    }
    
    /**
     * Devuelve el contenido de la tabla en forma de String. Recorriendo
     * cada objeto Entry para pedirle su implementación del método toString().
     * @return una cadena con el contenido completo de la tabla.
     */
    @Override
    public String toString()
    {
        boolean entro = false;
        StringBuilder cad = new StringBuilder("{");
        for (int i = 0; i < table.length; i++) {
            Entry<K,V> e =(Entry<K,V>) table[i];
            if (e.getEstado()==1)
            {
                cad.append(e.toString()).append(", ");
                entro = true;
            }
        }
        if (entro) {cad.setLength(cad.length()-2);}
        cad.append("}");
        return cad.toString();
    }


    //************************ Métodos específicos de la clase.

    /**
     * Determina si alguna clave de la tabla está asociado al objeto value que
     * entra como parámetro. Si el value es null retorna false, y en el caso de que
     * se encuentre alguna key con el value ingresado como parámetro, se retorna true.
     * @param value es el objeto a buscar en la tabla.
     * @return true si el value ingresado por parámetro está asociado a alguna clave
     * de la tabla y si el estado es "cerrado" (=1).
     */
    public boolean contains(Object value)
    {
        if(value == null) return false;
        Object valor;

        for(int i = 0; i<table.length; i++)
        {
            valor = ((Entry) table[i]).value;
            if(valor == value && ((Entry) table[i]).getEstado()==1)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Incrementa el tamaño de la tabla y reorganiza su contenido. Se invoca 
     * automaticamente cuando se detecta que la cantidad de Entry cerrados en la tabla
     * supera a cierto  valor critico dado por (load_factor * table.length). Si el
     * valor de load_factor es 0.5, esto implica que el límite antes de invocar
     * rehash es del 50% de el tamaño de la tabla.
     * Para calcular el nuevo tamaño, lo que hace es buscar el primo que le sigue al resultado de hacer el
     * largo anterior multiplicado por dos. Una vez obtenido, reordena. También se verifica que el nuevo
     * primo no supere al tamaño máximo de la tabla.
     */
    protected void rehash()
    {
        int old_length = this.table.length;

        // nuevo tamaño: el siguiente número primo del doble del anterior.
        int new_length = siguientePrimo(old_length * 2);

        // no permitir que la tabla tenga un tamaño mayor al límite máximo...
        // ... para evitar overflow y/o desborde de índices...
        if(new_length > TSBHashTableDA.MAX_SIZE)
        {
            new_length = TSBHashTableDA.MAX_SIZE;
        }

        // crear el nuevo arreglo con new_length listas vacías...
        Object []temp = new Object[new_length]; // temp[] es la nueva table[]
        for (int i = 0; i < new_length; i++)
        {
            temp[i] = new Entry<>();   // todo puede ser null?
        }

        // notificación fail-fast iterator... la tabla cambió su estructura...
        this.modCount++;  // la idea del incremento del modCount es mostrar que varió su valor, no tanto cuánto vale.
                          // aca se hace modCount++ para avisar que hubo rehash...

        // recorrer el viejo arreglo y redistribuir los objetos que tenia...

        for(int i = 0; i < this.table.length; i++)
        {
            Entry entry = (Entry) table[i];
            if (entry.getEstado()==1) // si no es una tumba, llama al put() para agregarlo a la nueva tabla
            {
                K key = (K)entry.getKey();
                int index = h(key, new_length);
                temp[index] = entry;
            }
        }

        // cambiar la referencia table para que apunte a temp...
        this.table = temp;
    }
    

    //************************ Métodos privados.

        // metodos hash.
    /**
     * Función hash. Toma una clave entera k y calcula y retorna un índice 
     * válido para esa clave para entrar en la tabla.
     * @param k recibe una clave de tipo int
     * @return un indice válido de la tabla para dicha clave.
     */

    private int h(int k)
    {
        return h(k, table.length);
    }
    
    /**
     * Función hash. Toma un objeto key que representa una clave y calcula y 
     * retorna un índice válido para esa clave para entrar en la tabla.
     * @param key es una clave que se quiere hashear
     * @return un indice valido para dicha clave para la tabla.
     */
    private int h(K key)
    {
        return h(key.hashCode(), table.length);
    }
    
    /**
     * Función hash. Toma un objeto key que representa una clave y un tamaño de 
     * tabla t, y calcula y retorna un índice válido para esa clave dedo ese
     * tamaño.
     * @param key es una clave que se quiere hashear
     * @param t es el tamaño de la tabla
     * @return un indice valido para esa clave y para el tamaño de tabla ingresado
     */
    private int h(K key, int t)
    {
        return h(key.hashCode(), t);
    }
    
    /**
     * Función hash. Toma una clave entera k y un tamaño de tabla t, y calcula y 
     * retorna un índice válido para esa clave dado ese tamaño.
     * @param t recibe el tamaño de la tabla
     * @param k recibe una clave de tipo entera
     * @return un indice valido para el numero de clave y tamaño de tabla soliciotados.
     */
    private int h(int k, int t)
    {
        if(k < 0) k *= -1;
        return k % t;        
    }

        // otros metodos privados.
    /**
     * Busca en la lista bucket un objeto Nodo cuya clave coincida con key.
     * Si lo encuentra, retorna ese objeto Nodo. Si no lo encuentra, retorna
     * null.
     */
    private int search_for_entry_index(K key)
    {
        int hashMadre = h(key);
        int index = 0;
        int t = -1;
        for (int j = 0; ;j++)
        {
            index = (hashMadre + j*j) % table.length;
            Entry entrada = (Entry) table[index];
            if (!(entrada.getEstado() == 0)) // el casillero está ocupado.
            {
                if (entrada.getKey() == key) // las keys son las mismas.
                {
                    return index; // retorna el index de ese nodo.
                }
                else // las keys son distintas.
                {
                    if (entrada.getEstado() == 2) // ese nodo es una tumba.
                    {
                        if (t == -1)  // ya encontró una tumba antes? si no, guarda el indice
                        {
                            t = index;
                        }
                    }
                }
            }
            else
            { // el casillero está vacio.
                if (t == -1) // si no se encontró una tumba antes, retorna el index del casillero vacío.
                {
                    return index;
                }
                return t; // sino retorna el casillero de la primera tumba que encontró
            }

        }
    }


    /**
     * Método que verifica si número ingresado por parámetro es primo. En caso de serlo, retorna true
     * @param numero es el numero que se quiere verificar si es primo
     * @return retorna un valor booleano. Retorna true en caso de que el número a verificar es primo,
     *         y false en caso de no serlo.
     */
    private boolean esPrimo(int numero)
    {
        // El 0, 1 y 4 no son primos
        if (numero == 0 || numero == 1 || numero == 4) {
            return false;
        }
        for (int x = 2; x < numero / 2; x++) {
            // Si es divisible por cualquiera de estos números, no
            // es primo
            if (numero % x == 0)
                return false;
        }
        // Si no se pudo dividir por ninguno de los de arriba, sí es primo
        return true;
    }


    /**
     * Método que busca el siguiente primo de un número ingresado por parámetro.
     * @param n valor del tipo int que representa el número del que se quiere saber el siguiente primo
     * @return el valor del siguiente número primo del número solicitado
     */
    private  int siguientePrimo(int n)
    {
        if ( n % 2 == 0)
            n++;
        for ( ; !esPrimo(n); n+=2 ) ;
        return n;
    }

    //************************ Clases Internas.
    
    /**
     * Clase interna que representa los pares de objetos que se almacenan en la
     * tabla hash: son instancias de esta clase las que realmente se guardan en 
     * cada uno de los nodos del arreglo table que se insertan en esta tabla.
     * Lanzará una IllegalArgumentException si alguno de los dos parámetros es null.
     * si estado == 0 -> casillero abierto.
     * si estado == 1 -> casillero cerrado.
     * si estado == 2 -> casillero tumba.
     */
    private class Entry<K, V> implements Map.Entry<K, V>
    {
        private K key;
        private V value;
        private int estado;

        //****************** Constructores

        public Entry(K key, V value) 
        {
            if(key == null || value == null)
            {
                throw new IllegalArgumentException("Entry(): parámetro null...");
            }
            this.key = key;
            this.value = value;
            this.estado = 1;
        }

        public Entry(K key, V value,int estado)
        {
            if(key == null || value == null)
            {
                throw new IllegalArgumentException("Entry(): parámetro null...");
            }
            this.key = key;
            this.value = value;
            this.estado = estado ; // 0 = abierto  1 = cerrado  2 = tumba
        }

        public Entry()
        {
            this.key = null;
            this.value = null;
            this.estado = 0;
        }

        //****************** Implementación de métodos especificados por Map.Entry

        /**
         * Método que devuelve la clave de un objeto Entry.
         * @return el valor de la key
         */
        @Override
        public K getKey() 
        {
            return key;
        }

        /**
         * Método que devuelve el valor de un objeto Entry.
         * @return un valor
         */
        @Override
        public V getValue() 
        {
            return value;
        }

        /**
         * Método que setea el valor de un objeto Entry, con el valor recibido por parámetro.
         * @param value es el valor que se debe setear al objeto Entry.
         * @return el valor guardado anteriormente
         * @throws IllegalArgumentException si el value ingresado como parámetro es nulo
         */
        @Override
        public V setValue(V value)
        {
            if(value == null)
            {
                throw new IllegalArgumentException("setValue(): parámetro null...");
            }

            V old = this.value;
            this.value = value;
            return old;
        }

        /**
         * Método que obtiene el estado del objeto Entry.
         * @return el valor que identifica el estado = 0 si es abierto, 1 si es cerrado y 2 si fue eliminado.
         */
        public int getEstado(){return estado; }

        /**
         * Método que modifica el valor del estado de entry.
         * Lo que hace es setear cualquiera de los valores del estado (ya sea 0, 1 o 2) con el
         * valor ingresado como parámetro.
         * @param i valor por el que se debe cambiar el estado.
         */
        public void setEstado(int i)
        {
            if (i == 0 || i ==1 || i == 2) {this.estado = i;}
        }


        /**
         * Método que devuelve un valor booleano true en caso de que los dos objetos comparados sean iguales
         * (el ingresado por parámetro y el actual de la tabla).
         * @param obj recibe el objeto con el cual se quiere realizar la comparación.
         * @return true en caso de que sean iguales los objetos comparados. Retorna false
         *         en caso de que el objeto ingresado sea null; en el caso de que la clase de los objetos sea diferente;
         *         en el caso de que el estado de alguno de los dos objetos sea abierto o tumba. Finalmente,
         *         tambien retorna false en el caso de que las key o value de ambos objetos sean diferentes.
         */
        @Override
        public boolean equals(Object obj) 
        {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (this.getClass() != obj.getClass()) { return false; }

            final Entry other = (Entry) obj;
            if (this.estado != 1 || other.estado != 1){return false; } // si las entry no están cerradas no las debería poder comparar
            

            if (!Objects.equals(this.key, other.key)) { return false; }
            if (!Objects.equals(this.value, other.value)) { return false; }            
            return true;
        }

        /**
         * Método que permite obtener un valor identificador para un objeto. Este
         * debería ser diferente para cada objeto.
         * @return el valor hashCode, que es el identificador del objeto
         */
        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;
        }

        /**
         * Método que devuelve una copia de un objeto Entry.
         * El nuevo objeto de tipo Entry tiene la misma key y mismo value del objeto del cual se está clonando, pero
         * ambos objetos son independientes entre sí
         * @return el objeto clonado de tipo entry
         * @throws CloneNotSupportedException
         */

        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            Entry<K, V> entry = (Entry<K, V>) super.clone();
            entry.key = getKey();
            entry.value = getValue();
            return entry;
        }


        /**
         * Método redefinido que retorna una cadena.
         * @return el string reultante de la concatenación de otros strings
         */
        @Override // de Object.
        public String toString()
        {
            return "(" + key.toString() + " --> " + value.toString() + ")";
        }
    }

    /**
     * Clase interna que representa una vista de todas los Claves mapeadas en la
     * tabla: si la vista cambia, cambia también la tabla que le da respaldo, y
     * viceversa. La vista es stateless: no mantiene estado alguno (es decir, no
     * contiene datos ella misma, sino que accede y gestiona directamente datos
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar toodo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class KeySet extends AbstractSet<K>
    {
        @Override
        public Iterator<K> iterator()
        {
            return new KeySetIterator();
        }

        /**
         * Método que obtiene la cantidad de elementos contenidos en la tabla.
         * @return la cantidad de elementos contenidos en la tabla.
         */
        @Override
        public int size()
        {
            return TSBHashTableDA.this.count;
        }

        /**
         * Método que verifica si la tabla contiene o posee en su interior el objeto
         * enviado por parametro
         * @param o se recibe el objeto que se quiere verificar si se encuentra en la tabla
         * @return retorna un true en caso de que el objeto esté contenido en la tabla, y un false en caso
         *         de que no lo esté
         */
        @Override
        public boolean contains(Object o)
        {
            return TSBHashTableDA.this.containsKey(o);
        }

        /**
         * Método que elimina o remueve de la tabla el objeto pasado por parámetro.
         * @param o recibe un objeto que se quiere remover de la tabla
         * @return un booleano, true si se pudo remover y false si no se removió el objeto
         */
        @Override
        public boolean remove(Object o)
        {
            return (TSBHashTableDA.this.remove(o) != null);
        }

        /**
         * Método que limpia el interior de la tabla. No retorna nada
         */
        @Override
        public void clear()
        {
            TSBHashTableDA.this.clear();
        }


        private class KeySetIterator implements Iterator<K>
        {
            // índice de la lista actualmente recorrida...
            private int current_entry;

            // índice de la lista anterior (si se requiere en remove())...
            private int last_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public KeySetIterator()
            {
                current_entry = 0;
                last_entry = 0;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }


            /**
             * Método que retorna un true en caso de que haya un próximo elemento en la tabla, es decir,
             * que su estado sea "cerrado". Va avanzando de uno en uno buscando y corroborando cada estado de cada entry
             * @return un valor true en caso de que haya un próximo elemento en la tabla y retorna false en
             *          los siguientes casos: la tabla está vacia, la posición actual superó al tamaño de la tabla.
             */

            @Override
            public boolean hasNext()
            {
                Object []t = TSBHashTableDA.this.table;

                if(TSBHashTableDA.this.isEmpty()) { return false; }
                if(current_entry >= t.length) { return false; }

                int next_entry = current_entry + 1;
                for (; next_entry <= t.length - 1; next_entry++)
                {
                    Entry e = (Entry) t[next_entry];
                    if(e.getEstado()==1) {return true;}
                }
                return false;
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            /**
             * Método que obtiene el siguiente elemento que se encuentre en la tabla.
             * @return la clave del objeto encontrado en posiciones posteriores de la tabla.
             * @throws  ConcurrentModificationException
             * @throws NoSuchElementException en caso de que el metodo hasNext haya dado false, es decir, que no
             * hay un próximo elemento
             */
            @Override
            public K next()
            {
                // control: fail-fast iterator...
                if(TSBHashTableDA.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                Object t[] = TSBHashTableDA.this.table;

                Entry entry;

                // ...recordar el índice del nodo que se va a abandonar.
                last_entry = current_entry;

                // buscar el siguiente bucket no vacío, que DEBE existir, ya
                // que se hasNext() retornó true...
                current_entry++;
                entry = (Entry) t[current_entry];
                while(entry.getEstado()!=1)
                {
                    current_entry++;
                    entry = (Entry) t[current_entry];
                }

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar la clave del elemento alcanzado...
                K key = (K)entry.getKey();
                return key;
            }


            /**
             * Remueve el elemento actual de la tabla. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next(). Además, a la tabla se le
             * descuenta un elemento a través del count
             * @throws IllegalStateException en caso de que el next no haya sido invocado con éxito.
             */

            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                V valor = TSBHashTableDA.this.remove(((Entry)table[current_entry]).getKey());

                // quedar apuntando al anterior al que se retornó...
                if(last_entry != current_entry)
                {
                    current_entry = last_entry;
                }

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elementon menos...
                TSBHashTableDA.this.count--;

                // fail_fast iterator: todo en orden...
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }


    /**
     * Clase interna que representa una vista de todos los PARES mapeados en la
     * tabla: si la vista cambia, cambia también la tabla que le da respaldo, y
     * viceversa. La vista es stateless: no mantiene estado alguno (es decir, no
     * contiene datos ella misma, sino que accede y gestiona directamente datos
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar toodo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class EntrySet extends AbstractSet<Map.Entry<K, V>>
    {

        @Override
        public Iterator<Map.Entry<K, V>> iterator()
        {
            return new EntrySetIterator();
        }
        /*
         * Verifica si esta vista (y por lo tanto la tabla) contiene al par
         * que entra como parámetro (que debe ser de la clase Entry).
         */

        @Override
        public boolean contains(Object o)
        {
            if(o == null) { return false; }
            if(!(o instanceof Entry)) { return false; }

            Map.Entry<K, V> entry = (Map.Entry<K,V>)o;
            K key = entry.getKey();
            V value = entry.getValue();
            int index = TSBHashTableDA.this.h(key);


            if(value == get(key)) { return true; }
            return false;
        }
        /*
         * Elimina de esta vista (y por lo tanto de la tabla) al par que entra
         * como parámetro (y que debe ser de tipo Entry).
         */
        @Override
        public boolean remove(Object o)
        {
            if(o == null) { throw new NullPointerException("remove(): parámetro null");}
            if(!(o instanceof Entry)) { return false; }

            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            K key = entry.getKey();
            int index = TSBHashTableDA.this.h(key);

            if(TSBHashTableDA.this.remove(key) != null)
            {
                TSBHashTableDA.this.count--;
                TSBHashTableDA.this.modCount++;
                return true;
            }
            return false;
        }
        @Override
        public int size()
        {
            return TSBHashTableDA.this.count;
        }
        @Override
        public void clear()
        {
            TSBHashTableDA.this.clear();
        }

        private class EntrySetIterator implements Iterator<Map.Entry<K, V>>
        {
            // índice de la lista actualmente recorrida...
            private int current_entry;

            // índice de la lista anterior (si se requiere en remove())...
            private int last_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public EntrySetIterator()
            {
                current_entry = -1;
                last_entry = 0;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                Object[] t = TSBHashTableDA.this.table;

                if (TSBHashTableDA.this.isEmpty()) { return false;}
                if (current_entry >= t.length) {return false;}

                int next_entry = current_entry + 1;
                for (; next_entry <= t.length - 1; next_entry++) {
                    Entry e = (Entry) t[next_entry];
                    if (e.getEstado() == 1) {
                        return true;
                    }
                }
                return false;

            }
            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public Map.Entry<K, V> next()
            {
                // control: fail-fast iterator...
                if(TSBHashTableDA.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                Object t[] = TSBHashTableDA.this.table;

                Entry entry;

                // ...recordar el índice del nodo que se va a abandonar.
                last_entry = current_entry;

                // buscar el siguiente bucket no vacío, que DEBE existir, ya
                // que se hasNext() retornó true...
                current_entry++;
                entry = (Entry) t[current_entry];
                while(entry.getEstado()!=1)
                {
                    current_entry++;
                    entry = (Entry) t[current_entry];
                }

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar la clave del elemento alcanzado...
                return entry;
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                V valor = TSBHashTableDA.this.remove(((Entry)table[current_entry]).getKey());

                // quedar apuntando al anterior al que se retornó...
                if(last_entry != current_entry)
                {
                    current_entry = last_entry;
                }

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elementon menos...
                TSBHashTableDA.this.count--;

                // fail_fast iterator: todo en orden...
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }


    /**
     * Clase interna que representa una vista de todos los VALORES mapeados en
     * la tabla: si la vista cambia, cambia también la tabla que le da respaldo,
     * y viceversa. La vista es stateless: no mantiene estado alguno (es decir,
     * no contiene datos ella misma, sino que accede y gestiona directamente los
     * de otra fuente), por lo que no tiene atributos y sus métodos gestionan en
     * forma directa el contenido de la tabla. Están soportados los metodos para
     * eliminar un objeto (remove()), eliminar toodo el contenido (clear) y la
     * creación de un Iterator (que incluye el método Iterator.remove()).
     */
    private class ValueCollection extends AbstractCollection<V>
    {
        @Override
        public Iterator<V> iterator()
        {
            return new ValueCollectionIterator();
        }
        @Override
        public int size()
        {
            return TSBHashTableDA.this.count;
        }
        @Override
        public boolean contains(Object o)
        {
            return TSBHashTableDA.this.containsValue(o);
        }
        @Override
        public void clear()
        {
            TSBHashTableDA.this.clear();
        }


        private class ValueCollectionIterator implements Iterator<V>
        {
            // índice de la lista actualmente recorrida...
            private int current_entry;

            // índice de la lista anterior (si se requiere en remove())...
            private int last_entry;

            // flag para controlar si remove() está bien invocado...
            private boolean next_ok;

            // el valor que debería tener el modCount de la tabla completa...
            private int expected_modCount;

            /*
             * Crea un iterador comenzando en la primera lista. Activa el
             * mecanismo fail-fast.
             */
            public ValueCollectionIterator()
            {
                current_entry = 0;
                last_entry = 0;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            /*
             * Determina si hay al menos un elemento en la tabla que no haya
             * sido retornado por next().
             */
            @Override
            public boolean hasNext()
            {
                Object []t = TSBHashTableDA.this.table;

                if(TSBHashTableDA.this.isEmpty()) { return false; }
                if(current_entry >= t.length) { return false; }

                int next_entry = current_entry + 1;
                for (; next_entry <= t.length - 1; next_entry++)
                {
                    Entry e = (Entry) t[next_entry];
                    if(e.getEstado()==1) {return true;}
                }
                return false;
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
             */
            @Override
            public V next()
            {
                // control: fail-fast iterator...
                if(TSBHashTableDA.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if(!hasNext())
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                // variable auxiliar t para simplificar accesos...
                Object t[] = TSBHashTableDA.this.table;

                Entry entry;

                // ...recordar el índice del nodo que se va a abandonar.
                last_entry = current_entry;

                // buscar el siguiente bucket no vacío, que DEBE existir, ya
                // que se hasNext() retornó true...
                current_entry++;
                entry = (Entry) t[current_entry];
                while(entry.getEstado()!=1)
                {
                    current_entry++;
                    entry = (Entry) t[current_entry];
                }

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar la clave del elemento alcanzado...
                V value = (V)entry.getValue();
                return value;
            }

            /*
             * Remueve el elemento actual de la tabla, dejando el iterador en la
             * posición anterior al que fue removido. El elemento removido es el
             * que fue retornado la última vez que se invocó a next(). El método
             * sólo puede ser invocado una vez por cada invocación a next().
             */
            @Override
            public void remove()
            {
                if(!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                V valor = TSBHashTableDA.this.remove(((Entry)table[current_entry]).getKey());

                // quedar apuntando al anterior al que se retornó...
                if(last_entry != current_entry)
                {
                    current_entry = last_entry;
                }

                // avisar que el remove() válido para next() ya se activó...
                next_ok = false;

                // la tabla tiene un elementon menos...
                TSBHashTableDA.this.count--;

                // fail_fast iterator: todo en orden...
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }
}

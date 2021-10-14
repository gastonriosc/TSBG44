package clases;

import java.io.Serializable;
import java.sql.Array;
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
 * en forma nativa por Java. Una TSBHashtable usa un arreglo de listas de la 
 * clase TSBArrayList a modo de buckets (o listas de desborde) para resolver las
 * colisiones que pudieran presentarse. 
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
 * (ahora llamado load_factor) pero con una interpretación distinta: en nuestro
 * modelo, se lanza un rehash si la cantidad promedio de valores por lista es 
 * mayor a cierto número constante y pequeño, que asociamos al load_factor para 
 * mantener el espíritu de la implementación nativa. En nuestro caso, si el 
 * valor load_factor es 0.8 entonces se lanzará un rehash si la cantidad 
 * promedio de valores por lista es mayor a 0.8 * 10 = 8 elementos por lista.
 * 
 * @author Ing. Valerio Frittelli.
 * @version Septiembre de 2017.
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
    private Nodo []table;
    
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
     * de carga igual a 0.8f. 
     */    
    public TSBHashTableDA()
    {
        this(11, 0.5f);
    }
    
    /**
     * Crea una tabla vacía, con la capacidad inicial indicada y con factor 
     * de carga igual a 0.8f. 
     * @param initial_capacity la capacidad inicial de la tabla.
     */    
    public TSBHashTableDA(int initial_capacity)
    {
        this(initial_capacity, 0.5f);
    }

    /**
     * Crea una tabla vacía, con la capacidad inicial indicada y con el factor 
     * de carga indicado. Si la capacidad inicial indicada por initial_capacity 
     * es menor o igual a 0, la tabla será creada de tamaño 11. Si el factor de
     * carga indicado es negativo o cero, se ajustará a 0.8f.
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


        this.table = (Nodo[]) new Object[initial_capacity];
        
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

       int index = this.search_for_node_index((K)key);
       Nodo nodo = table[index];
       return (nodo != null && !nodo.getTumba()) ? nodo.getValue() : null; // se evalua que sea un nodo real y no una tumba.
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
        index = this.search_for_node_index(key);
        Nodo nodo = table[index];
        if(nodo != null) // es de tipo Nodo
        {
            if (!nodo.getTumba()) // si es nodo, no es tumba
           {
               old = nodo.getValue();
           }
           nodo.setValue(value);
           nodo.setTumba(false);
        }
        else // no es nodo
        {
            Entry<K, V> entry = new Entry<>(key, value);
            nodo = new Nodo(entry, false);
            table[index] = nodo;
            this.count++;
            this.modCount++; // aca se hace modCount++ por si no se hizo rehash, para mostrar que se modificó la tabla
        }
       
       return old;
    }

    /**
     * Elimina de la tabla la clave key (y su correspondiente valor asociado).  
     * El método no hace nada si la clave no está en la tabla. 
     * @param key la clave a eliminar.
     * @return El objeto al cual la clave estaba asociada, o null si la clave no
     *         estaba en la tabla.
     * @throws NullPointerException - if the key is null.
     */
    @Override
    public V remove(Object key) 
    {
       if(key == null) throw new NullPointerException("remove(): parámetro null");

       int index = this.search_for_node_index((K)key);
       V old = null;
       Nodo nodo = table[index];
       if(nodo != null && !nodo.getTumba())
       {
           old = table[index].getValue();
           table[index].setTumba(true);
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
     * inicialmente tuvo al ser creado el objeto.
     */
    @Override
    public void clear() 
    {
        this.table =(Nodo[]) new Object[this.initial_capacity];
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
     * Retorna una copia superficial de la tabla. Las listas de desborde o 
     * buckets que conforman la tabla se clonan ellas mismas, pero no se clonan 
     * los objetos que esas listas contienen: en cada bucket de la tabla se 
     * almacenan las direcciones de los mismos objetos que contiene la original. 
     * @return una copia superficial de la tabla.
     * @throws java.lang.CloneNotSupportedException si la clase no implementa la
     *         interface Cloneable.    
     */ 
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        TSBHashTableDA<K, V> t = (TSBHashTableDA<K, V>)super.clone();
        t.table = (Nodo[]) new Object[this.table.length];
        for (int i = table.length ; i-- > 0 ; )
        {
            t.table[i] = (Nodo) table[i].clone();
        }
        t.keySet = null;
        t.entrySet = null;
        t.values = null;
        t.modCount = 0;
        return t;
    }

    /**
     * Determina si esta tabla es igual al objeto espeficicado.
     * @param obj el objeto a comparar con esta tabla.
     * @return true si los objetos son iguales.
     */
    @Override
    public boolean equals(Object obj) 
    {
        if(!(obj instanceof Map)) { return false; }

        Map<K, V> t = (Map<K, V>) obj;
        if(t.size() != this.size()) { return false; }

        try 
        {
            for (Nodo nodo : this.table)
            {
                Map.Entry<K, V> e = nodo.entrada;
                K key = e.getKey();
                V value = e.getValue();
                if(t.get(key) == null) { return false; }
                else 
                {
                    if(!value.equals(t.get(key))) { return false; }
                }
            }
        }
        catch (ClassCastException | NullPointerException e) 
        {
            return false;
        }
        return true;    
    }

    /**
     * Retorna un hash code para la tabla completa.
     * @return un hash code para la tabla.
     */
    @Override
    public int hashCode() // deberia ser la suma de todos los hashcodes...
    {
        int hc = 0;
        for (Nodo nodo : this.table)
        {
            if (!nodo.getTumba()) { hc += nodo.hashCode(); }
        }
        return hc;
    }
    
    /**
     * Devuelve el contenido de la tabla en forma de String.
     * @return una cadena con el contenido completo de la tabla.
     */
    @Override
    public String toString()
    {
        StringBuilder cad = new StringBuilder("{");
        for (Nodo nodo : this.table) {
            cad.append(nodo.toString()).append(", ");
        }
        cad.setLength(cad.length()-1);
        cad.append("}");
        return cad.toString();
    }


    //************************ Métodos específicos de la clase.

    /**
     * Determina si alguna clave de la tabla está asociada al objeto value que
     * entra como parámetro. Equivale a containsValue().
     * @param value el objeto a buscar en la tabla.
     * @return true si alguna clave está asociada efectivamente a ese value.
     */
    public boolean contains(Object value)
    {
        if(value == null) return false;

        for(Nodo nodo : this.table)
        {
            if(value.equals(nodo.getValue())) return true;
        }
        return false;
    }
    
    /**
     * Incrementa el tamaño de la tabla y reorganiza su contenido. Se invoca 
     * automaticamente cuando se detecta que la cantidad de nodos
     * supera a cierto  valor critico dado por (load_factor * table.length). Si el
     * valor de load_factor es 0.5, esto implica que el límite antes de invocar
     * rehash es del 50% de el tamaño de la tabla.
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
        Nodo []temp = (Nodo[]) new Object[new_length]; // temp[] es la nueva table[]

        // notificación fail-fast iterator... la tabla cambió su estructura...
        this.modCount++;  // la idea del incremento del modCount es mostrar que varió su valor, no tanto cuánto vale.
                          // aca se hace modCount++ para avisar que hubo rehash...

        // recorrer el viejo arreglo y redistribuir los objetos que tenia...
        for(int i = 0; i < this.table.length; i++)
        {
            Nodo nodo = table[i];
            if (!nodo.getTumba()) // si no es una tumba, llama al put() para agregarlo a la nueva tabla
            {
                put(nodo.getKey(), nodo.getValue());
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
     */

    private int h(int k)
    {
        return h(k, table.length);
    }
    
    /**
     * Función hash. Toma un objeto key que representa una clave y calcula y 
     * retorna un índice válido para esa clave para entrar en la tabla.     
     */
    private int h(K key)
    {
        return h(key.hashCode(), table.length);
    }
    
    /**
     * Función hash. Toma un objeto key que representa una clave y un tamaño de 
     * tabla t, y calcula y retorna un índice válido para esa clave dedo ese
     * tamaño.     
     */
    private int h(K key, int t)
    {
        return h(key.hashCode(), t);
    }
    
    /**
     * Función hash. Toma una clave entera k y un tamaño de tabla t, y calcula y 
     * retorna un índice válido para esa clave dado ese tamaño.     
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
    private int search_for_node_index(K key)
    {
        int hashMadre = h(key);
        int index, t = -1;
        for (int j = 0; ;j++)
        {
            index = (hashMadre + j^2) % table.length;
            Nodo nodo = table[index];
            if (nodo != null) // el casillero está ocupado.
            {
                if (nodo.getKey() == key) // las keys son las mismas.
                {
                    return index; // retorna el index de ese nodo.
                }
                else // las keys son distintas.
                {
                    if (nodo.getTumba()) // ese nodo es una tumba.
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
     */
    private class Entry<K, V> implements Map.Entry<K, V>
    {
        private K key;
        private V value;

        //****************** Constructor

        public Entry(K key, V value) 
        {
            if(key == null || value == null)
            {
                throw new IllegalArgumentException("Entry(): parámetro null...");
            }
            this.key = key;
            this.value = value;
        }



        //****************** Implementación de métodos especificados por Map.Entry

        @Override
        public K getKey() 
        {
            return key;
        }

        @Override
        public V getValue() 
        {
            return value;
        }

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

        @Override
        public boolean equals(Object obj) 
        {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (this.getClass() != obj.getClass()) { return false; }
            
            final Entry other = (Entry) obj;
            if (!Objects.equals(this.key, other.key)) { return false; }
            if (!Objects.equals(this.value, other.value)) { return false; }            
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;
        }
        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            Entry<K, V> entry = (Entry<K, V>) super.clone();
            entry.key = getKey();
            entry.value = getValue();
            return entry;
        }

        
        @Override // de Object.
        public String toString()
        {
            return "(" + key.toString() + " --> " + value.toString() + ")";
        }
    }

    /**
     * Clase interna que representa los Nodos que se agregan a cada uno
     * de los casilleros del arreglo table. ALmacena un valor de tipo Entry<K, V>
     * y un valor de marca (para tumbas).
     * Si la tumba vale false --> Nodo cerrado.
     *                  true  --> Nodo borrado.
     */
    private class Nodo
    {
        private Entry<K, V> entrada;
        private boolean tumba;

        public Nodo(Entry<K, V> entrada, boolean tumba)
        {
            this.entrada = entrada;
            this.tumba = tumba;
        }

        public Nodo(Entry<K, V> entrada)
        {
            this(entrada, false);
        }

        public K getKey()
        {
            return entrada.getKey();
        }

        public V getValue()
        {
            return entrada.getValue();
        }

        public boolean getTumba() { return this.tumba; }

        public void setTumba(boolean tumba) { this.tumba = tumba; }

        public V setValue(V value) { return entrada.setValue(value); }

        @Override
        public int hashCode() // deberia ser la suma de todos los hashcodes...
        {
            return entrada.hashCode();
        }

        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            Nodo n = (Nodo) super.clone();
            n.entrada = (Entry<K, V>) entrada.clone();
            return n;
        }

        public Entry<K,V> getEntry()
        {
            return entrada;
        }

        @Override
        public String toString()
        {
            if (!this.tumba)
            {
                return entrada.toString();
            }
            return "";
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
        @Override
        public int size()
        {
            return TSBHashTableDA.this.count;
        }
        @Override
        public boolean contains(Object o)
        {
            return TSBHashTableDA.this.containsKey(o);
        }
        @Override
        public boolean remove(Object o)
        {
            return (TSBHashTableDA.this.remove(o) != null);
        }
        @Override
        public void clear()
        {
            TSBHashTableDA.this.clear();
        }


        private class KeySetIterator implements Iterator<K>
        {
            // índice de la lista actualmente recorrida...
            private int current_nodo;

            // índice de la lista anterior (si se requiere en remove())...
            private int last_nodo;

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
                current_nodo = 0;
                last_nodo = 0;
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
                Nodo []t = TSBHashTableDA.this.table;

                if(TSBHashTableDA.this.isEmpty()) { return false; }
                if(current_nodo >= t.length) { return false; }

                int next_nodo = current_nodo + 1;
                for (; next_nodo < t.length -1; next_nodo++)
                {
                    if(t[next_nodo] != null && !t[next_nodo].getTumba()) {return true;}
                }
                return false;
            }

            /*
             * Retorna el siguiente elemento disponible en la tabla.
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
                Nodo t[] = TSBHashTableDA.this.table;

                Nodo nodo;

                // ...recordar el índice del nodo que se va a abandonar..
                last_nodo = current_nodo;

                // buscar el siguiente bucket no vacío, que DEBE existir, ya
                // que se hasNext() retornó true...
                current_nodo++;
                while(t[current_nodo] == null || t[current_nodo].getTumba())
                {
                    current_nodo++;
                }

                // actualizar la referencia bucket con el núevo índice...
                nodo = t[current_nodo];

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar la clave del elemento alcanzado...
                K key = nodo.getKey();
                return key;
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
                V valor = TSBHashTableDA.this.remove(table[current_nodo].getKey());

                // quedar apuntando al anterior al que se retornó...
                if(last_nodo != current_nodo)
                {
                    current_nodo = last_nodo;
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

//        @Override
//        public boolean contains(Object o)
//        {
//            if(o == null) { return false; }
//            if(!(o instanceof Entry)) { return false; }
//
//            Map.Entry<K, V> entry = (Map.Entry<K,V>)o;
//            K key = entry.getKey();
//            int index = TSBHashTableDA.this.h(key);
//
//            TSBArrayList<Map.Entry<K, V>> bucket = TSBHashTableDA.this.table[index];
//            if(bucket.contains(entry)) { return true; }
//            return false;
//        }
//        /*
//         * Elimina de esta vista (y por lo tanto de la tabla) al par que entra
//         * como parámetro (y que debe ser de tipo Entry).
//         */
//        @Override
//        public boolean remove(Object o)
//        {
//            if(o == null) { throw new NullPointerException("remove(): parámetro null");}
//            if(!(o instanceof Entry)) { return false; }
//
//            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
//            K key = entry.getKey();
//            int index = TSBHashTableDA.this.h(key);
//            TSBArrayList<Map.Entry<K, V>> bucket = TSBHashTableDA.this.table[index];
//
//            if(bucket.remove(entry))
//            {
//                TSBHashTableDA.this.count--;
//                TSBHashTableDA.this.modCount++;
//                return true;
//            }
//            return false;
//        }
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
            private int current_nodo;

            // índice de la lista anterior (si se requiere en remove())...
            private int last_nodo;

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
                current_nodo = 0;
                last_nodo = 0;
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
                Nodo []t = TSBHashTableDA.this.table;

                if(TSBHashTableDA.this.isEmpty()) { return false; }
                if(current_nodo >= t.length) { return false; }

                int next_nodo = current_nodo + 1;
                for (; next_nodo < t.length -1; next_nodo++)
                {
                    if(t[next_nodo] != null && !t[next_nodo].getTumba()) {return true;}
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
                Nodo t[] = TSBHashTableDA.this.table;

                Nodo nodo;

                // ...recordar el índice del nodo que se va a abandonar..
                last_nodo = current_nodo;

                // buscar el siguiente bucket no vacío, que DEBE existir, ya
                // que se hasNext() retornó true...
                current_nodo++;
                while(t[current_nodo] == null || t[current_nodo].getTumba())
                {
                    current_nodo++;
                }

                // actualizar la referencia bucket con el núevo índice...
                nodo = t[current_nodo];

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar la clave del elemento alcanzado...
                return nodo.getEntry();
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
                V valor = TSBHashTableDA.this.remove(table[current_nodo].getKey());

                // quedar apuntando al anterior al que se retornó...
                if(last_nodo != current_nodo)
                {
                    current_nodo = last_nodo;
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
            private int current_nodo;

            // índice de la lista anterior (si se requiere en remove())...
            private int last_nodo;

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
                current_nodo = 0;
                last_nodo = 0;
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
                Nodo []t = TSBHashTableDA.this.table;

                if(TSBHashTableDA.this.isEmpty()) { return false; }
                if(current_nodo >= t.length) { return false; }

                int next_nodo = current_nodo + 1;
                for (; next_nodo < t.length -1; next_nodo++)
                {
                    if(t[next_nodo] != null && !t[next_nodo].getTumba()) {return true;}
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
                Nodo t[] = TSBHashTableDA.this.table;

                Nodo nodo;

                // ...recordar el índice del nodo que se va a abandonar..
                last_nodo = current_nodo;

                // buscar el siguiente bucket no vacío, que DEBE existir, ya
                // que se hasNext() retornó true...
                current_nodo++;
                while(t[current_nodo] == null || t[current_nodo].getTumba())
                {
                    current_nodo++;
                }

                // actualizar la referencia bucket con el núevo índice...
                nodo = t[current_nodo];

                // avisar que next() fue invocado con éxito...
                next_ok = true;

                // y retornar la clave del elemento alcanzado...
                V value = nodo.getValue();
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
                V valor = TSBHashTableDA.this.remove(table[current_nodo].getKey());

                // quedar apuntando al anterior al que se retornó...
                if(last_nodo != current_nodo)
                {
                    current_nodo = last_nodo;
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

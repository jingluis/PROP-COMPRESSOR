/**
 * @file ByteArray.java
 */

package Domain;

/**
 * @class ByteArray
 * @brief Estructura de dades ByteArray
 * Estructura de dades que emmagatzema un array de bytes. Proporciona accés sequancial, aleatori i creixement dinàmic. També conté altres mètodes útils addicionals.
 */

class ByteArray
{
    /** @brief Array de bytes base sobre el que treballa l'estructura de dades*/
    private byte[] data;
    /** @brief Posició de l'iterador intern, per a l'accés seqüencial */
    private int position;
    /** @brief Posició fins la qual hi ha dades vàlides emmagatzemades */
    private int limit;

    /**
     * @brief Constructora per defecte
     * \pre true
     * \post S'ha creat l'estructura de dades amb mida 0, el punter intern apunta a la primera posició
     */
    ByteArray()
    {
        data = new byte[4];
        position = 0;
        limit = 0;
    }

    /**
     * @brief Constructora a partir d'un array base
     * \pre true
     * \post S'ha creat l'estructura de dades amb l'array passat com a paràmetre com a base (o un array buit nou, si l'array passat com a paràmetre té mida 0), el punter intern apunta a la primera posició
     * \param d Array de bytes base
     */
    ByteArray(byte[] d)
    {
        if(d.length == 0)
        {
            data = new byte[4];
            position = 0;
            limit = 0;
        }
        else
        {
            data = d;
            position = 0;
            limit = data.length;
            while(data.length < 4) doubleData();
        }
    }

    /**
     * @brief Obtenir la mida
     * \pre true
     * \post Retorna el número de bytes emmagatzemats
     */
    int size()
    {
        return limit;
    }

    /**
     * @brief Obtenir la posició del punter intern
     * \pre true
     * \post Retorna la posició del punter intern
     */
    int position()
    {
        return position;
    }

    /**
     * @brief Definir la posició del punter intern
     * \pre "pos" és una posició valida o la posició següent a l'últim byte vàlid
     * \post Retorna la posició del punter intern
     * \exception ByteArrayException : Si no es compleix la precondició es llança excepció
     * \param pos Nova posició del punter intern
     */
    void position(int pos) throws ByteArrayException
    {
        if(pos < 0 || pos > limit) throw new ByteArrayException("position(int pos) : 'pos' out of bounds"); //it can be placed after the last valid byte
        position = pos;
    }

    /**
     * @brief Obtenir en número de bytes restants per llegir seqüencialment
     * \pre true
     * \post Retorna el número de bytes vàlid que queden per llegir entre la posició del punter intern i el limit
     */
    int remaining()
    {
        return limit-position;
    }

    /**
     * @brief Obtenir seqüencialent el següent byte
     * \pre Queda almenys un byte vàlid per llegir a partir del punter intern
     * \post Retorna el byte al que apunta el punter intern. El punter intern avança una posició
     * \exception ByteArrayException : Si no es compleix la precondició es llança excepció
     */
    byte get() throws ByteArrayException
    {
        if(position == limit) throw new ByteArrayException("get() : no bytes left to read");
        return data[position++];
    }

    /**
     * @brief Obtenir el byte de una certa posició
     * \pre "pos" és una posició valida
     * \post Retorna el byte que està en la posició indicada
     * \exception ByteArrayException : Si no es compleix la precondició es llança excepció
     * \param pos Posició del byte
     */
    byte get(int pos) throws ByteArrayException
    {
        if(pos < 0 || pos >= limit) throw new ByteArrayException("get(int pos) : 'pos' out of bounds");
        return data[pos];
    }

    /**
     * @brief Escriure seqüencialent un byte
     * \pre true
     * \post S'ha escrit el byte a la posició a la que apunta el punter intern. El punter intern avança una posició
     * \param b Byte a escriure
     */
    void put(byte b)
    {
        if(position == data.length) doubleData();
        if(position == limit) limit++;
        data[position++] = b;
    }

    /**
     * @brief Escriure un byte a una certa posició
     * \pre "pos" és una posició valida o la posició següent a l'últim byte vàlid
     * \post S'ha escrit el byte a la posició indicada
     * \exception ByteArrayException : Si no es compleix la precondició es llança excepció
     * \param b Byte a escriure
     * \param pos Posició a escriure
     */
    void put(byte b, int pos) throws ByteArrayException
    {
        if(pos < 0 || pos > limit) throw new ByteArrayException("put(byte b, int pos) : 'pos' out of bounds");
        if(pos == data.length) doubleData();
        if(pos == limit) limit++;
        data[pos] = b;
    }

    /**
     * @brief Obtenir seqüencialent el següent short
     * \pre Queden almenys dos bytes vàlids per llegir a partir del punter intern
     * \post Retorna la interpretació com a short del byte al que apunta el punter intern i el següent. El punter intern avança dues posicions
     * \exception ByteArrayException : Si no es compleix la precondició es llança excepció
     */
    short getShort() throws ByteArrayException
    {
        if(position+2 > limit) throw new ByteArrayException("getShort() : insufficient bytes to read");
        return (short)((data[position++] & 0xFF) << 8 | (data[position++] & 0xFF));
    }

    /**
     * @brief Escriure seqüencialent un short
     * \pre true
     * \post S'ha escrit el parell bytes que formen el short a la posició a la que apunta el punter intern i la següent. El punter intern avança dues posicions
     * \param val Short a escriure
     */
    void putShort(short val)
    {
        if(position+2 > data.length) doubleData();
        if(position+2 > limit) limit = position+2;
        data[position++] = (byte)(val >> 8);
        data[position++] = (byte)(val);
    }

    /**
     * @brief Obtenir seqüencialent el següent int
     * \pre Queden almenys quatre bytes vàlids per llegir a partir del punter intern
     * \post Retorna la interpretació com a int del byte al que apunta el punter intern i el tres següents. El punter intern avança quatre posicions
     * \exception ByteArrayException : Si no es compleix la precondició es llança excepció
     */
    int getInt() throws ByteArrayException
    {
        if(position+4 > limit) throw new ByteArrayException("getInt() : insufficient bytes to read");
        return ((data[position++] & 0xFF) << 24 | (data[position++] & 0xFF) << 16 | (data[position++] & 0xFF) << 8 | (data[position++] & 0xFF));
    }

    /**
     * @brief Escriure seqüencialent un int
     * \pre true
     * \post S'ha escrit els quatre bytes que formen el int a la posició a la que apunta el punter intern i les tres següent. El punter intern avança quatre posicions
     * \param val Int a escriure
     */
    void putInt(int val)
    {
        if(position+4 > data.length) doubleData();
        if(position+4 > limit) limit = position+4;
        data[position++] = (byte)(val >> 24);
        data[position++] = (byte)(val >> 16);
        data[position++] = (byte)(val >> 8);
        data[position++] = (byte)(val);
    }

    /**
     * @brief Convertir i obtenir a array de bytes
     * \pre true
     * \post Si en nombre de bytes vàlids és inferior a la mida de l'array base, retorna un nou array de bytes mínim amb tots els bytes emmagatzemats a l'estructura. Altrament retorna l'array de bytes base.
     */
    byte[] getArray()
    {
        if(limit == data.length)
        {
            return data;
        }
        else
        {
            byte[] arr = new byte[limit];
            System.arraycopy(data, 0, arr, 0, limit);
            return arr;
        }
    }

    /**
     * @brief Transferir bytes de un ByteArray a un altre ByteArray
     * \pre pos1 és una posició vàlida de ba1 o és ‘-1’; pos2 és una posició vàlida de ba2 o és ‘-1’; queden almenys "len" bytes per llegir a partir del punter intern de ba1 si pos1 és ‘-1’, o a partir de la posició pos1 de ba1 altrament
     * \post S’han copiat els "len" bytes de ba1 a partir de la posició a la qual apunta el punter intern, si pos1 és ‘-1’, o altrament a partir de la posició pos1 a ba2, a partir de la posició al que apunta el punter intern, si pos2 és ‘-1’, o altrament partir de la posició pos2.
     * \exception ByteArrayException : Si no es compleix la precondició es llança excepció
     * \param ba1 ByteArray d’origen
     * \param pos1 Posició d’inici de la transferència en ba1. Si és ‘-1’ vol dir a partir del punter intern
     * \param ba1 ByteArray de destí
     * \param pos2 Posició d’inici de la transferència en ba2. Si és ‘-1’ vol dir a partir del punter intern
     * \param len Longitud de la transferència
     */
    static void transfer(ByteArray ba1, int pos1, ByteArray ba2, int pos2, int len) throws ByteArrayException
    {
        boolean mpos1 = false, mpos2 = false;
        if(pos1 == -1) { pos1 = ba1.position; mpos1 = true; }
        if(pos2 == -1) { pos2 = ba2.position; mpos2 = true; }
        if(pos1 < 0 || pos1 >= ba1.limit) throw new ByteArrayException("transfer(ByteArray ba1, int pos1, ByteArray ba2, int pos2, int len) : 'pos1' out of bounds");
        if(pos2 < 0 || pos2 > ba2.limit) throw new ByteArrayException("transfer(ByteArray ba1, int pos1, ByteArray ba2, int pos2, int len) : 'pos2' out of bounds"); //pos2 can be placed after the last valid byte of ba2
        if(pos1+len > ba1.limit) throw new ByteArrayException("transfer(ByteArray ba1, int pos1, ByteArray ba2, int pos2, int len) : insufficient bytes to read");
        while(pos2+len > ba2.data.length) ba2.doubleData();
        if(pos2+len > ba2.limit) ba2.limit = pos2+len;
        System.arraycopy(ba1.data, pos1, ba2.data, pos2, len);
        if(mpos1) ba1.position += len;
        if(mpos2) ba2.position += len;
    }

    /**
     * @brief Convertir i obtenir a array de bytes
     * \pre true
     * \post S'ha assignat com a array base de l'estructura de dades un array de mida el doble que l'anterior en el qual es copien els bytes que hi havia a l'anterior
     */
    private void doubleData()
    {
        byte[] newData = new byte[data.length*2];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;

    }

    /**
     * @class ByteArrayException
     * @brief Excepció llançada per els mètodes de la estructura de dades ByteArray
     */
    static class ByteArrayException extends Exception
    {
        ByteArrayException()
        {
            super();
        }

        ByteArrayException(String msg)
        {
            super(msg);
        }
    }
}

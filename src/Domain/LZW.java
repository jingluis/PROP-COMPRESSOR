/**
 * @file LZW.java
 */
package Domain;
import java.util.HashMap;

/**
 * @class LZW
 * @brief Implementació específica de l'algorisme de compressió LZW
 * És la classe que implementa els mètodes específics de compressió i descompressió heredats de Algorithm per a l'algorisme LZW
 */
class LZW extends Algorithm
{
    /**
     * @brief Constructora
     * \pre true
     * \post S'ha creat una instància de l'algorisme LZW, amb el nom "LZW"
     */
    LZW ()
    {
        super("LZW");
    }

    /**
     * @brief Comprimir un arxiu, implementació específica
     * \pre true
     * \post S'ha comprimit l'array de bytes d'entrada amb l'algorisme LZW. Retorna l'array de bytes que representa el fitxer comprimit
     * \exception ByteArrayException : Si en el procés intern de compressió hi ha algun error relacionat amb l'estructura ByteArray es llança excepció
     * \param input Dades a comprimir
     */
    protected byte[] specificCompress(final byte[] input) throws ByteArray.ByteArrayException
    {
        ByteArray output = new ByteArray();
        ByteArray input1 = new ByteArray(input);
        byte chSymbol;

        Trie t = new Trie();
        t.InitializeTriesASCII();
        int s_u2Code = 256;
        while(input1.remaining() > 0)
        {
            int code = t.Search_Insert(t.GetRootNode(), input1, s_u2Code);
            // Output the code for 'word'
            output.putShort((short)code);
            // Add new word into Trie, if Trie is not full
            if (s_u2Code < Short.MAX_VALUE)
            {
                s_u2Code++;
            }
            else
            {
                t = new Trie();
                t.InitializeTriesASCII();
                output.putShort(Short.MAX_VALUE);
                s_u2Code = 256;
            }
        }
        return output.getArray();
    }

    /**
     * @brief Descomprimir un arxiu, implementació específica
     * \pre true
     * \post S'ha descomprimit l'array de bytes d'entrada amb l'algorisme LZW. Retorna l'array de bytes que representa el fitxer descomprimit
     * \exception ByteArrayException : Si en el procés intern de compressió hi ha algun error relacionat amb l'estructura ByteArray es llança excepció
     * \param input Dades a descomprimir
     * \param originalsize Mida de l'arxiu original sense comprimir
     */
    protected byte[] specificDecompress(final byte[] input, int originalsize) throws ByteArray.ByteArrayException
    {
        HashMap <Integer, byte[]> table = new HashMap<Integer, byte[]>();
        ByteArray output = new ByteArray(new byte[originalsize]);
        if(input.length != 0)
        {
            for (int i = 0; i < 256; i++)
            {
                byte[] ch = new byte[1];
                ch[0] = (byte) i;
                table.put(i, ch);
            }
            ByteArray index = new ByteArray(input);
            int old = index.getShort();
            int n = -1;
            ByteArray s;
            s = new ByteArray(table.get(old).clone());
            byte c = s.get(0);
            output.transfer(s,0,output,-1,s.size());
            int count = 256;
            while(index.remaining() != 0)
            {
                n = index.getShort();
                //code N is not in the hashmap

                if(n != Short.MAX_VALUE)
                {
                    if(!table.containsKey(n))
                    {
                        s = new ByteArray(table.get(old).clone());
                        //S = S+C
                        s.position(s.size());
                        s.put(c);
                    }
                    else
                    {
                        s = new ByteArray(table.get(n).clone());
                    }
                    //Put S to the result output byte[]
                    output.transfer(s,0,output,-1,s.size());
                    c = s.get(0);
                    byte[] aux = new byte[table.get(old).length + 1];
                    System.arraycopy(table.get(old),0,aux,0,table.get(old).length);
                    aux[aux.length - 1] = c;
                    // add OLD + C to the Hashmap
                    table.put(count, aux);
                    count++;
                    old = n;
                }
                else
                {
                    table = new HashMap<Integer, byte[]>();
                    for (int i = 0; i < 256; i++)
                    {
                        byte[] ch = new byte[1];
                        ch[0] = (byte) i;
                        table.put(i, ch);
                    }
                    old = index.getShort();
                    n = -1;
                    s = new ByteArray(table.get(old).clone());
                    c = s.get(0);
                    output.transfer(s,0,output,-1,s.size());
                    count = 256;
                }

            }
        }
        return output.getArray();
    }


}

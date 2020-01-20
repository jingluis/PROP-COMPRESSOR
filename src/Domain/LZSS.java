/**
 * @file LZSS.java
 */

package Domain;

import java.util.*;

/**
 * @class LZSS
 * @brief Implementació específica de l'algorisme de compressió LZSS
 * És la classe que implementa els mètodes específics de compressió i descompressió heredats de Algorithm per a l'algorisme LZSS
 */

class LZSS extends Algorithm
{
    /**
     * @brief Constructora
     * \pre true
     * \post S'ha creat una instància de l'algorisme LZSS, amb el nom "LZSS"
     */
    LZSS()
    {
        super("LZSS");
    }

    /**
     * @brief Comprimir un arxiu, implementació específica
     * \pre true
     * \post S'ha comprimit l'array de bytes d'entrada amb l'algorisme LZSS. Retorna l'array de bytes que representa el fitxer comprimit
     * \exception ByteArrayException : Si en el procés intern de compressió hi ha algun error relacionat amb l'estructura ByteArray es llança excepció
     * \param input Dades a comprimir
     */
    protected byte[] specificCompress(final byte[] input) throws ByteArray.ByteArrayException
    {
        ByteArray in = new ByteArray(input);
        ByteArray out = new ByteArray();
        WindowPositionsHolder wph = new WindowPositionsHolder();

        int remainingflags = 8;
        int flagsposition = 0;
        byte flags = 0x00;
        out.put((byte)0x00);

        int count = 0;
        while(count < 3 && in.remaining() > 0) //first 3 bytes aren't processed, because < min length
        {
            byte b = in.get();
            out.put(b);
            wph.add(b, count);
            //flags <<= 1;
            remainingflags--;
            count++;
        }

        while(in.remaining() > 0)
        {
            if(remainingflags == 0)
            {
                out.put(flags, flagsposition);
                flagsposition = out.position();
                flags = 0x00;
                out.put((byte)0x00);
                remainingflags = 8;
            }

            //PARAMS (16 bits: 12 bits offset + 4 bits length)
            int MAXWINDOWSIZE = 4095;
            int MAXLENGHT = 18; //length never will be < 3 because is not worth replacing

            int window = Math.max(0, in.position()-MAXWINDOWSIZE);
            boolean fw = (window == in.position()-MAXWINDOWSIZE);
            int mlength = Math.min(MAXLENGHT, in.position());

            int[] results = searchCoincidence(in, wph, mlength);

            if(results[1] >= 3) //worth replacing
            {
                int offset = in.position()-results[0];
                int length = results[1];

                short offsetlength = (short)((offset << 4) | ((length-3) & 0x000F)); //encode codepair
                out.putShort(offsetlength);

                in.position(in.position());

                for(int i = 0; i < length; i++)
                {
                    byte b = in.get();
                    wph.add(b, in.position()-1);
                    if(fw) wph.delete(in.get(window+i));
                    else if(in.position()-1-MAXWINDOWSIZE >= 0) wph.delete(in.get(in.position()-1-MAXWINDOWSIZE));
                }

                flags <<= 1;
                flags |= 0b1;
            }
            else //not worth replacing because max is length 2, the length of codeword
            {
                byte b = in.get();
                wph.add(b, in.position()-1);
                if(fw) wph.delete(in.get(window));

                out.put(b);
                flags <<= 1;
            }
            remainingflags--;
        }

        if(remainingflags == 0)
        {
            out.put(flags, flagsposition);
            flagsposition = out.position();
            flags = 0x00;
            out.put((byte)0x00);
            remainingflags = 8;
        }

        flags <<= 1;
        flags |= 0b1;
        out.putShort((short)0x0000); //end condition: offset 0
        remainingflags--;
        while (remainingflags > 0)
        {
            flags <<= 1;
            remainingflags--;
        }
        out.put(flags, flagsposition);

        return out.getArray();
    }

    /**
     * @brief Descomprimir un arxiu, implementació específica
     * \pre true
     * \post S'ha descomprimit l'array de bytes d'entrada amb l'algorisme LZSS. Retorna l'array de bytes que representa el fitxer descomprimit
     * \exception ByteArrayException : Si en el procés intern de compressió hi ha algun error relacionat amb l'estructura ByteArray es llança excepció
     * \param input Dades a descomprimir
     * \param originalsize Mida de l'arxiu original sense comprimir
     */
    protected byte[] specificDecompress(final byte[] input, int originalsize) throws ByteArray.ByteArrayException
    {
        ByteArray in = new ByteArray(input);
        ByteArray out = new ByteArray(new byte[originalsize]);

        int remainingflags = 8;
        byte flags = in.get();

        while (true)
        {
            if ((flags >> 7) == 0) //is byte
            {
                out.put(in.get());
            }
            else //is repetition pair info
            {
                short offsetlength = in.getShort();
                int offset = (offsetlength >> 4) & 0xFFF;
                int length = (offsetlength & 0x000F) + 3; //compress length is length-3

                if (offset == 0) break; //finish cond: offsetlength statement with offset 0

                ByteArray.transfer(out, out.position()-offset, out, -1, length);
            }

            flags <<= 1;
            remainingflags--;
            if (remainingflags == 0) {
                flags = in.get();
                remainingflags = 8;
            }
        }

        return out.getArray();
    }

    /**
     * @brief Trabar la coincidència màxima en la finestra de cerca
     * \pre maxlen >= 0, el punter intern de ba esta en una posició vàlida i les posicions guardades en sw són vàlides per al ByteArray donat (ba)
     * \post S'ha realitzat la cerca. Retorna un array de ints de mida 2: El primer valor indica la posició on comença la coincidècia i el segon valor la llargada d'aquesta.
     * \exception ByteArrayException : Si en el procés hi ha algun error relacionat amb l'estructura ByteArray es llança excepció
     * \param ba ByteArray sobre el que es fa la cerca
     * \param sw WindowPositionsHolder que conte les posicions d'incici del ByteArray passat com a primer paràmetre
     * \param maxlen Llargada màxima de la coincidència que volem trobar
     */
    private static int[] searchCoincidence(ByteArray ba, WindowPositionsHolder sw, int maxlen) throws ByteArray.ByteArrayException
    {
        int[] res = new int[2];
        int fixposition = ba.position();
        int max = Math.min(maxlen, ba.remaining());

        Iterator<Integer> it = sw.iterator(ba.get());
        while(it.hasNext())
        {
            int p = it.next();
            if(fixposition-p <= res[1]) break;
            ba.position(fixposition+1);
            int j = p+1;
            int tmplen = 1;
            while(tmplen < max && j < fixposition && ba.get(j) == ba.get())
            {
                j++;
                tmplen++;
            }
            if(tmplen > res[1])
            {
                res[0] = p;
                res[1] = tmplen;
                if(res[1] == maxlen) break;
            }
        }
        ba.position(fixposition);

        return res;
    }

    /**
     * @class WindowPositionsHolder
     * @brief Estructura de dades per emmagatzemar posicions
     * És una estructura de dades destinada a guardar posicions de caràcters dins la finestra de cerca eficientment. Proporciona mètodes per l'adicció, la eliminació i l'iteració sobre caràcters.
     */
    private static class WindowPositionsHolder
    {
        /** Contenidor base per a emmagatzemar la informació de manera eficient */
        private List<Queue<Integer>> queue = new ArrayList<>();

        /**
         * @brief Constructora
         * \pre true
         * \post S'ha creat una intància de WindowPositionsHolder i s'ha inicialitzat
         */
        WindowPositionsHolder()
        {
            for(int i = 0; i < 256; i++)
            {
                queue.add(new LinkedList<>());
            }
        }

        /**
         * @brief Afegir posició de caràcter
         * \pre true
         * \post S'ha afegit la posició donada per al caràcter donat
         * \param b Caràcter (byte) el qual volem guardar una posició
         * \param pos Posició que volem guardar
         */
        void add(byte b, int pos)
        {
            int index = ((int)b) & 0xFF;
            queue.get(index).add(pos);
        }

        /**
         * @brief Eliminar última posició de caràcter
         * \pre Hi ha almenys una posicó emmagatzemada per al caràcter donat
         * \post S'ha eliminat la primera posició que s'ha afegit per al caràcter donat
         * \param b Caràcter (byte) el qual volem eliminar la primera posició guardada
         */
        void delete(byte b)
        {
            int index = ((int)b) & 0xFF;
            queue.get(index).remove();
        }

        /**
         * @brief Obtenir iterador per a les posicions de caràcter
         * \pre true
         * \post Retorna un iterador a través de totes les posicions guardades del caràcter donat
         * \param b Caràcter (byte) el qual volem obtenir l'iterador de les posicions
         */
        Iterator<Integer> iterator(byte b)
        {
            int index = ((int)b) & 0xFF;
            return queue.get(index).iterator();
        }
    }
}

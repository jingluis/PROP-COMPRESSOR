/**
 * @file Algorithm.java
 */

package Domain;

import Global.*;

/**
 * @class Algorithm
 * @brief Superclasse abstracta base per als diferents algorismes del compressor
 * És la classe base per als algorismes del compressor. Conté les funcions de comprimir i descomprimir, les quals han de ser implementades en les subclasses
 */

abstract class Algorithm
{
    /** @brief Nom de l'algorisme */
    private String name;
    /** @brief Estadístiques globals de l'algorisme */
    private GlobalStatistics statistics;

    /**
     * @brief Constructora
     * \pre true
     * \post S'ha creat una instància d'algorisme amb el nom donat
     * \param name Nom de l'algorisme
     */
    Algorithm(String name)
    {
        this.name = name;
    }

    /**
     * @brief Obtenir el nom
     * \pre true
     * \post Retorna el nom de l'algorisme
     */
    String getName()
    {
        return name;
    }

    /**
     * @brief Definir les estadístiques globals
     * \pre true
     * \post S'ha assignat com a estadístiques globals de l'algorisme les estadístiques donades
     * \param val Short a escriure
     */
    void setStatistics(GlobalStatistics statistics)
    {
        this.statistics = statistics;
    }

    /**
     * @brief Obtenir les estadístiques globals
     * \pre true
     * \post Retorna les estadístiques globals de l'algorisme
     */
    GlobalStatistics getStatistics()
    {
        return statistics;
    }

    /**
     * @brief Comprimir un arxiu
     * \pre true
     * \post S'ha comprimit l'array de bytes d'entrada amb l'algorisme i s'han actualitzat les estadísituques globals de l'algorisme amb les estadístiques de la compressió. Retorna l'array de bytes que representa el fitxer comprimit i les estadístiques locals de la compressió
     * \exception AlgorithmException : Si en el procés intern de compressió hi ha algun error es llança excepció
     * \param input Dades a comprimir
     */
    Pair<byte[], LocalStatistics> compress(final byte[] input) throws AlgorithmException
    {
        try
        {
            long start = System.currentTimeMillis();
            byte[] ret = specificCompress(input);
            long total = System.currentTimeMillis() - start;
            LocalStatistics ls = new LocalStatistics(input.length, ret.length, (double)total/1000.0);
            statistics.addCompressionStatistic(ls);
            return new Pair<>(ret, ls);
        }
        catch(RuntimeException | ByteArray.ByteArrayException e)
        {
            throw new AlgorithmException("Internal error when compressing \n[ " + e.toString() + " ]");
        }
    }

    /**
     * @brief Descomprimir un arxiu
     * \pre Les dades d'entrada representen un fitxer vàlid comprimit
     * \post S'ha descomprimit l'array de bytes d'entrada amb l'algorisme i s'han actualitzat les estadísituques globals de l'algorisme amb les estadístiques de la descompressió. Retorna l'array de bytes que representa el fitxer descomprimit i les estadístiques locals de la descompressió
     * \exception AlgorithmException : Si en el procés intern de descompressió hi ha algun error o no es compleix la precondició es llança excepció
     * \param input Dades a descomprimir
     * \param originalsize Mida de l'arxiu original sense comprimir
     */
    Pair<byte[], LocalStatistics> decompress(final byte[] input, int oiginalsize) throws AlgorithmException
    {
        try
        {
            long start = System.currentTimeMillis();
            byte[] ret = specificDecompress(input, oiginalsize);
            long total = System.currentTimeMillis() - start;
            LocalStatistics ls = new LocalStatistics(ret.length, input.length, (double)total/1000.0);
            statistics.addDecompressionStatistic(ls);
            return new Pair<>(ret, ls);
        }
        catch(RuntimeException | ByteArray.ByteArrayException e)
        {
            throw new AlgorithmException("Internal error when decompressing \n[ " + e.toString() + " ]");
        }
    }

    /**
     * @brief Comprimir un arxiu, implementació específica
     * \pre true
     * \post S'ha comprimit l'array de bytes d'entrada amb l'algorisme. Retorna l'array de bytes que representa el fitxer comprimit
     * \exception ByteArrayException : Si en el procés intern de compressió hi ha algun error relacionat amb l'estructura ByteArray es llança excepció
     * \param input Dades a comprimir
     */
    protected abstract byte[] specificCompress(final byte[] input) throws ByteArray.ByteArrayException;

    /**
     * @brief Descomprimir un arxiu, implementació específica
     * \pre true
     * \post S'ha descomprimit l'array de bytes d'entrada amb l'algorisme. Retorna l'array de bytes que representa el fitxer descomprimit
     * \exception ByteArrayException : Si en el procés intern de compressió hi ha algun error relacionat amb l'estructura ByteArray es llança excepció
     * \param input Dades a descomprimir
     * \param originalsize Mida de l'arxiu original sense comprimir
     */
    protected abstract byte[] specificDecompress(final byte[] input, int oiginalsize) throws ByteArray.ByteArrayException;

    /**
     * @class AlgorithmException
     * @brief Excepció llançada per els mètodes de la classe Algorithm
     */
    static class AlgorithmException extends Exception
    {
        AlgorithmException()
        {
            super();
        }

        AlgorithmException(String msg)
        {
            super(msg);
        }
    }
}

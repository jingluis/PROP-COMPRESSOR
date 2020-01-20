/**
 * @file LocalStatistics.java
 */
package Domain;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

/**
 * @class GlobalStatistics
 * @brief La classe d'estadístiques locals
 * Representa les estadístiques d'una compressió/descompressió  amb un algorisme específic.
 */
class LocalStatistics
{
	/** @brief la mida del fitxer descomprès */
	private int decompressedSize;
	/** @brief la mida del fitxer comprès */
    private int compressedSize;
    /** @brief el temps trigat en fer l'operació */
    private double time; // in seconds

    /**
     * @brief La constructora per defecte
     * \pre true
     * \post S'ha creat una instància de LocalStatistics amb tots atributs de valor 0
     */
    LocalStatistics()
    {
        decompressedSize = compressedSize = 0;
        time = 0.001;
    }

    /**
     * @brief La constructora a partir de informacions donades
     * \pre true
     * \post S'ha creat una instància de LocalStatistics amb this.decompressedSize = decompressedSize, this.compressedSize = compressedSize, this.time = time
     * \param decompressedSize int que representa la mida del fitxer descomprès
     * \param compressedSize int que representa la mida del fitxer comprès
     * \param time double que representa el temps trigat
     */
    LocalStatistics(int decompressedSize, int compressedSize, double time)
    {
        this.decompressedSize = decompressedSize;
        this.compressedSize = compressedSize;
        this.time = Math.max(time, 0.001);
    }

    /**
     * @brief Obtenir la mida del fitxer descomprès
     * \pre true
     * \post Retornar la mida del fitxer descomprès
     */
    int getDecompressedSize()
    {
        return decompressedSize;
    }

    /**
     * @brief Obtenir la mida del fitxer comprès
     * \pre true
     * \post Retornar la mida del fitxer comprès
     */
    int getCompressedSize()
    {
        return compressedSize;
    }

    /**
     * @brief Obtenir el temps trigat de l'operació
     * \pre true
     * \post Retornar el temps trigat de l'operació
     */
    double getTime()
    {
        return time;
    }

    /**
     * @brief Obtenir el ratio de descompressió/compressió
     * \pre true
     * \post Retornar el ratio de descompressió/compressió
     */
    double getRatio()
    {
        if(compressedSize == 0) return 1.0;
        return (double)decompressedSize/compressedSize;
    }

    /**
     * @brief Obtenir la velocitat de l'operació
     * \pre true
     * \post Retornar la velocitat de l'operació
     */
    double getSpeed()
    {
        return (double)decompressedSize/time;
    }

    /**
     * @brief Obtenir tota informació de la classe
     * \pre true
     * \post Retornar un ArrayList de String que conté tots atributs de la classe, ratio i velocitat, passats a String
     */
    ArrayList<String> toStrings()
    {
        ArrayList<String> ret = new ArrayList<>();
        ret.add(String.valueOf(decompressedSize));
        ret.add(String.valueOf(compressedSize));
        ret.add(String.valueOf(time));
        if(compressedSize == 0) ret.add(String.valueOf(1.0));
        else ret.add(String.valueOf((double)decompressedSize/compressedSize));
        ret.add(String.valueOf((double)decompressedSize/time));
        return ret;
    }
}

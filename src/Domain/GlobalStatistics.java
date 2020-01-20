/**
 * @file GlobalStatistics.java
 */
package Domain;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

/**
 * @class GlobalStatistics
 * @brief La classe d'estadístiques globals
 * Representa les estadístiques de compressió/descompressió d'un algorisme específic.
 */
class GlobalStatistics
{
	/** @brief el nombre de compressions que ha fet amb l'algorisme. */
	private int numberCompressions;
	/** @brief el nombre de descompressions que ha fet amb l'algorisme. */
	private int numberDecompressions;
	/** @brief el ratio de compressió mitjà de l'algorisme */
	private double averageCompressionRatio;
	/** @brief la velocitat de compressió mitjà de l'algorisme */
    private double averageCompressionSpeed;
    /** @brief el ratio de descompressió mitjà de l'algorisme */
    private double averageDecompressionRatio;
    /** @brief la velocitat de descompressió mitjà de l'algorisme */
    private double averageDecompressionSpeed;

    
    /**
     * @brief La constructora per defecte
     * \pre true
     * \post S'ha creat una instància de GlobalStatistics amb tots atributs inicialitzats a 0
     */
    GlobalStatistics()
    {
        numberCompressions = numberDecompressions = 0;
        averageCompressionRatio = averageCompressionSpeed = averageDecompressionRatio = averageDecompressionSpeed = 0.0;
    }

    /**
     * @brief La constructora a partir de informacions donades
     * \pre true
     * \post S'ha creat una instància de GlobalStatistics amb atributs inicialitzats pels paràmetres donats
     * \param numberCompressions int que representa el nombre de compressions
     * \param numberDecompressions int que representa el nombre de descompressions
     * \param averageCompressionRatio double que representa el ratio de compressió mitjà
     * \param averageCompressionSpeed double que representa la velocitat de compressió mitjà
     * \param averageDecompressionRatio double que representa el ratio de descompressió mitjà
     * \param averageDecompressionSpeed double que representa la velocitat de descompressió mitjà
     */
    GlobalStatistics(int numberCompressions, int numberDecompressions, double averageCompressionRatio, double averageCompressionSpeed, double averageDecompressionRatio, double averageDecompressionSpeed)
    {
        this.numberCompressions = numberCompressions;
        this.numberDecompressions = numberDecompressions;
        this.averageCompressionRatio = averageCompressionRatio;
        this.averageCompressionSpeed = averageCompressionSpeed;
        this.averageDecompressionRatio = averageDecompressionRatio;
        this.averageDecompressionSpeed = averageDecompressionSpeed;
    }

    /**
     * @brief La constructora a partir d'un ArrayList de String
     * \pre true
     * \post S'ha creat una instància de GlobalStatistics amb atributs inicialitzats pels valors d'Arraylist
     * \exception IndexOutOfBoundsException: es llança aquesta excepció quan l'ArrayList no conté elements suficients per inicialitzar els atributs
     * \exception NumberFormatException: es llança aquesta excepció quan algun String de l'ArrayList no es pot convertir en nombre
     * \param strings ArrayList<String> que conté els valors dels atributs a inicialitzar
     */
    GlobalStatistics(ArrayList<String> strings) throws RuntimeException
    {
        try
        {
            numberCompressions = Integer.parseInt(strings.get(0));
            numberDecompressions = Integer.parseInt(strings.get(1));
            averageCompressionRatio = Double.parseDouble(strings.get(2));
            averageCompressionSpeed = Double.parseDouble(strings.get(3));
            averageDecompressionRatio = Double.parseDouble(strings.get(4));
            averageDecompressionSpeed = Double.parseDouble(strings.get(5));
        }
        catch(IndexOutOfBoundsException | NumberFormatException e)
        {
            numberCompressions = numberDecompressions = 0;
            averageCompressionRatio = averageCompressionSpeed = averageDecompressionRatio = averageDecompressionSpeed = 0.0;
            throw new RuntimeException("Error parsing GlobalStatistics\n{" + e.getMessage() + "}\nAll values set to 0");
        }
    }

    /**
     * @brief Obtenir el nombre de compressions
     * \pre true
     * \post Retorna el nombre de compressions
     */
    int getNumberCompressions()
    {
        return numberCompressions;
    }

    /**
     * @brief Obtenir el nombre de descompressions
     * \pre true
     * \post Retorna el nombre de descompressions
     */
    int getNumberDecompressions()
    {
        return numberDecompressions;
    }

    /**
     * @brief Obtenir el ratio de compressió mitjà
     * \pre true
     * \post Retorna el ratio de compressió mitjà
     */
    double getAverageCompressionRatio()
    {
        return averageCompressionRatio;
    }

    /**
     * @brief Obtenir la velocitat de compressió mitjà
     * \pre true
     * \post Retorna la velocitat de compressió mitjà
     */
    double getAverageCompressionSpeed()
    {
        return averageCompressionSpeed;
    }

    /**
     * @brief Obtenir el ratio de descompressió mitjà
     * \pre true
     * \post Retorna el ratio de descompressió mitjà
     */
    double getAverageDecompressionRatio()
    {
        return averageDecompressionRatio;
    }

    /**
     * @brief Obtenir la velocitat de descompressió mitjà
     * \pre true
     * \post Retorna la velocitat de descompressió mitjà
     */
    double getAverageDecompressionSpeed()
    {
        return averageDecompressionSpeed;
    }

    /*
        s = (V1 + ... + Vn)/n --> AVERAGE OF N VALUES
        We want to add a new value to average:
        s' = s + (Vn+1 - s)/n+1 --> NEW AVERAGE OF N+1 VALUES
    */

    /**
     * @brief Afegir estadística de compressió
     * \pre true
     * \post L'atribut numberCompressions és incrementat a 1, s'actualitza els atributs averageCompressionRatio i averageCompressionSpeed per les mitjanes després d'afegir l'estadística
     * \param statistic Objecte LocalStatistic que representa l'estadística d'una compressió específica
     */
    void addCompressionStatistic(LocalStatistics statistic)
    {
        numberCompressions++;
        averageCompressionRatio += (statistic.getRatio()-averageCompressionRatio)/numberCompressions;
        averageCompressionSpeed += (statistic.getSpeed()-averageCompressionSpeed)/numberCompressions;
    }

    /**
     * @brief Afegir estadística de descompressió
     * \pre true
     * \post L'atribut numberDecompressions és incrementat a 1, s'actualitza els atributs averageDecompressionRatio i averageDecompressionSpeed per les mitjanes després d'afegir l'estadística
     * \param statistic Objecte LocalStatistic que representa l'estadística d'una descompressió específica
     */
    void addDecompressionStatistic(LocalStatistics statistic)
    {
        numberDecompressions++;
        averageDecompressionRatio += (statistic.getRatio()-averageDecompressionRatio)/numberDecompressions;
        averageDecompressionSpeed += (statistic.getSpeed()-averageDecompressionSpeed)/numberDecompressions;
    }

    /**
     * @brief Obtenir tota informació de la classe
     * \pre true
     * \post Retornar un ArrayList de String que conté tots atributs de la classe en forma de String
     */
    ArrayList<String> toStrings()
    {
        ArrayList<String> ret = new ArrayList<>();
        ret.add(String.valueOf(numberCompressions));
        ret.add(String.valueOf(numberDecompressions));
        ret.add(String.valueOf(averageCompressionRatio));
        ret.add(String.valueOf(averageCompressionSpeed));
        ret.add(String.valueOf(averageDecompressionRatio));
        ret.add(String.valueOf(averageDecompressionSpeed));
        return ret;
    }

}

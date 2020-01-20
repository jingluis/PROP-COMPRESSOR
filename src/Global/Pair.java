/**
 * @file Pair.java
 */
package Global;

import java.util.AbstractMap;

/**
 * @class Pair
 * @brief Estructura de dades Pair
 * Estructura de dades que permet definir parelles d'objectes.
 */
public class Pair<F, S> extends AbstractMap.SimpleImmutableEntry<F, S>
{
	/**
     * @brief La constructora a partir de dos objectes
     * \pre true
     * \post S'ha creat una instància de Pair amb this.first = f, this.second = s
     * \param f Objecte de tipus F, primer valor de la parella
     * \param s Objecte de tipus S, segon valor de la parella
     */
	public  Pair(F f, S s)
    {
        super(f, s);
    }

	/**
     * @brief Obtenir el primer valor
     * \pre true
     * \post Retorna el primer valor de la parella.
     */
	public F first()
    {
        return getKey();
    }

	/**
     * @brief Obtenir el segon valor
     * \pre true
     * \post Retorna el segon valor de la parella
     */
	public S second()
    {
        return getValue();
    }

	/**
     * @brief Obtenir la parella en forma de String
     * \pre true
     * \post Retorna la parella en forma de String, tancada per banda i banda amb "[" i "]" i una coma entre els dos valors
     */
	public String toString()
    {
        return "["+getKey()+","+getValue()+"]";
    }
}

/**
 * @file LZ78.java
 */
package Domain;

import java.util.ArrayList;

/**
 * @class LZ78
 * @brief Implementació específica de l'algorisme de compressió LZ78
 * És la classe que implementa els mètodes específics de compressió i descompressió heredats de Algorithm per a l'algorisme LZ78
 */
class LZ78 extends Algorithm
{
	/**
     * @brief Constructora
     * \pre true
     * \post S'ha creat una instància de l'algorisme LZ78, amb el nom "LZ78"
     */
	LZ78()
	{
		super("LZ78");
	}
	
	/**
     * @brief Comprimir un arxiu, implementació específica
     * \pre true
     * \post S'ha comprimit l'array de bytes d'entrada amb l'algorisme LZ78. Retorna l'array de bytes que representa el fitxer comprimit
     * \exception ByteArrayException : Si en el procés intern de compressió hi ha algun error relacionat amb l'estructura ByteArray es llança excepció
     * \param input Dades a comprimir
     */
	protected byte[] specificCompress(final byte[] input) throws ByteArray.ByteArrayException
	{
		Trie.Node dict = new Trie.Node();
		ByteArray ret = new ByteArray();
		int indexdict = 1;
		Trie.Node currentnode = dict;
		int outindex1 = 0;
		for (int i = 0; i < input.length; ++i) {
			Trie.Node findnode = currentnode.SearchChildNode(input[i]); //find current word
			if (findnode == null) {
				//add current word to dictionary
                findnode = currentnode.AddChildNode(input[i],indexdict);
				//add the code of current word to output
                ret.putShort((short)outindex1);
				ret.put(input[i]);
				//empty trie if trie is full
				if (indexdict < Short.MAX_VALUE) {
				    indexdict++;
				} else {
					indexdict = 1;
					dict = new Trie.Node();
					ret.putShort(Short.MAX_VALUE);
				}
				currentnode = dict;
				outindex1 = 0;
			}
			else if (i == input.length-1) {
				//add the code of current word to output
				ret.putShort((short)findnode.GetCode());
				currentnode = findnode;
			}
			else {
				outindex1 = findnode.GetCode();
				currentnode = findnode;
			}
		}
		
		return ret.getArray();
	}
	
	/**
     * @brief Descomprimir un arxiu, implementació específica
     * \pre true
     * \post S'ha descomprimit l'array de bytes d'entrada amb l'algorisme LZ78. Retorna l'array de bytes que representa el fitxer descomprimit
     * \exception ByteArrayException : Si en el procés intern de compressió hi ha algun error relacionat amb l'estructura ByteArray es llança excepció
     * \param input Dades a descomprimir
     * \param originalsize Mida de l'arxiu original sense comprimir
     */
	protected byte[] specificDecompress(final byte[] input, int originalsize) throws ByteArray.ByteArrayException
	{
		ArrayList<ByteArray> dict = new ArrayList<ByteArray>();
		ByteArray ret = new ByteArray(new byte[originalsize]);
		int i = 0;
		int dictindex = 0;
		while (i < input.length) {
			//convert the code to the position of word in dict
			dictindex = (int)((input[i] & 0xFF) << 8 | (input[i+1] & 0xFF));
			if (dictindex >= Short.MAX_VALUE) {
				dict = new ArrayList<ByteArray>();
				dictindex = 0;
				i += 2;
			}
			else {
				if (i+1 != input.length-1) {
					byte c2 = input[i+2];
					if (dictindex == 0) {
						ByteArray tmp = new ByteArray();
						tmp.put(c2);
						//add a character to dict
						dict.add(tmp);
						//add a character to output
						ret.put(c2);
					}
					else {
						ByteArray str = dict.get(dictindex-1);
						ByteArray str2 = new ByteArray(str.getArray());
						str2.put(c2, str2.size());
						//add current word to dict
						dict.add(str2);
						//add current word to output
						ByteArray.transfer(str2, 0, ret, -1, str2.size());
					}
				}
				else {
					if (dictindex != 0) {
						//add current word to output
						ByteArray str = dict.get(dictindex-1);
						ByteArray.transfer(str, 0, ret, -1, str.size());
					}
				}
			dictindex = 0;
			i+=3;
			}
		}

		return ret.getArray();
	}
}

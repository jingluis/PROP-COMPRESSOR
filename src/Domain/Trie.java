/**
 * @file Trie.java
 */
package Domain;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @class Trie
 * @brief Estructura de dades Trie
 * Estructura de dades que emmagatzema un arbre de cerca. Cada node de l'arbre representa una paraula. Permet fer cerca i inserció d'una paraula de manera eficient.
 */
public class Trie {
	/**
	 * @class Node
	 * @brief Node d'estructura de dades Trie
	 * Node del Trie que representa una paraula de forma [paraula que representa el node pare] + [byte del node]. També guarda informació dels seus fills.
	 */
        protected static class Node{
        	/** @brief el caràcter(byte) que el node guarda */
            private byte m_chSymbol;
            /** @brief el codi de la paraula que serveix pels algorismes LZ78 i LZW */
            private int m_u2Code;
            /** @brief Array dels fills del node. S'utilitza ArrayList en lloc de HashMap perquè sabem que un node té com a màxim 256 fills, que és constant i per tant fer busca en ArrayList no és pitjor que HashMap. */
            private ArrayList<Node> m_vpChildren = new ArrayList<Node>();

            /**
             * @brief La constructora per defecte
             * \pre true
             * \post S'ha creat una instància de Node
             */
            Node() {

            }

            /**
             * @brief La constructora a partir d'un byte i un int
             * \pre true
             * \post S'ha creat una instància de Node amb caràcter = chSymbol i codi = u2Code.
             * \param chSymbol byte que representa un caràcter
             * \param u2Code int que representa el codi
             */
            Node(byte chSymbol, int u2Code)
            {
                m_chSymbol = chSymbol;
                m_u2Code   = u2Code;
            }
            
            /**
             * @brief Modificar el caràcter del node
             * \pre true
             * \post S'ha canviat el caràcter del node per chSymbol
             * \param chSymbol byte que representa un caràcter
             */
            public void SetSymbol(byte chSymbol)
            {
                m_chSymbol = chSymbol;
            }
            
            /**
             * @brief Modificar el codi del node
             * \pre true
             * \post S'ha modificat el codi del node per u2Code
             * \param u2Code int que representa el codi
             */
            public void SetCode(int u2Code)
            {
                m_u2Code = u2Code;
            }

            // Public getter
            /**
             * @brief Obtenir el caràcter del node
             * \pre true
             * \post Retorna el caràcter del node
             */
            final byte GetSymbol()
            {
                return m_chSymbol;
            }
            
            /**
             * @brief Obtenir el codi del node
             * \pre true
             * \post Retorna el codi del node
             */
            final int GetCode()
            {
                return m_u2Code;
            }
            
            /**
             * @brief Obtenir els fills del node
             * \pre true
             * \post Retorna un ArrayList dels fills del node
             */
            final ArrayList<Node> GetChildren()
            {
                return m_vpChildren;
            }


            // Add a child node
            /**
             * @brief Afegir un node fill al node
             * \pre true
             * \post S'ha creat una nova instància del Node amb caràcter = chSymbol i codi = u2Code i és afegit al ArrayList dels fills del node i retorna aquest Node
             * \param chSymbol byte que representa un caràcter
             * \param u2Code int que representa el codi
             */
            Node AddChildNode(byte chSymbol, int u2Code)
            {
                Node pChild = new Node(chSymbol, u2Code);
            	m_vpChildren.add(pChild);
            	return pChild;
            }

            //

            // Search for a child node
            /**
             * @brief Cerca d'un node fill
             * \pre true
             * \post Si el node té un fill amb caràcter = chSymbol, retorna aquest, si no retorna null
             * \param chSymbol byte que representa un caràcter
             */
            Node SearchChildNode(byte chSymbol)
            {
                for(int i = 0; i < m_vpChildren.size(); i++)
                {
                    if(m_vpChildren.get(i).GetSymbol() == chSymbol) return m_vpChildren.get(i);
                }
                return null;
            }
        }

        /** @brief Node arrel del Trie */
        private Node m_pRoot;
        
        /**
         * @brief La constructora per defecte
         * \pre true
         * \post S'ha creat una instància del Trie amb arrel una nova instància de Node
         */
        public Trie()
        {
            m_pRoot = new Node();
        }

        // Get a Root Node of Trie data structure
        /**
         * @brief Obtenir node arrel del Trie
         * \pre true
         * \post Retorna el node arrel del Trie
         */
        public final Node  GetRootNode(){
            return m_pRoot;
        }


        //Initialize
        /**
         * @brief Inicialitzar el Trie
         * \pre true
         * \post Inicialitza el Trie amb cada caràcter de l'extended ASCII en forma de byte i el seu codi corresponent, com fills del node arrel
         */
        public void InitializeTriesASCII()
        {
            m_pRoot = new Node();
            for(int i = 0; i < 256; ++i)
            {
                m_pRoot.AddChildNode((byte)i, i);
            }
        }
        //find the largest coincidence of pNode.GetSymbol and the inside Pointer of pszWord,
        //and returns the code of the last nodes that has the same symbol as the inside pointer of pszWord.
        //Useful to find the largest substring starting with a specific byte in LZW.
        /**
         * @brief Cerca del codi d'un substring al Trie
         * \pre true
         * \post Busca el substring més llarg a partir de la posició del punter intern del ByteArray al Trie i retorna el seu codi. El substring és insertat al Trie.
         * \exception ByteArrayException : Si en el procés intern de cerca hi ha algun error relacionat amb l'estructura ByteArray es llança excepció
         * \param pNode Node que representa node de partida de cerca
         * \param pszWord ByteArray que conté el substring a cercar
         * \param u2Code int que representa el codi del substring després de ser insertat al Trie
         */
        public int Search_Insert(Node pNode, ByteArray pszWord, int u2Code) throws ByteArray.ByteArrayException {

            if(pszWord.remaining() == 0)
            {
                return pNode.GetCode();
            }
            byte next = pszWord.get();
            Node pChildNode = pNode.SearchChildNode(next);
            if (pChildNode == null)
            {
                if(u2Code < Short.MAX_VALUE)
                {
                    pNode.AddChildNode(next, u2Code);
                }
                //Move the inside pointer of input1 to the first byte that differs
                //from the previous largest substring found, that is, one position behind.
                pszWord.position(pszWord.position() - 1);
                return pNode.GetCode();
            }
            return Search_Insert(pChildNode,pszWord,u2Code);
        }
    }

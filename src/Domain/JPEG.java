package Domain;

import java.util.*;
import Global.*;

/**
 * @class JPEG
 * @brief Implementació específica de l'algorisme de compressió JPEG
 * És la classe que implementa els mètodes específics de compressió i descompressió heredats de Algorithm per a l'algorisme JPEG
 */

class JPEG extends Algorithm
{

    /**
     * @brief Constructora
     * \pre true
     * \post S'ha creat una instància de l'algorisme JPEG, amb el nom "JPEG"
     */
    JPEG() {
        super("JPEG");
    }

    /**
     * @class compareValuesPQ
     * @brief Comparator per valors de la imatge
     * Es una comparador per orderar la cua de prioritat que s'utilitzarà a la hora de fer la codificació de Huffman
     */
    private static class compareValuesPQ implements Comparator<Pair<Integer, Pair<Integer, Integer>>> {
        public int compare(Pair<Integer, Pair<Integer, Integer>> p1, Pair<Integer, Pair<Integer, Integer>> p2) {
            return Integer.compare(p1.first(), p2.first());
        }
    }

    /**
     * @class compareValuesTreePQ
     * @brief Comparator per els nodes de l'arbre de Huffman
     * Es una comparador per escollir quin node de l'arbre de Huffman té la freqüència més baixa
     */
    private static class compareValuesTreePQ implements Comparator<huffmanNode> {
        public int compare(huffmanNode h1, huffmanNode h2) {
            return Integer.compare(h1.freq, h2.freq);
        }
    }

    /**
     * @brief Declarar matriu de Cosinus
     * \pre true
     * \post S'ha creat una matriu de Cosinus. Retorna aquesta matriu de Cosinus
     */
    private float[][] declareCos() {
        float[][] cos = new float[8][8];
        //CALCULATE MATRIX OF COSINES
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                double aux = Math.cos(((2 * i + 1) * j * Math.PI) / (2 * 8));
                cos[i][j] = (float) aux;
            }
        }
        return cos;
    }

    /**
     * @brief Declarar matriu de Coeficients
     * \pre true
     * \post S'ha creat una matriu de Coeficients. Retorna aquesta matriu de Coeficients
     */
    private float[][] declareCoeff() {
        float[][] coeff = new float[8][8];
        //CALCULATE MATRIX OF COEFFICIENTS
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                float n1, n2;
                n1 = n2 = 1;
                if (i == 0) n1 = 1f / (float) Math.sqrt(2);
                if (j == 0) n2 = 1f / (float) Math.sqrt(2);
                coeff[i][j] = n1 * n2;
            }
        }
        return coeff;
    }

    /**
     * @brief Taula per fer la Quantització Digital
     */
    private double[][] qTable = new double[][]{
            {16, 11, 10, 16, 24, 40, 51, 61},
            {12, 12, 14, 19, 26, 58, 60, 55},
            {14, 13, 16, 24, 40, 57, 69, 56},
            {14, 17, 22, 29, 51, 87, 80, 62},
            {18, 22, 37, 56, 68, 109, 103, 77},
            {24, 35, 55, 64, 81, 104, 113, 92},
            {49, 64, 78, 87, 103, 121, 120, 101},
            {72, 92, 95, 98, 112, 100, 103, 99}
    };

    /**
     * @brief Declarar lista de parelles de valors
     * \pre wImg > 0, hImg > 0, arryList no es buida
     * \post S'ha creat una llista amb totes les parelles (r,s) de cada bloc de la imatge. Retorna aquesta llista de parelles
     * \param wImg Amplada de la imatge
     * \param hImg Alçada de la imatge
     * \param arryList Llista que conte els valors de la matriu amb codificació entròpica
     */
    private ArrayList<Pair>[] makeHuffmanStruct(int wImg, int hImg, List<Integer> arrayList) {

        int numB = (wImg * hImg) / 64;
        ArrayList<Pair>[] huffman = new ArrayList[numB];

        for (int i = 0; i < numB; i++) huffman[i] = new ArrayList<>();

        for (int b = 0; b < (wImg * hImg); b += 64) {
            int countZ = 0;
            for (int i = b; i < b + 64; i++) {
                if (arrayList.get(i) != 0) {
                    int r, s, n;
                    n = arrayList.get(i);
                    r = countZ;
                    countZ = 0;
                    s = (int) (Math.log10(Math.abs(n)) / Math.log10(2.0)) + 1;
                    Pair value = new Pair(r, n);
                    huffman[b / 64].add(value);
                } else if (countZ == 15 && i != b + 63) {
                    int r, s, n;
                    n = arrayList.get(i);
                    r = countZ;
                    countZ = 0;
                    s = 0;
                    Pair value = new Pair(r, n);
                    huffman[b / 64].add(value);
                } else if (i != b + 63) {
                    ++countZ;
                } else {
                    Pair value = new Pair(0, 0);
                    huffman[b / 64].add(value);
                }
            }
        }
        return huffman;
    }

    /**
     * @class huffmanNode
     * @brief Node de l'arbre de Huffman
     * És un node de l'abre de Huffman que conté la freqüència, el valor i quins son els fills d'aquest node
     */
    private static class huffmanNode {
        int freq;
        Pair<Integer, Integer> value;
        huffmanNode left;
        huffmanNode right;
    }

    /**
     * @brief Diccionai de Huffman de la imatge
     */
    private HashMap dictionaryy;

    /**
     * @brief Genera el diccionari de Huffman a partir de l'arbre de Huffman
     * \pre root != null, s != null
     * \post S'ha creat un diccionari de Huffman. Retorna aquest diccionari, representat amb un HashMap
     * \param root Arrel del arbre actual
     * \param s Codificació de Huffman fins el node actual
     */
    private void generateDictionary(huffmanNode root, String s) {
        if (root.left == null && root.right == null && root.value != null) {
            dictionaryy.put(root.value, s);
            return;
        }
        if (root.left != null) generateDictionary(root.left, s + "0");
        if (root.right != null) generateDictionary(root.right, s + "1");
    }

    /**
     * @brief Genera el diccionari de Huffman a partir de les parelles de valor de la imatge
     * \pre PQ not empty
     * \post S'ha creat un diccionari de Huffman. Retorna aquest diccionari, representat amb un HashMap
     * \param PQ Cua de prioritat de les parelles de valors i les frequencies d'aquestes parelles
     */
    private HashMap generateHuffmanTree(PriorityQueue<Pair<Integer, Pair<Integer, Integer>>> PQ) {
        PriorityQueue<huffmanNode> q = new PriorityQueue(new compareValuesTreePQ());
        while (PQ.size() > 0) {
            Pair<Integer, Pair<Integer, Integer>> pair = PQ.poll();
            huffmanNode node = new huffmanNode();
            node.right = null;
            node.left = null;
            node.freq = pair.first();
            node.value = pair.second();
            q.add(node);
        }
        if (q.size() == 1) {
            dictionaryy = new HashMap();
            dictionaryy.put(q.poll().value, "0");
        } else {
            huffmanNode root = null;
            while (q.size() > 1) {
                huffmanNode nodeRight = q.poll();
                huffmanNode nodeLeft = q.poll();
                huffmanNode nodeActual = new huffmanNode();
                nodeActual.freq = nodeLeft.freq + nodeRight.freq;
                nodeActual.value = null;
                nodeActual.left = nodeLeft;
                nodeActual.right = nodeRight;
                root = nodeActual;
                q.add(nodeActual);
            }
            dictionaryy = new HashMap();
            generateDictionary(root, "");
        }
        return dictionaryy;
    }

    /**
     * @brief Reconstruiex l'arbre de Huffman a partir de l'arrel, la parella i el seu codi
     * \pre root != null, code != null, key != null
     * \post S'ha creat un diccionari de Huffman. Retorna aquest diccionari, representat amb un HashMap
     * \param root Arrel del arbre actual
     * \param s Codificació de Huffman fins el node actual
     */
    private huffmanNode rebuildTree(huffmanNode root, String code, Pair key) {
        if (code.charAt(0) == '0') {
            huffmanNode left;
            if (root.left == null) {
                left = new huffmanNode();
                left.value = null;
                left.freq = 0;
            } else left = root.left;
            if (code.length() > 1) root.left = rebuildTree(left, code.substring(1), key);
            else {
                left.value = key;
                root.left = left;
            }
        } else if (code.charAt(0) == '1') {
            huffmanNode right;
            if (root.right == null) {
                right = new huffmanNode();
                right.value = null;
                right.freq = 0;
            } else right = root.right;
            if (code.length() > 1) root.right = rebuildTree(right, code.substring(1), key);
            else {
                right.value = key;
                root.right = right;
            }
        }
        return root;
    }

    /**
     * @brief Reconstruiex l'arbre de Huffman a partir del diccionari de les parelles de valors
     * \pre dictionaryAC not empty
     * \post S'ha creat un arbre que codifica Huffman i a les fulles conte les parelles de valors. Retorna l'arrel d'aquest arbre
     * \param dictionaryAC HashMap amb totes les codificacions de Huffman de la imatge
     */
    private huffmanNode rebuildTreeFromHuffmanCodes(HashMap<Pair<Integer, Integer>, String> dictionaryAC) {
        huffmanNode root = new huffmanNode();
        root.value = null;
        root.freq = 0;
        for (Map.Entry<Pair<Integer, Integer>, String> entry : dictionaryAC.entrySet()) {
            Pair key = entry.getKey();
            String code = entry.getValue();
            root = rebuildTree(root, code, key);
        }
        return root;
    }

    /**
     * @brief Codifica entròpicament la imatge
     * \pre wImg > 0, hImg > 0, dct son 3 matrius de wImg*hImg dimensions
     * \post Es llegeixen les 3 matrius entropicament i s'escriuren en una llista els valors. Es retornen aquestes 3 llistes
     * \param wImg Amplada de imatge
     * \param hImg Alçada de la imatge
     * \ dct Matrius amb els 3 canals després de fer dct a la imatge
     */
    private List<Integer>[] createEntropyCoding(int wImg, int hImg, int[][][] dct) {
        //CREATING ENTROPY CODING // arrayListYUV[] Y -> 0 \ U -> 1 \ V -> 2
        List<Integer>[] arrayListYUV = new ArrayList[3];
        arrayListYUV[0] = new ArrayList<>();
        arrayListYUV[1] = new ArrayList<>();
        arrayListYUV[2] = new ArrayList<>();
        for (int bi = 0; bi < hImg; bi += 8) {
            for (int bj = 0; bj < wImg; bj += 8) {
                boolean end = false;
                int i = 0;
                int j = 0;
                while (!end) {
                    if (i + 1 == 7 && j - 1 == 7) end = true;
                    if (j == 0 && i == 0) {
                        arrayListYUV[0].add(dct[0][bi + i][bj + j]);
                        arrayListYUV[1].add(dct[1][bi + i][bj + j]);
                        arrayListYUV[2].add(dct[2][bi + i][bj + j]);
                        j++;
                    }
                    //DOWN FIRST HALF
                    else if (i - 1 < 0 && j < 8) {
                        i = 0;
                        for (; j >= 0; j--, i++) {
                            arrayListYUV[0].add(dct[0][bi + i][bj + j]);
                            arrayListYUV[1].add(dct[1][bi + i][bj + j]);
                            arrayListYUV[2].add(dct[2][bi + i][bj + j]);
                        }
                    }
                    //UP FIRST HALF
                    else if (j - 1 < 0 && i < 8) {
                        j = 0;
                        for (; i >= 0; i--, j++) {
                            arrayListYUV[0].add(dct[0][bi + i][bj + j]);
                            arrayListYUV[1].add(dct[1][bi + i][bj + j]);
                            arrayListYUV[2].add(dct[2][bi + i][bj + j]);
                        }
                    }
                    //DOWN SECOND HALF
                    else if (i + 1 >= 0 && j + 1 > 7) {
                        j = 7;
                        i += 2;
                        for (; i < 8; j--, i++) {
                            arrayListYUV[0].add(dct[0][bi + i][bj + j]);
                            arrayListYUV[1].add(dct[1][bi + i][bj + j]);
                            arrayListYUV[2].add(dct[2][bi + i][bj + j]);
                        }
                    }
                    //UP SECOND HALF
                    else if (j + 1 >= 0 && i + 1 > 7) {
                        j += 2;
                        i = 7;
                        for (; j < 8; i--, j++) {
                            arrayListYUV[0].add(dct[0][bi + i][bj + j]);
                            arrayListYUV[1].add(dct[1][bi + i][bj + j]);
                            arrayListYUV[2].add(dct[2][bi + i][bj + j]);
                        }
                    }
                }
            }
        }
        return arrayListYUV;
    }

    /**
     * @brief LLegeix el Header de la imatge Original
     * \pre true
     * \post LLegeix el Header de la imatge. Retorna amplada, alçada, pixels d'amplada i alçada fins a multiple de 8, codificació rgb de la imatge i en quin byte de la imatge acaba el header
     * \param data Dades de la imatge
     */
    private int[] readHeader(byte[] data) {
        int[] result = new int[6];
        int itImg = 3;
        //READ REST OF THE HEADER
        boolean endHeader = false;
        for (; itImg < data.length && !endHeader; itImg++) {
            if ((char) data[itImg] == '#') {
                while ((char) data[itImg] != '\n') itImg++;
            } else {
                endHeader = true;
                int aux = itImg;
                String wString, hString, rgbString;
                wString = hString = rgbString = "";
                for (; (char) data[aux] >= '0' && (char) data[aux] <= '9'; aux++) {
                    wString += (char) data[aux];
                }
                aux++;
                for (; (char) data[aux] >= '0' && (char) data[aux] <= '9'; aux++) {
                    hString += (char) data[aux];
                }
                aux++;
                for (; (char) data[aux] >= '0' && (char) data[aux] <= '9'; aux++) {
                    rgbString += (char) data[aux];
                }
                ++aux;
                result[0] = ((Integer.parseInt(wString)) % 8);
                result[1] = ((Integer.parseInt(hString)) % 8);
                result[2] = Integer.parseInt(wString);
                result[3] = Integer.parseInt(hString);
                result[4] = ((Integer.parseInt(rgbString) / 8) * 8);
                itImg = aux - 1;
            }
        }
        result[5] = itImg;
        return result;
    }

    /**
     * @brief LLegeix el Header de la imatge Comprimida
     * \pre true
     * \post LLegeix el Header de la imatge. Retorna amplada, alçada, pixels d'amplada i alçada fins a multiple de 8, codificació rgb de la imatge, en quin byte de la imatge acaba el header i mida dels diccionaris de Huffman
     * \param imageCompressed Dades de la imatge
     */
    private int[] readHeaderDecompress(byte[] imageCompressed) {
        String sizeDicACy = "";
        String sizeDicACu = "";
        String sizeDicACv = "";

        int[] result = new int[9];

        int itImg = 3;
        //READ REST OF THE HEADER
        boolean endHeader = false;
        for (; itImg < imageCompressed.length && !endHeader; itImg++) {
            if ((char) imageCompressed[itImg] == '#') {
                //WILL IMPLEMENT
            } else {
                endHeader = true;
                int aux = itImg;
                String wString, hString, rgbString, wTo8String, hTo8String;
                wString = hString = rgbString = wTo8String = hTo8String = "";
                for (; (char) imageCompressed[aux] >= '0' && (char) imageCompressed[aux] <= '9'; aux++) {
                    wString += (char) imageCompressed[aux];
                }
                aux++;
                for (; (char) imageCompressed[aux] >= '0' && (char) imageCompressed[aux] <= '9'; aux++) {
                    hString += (char) imageCompressed[aux];
                }
                aux++;
                for (; (char) imageCompressed[aux] >= '0' && (char) imageCompressed[aux] <= '9'; aux++) {
                    rgbString += (char) imageCompressed[aux];
                }
                ++aux;
                for (; (char) imageCompressed[aux] >= '0' && (char) imageCompressed[aux] <= '9'; aux++) {
                    hTo8String += (char) imageCompressed[aux];
                }
                ++aux;
                for (; (char) imageCompressed[aux] >= '0' && (char) imageCompressed[aux] <= '9'; aux++) {
                    wTo8String += (char) imageCompressed[aux];
                }
                ++aux;
                for (; (char) imageCompressed[aux] >= '0' && (char) imageCompressed[aux] <= '9'; aux++) {
                    sizeDicACy += (char) imageCompressed[aux];
                }
                ++aux;
                for (; (char) imageCompressed[aux] >= '0' && (char) imageCompressed[aux] <= '9'; aux++) {
                    sizeDicACu += (char) imageCompressed[aux];
                }
                ++aux;
                for (; (char) imageCompressed[aux] >= '0' && (char) imageCompressed[aux] <= '9'; aux++) {
                    sizeDicACv += (char) imageCompressed[aux];
                }
                ++aux;
                result[0] = ((Integer.parseInt(wString)) / 8) * 8;
                result[1] = ((Integer.parseInt(hString) / 8) * 8);
                result[2] = ((Integer.parseInt(rgbString) / 8) * 8);
                result[3] = Integer.parseInt(hTo8String);
                result[4] = Integer.parseInt(wTo8String);
                result[6] = Integer.parseInt(sizeDicACy);
                result[7] = Integer.parseInt(sizeDicACu);
                result[8] = Integer.parseInt(sizeDicACv);
                itImg = aux - 1;
            }
        }
        result[5] = itImg;
        return result;
    }

    /**
     * @brief Converteix les parelles (r,s) a una llista mb tots els valors de la imatge
     * \pre wImg > 0, hImg > 0, dHuffmanAC not empty
     * \post Crea una llista amb tots els valors de la imatge a partir de les parelles (r,s) afegint els zeros necessaris entre números. Retorna aquesta nova llista
     * \param wImg Amplada de imatge
     * \param hImg Alçada de la imatge
     * \param dHuffmanAC Llista amb les parelles de valors de la imatge
     */
    private List<Integer>[] transformPairsToLists(int wImg, int hImg, ArrayList<Pair>[][] dHuffmanAC) {
        //TRANSFORM PAIRS IN LIST WITH ALL THE ELEMENTS // dArrayList[] Y -> 0 \ U -> 1 \ V -> 2
        List<Integer>[] dArrayListYUV = new ArrayList[3];
        dArrayListYUV[0] = new ArrayList<>(wImg * hImg);
        dArrayListYUV[1] = new ArrayList<>(wImg * hImg);
        dArrayListYUV[2] = new ArrayList<>(wImg * hImg);
        //DECLARE
        for (int i = 0; i < (wImg * hImg); i++) {
            dArrayListYUV[0].add(0);
            dArrayListYUV[2].add(0);
            dArrayListYUV[1].add(0);
        }
        //TRANSFORM
        for (int i = 0; i < (wImg * hImg) / 64; i++) {
            for (int yuv = 0; yuv < 3; yuv++) {
                int pointer = i * 64;
                for (int j = 0; j < dHuffmanAC[yuv][i].size(); j++) {
                    Pair value = dHuffmanAC[yuv][i].get(j);
                    int r = (int) value.first();
                    int n = (int) value.second();
                    while (r > 0) {
                        r--;
                        dArrayListYUV[yuv].set(pointer, 0);
                        ++pointer;
                    }
                    dArrayListYUV[yuv].set(pointer, n);
                    ++pointer;
                }
            }
        }
        return dArrayListYUV;
    }

    /**
     * @brief Desfà la codificació entropica
     * \pre wImg > 0, hImg > 0, dArrayListYUV not empty
     * \post Declara tres matrius (per els tres canals) amb els valors de la imatge al desfer la codificació entròpica realizada en la compressio. Retorna aquestes tres matrius
     * \param wImg Amplada de imatge
     * \param hImg Alçada de la imatge
     * \param dArrayListYUV Llista amb els valors de la imatge després de fer la codificació entròpica
     */
    private int[][][] reverseEntropyCoding(int wImg, int hImg, List<Integer>[] dArrayListYUV) {
        //INVERSE ENTROPY CODING // dDct[] Y -> 0 \ U -> 1 \ V -> 2
        int[][][] dDct = new int[3][hImg][wImg];
        int it = 0;
        for (int bi = 0; bi < hImg; bi += 8) {
            for (int bj = 0; bj < wImg; bj += 8) {
                boolean end = false;
                int i = 0;
                int j = 0;
                while (!end) {
                    if (i + 1 == 7 && j - 1 == 7) end = true;
                    if (j == 0 && i == 0) {
                        dDct[0][bi + i][bj + j] = dArrayListYUV[0].get(it);
                        dDct[1][bi + i][bj + j] = dArrayListYUV[1].get(it);
                        dDct[2][bi + i][bj + j] = dArrayListYUV[2].get(it);
                        it++;
                        j++;
                    }
                    //DOWN FIRST HALF
                    else if (i - 1 < 0 && j < 8) {
                        i = 0;
                        for (; j >= 0; j--, i++) {
                            dDct[0][bi + i][bj + j] = dArrayListYUV[0].get(it);
                            dDct[1][bi + i][bj + j] = dArrayListYUV[1].get(it);
                            dDct[2][bi + i][bj + j] = dArrayListYUV[2].get(it);
                            it++;
                        }
                    }
                    //UP FIRST HALF
                    else if (j - 1 < 0 && i < 8) {
                        j = 0;
                        for (; i >= 0; i--, j++) {
                            dDct[0][bi + i][bj + j] = dArrayListYUV[0].get(it);
                            dDct[1][bi + i][bj + j] = dArrayListYUV[1].get(it);
                            dDct[2][bi + i][bj + j] = dArrayListYUV[2].get(it);
                            it++;
                        }
                    }
                    //DOWN SECOND HALF
                    else if (i + 1 >= 0 && j + 1 > 7) {
                        j = 7;
                        i += 2;
                        for (; i < 8; j--, i++) {
                            dDct[0][bi + i][bj + j] = dArrayListYUV[0].get(it);
                            dDct[1][bi + i][bj + j] = dArrayListYUV[1].get(it);
                            dDct[2][bi + i][bj + j] = dArrayListYUV[2].get(it);
                            it++;
                        }
                    }
                    //UP SECOND HALF
                    else if (j + 1 >= 0 && i + 1 > 7) {
                        j += 2;
                        i = 7;
                        for (; j < 8; i--, j++) {
                            dDct[0][bi + i][bj + j] = dArrayListYUV[0].get(it);
                            dDct[1][bi + i][bj + j] = dArrayListYUV[1].get(it);
                            dDct[2][bi + i][bj + j] = dArrayListYUV[2].get(it);
                            it++;
                        }
                    }
                }
            }
        }
        return dDct;
    }

    /**
     * @brief Desfà la DCT
     * \pre wImg > 0, hImg > 0, dDct not empty
     * \post Declara tres matrius (per els tres canals) amb els valors de la imatge al desfer la DCT realizada en la compressió. Retorna aquestes tres matrius
     * \param wImg Amplada de imatge
     * \param hImg Alçada de la imatge
     * \param dDct Matrius dels 3 canals de la imatge després de fer DCT a la imatge
     */
    private int[][][] reverseDCT(int wImg, int hImg, int[][][] dDct) {
        float[][] cos = declareCos();
        float[][] coeff = declareCoeff();

        //DCT INVERSE // dImgYUV[] Y -> 0 \ U -> 1 \ V -> 2
        int[][][] dImgYUV = new int[3][hImg][wImg];
        for (int bi = 0; bi < hImg; bi += 8) {
            for (int bj = 0; bj < wImg; bj += 8) {
                for (int u = bi; u < bi + 8; u++) {
                    for (int v = bj; v < bj + 8; v++) {
                        float sumY = 0f;
                        float sumU = 0f;
                        float sumV = 0f;
                        for (int i = bi; i < bi + 8; i++) {
                            for (int j = bj; j < bj + 8; j++) {
                                sumY += cos[u % 8][i % 8] * cos[v % 8][j % 8] * dDct[0][i][j] * coeff[i % 8][j % 8];
                                sumU += cos[u % 8][i % 8] * cos[v % 8][j % 8] * dDct[1][i][j] * coeff[i % 8][j % 8];
                                sumV += cos[u % 8][i % 8] * cos[v % 8][j % 8] * dDct[2][i][j] * coeff[i % 8][j % 8];
                            }
                        }
                        dImgYUV[0][u][v] = Math.round(1 / (float) Math.sqrt(2 * 8) * sumY) + 128;
                        dImgYUV[1][u][v] = Math.round(1 / (float) Math.sqrt(2 * 8) * sumU) + 128;
                        dImgYUV[2][u][v] = Math.round(1 / (float) Math.sqrt(2 * 8) * sumV) + 128;
                    }
                }
            }
        }
        return dImgYUV;
    }

    /**
     * @brief Comprimir un arxiu, implementació específica
     * \pre true
     * \post S'ha comprimit l'array de bytes d'entrada amb l'algorisme JPEG. Retorna l'array de bytes que representa el fitxer comprimit
     * \param data Dades a comprimir
     */
    public byte[] specificCompress(final byte[] data) {

        int wImg, hImg, rgbImg, wExtra8, hExtra8;

        //READ TYPE OF THE .PPM
        String type = "";
        type += (char) data[0];
        type += (char) data[1];

        int[] header = readHeader(data);
        wExtra8 = header[0];
        hExtra8 = header[1];
        wImg = header[2];
        hImg = header[3];
        rgbImg = header[4];
        int itImg = header[5];

        //READ ALL DATA INFORMATION ABOUT THE PIXELS OF THE IMAGE IN RGB, CONVERT IT TO YUV AND SUBTRACT 128 FROM
        // EVERY VALUE. ADD NEUTRAL PIXELS IF IMAGE IS NOT 8X8 MULTIPLE IN HORIZONTAL OR VERTICAL AXIS AND SAVE HOW
        // MANY PIXELS ADDED IN ORDER TO DELETE THEM IN DECOMPRESSION.
        int addW, addH;
        addW = addH = 0;
        if (wExtra8 != 0) addW = 8 - wExtra8;
        else wExtra8 = 8;
        if (hExtra8 != 0) addH = 8 - hExtra8;
        else hExtra8 = 8;

        int[][][] imgRGB = new int[3][hImg + addH][wImg + addW];
        int[][][] imgYUV = new int[3][hImg + addH][wImg + addW];
        for (int i = 0; i < hImg; i++) {
            for (int j = 0; j < wImg; j++) {
                int value = (int) data[itImg];
                if (value < 0) value += 256;
                imgRGB[0][i][j] = value;
                value = (int) data[itImg + 1];
                if (value < 0) value += 256;
                imgRGB[1][i][j] = value;
                value = (int) data[itImg + 2];
                if (value < 0) value += 256;
                imgRGB[2][i][j] = value;
                itImg += 3;
                float yValue = 0.257f * imgRGB[0][i][j] + 0.504f * imgRGB[1][i][j] + 0.098f * imgRGB[2][i][j] + 16f;
                imgYUV[0][i][j] = Math.round(yValue) - 128;
                float uValue = -0.148f * imgRGB[0][i][j] - 0.291f * imgRGB[1][i][j] + 0.439f * imgRGB[2][i][j] + 128f;
                imgYUV[1][i][j] = Math.round(uValue) - 128;
                float vValue = 0.439f * imgRGB[0][i][j] - 0.368f * imgRGB[1][i][j] - 0.071f * imgRGB[2][i][j] + 128f;
                imgYUV[2][i][j] = Math.round(vValue) - 128;
            }
            for (int iAux = 0; iAux < 8 - wExtra8; iAux++) {
                imgYUV[0][i][wImg + iAux] = 0;
                imgYUV[1][i][wImg + iAux] = 0;
                imgYUV[2][i][wImg + iAux] = 0;
            }
        }
        for (int iAux = 0; iAux < 8 - hExtra8; iAux++) {
            for (int jAux = 0; jAux < wImg + addW; jAux++) {
                imgYUV[0][hImg + iAux][jAux] = 0;
                imgYUV[1][hImg + iAux][jAux] = 0;
                imgYUV[2][hImg + iAux][jAux] = 0;
            }
        }
        wImg += (8 - wExtra8);
        hImg += (8 - hExtra8);

        //DCT OVER THE IMAGE AND QUANTIZATION // dct[] Y -> 0 \ U -> 1 \ V -> 2
        float[][] cos = declareCos();
        float[][] coeff = declareCoeff();
        int[][][] dct = new int[3][hImg][wImg];
        for (int bi = 0; bi < hImg; bi += 8) {
            for (int bj = 0; bj < wImg; bj += 8) {
                for (int u = bi; u < bi + 8; u++) {
                    for (int v = bj; v < bj + 8; v++) {
                        float sumY = 0f;
                        float sumU = 0f;
                        float sumV = 0f;
                        for (int i = bi; i < bi + 8; i++) {
                            for (int j = bj; j < bj + 8; j++) {
                                sumY += cos[i % 8][u % 8] * cos[j % 8][v % 8] * imgYUV[0][i][j];
                                sumU += cos[i % 8][u % 8] * cos[j % 8][v % 8] * imgYUV[1][i][j];
                                sumV += cos[i % 8][u % 8] * cos[j % 8][v % 8] * imgYUV[2][i][j];
                            }
                        }
                        double rY = (double) (Math.round(1 / (float) Math.sqrt(2 * 8) * coeff[u % 8][v % 8] * sumY)) / (double) qTable[u % 8][v % 8];
                        double rU = (double) (Math.round(1 / (float) Math.sqrt(2 * 8) * coeff[u % 8][v % 8] * sumU)) / (double) qTable[u % 8][v % 8];
                        double rV = (double) (Math.round(1 / (float) Math.sqrt(2 * 8) * coeff[u % 8][v % 8] * sumV)) / (double) qTable[u % 8][v % 8];
                        dct[0][u][v] = (int) Math.round(rY);
                        dct[1][u][v] = (int) Math.round(rU);
                        dct[2][u][v] = (int) Math.round(rV);

                    }
                }
            }
        }

        List<Integer>[] arrayListYUV = createEntropyCoding(wImg, hImg, dct);

        // huffmanDC[] and huffmanAC Y -> 0 \ U -> 1 \ V -> 2
        int numB = (wImg * hImg) / 64;
        ArrayList<Pair>[][] huffman = new ArrayList[3][numB];

        //DECLARE AC
        for (int i = 0; i < numB; i++) {
            huffman[0][i] = new ArrayList<>();
            huffman[1][i] = new ArrayList<>();
            huffman[2][i] = new ArrayList<>();
        }

        //MAKING PAIRS LIST FOR Y,U,V
        for (int i = 0; i < 3; i++) huffman[i] = makeHuffmanStruct(wImg, hImg, arrayListYUV[i]);

        //HASHMAP OF FREQUENCIES OF AC PAIRS // freqAC[] Y -> 0 \ U -> 1 \ V -> 2
        HashMap<Pair<Integer, Integer>, Integer>[] freq = new HashMap[3];
        freq[0] = new HashMap<>();
        freq[1] = new HashMap<>();
        freq[2] = new HashMap<>();
        for (int yuv = 0; yuv < 3; yuv++) {
            for (int i = 0; i < huffman[yuv].length; i++) {
                for (int j = 0; j < huffman[yuv][i].size(); j++) {
                    //Y
                    if (freq[yuv].containsKey(huffman[yuv][i].get(j)))
                        freq[yuv].put(huffman[yuv][i].get(j), freq[yuv].get(huffman[yuv][i].get(j)) + 1);
                    else freq[yuv].put(huffman[yuv][i].get(j), 1);
                }
            }
        }

        //PRIORITY QUEUES FOR DC AND AC HUFFMAN
        PriorityQueue<Pair<Integer, Pair<Integer, Integer>>>[] PQ = new PriorityQueue[3];
        PQ[0] = new PriorityQueue<>(new compareValuesPQ());
        PQ[1] = new PriorityQueue<>(new compareValuesPQ());
        PQ[2] = new PriorityQueue<>(new compareValuesPQ());


        //CREATING PRIORITY QUEUE AC VALUES
        for (int yuv = 0; yuv < 3; yuv++) {
            for (Map.Entry<Pair<Integer, Integer>, Integer> entry : freq[yuv].entrySet()) {
                Pair<Integer, Pair<Integer, Integer>> value = new Pair<>(entry.getValue(), entry.getKey());
                PQ[yuv].add(value);
            }
        }

        //GENERATING DICTIONARIES FOR HUFFMAN ENCODING
        //AC
        HashMap<Pair<Integer, Integer>, String>[] dictionary = new HashMap[3];
        dictionary[0] = generateHuffmanTree(PQ[0]);
        dictionary[1] = generateHuffmanTree(PQ[1]);
        dictionary[2] = generateHuffmanTree(PQ[2]);

        //CALCULATING SIZE OF HUFFMAN BITSET // [] Y -> 0 \ U -> 1 \ V -> 2
        int[] sizeBitSet = new int[3];
        int[] sizeDictionary = new int[3];
        for (int yuv = 0; yuv < 3; yuv++) {
            sizeBitSet[yuv] = 0;
            sizeDictionary[yuv] = 0;
            for (Map.Entry<Pair<Integer, Integer>, String> entry : dictionary[yuv].entrySet()) {
                sizeDictionary[yuv] += entry.getValue().length();
                sizeBitSet[yuv] += entry.getValue().length() * freq[yuv].get(entry.getKey());
            }
            sizeDictionary[yuv] += 4 * dictionary[yuv].size();
        }

        int sizeAllDictionaries = sizeDictionary[0] + sizeDictionary[1] + sizeDictionary[2];

        //SAVE BITS IN ARRAY (byteSetAC) WITH HUFFMAN ENCODING
        BitSet bitSet[] = new BitSet[3];
        bitSet[0] = new BitSet(sizeBitSet[0]);
        bitSet[1] = new BitSet(sizeBitSet[1]);
        bitSet[2] = new BitSet(sizeBitSet[2]);
        byte[][] byteSet = new byte[3][];
        for (int yuv = 0; yuv < 3; yuv++) {
            //Y
            int itBitSet = 0;
            for (int i = 0; i < huffman[yuv].length; i++) {
                for (int j = 0; j < huffman[yuv][i].size(); j++) {
                    Pair value = huffman[yuv][i].get(j);
                    String code = dictionary[yuv].get(value);
                    for (int iCode = 0; iCode < code.length(); iCode++) {
                        if (code.charAt(iCode) == '1') bitSet[yuv].set(itBitSet);
                        ++itBitSet;
                    }
                }
            }
            byteSet[yuv] = new byte[(sizeBitSet[yuv] + 7) / 8];
            for (int i = 0; i < sizeBitSet[yuv]; i++) {
                if (bitSet[yuv].get(i)) {
                    byteSet[yuv][i / 8] |= 1 << (7 - i % 8);
                }
            }
        }

        int sizeSets = byteSet[0].length + byteSet[1].length + byteSet[2].length;

        //SAVE ALL DATA IN ARRAY TO RETURN IMAGE COMPRESSED
        String headerImage = type + "\n" + wImg + "\n" + hImg + "\n" + rgbImg + "\n" + hExtra8 + "\n" + wExtra8 + "\n" +
                sizeDictionary[0] + "\n" + sizeDictionary[1] + "\n" + sizeDictionary[2] + "\n";
        int sizeImageCompressed = headerImage.length() + 3 + sizeAllDictionaries + sizeSets;
        byte[] imageCompressed = new byte[sizeImageCompressed];
        int p = 0;
        //SAVE HEADER
        for (; p < headerImage.length(); p++) {
            imageCompressed[p] = (byte) headerImage.charAt(p);
        }

        //SAVE AC DICTIONARIES
        for (int yuv = 0; yuv < 3; yuv++) {
            for (Map.Entry<Pair<Integer, Integer>, String> entry : dictionary[yuv].entrySet()) {
                imageCompressed[p] = entry.getKey().first().byteValue();
                p++;
                imageCompressed[p] = entry.getKey().second().byteValue();
                p++;
                imageCompressed[p] = (byte) ' ';
                p++;
                for (int i = 0; i < entry.getValue().length(); p++, i++) {
                    imageCompressed[p] = (byte) entry.getValue().charAt(i);
                }
                imageCompressed[p] = ' ';
                p++;
            }
        }

        //THEN SAVE AC's
        for (int i = 0; i < byteSet[0].length; i++, p++) imageCompressed[p] = byteSet[0][i];
        for (int i = 0; i < byteSet[1].length; i++, p++) imageCompressed[p] = byteSet[1][i];
        for (int i = 0; i < byteSet[2].length; i++, p++) imageCompressed[p] = byteSet[2][i];

        return imageCompressed;
    }

    /**
     * @brief Descomprimir un arxiu, implementació específica
     * \pre true
     * \post S'ha descomprimit l'array de bytes d'entrada amb l'algorisme JEPG. Retorna l'array de bytes que representa el fitxer descomprimit
     * \param imageCompressed Dades a descomprimir
     * \param originalsize Parametre utilitzat per els altres algorsimes
     */
    public byte[] specificDecompress(final byte[] imageCompressed, int originalsize) {

        //READ TYPE OF THE .PPM
        String type = "";
        type += (char) imageCompressed[0];
        type += (char) imageCompressed[1];

        int[] header = readHeaderDecompress(imageCompressed);
        int wImg, hImg, rgbImg, hTo8, wTo8;
        wImg = header[0];
        hImg = header[1];
        rgbImg = header[2];
        hTo8 = header[3];
        wTo8 = header[4];
        int itImg = header[5];

        int sizeHeader = itImg;
        int numB = (wImg * hImg) / 64;
        int p = sizeHeader;

        int[] sizeDictAC = new int[3]; // sizeDictAC[] Y -> 0 \ U -> 1 \ V -> 2
        sizeDictAC[0] = header[6];
        sizeDictAC[1] = header[7];
        sizeDictAC[2] = header[8];

        HashMap<Pair<Integer, Integer>, String>[] dictionaryAC = new HashMap[3];
        dictionaryAC[0] = new HashMap<>();
        dictionaryAC[1] = new HashMap<>();
        dictionaryAC[2] = new HashMap<>();
        for (int yuv = 0; yuv < 3; yuv++) {
            int max = sizeDictAC[yuv] + p;
            while (p < max) {
                int keyFirst = (int) imageCompressed[p];
                p++;
                int keySecond = (int) imageCompressed[p];
                p += 2;
                String value = "";
                while ((char) imageCompressed[p] == '0' || (char) imageCompressed[p] == '1') {
                    value += (char) imageCompressed[p];
                    ++p;
                }
                dictionaryAC[yuv].put(new Pair<Integer, Integer>(keyFirst, keySecond), value);
                p++;
            }
        }

        huffmanNode[] rootAC = new huffmanNode[3];
        rootAC[0] = rebuildTreeFromHuffmanCodes(dictionaryAC[0]);
        rootAC[1] = rebuildTreeFromHuffmanCodes(dictionaryAC[1]);
        rootAC[2] = rebuildTreeFromHuffmanCodes(dictionaryAC[2]);
        huffmanNode[] nodeAC = new huffmanNode[3];
        nodeAC[0] = rootAC[0];
        nodeAC[1] = rootAC[1];
        nodeAC[2] = rootAC[2];

        //READ IMAGE INFORMATION
        ArrayList<Pair>[][] dHuffmanAC = new ArrayList[3][numB];
        for (int i = 0; i < numB; i++) {
            dHuffmanAC[0][i] = new ArrayList<>();
            dHuffmanAC[1][i] = new ArrayList<>();
            dHuffmanAC[2][i] = new ArrayList<>();
        }

        //READ AC VALUES
        for (int yuv = 0; yuv < 3; yuv++) {
            int iAux = 0;
            while (iAux < numB) {
                int b = imageCompressed[p];
                int mask = 128;
                for (int j = 0; j < 8 && iAux < numB; j++) {
                    int value = b & mask;
                    if (value == 0) {
                        if (nodeAC[yuv].value == null) nodeAC[yuv] = nodeAC[yuv].left;
                        else {
                            dHuffmanAC[yuv][iAux].add(nodeAC[yuv].value);
                            if (nodeAC[yuv].value.first() == 0 && nodeAC[yuv].value.second() == 0) ++iAux;
                            nodeAC[yuv] = rootAC[yuv].left;
                            if (j == 0 && iAux == numB) p--;
                        }
                    } else {
                        if (nodeAC[yuv].value == null) nodeAC[yuv] = nodeAC[yuv].right;
                        else {
                            dHuffmanAC[yuv][iAux].add(nodeAC[yuv].value);
                            if (nodeAC[yuv].value.first() == 0 && nodeAC[yuv].value.second() == 0) ++iAux;
                            nodeAC[yuv] = rootAC[yuv].right;
                            if (j == 0 && iAux == numB) p--;
                        }
                    }
                    b = b << 1;
                }
                p++;

                if (yuv == 2 && p == imageCompressed.length) --p;
            }
        }

        List<Integer>[] dArrayListYUV = transformPairsToLists(wImg, hImg, dHuffmanAC);
        int[][][] dDct = reverseEntropyCoding(wImg, hImg, dArrayListYUV);

        //QUANTIZATION INVERSE
        for (int i = 0; i < hImg; i++) {
            for (int j = 0; j < wImg; j++) {
                dDct[0][i][j] = dDct[0][i][j] * (int)qTable[i % 8][j % 8];
                dDct[1][i][j] = dDct[1][i][j] * (int)qTable[i % 8][j % 8];
                dDct[2][i][j] = dDct[2][i][j] * (int)qTable[i % 8][j % 8];
            }
        }
        int[][][] dImgYUV = reverseDCT(wImg, hImg, dDct);

        //RGB FORM // dImgRGB[] Y -> 0 \ U -> 1 \ V -> 2
        int[][][] dImgRGB = new int[3][hImg][wImg];
        for (int i = 0; i < hImg; i++) {
            for (int j = 0; j < wImg; j++) {
                dImgRGB[0][i][j] = (int) (1.164 * (dImgYUV[0][i][j] - 16f) + 2.018 * (dImgYUV[1][i][j] - 128f));
                dImgRGB[1][i][j] = (int) (1.164 * (dImgYUV[0][i][j] - 16) - 0.813 * (dImgYUV[2][i][j] - 128) - 0.391 * (dImgYUV[1][i][j] - 128));
                dImgRGB[2][i][j] = (int) (1.164 * (dImgYUV[0][i][j] - 16) + 1.596 * (dImgYUV[2][i][j] - 128));
                if (dImgRGB[2][i][j] < 0) dImgRGB[2][i][j] = 0;
                else if (dImgRGB[2][i][j] >= 256) dImgRGB[2][i][j] = 255;
                if (dImgRGB[1][i][j] < 0) dImgRGB[1][i][j] = 0;
                else if (dImgRGB[1][i][j] >= 256) dImgRGB[1][i][j] = 255;
                if (dImgRGB[0][i][j] < 0) dImgRGB[0][i][j] = 0;
                else if (dImgRGB[0][i][j] >= 256) dImgRGB[0][i][j] = 255;
            }
        }

        //WRITE BYTES AND RETURN
        if (hTo8 != 0) hImg = hImg - 8 + hTo8;
        if (wTo8 != 0) wImg = wImg - 8 + wTo8;
        String finalImage = type + "\n" + wImg + "\n" + hImg + "\n" + rgbImg + "\n";
        byte[] imageDC = new byte[finalImage.length() + wImg * hImg * 3];
        int point = 0;
        for (; point < finalImage.length(); point++) {
            imageDC[point] = (byte) finalImage.charAt(point);
        }
        for (int i = 0; i < hImg; i++) {
            for (int j = 0; j < wImg; j++) {
                imageDC[point] = (byte) dImgRGB[2][i][j];
                ++point;
                imageDC[point] = (byte) dImgRGB[1][i][j];
                ++point;
                imageDC[point] = (byte) dImgRGB[0][i][j];
                ++point;
            }
        }
        return imageDC;
    }

}


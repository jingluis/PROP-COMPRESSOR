/**
 * @file DomainController.java
 */

package Domain;

import Data.DataController;
import Global.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @class DomainController
 * @brief Controlador de la capa de domini
 * Conté totes les funcions i mètodes que implementen els casos d'ús
 */

public class DomainController
{
    /** @brief Número d'algorismes implementats */
    private final int numAlgorithms = 4;
    /** @brief Instàncies dels algorismes */
    private Algorithm[] algorithms = new Algorithm[numAlgorithms];

    /**
     * @brief Constructora
     * \pre true
     * \post S'ha creat una instància del controllador de domini, s'ha creat una instància de cada algorisme
     */
    public DomainController()
    {
        algorithms[0] = new LZ78();
        algorithms[1] = new LZSS();
        algorithms[2] = new LZW();
        algorithms[3] = new JPEG();
    }

    /**
     * @brief Inicialitzar l'aplicació
     * \pre Existeix el fitxer on es guarden les estadístiques i és vàlid
     * \post Carrega les estadístiques globals dels algorismes guardades disc
     * \exception DomainControllerException : Si no es compleix la precondició es llança excepció
     */
    public void initialize() throws DomainControllerException
    {
        try
        {
            ArrayList<String> algs = new ArrayList<>();
            for(int i = 0; i < numAlgorithms; i++) algs.add(algorithms[i].getName());

            ArrayList<ArrayList<String>> stats = DataController.getStatistics(algs);

            boolean anyError = false;

            for(int i = 0; i < numAlgorithms; i++)
            {
                if(stats.get(i) == null) algorithms[i].setStatistics(new GlobalStatistics());
                else
                {
                    try
                    {
                        algorithms[i].setStatistics(new GlobalStatistics(stats.get(i)));
                    }
                    catch (RuntimeException e)
                    {
                        anyError = true;
                        algorithms[i].setStatistics(new GlobalStatistics());
                    }
                }
            }

            if(anyError) throw new DomainControllerException("Error loading some statistics. Set the wrong statistics to 0");
        }
        catch(DataController.DataControllerException e)
        {
            for(int i = 0; i < numAlgorithms; i++)
            {
                algorithms[i].setStatistics(new GlobalStatistics());
            }
            throw new DomainControllerException("Error loading statistics: {\n" + e.getMessage() + "\n} All statistics set to 0"); //????
        }
    }

    /**
     * @brief Finalitzar l'aplicació
     * \pre S'ha inicialitzat l'aplicació anteriorment
     * \post Guarda les estadístiques globals dels algorismes a disc
     * \exception DomainControllerException : Si hi ha algun problema en guardar les estadístiques llança excepció
     */
    public void finalize() throws DomainControllerException
    {
        try
        {
            ArrayList<Pair<String,ArrayList<String>>> data = new ArrayList<>();
            for(int i = 0; i < numAlgorithms; i++)
            {
                data.add(new Pair<>(algorithms[i].getName(), algorithms[i].getStatistics().toStrings()));
            }
            DataController.setStatistics(data);
        }
        catch(DataController.DataControllerException e)
        {
            throw new DomainControllerException("Error saving statistics: {\n" + e.getMessage() + "\n}");
        }
    }

    /**
     * @brief Obtenir l'instància d'algorisme a través del seu nom
     * \pre true
     * \post Retorna l'instància de l'algorisme identificat per el nom donat, si no existeix retorna null
     * \param name Nom de l'algorisme
     */
    private Algorithm getAlgorithmByName(String name)
    {
        for(int i = 0; i < algorithms.length; i++)
        {
            if(algorithms[i].getName().equals(name)) return algorithms[i];
        }
        return null;
    }

    /**
     * @brief Obtenir els algorismes disponibles per un cert arxiu
     * \pre true
     * \post Retorna els noms dels algorismes disponibles per a l'arxiu donat
     * \param file Path de l'arxiu
     */
    public ArrayList<String> getAlgorithmsFor(Path file)
    {
        String name = file.getFileName().toString();
        ArrayList<String> algs = new ArrayList<>();
        String ext = name.substring(name.lastIndexOf('.')+1);
        switch(ext)
        {
            case "ppm":
            {
                algs.add("JPEG");
                algs.add("LZSS");
                algs.add("LZ78");
                algs.add("LZW");
            }
                break;
            case "txt":
            default:
            {
                algs.add("LZSS");
                algs.add("LZ78");
                algs.add("LZW");
            }
                break;
        }
        return algs;
    }

    /**
     * @brief Comprimir un arxiu amb un cert algorisme
     * \pre "input" és un Path vàlid d'un arxiu .txt o .ppm, ha d'existir un algorisme amb el nom donat, "output" és un Path vàlid d'un arxiu existent o no
     * \post S'ha comprimit l'arxiu del path d'entrada amb l'algorisme amb el nom donat i s'ha guardat al path de sortida donat. S'ha actualitzat les estadístiques globals de l'algorisme i s'ha afegit una entrada a l'historial. Retorna les estadístiques locals de la compressió
     * \exception DomainControllerException : Si hi ha algun problema per llegir el fitxer a comprimir, en guardar el fitxer comprimit, un problema intern del procés de compressió o no es compleix la precondició es llança excepció
     * \param input Path del fitxer a comprimir
     * \param algorithm Nom de l'algorisme
     * \param output Path de sortida
     */
    public ArrayList<String> compressFile(Path input, String algorithm, Path output) throws DomainControllerException
    {
        if(!DataController.isAccesible(output.getParent())) throw new DomainControllerException("Destination not accessible");

        Algorithm alg = getAlgorithmByName(algorithm);
        if(alg == null) throw new DomainControllerException("Algorithm not found");

        try
        {
            byte[] in = DataController.readFile(input);
            Pair<byte[], LocalStatistics> out = alg.compress(in);
            Header h = Header.fileHeader(out.first().length, input.getFileName().toString(), in.length, algorithm);
            DataController.writeFile(output, h.encode());
            DataController.appendToFile(output, out.first());
            DataController.addToHistory(encodeHistoryEntry("File compression", algorithm, Calendar.getInstance().getTime(), out.second()));
            return out.second().toStrings();
        }
        catch(Algorithm.AlgorithmException | DataController.DataControllerException e)
        {
            throw new DomainControllerException("Error in compressFile: {\n" + e.getMessage() + "\n}");
        }
    }

    /**
     * @brief Comprimir una carpeta amb un cert algorisme per els fitxers .txt i un altre pels fitxers .ppm
     * \pre "input" és un Path vàlid d'una carpeta, la carpeta no pot contenir fitxers que no siguin .txt o .ppm, han d'existir els algorismes que corresponen als nom donats, "output" és un Path vàlid d'un arxiu existent o no
     * \post S'ha comprimit tots els fitxers i subcarpetes dins la carpeta del path d'entrada amb l'algorisme per els fitxers txt i l'algorisme per els fitxers ppm donats i s'ha guardat la carpeta comprimida al path de sortida donat. S'han actualitzat les estadístiques globals dels algorismes i s'ha afegit una entrada a l'historial.  Retorna les estadístiques locals de la compressió
     * \exception DomainControllerException : Si hi ha algun problema en llegir el fitxers a comprimir, en guardar el fitxer comprimit, un problema intern del procés de compressió o no es compleix la precondició es llança excepció
     * \param input Path de la carpeta a comprimir
     * \param algorithmTXT Nom de l'algorisme per als fitxers .txt
     * \param algorithmPPM Nom de l'algorisme per als fitxers .ppm
     * \param output Path de sortida
     */
    public ArrayList<String> compressFolder(Path input, String algorithmTXT, String algorithmPPM, Path output) throws DomainControllerException
    {
        if(!DataController.isAccesible(output.getParent())) throw new DomainControllerException("Destination not accessible");

        Algorithm algTXT = getAlgorithmByName(algorithmTXT);
        if(algTXT == null) throw new DomainControllerException("Algorithm not found");

        Algorithm algPPM = getAlgorithmByName(algorithmPPM);
        if(algPPM == null) throw new DomainControllerException("Algorithm not found");

        try
        {
            byte[] dH = Header.dummyFolderHeader(input.getFileName().toString());
            DataController.writeFile(output, dH);
            LocalStatistics res = compressFolderRecursive(dH.length, input, algTXT, algPPM, output);
            Header h = Header.folderHeader(res.getCompressedSize(), input.getFileName().toString());
            DataController.writeFileAt(output, 0, h.encode());
            LocalStatistics ls = new LocalStatistics(res.getDecompressedSize(), res.getCompressedSize()+dH.length, res.getTime());
            DataController.addToHistory(encodeHistoryEntry("Folder compression", algorithmTXT+'/'+algorithmPPM, Calendar.getInstance().getTime(), ls));
            return ls.toStrings();
        }
        catch(Algorithm.AlgorithmException | DataController.DataControllerException e)
        {
            throw new DomainControllerException("Error in compressFolder: {\n" + e.getMessage() + "\n}");
        }
    }

    /**
     * @brief Algorisme recursiu de compressió de carpetes
     * \pre "currentPosition" >= 0, "folder" és un Path vàlid d'una carpeta, la carpeta no pot contenir fitxers que no siguin .txt o .ppm, "output" és un Path vàlid d'un arxiu existent
     * \post S'ha comprimit els arxius i subcarpetes que es troben dins la carpeta donada amb els algorismes donats per als fitxers .txt o .ppm i s'han guardat al fitxer de sortida a partir de la posició donada. Retorna les estadístiques locals de la compressió
     * \exception DataControllerException : Si hi ha algun problema en llegir el fitxers a comprimir, en guardar en contingut al fitxer comprimit o no es compleix la precondició llança excepció
     * \exception AlgorithmException : Si hi ha algun problema intern en el procés de compressió es llança excepció
     * \exception DomainControllerException : Si no es compleix la precondició es llança excepció
     * \param currentPosition Posició (en bytes) dins l'arxiu de sortida a partir de la qual guardar la subcarpeta comprimida
     * \param input Path de la subcarpeta a comprimir
     * \param algorithmTXT Algorisme per als fitxers .txt
     * \param algorithmPPM Algorisme per als fitxers .ppm
     * \param output Path de sortida
     */
    private LocalStatistics compressFolderRecursive(int currentPosition, Path folder, Algorithm algTXT, Algorithm algPPM, Path output) throws DataController.DataControllerException, Algorithm.AlgorithmException, DomainControllerException
    {
        int size = 0;
        double time = 0.0;
        int originalsize = 0;

        Pair<Path,Boolean>[] elements = DataController.getFolderContent(folder);

        for(int i = 0; i < elements.length; i++)
        {
            if(elements[i].second()) //it's a folder
            {
                byte[] dH = Header.dummyFolderHeader(elements[i].first().toString());
                DataController.appendToFile(output, dH);
                LocalStatistics res = compressFolderRecursive(currentPosition+size+dH.length, folder.resolve(elements[i].first()), algTXT, algPPM, output);
                Header h = Header.folderHeader(res.getCompressedSize(), elements[i].first().toString());
                DataController.writeFileAt(output, currentPosition+size, h.encode());
                size += dH.length + res.getCompressedSize();
                time += res.getTime();
                originalsize += res.getDecompressedSize();
            }
            else
            {
                byte[] in = DataController.readFile(folder.resolve(elements[i].first()));
                String ext = elements[i].first().toString();
                ext = ext.substring(ext.lastIndexOf('.')+1);
                Pair<byte[], LocalStatistics> out;
                if(ext.equals("txt")) out = algTXT.compress(in);
                else if(ext.equals("ppm")) out = algPPM.compress(in);
                else throw new DomainControllerException("Not permitted file < " + elements[i].first().toString() + " >");
                Header h = Header.fileHeader(out.first().length, elements[i].first().toString(), in.length, (ext.equals("txt") ? algTXT.getName() : algPPM.getName()));
                DataController.appendToFile(output, h.encode());
                DataController.appendToFile(output, out.first());
                size += h.size() + out.first().length;
                time += out.second().getTime();
                originalsize += in.length;
            }
        }

        return new LocalStatistics(originalsize, size, time);
    }

    /**
     * @brief Descomprimir un fitxer comprimit
     * \pre "input" és un Path d'un fitxer comprimit i aquest és vàlid, "output" és un Path vàlid d'una carpeta
     * \post Si el fitxer d'entrada és un arxiu comprimit: s'ha descomprimit l'arxiu amb l'algorisme que es va utilitzar en la compressió i s'ha guardat a la ruta donada; si és una carpeta comprimida: s'ha descomprimit tots els arxius i subcarpetes de la carpeta amb els algorismes utilitzats per comprimir-los i d'han guardat en la mateixa estructura i noms a la ruta donada. S'han actualitzat les estadístiques globals dels algorismes i s'ha afegit una entrada a l'historial. Retorna les estadístiques locals de la descompressió
     * \exception DataControllerException : Si hi ha algun problema en llegir el fitxer a descomprimir, en guardar el(s) fitxer(s) descomprimit(s), un problema intern del procés de descompressió o no es compleix la precondició es llança excepció
     * \param input Path del fitxer comprimit
     * \param output Path de sortida de la descompressió
     */
    public ArrayList<String> decompress(Path input, Path output) throws DomainControllerException
    {
        if(!DataController.isAccesible(output)) throw new DomainControllerException("Destination not accessible");

        try
        {
            byte[] in = DataController.readFile(input);
            Header h = Header.decode(in, 0);
            if(h == null) throw new DomainControllerException("Bad header format");

            if(h.getType() == Header.Type.file) // it's a compressed file
            {
                Algorithm alg = getAlgorithmByName(h.getAlgorithm());
                if(alg == null) throw new DomainControllerException("Algorithm not found");

                byte[] data = new byte[h.getSize()];
                System.arraycopy(in, h.size(), data, 0, h.getSize());
                Pair<byte[], LocalStatistics> out = alg.decompress(data, h.getOriginalsize());
                DataController.writeFile(output.resolve(h.getFilename()), out.first());
                DataController.addToHistory(encodeHistoryEntry("File decompression", alg.getName(), Calendar.getInstance().getTime(), out.second()));
                return out.second().toStrings();
            }
            else // it's a compressed folder
            {
                DataController.createFolder(output.resolve(h.getFilename()));
                Pair<Integer,Double> res = decompressRecursive(in, h.size(), h.getSize(), output.resolve(h.getFilename()));
                LocalStatistics ls = new LocalStatistics(res.first(), in.length, res.second());
                DataController.addToHistory(encodeHistoryEntry("Folder decompression", "multiple", Calendar.getInstance().getTime(), ls));
                return ls.toStrings();
            }
        }
        catch(Algorithm.AlgorithmException | DataController.DataControllerException e)
        {
            throw new DomainControllerException("Error in decompress: {\n" + e.getMessage() + "\n}");
        }
    }

    /**
     * @brief Algorisme recursiu de descompressió de carpetes
     * \pre mida de "input" >= que "offset"+"blocksize", "base" és un Path vàlid d'una carpeta
     * \post S'ha descomprimit els arxius i subcarpetes que es troben comprimides en la porció de fitxer comprimit que va des de "offset" i té tamany "blocksize", amb l'algorisme utilitzat per comprimir-los, a la ruta de la subcarpeta donada. Retorna la suma de les mides dels arxius i subcarpetes després de descomprimir i la suma dels temps emprats en la descompressió
     * \exception DataControllerException : Si hi ha algun problema en guardar els fitxers descomprimits o no es compleix la precondició llança excepció
     * \exception AlgorithmException : Si hi ha algun problema intern en el procés de descompressió es llança excepció
     * \exception DomainControllerException : Si algun header del fitxers o subcarpetes no és vàlid o no es troba l'algorisme amb què s'han comprimit els arxius llança excepció
     * \param input Fitxer comprimit en forma d'array de bytes
     * \param offset Offset (en bytes) des del qual treballar dins el fitxer comprimit
     * \param blocksize Tamany (en bytes) que volem descomprimir dins el fitxer comprimit a partir de offset, porció que representa una subcarpeta
     * \param base Path de la carpeta on guardar els fitxers i subcarpetes descomprimides
     */
    //decompressed size, time
    private Pair<Integer,Double> decompressRecursive(byte[] input, int offset, int blocksize, Path base) throws DomainControllerException, Algorithm.AlgorithmException, DataController.DataControllerException
    {
        int size = 0;
        double time = 0.0;
        int limit = offset+blocksize;

        while(offset < limit)
        {
            Header h = Header.decode(input, offset);
            if(h == null) throw new DomainControllerException("Bad header format");
            offset += h.size();
            if(h.getType() == Header.Type.file) // it's a compressed file
            {
                Algorithm alg = getAlgorithmByName(h.getAlgorithm());
                if(alg == null) throw new DomainControllerException("Algorithm not found");
                byte[] data = new byte[h.getSize()];
                System.arraycopy(input, offset, data, 0, h.getSize());
                Pair<byte[], LocalStatistics> out = alg.decompress(data, h.getOriginalsize());
                DataController.writeFile(base.resolve(h.getFilename()), out.first());
                size += out.first().length;
                time += out.second().getTime();
            }
            else // it's a compressed folder
            {
                DataController.createFolder(base.resolve(h.getFilename()));
                Pair<Integer,Double> res = decompressRecursive(input, offset, h.getSize(), base.resolve(h.getFilename()));
                size += res.first();
                time += res.second();
            }
            offset += h.getSize();
        }
        return new Pair<>(size, time);
    }

    /**
     * @brief Camparar un arxiu abans i després de la compressió/descompressió amb un cert algorisme
     * \pre "input" és un Path vàlid d'un arxiu .txt o .ppm, ha d'existir un algorisme amb el nom donat
     * \post S'ha comprimit i descomprimit internament l'arxiu donat amb l'algorisme donat. S'ha actualitzat les estadístiques globals de l'algorisme i s'ha afegit una entrada a l'historial. Retorna l'arxiu original sense comprimir, les estadístiques de compressió, l'arxiu després de comprimir/descomprimir i les estadístiques de descompressió
     * \exception DomainControllerException : Si hi ha algun problema per llegir el fitxer, un problema intern del procés de compressió o descompressió o no es compleix la precondició es llança excepció
     * \param input Path de l'arxiu a comparar
     * \param algorithm Nom de l'algorisme
     */
    public Pair<Pair<byte[], ArrayList<String>>, Pair<byte[], ArrayList<String>>> compare(Path input, String algorithm) throws DomainControllerException
    {
        Algorithm alg = getAlgorithmByName(algorithm);
        if(alg == null) throw new DomainControllerException("Algorithm not found");

        try
        {
            byte[] in = DataController.readFile(input);
            Pair<byte[], LocalStatistics> compressed = alg.compress(in);
            Pair<byte[], LocalStatistics> decompressed = alg.decompress(compressed.first(), in.length);
            //DataController.addToHistory(encodeHistoryEntry("Comparision", algorithm, Calendar.getInstance().getTime(), compressed.second()));
            //DataController.addToHistory(encodeHistoryEntry("   |_________", "    \"\"  ",  Calendar.getInstance().getTime(), decompressed.second()));
            return new Pair<>(new Pair<>(in, compressed.second().toStrings()), new Pair<>(decompressed.first(), decompressed.second().toStrings()));
        }
        catch(Algorithm.AlgorithmException | DataController.DataControllerException e)
        {
            throw new DomainControllerException("Error in compare: {\n" + e.getMessage() + "\n}");
        }
    }

    /**
     * @brief Obtenir les estadístiques globals dels algorismes
     * \pre true
     * \post Retorna les estadístiques globals dels algorismes del programa, juntament amb els seus noms
     */
    public ArrayList<Pair<String,ArrayList<String>>> getStatistics()
    {
        ArrayList<Pair<String,ArrayList<String>>> ret = new ArrayList<>();
        for(int i = 0; i < numAlgorithms; i++)
        {
            ret.add(new Pair<>(algorithms[i].getName(), algorithms[i].getStatistics().toStrings()));
        }
        return ret;
    }

    /**
     * @brief Obtenir l'historial
     * \pre Existeix el fitxer on es guarda l'historial i és vàlid
     * \post Retorna el conjunt d'entrades de l'historial
     * \exception DomainControllerException : Si no es compleix la precondició es llança excepció
     */
    public ArrayList<ArrayList<String>> getHistory() throws DomainControllerException
    {
        try
        {
            return DataController.getHistory();
        }
        catch (DataController.DataControllerException e)
        {
            throw new DomainControllerException("Error getting history: {\n" + e.getMessage() + "\n}");
        }
    }

    /**
     * @brief Codificar una entrada d'historial
     * \pre true
     * \post Retorna els paràmetres d'entrada codificats com una entrada de l'historial, en forma de Strings
     * \param action Nom de l'acció feta
     * \param algorithm Nom de l'algorisme
     * \param date Data de l'acció
     * \param statistics Estadístiques locals de l'acció
        */
    private ArrayList<String> encodeHistoryEntry(String action, String algorithm, Date date, LocalStatistics statistics)
        {
        ArrayList<String> ret = new ArrayList<>();
        ret.add(action);
        ret.add(algorithm);
        DateFormat formatter = new SimpleDateFormat("hh:mm - dd/MM/yyyy");
        ret.add(formatter.format(date));
        ret.addAll(statistics.toStrings());
        return ret;
    }

    /**
     * @class Header
     * @brief Classe que representa un header d'un fixer comprimit
     * És una classe destinada a codificar, descodificar i guardar el header s'un fitxer comprimit: arxiu o carpeta
     */
    private static class Header
    {
        /** @brief Enum dels tipus de fitxer comprimits */
        enum Type
        {
            file((byte)0x00), folder((byte)0xFF);

            private byte code;

            Type(byte code)
            {
                this.code = code;
            }

            public byte getCode()
            {
                return code;
            }
        }

        /** @brief Tipus de fitxer comprimit */
        private Type type;
        /** @brief Número de bytes que componen la carpeta o fitxer comprimit */
        private int size;
        /** @brief Nom del fitxer o carpeta */
        private String filename;
        /** @brief Mida original de l'arxiu, abans de comprimir */
        private int originalsize;
        /** @brief Nom de l'algorisme utilitzat per comprimir */
        private String algorithm;

        /**
         * @brief Constructora privada
         */
        private Header() {}

        /**
         * @brief Obtenir tipus
         * \pre true
         * \post Retorna el tipus
         */
        Type getType()
        {
            return type;
        }

        /**
         * @brief Obtenir el tamany
         * \pre true
         * \post Retorna el número de bytes que componen l'arxiu o carpeta comprimida
         */
        int getSize()
        {
            return size;
        }

        /**
         * @brief Obtenir nom
         * \pre true
         * \post Retorna el nom de l'arxiu o carpeta
         */
        String getFilename()
        {
            return filename;
        }

        /**
         * @brief Obtenir el tamany original
         * \pre true
         * \post Retorna la mida en bytes de l'arxiu abans de comprimir
         */
        int getOriginalsize()
        {
            return originalsize;
        }

        /**
         * @brief Obtenir el nom de l'algorisme
         * \pre true
         * \post Retorna el nom de l'algorisme utilitzat per comprimir l'arxiu
         */
        String getAlgorithm()
        {
            return algorithm;
        }

        /**
         * @brief Obtenir la mida del header
         * \pre true
         * \post Retorna la mida en bytes del header
         */
        int size()
        {
            if(type == Type.file) return (1+4+filename.length()+1+4+algorithm.length()+1);
            else return (1+4+filename.length()+1);
        }

        /**
         * @brief Codificar header a bytes
         * \pre true
         * \post Retorna el header codificat com a array de bytes
         */
        byte[] encode()
        {
            byte[] header = new byte[this.size()];
            header[0] = type.getCode();
            header[1] = (byte)(size >> 24); header[2] = (byte)(size >> 16); header[3] = (byte)(size >> 8); header[4] = (byte)(size);
            byte[] filenameB = filename.getBytes(StandardCharsets.ISO_8859_1);
            System.arraycopy(filenameB, 0, header, 5, filenameB.length);
            header[5+filenameB.length] = '\0';
            if(type == Type.file)
            {
                int i = 5+filenameB.length+1;
                header[i] = (byte)(originalsize >> 24); header[i+1]= (byte)(originalsize >> 16); header[i+2] = (byte)(originalsize >> 8); header[i+3] = (byte)(originalsize);
                byte[] algorithmB = algorithm.getBytes(StandardCharsets.ISO_8859_1);
                System.arraycopy(algorithmB, 0, header, i+4, algorithmB.length);
                header[i+4+algorithmB.length] = '\0';
            }
            return header;
        }

        /**
         * @brief Obtenir header com a string
         * \pre true
         * \post Retorna el header com a string
         */
        @Override
        public String toString()
        {
            if(type == Type.file) return ("HEADER |FILE|" + size + '|' + filename + '|' + originalsize + '|' + algorithm + "|\n");
            else return ("HEADER |FOLDER|" + size + '|' + filename + "|\n");
        }

        /*
        static byte[] dummyFileHeader(String filename, String algorithm)
        {
            return new byte[1+4+filename.length()+1+4+algorithm.length()+1];
        }
        */

        /**
         * @brief Obtenir una codificació buida d'un header de tipus carpeta
         * \pre true
         * \post Retorna una codificació buida del header de carpeta, com si al camp del nom hi hagués "filename"
         * \param filename Nom de la carpeta
         */
        static byte[] dummyFolderHeader(String filename)
        {
            return new byte[1+4+filename.length()+1];
        }

        /**
         * @brief Obtenir un header d'arxiu amb els paràmetres donats
         * \pre true
         * \post Retorna un header de tipus arxiu els camps del qual s'han inicialitzat amb els paràmetres donats
         * \param size Número de bytes que componen l'arxiu comprimit
         * \param filename Nom de l'arxiu
         * \param originalsize Mida original del arxiu abans de comprimir
         * \param algorithm Nom de l'algorisme utilitzat per comprimir
         */
        static Header fileHeader(int size, String filename, int originalsize, String algorithm)
        {
            Header h = new Header();
            h.type = Type.file;
            h.size = size;
            h.filename = filename;
            h.originalsize = originalsize;
            h.algorithm = algorithm;
            return h;
        }

        /**
         * @brief Obtenir un header de carpeta amb els paràmetres donats
         * \pre true
         * \post Retorna un header de tipus carpeta els camps del qual s'han inicialitzat amb els paràmetres donats
         * \param size Número de bytes que componen la carpeta comprimida
         * \param filename Nom de la carpeta
         */
        static Header folderHeader(int size, String filename)
        {
            Header h = new Header();
            h.type = Type.folder;
            h.size = size;
            h.filename = filename;
            return h;
        }

        /**
         * @brief Descodificar i obtenir un header a partir d'un array de bytes
         * \pre true
         * \post Retorna un header els camps del qual s'han inicialitzat amb els valors descodificats de l'array de bytes a partir de la posició donada. Si el header està danyat o no té l'estructura correcta retorna null
         * \param header Array de bytes
         * \param offset Posició a partir de la qual descodificar el header
         */
        static Header decode(byte[] header, int offset)
        {
            if(header.length-offset < 6) return null; //not enough bytes
            byte type = header[offset];
            int size = (header[offset+1] & 0xFF) << 24 | (header[offset+2] & 0xFF) << 16 | (header[offset+3] & 0xFF) << 8 | (header[offset+4] & 0xFF);
            int i = 0;
            while(header[offset+5+i] != 0x00) i++;
            String filename = new String(header, offset+5, i, StandardCharsets.ISO_8859_1);
            if(size < 0 || filename.isEmpty()) return null; //negative number or empty filename
            if(type == Type.file.getCode())
            {
                int k = offset+5+i+1;
                if(header.length-k < 5) return null; //not enough bytes
                int originalsize = (header[k] & 0xFF) << 24 | (header[k+1] & 0xFF) << 16 | (header[k+2] & 0xFF) << 8 | (header[k+3] & 0xFF);
                if(originalsize < 0) return null; //negative number
                int j = 0;
                while(header[k+4+j] != 0x00) j++;
                String algorithm = new String(header, k+4, j, StandardCharsets.ISO_8859_1);
                return Header.fileHeader(size, filename, originalsize, algorithm);
            }
            else if(type == Type.folder.getCode())
            {
                return Header.folderHeader(size, filename);
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * @class DomainControllerException
     * @brief Excepció llançada per els mètodes del controlador de domini
     */
    public static class DomainControllerException extends Exception
    {
        DomainControllerException()
        {
            super();
        }

        DomainControllerException(String msg)
        {
            super(msg);
        }
    }
}

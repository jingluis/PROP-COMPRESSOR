/**
 * @file DataController.java
 */
package Data;

import Global.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.util.Arrays;

/**
 * @class DataController
 * @brief Controlador de la capa de dades
 * Conté totes les funcions i mètodes que es necessita per la lectura i escriptura de dades
 */

public class DataController
{
		/**
     * @brief Llegeix un fitxer
     * \pre Existeix la ruta del fitxer d'entrada es valida
     * \post Carrega la informacio que emmagatzema en el fitxer amb ruta path en forma de byte[]
     * \exception DataControllerException : Si no es compleix la precondició es llança excepcio
     * \param path La ruta del fitxer d'entrada
     */
    public static byte[] readFile(Path path) throws DataControllerException
    {
        try
        {
            return Files.readAllBytes(path);
        }
        catch(IOException e)
        {
            throw new DataControllerException("Error reading a file \n[ " + e.toString() + " ]");
        }
    }

    /**
     * @brief Escriu un fitxer en una ruta de destinacio
     * \pre Existeix la ruta path i es valida
     * \post Carrega la informacio que emmagatzema en data en la ruta de destinacio de l'input
     * \exception DataControllerException : Si no es compleix la precondició es llança excepcio
     * \param path La ruta del fitxer de sortida
     * \param data Informacio en forma de byte[]
     */
    public static void writeFile(Path path, byte[] data) throws DataControllerException
    {
        try
        {
            Files.write(path, data);
        }
        catch(IOException e)
        {
            throw new DataControllerException("Error writing a file \n[ " + e.toString() + " ]");
        }
    }

    /**
     * @brief Escriu un fitxer en una ruta de destinacio despres de l'ultim byte existent, sense sobreescriure el contigut anterior
     * \pre Existeix la ruta path i es valida
     * \post Carrega la informacio que emmagatzema en data en la ruta de destinacio de l'input sense sobreescriure el contigut anterior
     * \exception DataControllerException : Si no es compleix la precondició es llança excepcio
     * \param path La ruta del fitxer de sortida
     * \param data Informacio en forma de byte[]
     */
    public static void appendToFile(Path path, byte[] data) throws DataControllerException
    {
        try
        {
            Files.write(path, data, StandardOpenOption.APPEND);
        }
        catch(IOException e)
        {
            throw new DataControllerException("Error appending a file \n[ " + e.toString() + " ]");
        }
    }

    /**
     * @brief Escriu un fitxer en una ruta de destinacio a partir de la posicio pos
     * \pre Existeix la ruta path i es valida
     * \post Carrega la informacio que emmagatzema en data en la ruta de destinacio de l'input a partir de la posicio pos
     * \exception DataControllerException : Si no es compleix la precondició es llança excepcio
     * \param path La ruta del fitxer de sortida
     * \param data Informacio en forma de byte[]
     * \param pos La posicio on es comenca a escriure
     */
    public static void writeFileAt(Path path, int pos, byte[] data) throws DataControllerException
    {
        try
        {
            RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");
            raf.seek(pos);
            raf.write(data);
            raf.close();
        }
        catch(IOException e)
        {
            throw new DataControllerException("Error writing randomly a file \n[ " + e.toString() + " ]");
        }
    }

    /**
     * @brief Obte informacio de la carpeta de la ruta d'entrada
     * \pre Existeix la ruta d'entrada i es valida
     * \post Carrega en el vector de pair la informacio de la carpeta indicant el nom del fitxer, i si es un directori o no
     * \param path La ruta del fitxer d'entrada
     */	
    public static Pair[] getFolderContent(Path path)
    {
        File[] files = path.toFile().listFiles();
        assert files != null;
        Pair[] paths = new Pair[files.length];
        for(int i = 0; i < files.length; i++)
        {
            paths[i] = new Pair<>(files[i].toPath().getFileName(), files[i].isDirectory());
        }
        return paths;
    }

		/**
     * @brief Es crea un directori
     * \pre Existeix la ruta d'entrada i es valida
     * \post Es crea un directori nou en la ruta d'entrada
     * \exception DataControllerException : Si no es compleix la precondició es llança excepcio
     * \param path La ruta del fitxer d'entrada
     */	
    public static void createFolder(Path path) throws DataControllerException
    {
        try
        {
            Files.createDirectory(path);
        }
        catch(IOException e)
        {
            throw new DataControllerException("Error creating a directory  \n[ " + e.toString() + " ]");
        }
    }

    /**
     * @brief Indica si la ruta d'entrada es accessible o no
     * \pre True
     * \post Es crea un directori nou en la ruta d'entrada
     * \param path La ruta del fitxer d'entrada
     */	
    public static boolean isAccesible(Path path)
    {
        return Files.isWritable(path);
    }

    /**
     * @brief Emmagatzema les estadistiques globals dels algorismes en format json en el directori actual
     * \pre True
     * \post Es guarda les estadistiques globals dels algorismes en format json en el fitxer "global.json"
     * \exception DataControllerException : Si hi ha algun error en l'escriptura de les dades
     * \param globals Les estadistiques globals dels algorismes
     */	
    public static void setStatistics(ArrayList<Pair<String,ArrayList<String>>> globals) throws DataControllerException
    {
        try
        {
            JSONObject res = new JSONObject();
            for(int i = 0; i < globals.size(); i++)
            {
                JSONObject algo = new JSONObject();
                algo.put("numberCompressions", globals.get(i).second().get(0));
                algo.put("numberDecompressions", globals.get(i).second().get(1));
                algo.put("compressionRatio", globals.get(i).second().get(2));
                algo.put("compressionSpeed", globals.get(i).second().get(3));
                algo.put("decompressionRatio", globals.get(i).second().get(4));
                algo.put("decompressionSpeed", globals.get(i).second().get(5));
                res.put(globals.get(i).first(),algo);
            }

            Files.write(Paths.get("global.json"), res.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch(IOException e)
        {
            throw new DataControllerException("Error setting statistics \n[ " + e.toString() + " ]");
        }
    }

    /**
     * @brief S'obte les estadistiques globals dels algorismes en format json del directori actual
     * \pre True
     * \post S'obte les estadistiques globals dels algorismes en format json del fitxer "global.json"
     * \exception DataControllerException : Si el fitxer json es incorrecte, si hi ha error en la lectura del fitxer o si hi ha error en cast
     * \param algorithms Els noms dels algorismes
     */	
    public static ArrayList<ArrayList<String>> getStatistics(ArrayList<String> algorithms) throws DataControllerException
    {
        try
        {
            JSONParser json = new JSONParser();
            Object obj = json.parse(new FileReader("global.json"));
            JSONObject json1 = (JSONObject) obj;

            ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
            for(int i = 0; i < algorithms.size(); i++)
            {
                if(!json1.containsKey(algorithms.get(i))) res.add(null);
                else
                {
                    JSONObject j2 = (JSONObject) json1.get(algorithms.get(i));
                    ArrayList<String> res1 = new ArrayList<String>();
                    res1.add((String) j2.get("numberCompressions"));
                    res1.add((String) j2.get("numberDecompressions"));
                    res1.add((String) j2.get("compressionRatio"));
                    res1.add((String) j2.get("compressionSpeed"));
                    res1.add((String) j2.get("decompressionRatio"));
                    res1.add((String) j2.get("decompressionSpeed"));
                    res.add(res1);
                }
            }
            return res;
        }
        catch(IOException | ParseException | ClassCastException e)
        {
            throw new DataControllerException("Error getting statistics \n[ " + e.toString() + " ]");
        }
    }

    /**
     * @brief Emmagatzema l'entrada d'hirtorial en el directori actual
     * \pre True
     * \post Es guarda l'entrada d'hirtorial en el fitxer "history.txt", si no existeix el fitxer el crea
     * \exception DataControllerException : Si hi ha algun error en l'escriptura de les dades
     * \param entry L'entrada d'hirtorial
     */
    public static void addToHistory(ArrayList<String> entry) throws DataControllerException
    {
        try
        {
            StringBuffer res = new StringBuffer();
            for(int i = 0; i < entry.size(); i++)
            {
                res.append(entry.get(i));
                if(i != entry.size()-1) res.append('\0');
                else res.append('\n');
            }
            Files.write(Paths.get("history.txt"), res.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch(IOException e)
        {
            throw new DataControllerException("Error adding history \n[ " + e.toString() + " ]");
        }
    }

    /**
	   * @brief S'obte les entrades d'hirtorial en el directori actual
	   * \pre True
	   * \post S'obte les entrades d'hirtorial del fitxer "history.txt"
	   * \exception DataControllerException : Si hi ha error en la lectura del fitxer
	   */	
    public static ArrayList<ArrayList<String>> getHistory() throws DataControllerException
    {
        try
        {
            BufferedReader reader;
            ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
            reader = new BufferedReader(new FileReader("history.txt"));
            String line = reader.readLine();
            while(line != null)
            {
                String[] aux = line.split("\0");
                if(aux.length != 8) //file not valid, delete it
                {
                    reader.close();
                    Files.delete(Paths.get("history.txt"));
                    throw new DataControllerException("Error parsing history \n[ Bad format ]");
                }
                res.add(new ArrayList<String>(Arrays.asList(aux)));
                line = reader.readLine();
            }
            reader.close();
            return res;
        }
        catch(IOException e)
        {
            throw new DataControllerException("Error reading history \n[ " + e.toString() + " ]");
        }
    }

    /**
     * @class DataControllerException
     * @brief Excepció llançada per els mètodes de la capa de dades
     */	
    public static class DataControllerException extends Exception
    {
        DataControllerException()
        {
            super();
        }

        DataControllerException(String msg)
        {
            super(msg);
        }
    }
}

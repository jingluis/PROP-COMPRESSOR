/**
 * @file CompareState.java
 */

package Presentation;

import Domain.DomainController;
import Global.Pair;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @class CompareState
 * @brief Estat de comparar abans de compressió i després de desscompressió
 * Es defineix el comportament dels diferents listeners quan es troba en l'estat Compare.
 */
class CompareState extends MainViewState
{
    /**
     * @brief Constructor a partir de la vista principal, el controlador de presentació i el controlador de domini
     * \pre true
     * \post S'ha creat l'estat Compare passant a la seva superclasse els atributs necessaris
     * \param mv Vista principal
     * \param pc Controlador de presentació
     * \param dc Controlador de domini
     */
    CompareState(MainView mv, PresentationController pc, DomainController dc)
    {
        super(mv, pc, dc);
    }

    /**
     * @brief Funció modificadora que activa els elements que seran visibles
     * \pre true
     * \post S'ha activat els elements visibles de l'estat Decompress
     */
    @Override
    void specificSetLayout()
    {
    	mv.label_Algorithm1.setVisible(true);
        mv.combobox_Algorithm1.setVisible(true);
        mv.text_Title.setText("Compare");
        mv.label_Algorithm1.setText("Algorithm");
        mv.button_Action.setText("COMPARE");
        mv.button_compare.setDisable(true);
    }

    /**
     * @brief Descriu l'acció que realitza el listener Input
     * \pre true
     * \post Es realitza el comportament del listener Input, escull un arxiu de tipus txt o ppm, i ho escriu en el text_field.
     */
    @Override
    void specificlistener_Input()
    {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT and PPM files", "*.txt", "*.ppm");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Open Resource File");
        File f = fileChooser.showOpenDialog(new Stage());
        if(f != null)
        {
            mv.textfield_Input.setText(f.getAbsolutePath());
            mv.combobox_Algorithm1.setDisable(false);
            mv.combobox_Algorithm1.getItems().clear();
            ArrayList<String> aux = dc.getAlgorithmsFor(Paths.get(f.getAbsolutePath()));
            mv.combobox_Algorithm1.getItems().addAll(aux);
            mv.combobox_Algorithm1.getSelectionModel().select(0);

            mv.inputSet = true;
        }
    }

    /**
     * @brief Descriu l'acció que realitza el listener Help
     * \pre true
     * \post Es realitza el comportament del listener Help, mostra les instruccions de l'ús de la funcionalitat Compare
     */
    @Override
    void specificlistener_Help()
    {
        String s;
        s = "Please follow the below steps: " +
                "\n\n 1) Select the input file path to compare." +
                "\n\n 3) Press the button COMPARE.";
        pc.showHelp(s);
    }


    /**
     * @brief Descriu l'acció que realitza el listener Action
     * \pre true
     * \post Es realitza el comportament del listener Action, s'obre una nova pestanya on sortirà el fitxer original i el fitxer descompres amb l'algorisme seleccionat, a continuació tambe apareixen les estadístiques de la compressió i descompressió, i per les imatges, esta activada la funcionalitat ZOOM per engrandir l'imatge.
     */
    @Override
    void specificlistener_Action()
    {
        if(mv.inputSet)
        {
            Path inputPath = Paths.get(mv.textfield_Input.getText());

            String alg = (String)mv.combobox_Algorithm1.getSelectionModel().getSelectedItem();

            mv.enableLoading();

            Thread t = new Thread(new Runnable() {
                public void run()
                {
                    try
                    {
                        Pair<Pair<byte[],ArrayList<String>>, Pair<byte[],ArrayList<String>>> data = dc.compare(inputPath, alg);

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("CompareView.fxml"));

                        String ext = inputPath.getFileName().toString();
                        ext = ext.substring(ext.lastIndexOf('.'));

                        if(ext.equals(".txt"))
                        {
                            String text1 = new String(data.first().first(), StandardCharsets.ISO_8859_1);
                            String text2 = new String(data.second().first(),StandardCharsets.ISO_8859_1);
                            loader.setController(new CompareView(text1, data.first().second(), text2, data.second().second()));
                        }
                        else if(ext.equals(".ppm"))
                        {
                            Pair<Integer,Integer> wh = getWH(data.first().first());
                            BufferedImage BuffImageA = ppm(wh.first(), wh.second(), data.first().first());
                            BufferedImage BuffImageB = ppm(wh.first(), wh.second(), data.second().first());
                            Image imageA = SwingFXUtils.toFXImage(BuffImageA, null );
                            Image imageB = SwingFXUtils.toFXImage(BuffImageB, null );
                            loader.setController(new CompareView(imageA, data.first().second(), imageB, data.second().second()));
                        }
                        else
                        {
                            Platform.runLater(() -> pc.showError("Comparision not possible, file not displayable"));
                        }

                        Parent root = loader.load();

                        mv.disableLoading();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Stage compareWindow = new Stage();
                                compareWindow.setTitle("Comparision");
                                compareWindow.setScene(new Scene(root));
                                compareWindow.setResizable(false);
                                compareWindow.show();
                            }
                        });
                    }
                    catch(DomainController.DomainControllerException | IOException e)
                    {
                        Platform.runLater(() -> pc.showError(e.getMessage()));
                        mv.disableLoading();
                    }
                }
            });
            t.start();
        }
    }

    /**
     * @brief S'obté l'amplada i l'altura de la imatge en byte[]
     * \pre image vàlida
     * \post Es retorna en forma de Pair l'amplada i l'altura de la imatge en byte[], on el primer element és l'amplada, i el segon és l'altura
     * \param image L'imatge en format de byte[]
     */
    private Pair<Integer,Integer> getWH(byte[] image)
    {
        int it = 3;
        String widthS = "";
        while(image[it] >= '0' && image[it] <= '9')
        {
            widthS += (char)image[it];
            it++;
        }
        it++;
        String heightS = "";
        while(image[it] >= '0' && image[it] <= '9')
        {
            heightS += (char)image[it];
            it++;
        }
        return new Pair<>(Integer.parseInt(widthS), Integer.parseInt(heightS));
    }

    /**
     * @brief Transforma en BufferedImage l'imatge en forma byte[] que té amplada width i altura height
     * \pre width > 0, height > 0, data valida
     * \post Es retorna en forma BufferedImge l'imatge en forma byte[] que té amplada width i altura height
     * \param width Amplada de l'imatge
     * \param height Altura de l'imatge
     * \param data L'imatge en format byte[]
     */
    private static BufferedImage ppm(int width, int height, byte[] data)
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int offset = data.length - width*height*3;
        int r, g, b, k = 0, pixel;
        for (int y = 0; y < height; y++) {
            for (int x = 0; (x < width) && ((k + 3) < data.length-offset); x++) {
                r = data[offset+(k++)] & 0xFF;
                g = data[offset+(k++)] & 0xFF;
                b = data[offset+(k++)] & 0xFF;
                pixel = 0xFF000000 + (r << 16) + (g << 8) + b;
                image.setRGB(x, y, pixel);
            }
        }
        return image;
    }
}

/**
 * @file CompareView.java
 */
package Presentation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * @class CompareView
 * @brief Vista de la comparacó
 * Vista que compara dues imatges o dos texts abans i després de comprimir/descomprimir
 */
public class CompareView implements Initializable
{

    /** @brief El panell amb el objecte abans de comprimir/descomprimir*/
    @FXML
    private StackPane sp1;
    /** @brief El panell amb el objecte després de comprimir/descomprimir*/
    @FXML
    private StackPane sp2;
    /** @brief Slider fer fer zoom de les dues imatges o texts*/
    @FXML
    private Slider slider;
    /** @brief L'etiqueta del temps de compressió*/
    @FXML
    private Label timeC;
    /** @brief L'etiqueta de la velocitat de compressió*/
    @FXML
    private Label speedC;
    /** @brief L'etiqueta del ratio de la compressió/descompressió*/
    @FXML
    private Label ratio;
    /** @brief L'etiqueta de la velocitat de descompressió*/
    @FXML
    private Label speedD;
    /** @brief L'etiqueta del temps de descompressió*/
    @FXML
    private Label timeD;
    /** @brief Butó per tancar la vista*/
    @FXML
    private Button close;

    private Image imageA, imageB;
    private String textA, textB;
    private ArrayList<String> ls1, ls2;
    private boolean imageortext;

    /**
     * @brief Constructor a partir de dues imatges i les estadistiques de compressió i descompressió
     * \pre true
     * \post S'ha creat una instància de CompareView amb els atributs necessaris
     * \param imageA Imatge original
     * \param ls1 Llista amb les estadístiques de compressió
     * \param imageB Imatge després de comprimir/descomprimir
     * \param ls2 Llista amb les estadístiques de descompressió
     */
    CompareView(Image imageA, ArrayList<String> ls1, Image imageB, ArrayList<String> ls2)
    {
        imageortext = true;
        this.imageA = imageA;
        this.imageB = imageB;
        this.ls1 = ls1;
        this.ls2 = ls2;
    }

    /**
     * @brief Constructor a partir de dos texts i les estadistiques de compressió i descompressió
     * \pre true
     * \post S'ha creat una instància de CompareView amb els atributs necessaris
     * \param textA Text original
     * \param ls1 Llista amb les estadístiques de compressió
     * \param textB Text després de comprimir/descomprimir
     * \param ls2 Llista amb les estadístiques de descompressió
     */
    CompareView(String textA, ArrayList<String> ls1, String textB, ArrayList<String> ls2)
    {
        imageortext = false;
        this.textA = textA;
        this.textB = textB;
        this.ls1 = ls1;
        this.ls2 = ls2;
    }

    /**
     * @brief Inicialitzacio de la vista en javafx
     * \pre true
     * \post S'ha inicialitzat la vista de la comparació entre arxius abans i després de comprimir/descomprimir
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        if(imageortext)
        {
            ImageView imageView1 = new ImageView();
            ImageView imageView2 = new ImageView();
            imageView1.setImage(imageA);
            imageView2.setImage(imageB);

            boolean tall = (imageA.getWidth() < imageA.getHeight());
            if (!tall)
            {
                imageView1.setFitWidth(360);
                imageView2.setFitWidth(360);
            }
            else
            {
                imageView1.setFitHeight(380);
                imageView2.setFitHeight(380);
            }

            imageView1.setPreserveRatio(true);
            imageView2.setPreserveRatio(true);

            sp1.getChildren().add(imageView1);
            sp2.getChildren().add(imageView2);

            slider.valueProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val)
                {
                    if (!tall)
                    {
                        imageView1.setFitWidth(360 * (double) new_val);
                        imageView2.setFitWidth(360 * (double) new_val);
                    }
                    else
                    {
                        imageView1.setFitHeight(380 * (double) new_val);
                        imageView2.setFitHeight(380 * (double) new_val);
                    }
                }
            });
        }
        else
        {
            ListView<String> text1 = new ListView<>();
            text1.setEditable(false);
            String[] splits1 = textA.split("\n");
            text1.getItems().addAll(splits1);
            splits1 = null;
            ListView<String> text2 = new ListView<>();
            text2.setEditable(false);
            String[] splits2 = textB.split("\n");
            text2.getItems().addAll(splits2);
            splits2 = null;

            sp1.getChildren().add(text1);
            sp2.getChildren().add(text2);
            slider.setDisable(true);
        }

        timeC.setText("Time: " + PresentationController.makeHR(ls1.get(2), PresentationController.HRType.time) );
        ratio.setText("Ratio: " + PresentationController.makeHR(ls1.get(3), PresentationController.HRType.ratio));
        speedC.setText("Speed: " + PresentationController.makeHR(ls1.get(4), PresentationController.HRType.speed));
        timeD.setText("Time: " + PresentationController.makeHR(ls2.get(2), PresentationController.HRType.time) );
        speedD.setText("Speed: " + PresentationController.makeHR(ls2.get(4), PresentationController.HRType.speed));
    }

    /**
     * @brief Descriu l'accio que realitza el listener closeCompare
     * \pre true
     * \post Es tanca la vista de la comparació
     */
    public void closeCompare()
    {
        Stage stage = (Stage) close.getScene().getWindow();
        stage.close();
    }
}

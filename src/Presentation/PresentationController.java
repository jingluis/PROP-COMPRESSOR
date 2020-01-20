/**
 * @file PresentationController.java
 */
package Presentation;

import Domain.DomainController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @class PresentationController
 * @brief Controlador de la capa de presentació
 * Conté totes les funcions que es necessita per les interaccions amb usuari
 */
public class PresentationController extends Application
{
	/** @brief El controlador de domini */
	private DomainController dc;

	/**
     * @brief Constructora per defecte
     * \pre true
     * \post S'ha creat una nova instància de PresentationController que té dc com una nova instància de DomainCOntroller
     */
	public PresentationController()
    {
        dc = new DomainController();
    }

	/**
     * @brief Inicialitzar l'aplicació JavaFX
     * \pre No s'ha inicialitzat l'aplicació
     * \post S'ha inicialitzat l'aplicació JavaFX
     */
	public void initialize()
    {
        launch();
    }

    @Override
    /**
     * @brief Executar l'aplicació JavaFX
     * \pre S'ha inicialitzat l'aplicació i existeix el fitxer MainView.fxml i és vàlid
     * \post S'ha executat l'aplicació JavaFX i s'ha mostrat tot l'interfície.
     * \exception IOException: Si ha fallat al llegir fitxer o altres operacions I/O o load.
     */
    public void start(Stage primaryStage) throws IOException
    {
        try
        {
            dc.initialize();
        }
        catch(DomainController.DomainControllerException e)
        {
            showError(e.getMessage());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        loader.setController(new MainView(this, dc));
        Parent root = loader.load();
        primaryStage.setTitle("COMPRESSOR");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.setResizable(false);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try
                {
                    dc.finalize();
                }
                catch (DomainController.DomainControllerException e)
                {
                    showError(e.getMessage());
                }
            }
        });
    }

    
    /**
     * @brief Mostrar una finestra d'error
     * \pre true
     * \post S'ha mostrat una finestra d'error amb contingut del paràmetre error
     * \param error String que representa el missatge d'error
     */
    void showError(String error)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        VBox box = new VBox();
        TextArea ta = new TextArea();
        ta.setText(error);
        ta.setEditable(false);
        ta.setPrefSize(300, 150);
        ta.setWrapText(true);
        box.getChildren().add(ta);
        alert.getDialogPane().setContent(box);
        alert.showAndWait();
    }

    /**
     * @brief Mostrar una finestra d'ajuda
     * \pre true
     * \post S'ha mostrat una finestra d'ajuda amb contingut del paràmetre help
     * \param help String que representa informació d'ajuda
     */
    void showHelp(String help)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("HELP");
        alert.setHeaderText("User Guide");
        VBox box = new VBox();
        TextArea ta = new TextArea();
        ta.setStyle("-fx-font-size: 1.5em;");
        ta.setText(help);
        ta.setEditable(false);
        ta.setPrefSize(600, 300);
        ta.setWrapText(true);
        box.getChildren().add(ta);
        alert.getDialogPane().setContent(box);
        alert.showAndWait();
    }

    /**
     * @brief Mostrar una finestra d'estadístiques
     * \pre true
     * \post S'ha mostrat una finestra d'estadístiques amb contingut del paràmetre stats
     * \param stats ArrayList<String> que representa l'estadística d'una compressió/descompressió específica
     */
    void showStatistics(ArrayList<String> stats)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        ListView<String> lv = new ListView<>();
        lv.getItems().add("decompressed size: " + makeHR(stats.get(0), HRType.size));
        lv.getItems().add("compressed size: " + makeHR(stats.get(1), HRType.size));
        lv.getItems().add("time: " + makeHR(stats.get(2), HRType.time));
        lv.getItems().add("ratio: " + makeHR(stats.get(3), HRType.ratio));
        lv.getItems().add("speed: " + makeHR(stats.get(4), HRType.speed));
        lv.setPrefSize(400, 250);
        alert.setTitle("Statistics");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(lv);
        alert.getDialogPane().setStyle("-fx-font-size: 1.5em");
        alert.showAndWait();
    }

    /** @brief Enumeració que conté els tipus d'informació d'estadística */
    public enum HRType
    {
        size,
        time,
        speed,
        ratio
    }

    /**
     * @brief Afegir unitats
     * \pre true
     * \post Retorna un String que conté el input després de fer conversió d'unitat i unitat corresponent
     * \param input String que representa una informació d'estadística (mida, temps, velocitat, ratio)
     * \param type HRType que conté el tipus d'informació de input
     */
    public static String makeHR(String input, HRType type)
    {
        double data = Double.parseDouble(input);
        double res = 0;
        String ext = " ";

        switch(type)
        {
            case size:
            case speed:
            {
                if (data < 1000) res = data;
                else
                {
                    int exp = (int)(Math.log10(data) / Math.log10(1000));
                    res = (data/Math.pow(1000, exp));
                    ext += "?kMG".charAt(exp);
                }
                ext += "B";
                if(type == HRType.speed) ext += "/s";
            }
                break;
            case time:
                ext += "s";
            case ratio:
                res = data;
                break;
        }

        return (Math.floor(res*1000.0)/1000.0) + ext;
    }
}

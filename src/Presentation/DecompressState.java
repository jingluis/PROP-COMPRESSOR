/**
 * @file DecompressState.java
 */

package Presentation;

import Domain.DomainController;
import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @class DeompressState
 * @brief Estat de decompressió
 * Es defineix el comportament dels diferents listeners quan es troba en l'estat descomprimir.
 */
class DecompressState extends MainViewState
{
    /**
     * @brief Constructor a partir de la vista principal, el controlador de presentació i el controlador de domini
     * \pre true
     * \post S'ha creat l'estat descompressió passant a la seva superclasse els atributs necessaris
     * \param mv Vista princial
     * \param pc Controlador de presentació
     * \param dc Controlador de domini
     */
    DecompressState(MainView mv, PresentationController pc, DomainController dc)
    {
        super(mv, pc, dc);
    }

    /**
     * @brief Funció modificadora que activa els elements que serà visible
     * \pre true
     * \post S'ha activat els elements visibles de l'estat Decompress
     */
    @Override
    void specificSetLayout()
    {
    	mv.label_Output.setVisible(true);
        mv.textfield_Output.setVisible(true);
        mv.button_Output.setVisible(true);
        mv.text_Title.setText("Decompress");
        mv.button_Action.setText("DECOMPRESS");
        mv.button_decompress.setDisable(true);
    }

    /**
     * @brief Descriu l'acció que realitza el listener Input
     * \pre true
     * \post Es realitza el comportament del listener Input, s'escull l'arxiu prop que es vol descomprimir, i s'escriu en el textfield.
     */
    @Override
    void specificlistener_Input()
    {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PROP files", "*.prop");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Select input file");
        File f = fileChooser.showOpenDialog(new Stage());
        if(f != null)
        {
            mv.textfield_Input.setText(f.getAbsolutePath());
            mv.textfield_Output.setText(f.toPath().getParent().toString());
            mv.inputSet = true;
        }
    }

    /**
     * @brief Descriu l'acció que realitza el listener Output
     * \pre true
     * \post Es realitza el comportament del listener Output, s'escull el directori on es vol guardar el fitxer/carpeta i s'escriu en el textfield.
     */
    @Override
    void specificlistener_Output()
    {
        DirectoryChooser directory_chooser = new DirectoryChooser();
        directory_chooser.setTitle("Select output folder");
        File f = directory_chooser.showDialog(new Stage());
        if(f != null)
        {
            mv.textfield_Output.setText(f.getAbsolutePath());
        }
    }

    /**
     * @brief Descriu l'accio que realitza el listener Help
     * \pre true
     * \post Es realitza el comportament del listener Help,  mostra les instruccions de l'ús de la funcionalitat Decompress.
     */
    @Override
    void specificlistener_Help()
    {
        String s;
        s = "Please follow the below steps: " +
                "\n\n 1) Select the input file path to decompress." +
                "\n\n 2) Select the output file path." +
                "\n\n 3) Press the button DECOMPRESS.";
        pc.showHelp(s);
    }

    /**
     * @brief Descriu l'acció que realitza el listener Action
     * \pre true
     * \post Es realitza el comportament del listener Action, es descomprimeix l'arxiu internament i es mostra les estadistiques locals un cop fet la descompessio, i durant la descompressio es produeix algun error, s'aborta la tasca i mostra el missatge d'error.
     */
    @Override
    void specificlistener_Action()
    {
        if(mv.inputSet)
        {
            Path inputPath = Paths.get(mv.textfield_Input.getText());
            Path outputPath = Paths.get(mv.textfield_Output.getText());

            mv.enableLoading();

            Thread t = new Thread(new Runnable() {
                public void run()
                {
                    try
                    {
                        ArrayList<String> ls = dc.decompress(inputPath, outputPath);
                        mv.disableLoading();
                        Platform.runLater(() -> pc.showStatistics(ls));
                    }
                    catch(DomainController.DomainControllerException e)
                    {
                        Platform.runLater(() -> pc.showError(e.getMessage()));
                        mv.disableLoading();
                    }
                }
            });
            t.start();
        }
    }
}

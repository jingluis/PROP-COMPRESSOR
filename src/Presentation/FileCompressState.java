/**
 * @file FileCompressState.java
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
import java.util.HashMap;

/**
 * @class FileCompressState
 * @brief Estat de compressió de fitxers
 * Es defineix el comportament dels diferents listeners quan es troba en l'estat comprimir fitxer
 */


class FileCompressState extends MainViewState
{
    /**
     * @brief Constructor a partir de la vista principal, el controlador de presentació i el controlador de domini
     * \pre true
     * \post S'ha creat l'estat compressió fitxers passant a la seva superclasse els atributs necessaris
     * \param mv Vista principal
     * \param pc Controlador de presentació
     * \param dc Controlador de domini
     */
    FileCompressState(MainView mv, PresentationController pc, DomainController dc)
    {
        super(mv, pc, dc);
    }

    /**
     * @brief Funció modificadora que activa els elements que seran visibles
     * \pre true
     * \post S'ha activat els elements visibles de l'estat FileCompress
     */
    @Override
    void specificSetLayout()
    {
    	mv.label_Output.setVisible(true);
        mv.textfield_Output.setVisible(true);
        mv.button_Output.setVisible(true);
        mv.label_Algorithm1.setVisible(true);
        mv.combobox_Algorithm1.setVisible(true);
        mv.text_Title.setText("Compress File");
        mv.label_Algorithm1.setText("Algorithm");
        mv.button_Action.setText("COMPRESS");
        mv.button_compressFile.setDisable(true);
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
        fileChooser.setTitle("Select input file");
        File f = fileChooser.showOpenDialog(new Stage());
        if(f != null)
        {
            mv.textfield_Input.setText(f.getAbsolutePath());
            String filename = f.toPath().getFileName().toString();
            mv.textfield_Output.setText(f.toPath().getParent().resolve(filename.substring(0, filename.lastIndexOf('.')) + ".prop").toString());
            mv.combobox_Algorithm1.setDisable(false);
            if(mv.combobox_Algorithm1.getItems().isEmpty())
            {
                mv.combobox_Algorithm1.getItems().clear();
                ArrayList<String> aux = dc.getAlgorithmsFor(Paths.get(f.getAbsolutePath()));
                mv.combobox_Algorithm1.getItems().addAll(aux);
                mv.combobox_Algorithm1.getSelectionModel().select(0);
            }
            mv.inputSet = true;
        }
    }
    /**
     * @brief Descriu l'acció que realitza el listener Output
     * \pre true
     * \post Es realitza el comportament del listener Output, es selecciona el directori de sortida i el nom del fitxer prop que desitgi, i s'ecriurà en el textfield corresponent.
     */
    @Override
    void specificlistener_Output()
    {
        FileChooser file_chooser = new FileChooser();
        //FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PROP files", "*.prop");
        //file_chooser.getExtensionFilters().add(extFilter);
        file_chooser.setTitle("Select save file");
        File f = file_chooser.showSaveDialog(new Stage());
        if(f != null)
        {
            String filename = f.toPath().getFileName().toString();
            if(!filename.endsWith(".prop")) filename += ".prop";
            mv.textfield_Output.setText(f.toPath().getParent().resolve(filename).toString());
        }
    }

    /**
     * @brief Descriu l'acció que realitza el listener Help
     * \pre true
     * \post Es realitza el comportament del listener Help, mostra les instruccions de l'ús de la funcionalitat Compress File
     */
    @Override
    void specificlistener_Help()
    {
        String s;
        s = "Please follow the below steps: " +
                "\n\n 1) Select the input file path." +
                "\n\n 2) Select the output compressed file path." +
                "\n\n 3) Select the compresssion algorithm manually or it will remain the default algorithm." +
                "\n\n 4) Press the button COMPRESS.";
        pc.showHelp(s);
    }

    /**
     * @brief Descriu l'acció que realitza el listener Action
     * \pre true
     * \post Es realitza el comportament del listener Action, es realitza la compressió en un thread diferent i mostra les estadístiques locals un cop fet la compressió amb l'algorisme seleccionat, si es produeix algun error durant la compressió, es mostra el missatge d'error i s'aborta la tasca.
     */
    @Override
    void specificlistener_Action()
    {
        if(mv.inputSet)
        {
            Path inputPath = Paths.get(mv.textfield_Input.getText());
            Path outputPath = Paths.get(mv.textfield_Output.getText());

            String alg = (String)mv.combobox_Algorithm1.getSelectionModel().getSelectedItem();

            mv.enableLoading();

            Thread t = new Thread(new Runnable() {
                public void run()
                {
                    try
                    {
                        ArrayList<String> ls = dc.compressFile(inputPath, alg, outputPath);
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

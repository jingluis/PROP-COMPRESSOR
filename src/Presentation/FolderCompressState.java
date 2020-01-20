/**
 * @file FolderCompressState.java
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
 * @class FolderCompressState
 * @brief Estat de compressió de carpetes
 * Es defineix el comportament dels diferents listeners quan es troba en l'estat comprimir carpetes
 */

class FolderCompressState extends MainViewState
{
    /**
     * @brief Constructor a partir de la vista principal, el controlador de presentació i el controlador de domini
     * \pre true
     * \post S'ha creat l'estat compressió carpetes passant a la seva superclasse els atributs necessaris
     * \param mv Vista principal
     * \param pc Controlador de presentació
     * \param dc Controlador de domini
     */
    FolderCompressState(MainView mv, PresentationController pc, DomainController dc)
    {
        super(mv, pc, dc);
    }

    /**
     * @brief Funció modificadora que activa els elements que seran visibles
     * \pre true
     * \post S'ha activat els elements visibles de l'estat FolderCompress
     */
    @Override
    void specificSetLayout()
    {
    	mv.label_Output.setVisible(true);
        mv.textfield_Output.setVisible(true);
        mv.button_Output.setVisible(true);
        mv.label_Algorithm1.setVisible(true);
        mv.combobox_Algorithm1.setVisible(true);
        mv.label_Algorithm2.setVisible(true);
        mv.combobox_Algorithm2.setVisible(true);
        mv.text_Title.setText("Compress Folder");
        mv.label_Algorithm1.setText("Algorithm .txt");
        mv.label_Algorithm2.setText("Algorithm .ppm");
        mv.button_Action.setText("COMPRESS");
        mv.button_compressFolder.setDisable(true);
    }

    /**
     * @brief Descriu l'acció que realitza el listener Input
     * \pre true
     * \post Es realitza el comportament del listener Input, s'ecull el directori que es vol comprimir i s'escriu en el textfield.
     */
    @Override
    void specificlistener_Input()
    {
        DirectoryChooser directory_chooser = new DirectoryChooser();
        directory_chooser.setTitle("Open Resource Folder");
        File f = directory_chooser.showDialog(new Stage());
        if(f != null)
        {
            mv.textfield_Input.setText(f.toPath().toString());
            mv.textfield_Output.setText(f.toPath().getParent().resolve(f.toPath().getFileName().toString() + ".prop").toString());
            ArrayList<String> algsTXT = dc.getAlgorithmsFor(Paths.get("a.txt"));
            ArrayList<String> algsPPM = dc.getAlgorithmsFor(Paths.get("a.ppm"));

            mv.combobox_Algorithm1.getItems().clear();
            mv.combobox_Algorithm1.setDisable(false);
            mv.combobox_Algorithm1.getItems().addAll(algsTXT);
            mv.combobox_Algorithm1.getSelectionModel().select(0);
            mv.combobox_Algorithm2.getItems().clear();
            mv.combobox_Algorithm2.setDisable(false);
            mv.combobox_Algorithm2.getItems().addAll(algsPPM);
            mv.combobox_Algorithm2.getSelectionModel().select(0);

            mv.inputSet = true;
        }
    }
    /**
     * @brief Descriu l'acció que realitza el listener Output
     * \pre true
     * \post Es realitza el comportament del listener Output, s'escull el directori on es vol guardar el fitxer comprès i el nom.
     */
    @Override
    void specificlistener_Output()
    {
        FileChooser file_chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PROP files", "*.prop");
        file_chooser.getExtensionFilters().add(extFilter);
        file_chooser.setTitle("Open Resource File");
        File f = file_chooser.showSaveDialog(new Stage());
        if(f != null)
        {
            mv.textfield_Output.setText(f.getAbsolutePath());
        }
    }
    /**
     * @brief Descriu l'acció que realitza el listener Help
     * \pre true
     * \post Es realitza el comportament del listener Help, mostra les instruccions de l'ús de la funcionalitat Compress Folder.
     */
    @Override
    void specificlistener_Help()
    {
        String s;
        s = "Please follow the below steps: " +
                "\n\n 1) Select the input folder path." +
                "\n\n 2) Select the output compressed file path." +
                "\n\n 3) Select the compresssion algorithms manually or it will remain the default algorithms." +
                "\n\n 4) Press the button COMPRESS.";
        pc.showHelp(s);
    }

    /**
     * @brief Descriu l'acció que realitza el listener Action
     * \pre true
     * \post Es realitza el comportament del listener Action, es realitza la compressió en un thread diferent i mostra les estadístiques locals un cop fet la compressió amb l'algorismes seleccionats, si es produeix algun error durant la compressió, es mostra el missatge d'error i s'aborta la tasca.
     */
    @Override
    void specificlistener_Action()
    {
        if(mv.inputSet)
        {
            Path inputPath = Paths.get(mv.textfield_Input.getText());
            Path outputPath = Paths.get(mv.textfield_Output.getText());

            String algTXT = (String)mv.combobox_Algorithm1.getSelectionModel().getSelectedItem();
            String algPPM = (String)mv.combobox_Algorithm2.getSelectionModel().getSelectedItem();

            mv.enableLoading();

            Thread t = new Thread(new Runnable() {
                public void run()
                {
                    try
                    {
                        ArrayList<String> ls = dc.compressFolder(inputPath, algTXT, algPPM, outputPath);
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

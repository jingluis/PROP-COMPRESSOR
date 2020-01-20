/**
 * @file MainView.java
 */

package Presentation;

import Domain.DomainController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @class MainView
 * @brief Vista principal
 * Vista principal de l'interficie
 */
public class MainView implements Initializable
{
    /** @brief El controlodar de presentacio*/
    private PresentationController pc;
    /** @brief El controlodar de domini*/
    private DomainController dc;
    /** @brief L'estat que esta l'interficie*/
    private MainViewState state;

    /** @brief El titol del text*/
    @FXML
    public Text text_Title;
    /** @brief L'etiqueta l'output*/
    @FXML
    public Label label_Output;
    /** @brief L'area on es posa l'input path */
    @FXML
    public TextField textfield_Input;
    /** @brief L'area on es posa l'output path */
    @FXML
    public TextField textfield_Output;
    /** @brief Buto per seleccionar l'output path */
    @FXML
    public Button button_Output;
    /** @brief L'etiqueta l'algorisme*/
    @FXML
    public Label label_Algorithm1;
    /** @brief L'etiqueta l'algorisme d'imatge en comprimir carpeta*/
    @FXML
    public Label label_Algorithm2;
    /** @brief Combobox per seleccionar l'algorisme*/
    @FXML
    public ComboBox combobox_Algorithm1;
    /** @brief Combobox per seleccionar l'algorisme de compressio d'imatge*/
    @FXML
    public ComboBox combobox_Algorithm2;
    /** @brief Buto per l'accio */
    @FXML
    public Button button_Action;
    /** @brief Taula per mostrar historial */
    @FXML
    public TableView tableview_Tableview1;
    /** @brief Taula per mostrar estadistiques globals */
    @FXML
    public TableView tableview_Tableview2;
    /** @brief AnchorPane per quan es mostrar la rodona de progres */
    @FXML
    public AnchorPane anchorpane_Gray;
    /** @brief La rodona de progres */
    @FXML
    public ProgressIndicator pi_Loading;
    /** @brief Columna d'accio de l'entrada de l'historial */
    @FXML
    public TableColumn tv1_action;
    /** @brief Columna d'algorisme de l'entrada de l'historial */
    @FXML
    public TableColumn tv1_algorithm;
    /** @brief Columna de data de l'entrada de l'historial */
    @FXML
    public TableColumn tv1_date;
    /** @brief Columna de la mida descompressio de l'entrada de l'historial */
    @FXML
    public TableColumn tv1_decompressedSize;
    /** @brief Columna de la mida compressio de l'entrada de l'historial */
    @FXML
    public TableColumn tv1_compressedSize;
    /** @brief Columna del temps emprat de l'entrada de l'historial */
    @FXML
    public TableColumn tv1_time;
    /** @brief Columna de la ratio obtinguda de l'entrada de l'historial */
    @FXML
    public TableColumn tv1_ratio;
    /** @brief Columna de la velocitat obtinguda de l'entrada de l'historial */
    @FXML
    public TableColumn tv1_speed;
    /** @brief Columna del nom d'algorisme de les estadistiques globals */
    @FXML
    public TableColumn tv2_name;
    /** @brief Columna de la mida total de compressio de les estadistiques globals */
    @FXML
    public TableColumn tv2_nc;
    /** @brief Columna de la mida total de descompressio de les estadistiques globals */
    @FXML
    public TableColumn tv2_nd;
    /** @brief Columna de la ratio mitjana de compressio de les estadistiques globals */
    @FXML
    public TableColumn tv2_cr;
    /** @brief Columna de la velocitat mitjana de compressio de les estadistiques globals */
    @FXML
    public TableColumn tv2_cs;
    /** @brief Columna de la ratio mitjana de descompressio de les estadistiques globals */
    @FXML
    public TableColumn tv2_dr;
    /** @brief Columna de la velocitat mitjana de descompressio de les estadistiques globals */
    @FXML
    public TableColumn tv2_ds;
    /** @brief Buto per comprimir fitxer */
    @FXML
    public Button button_compressFile;
    /** @brief Buto per comprimir carpeta */
    @FXML
    public Button button_compressFolder;
    /** @brief Buto per descomprimir */
    @FXML
    public Button button_decompress;
    /** @brief Buto per comparar fitxer abans de compressio i despres de descompressio*/
    @FXML
    public Button button_compare;
    /** @brief Buto per mostrar estadistiques generals*/
    @FXML
    public Button button_globalStatistics;
    /** @brief Buto per mostrar historial */
    @FXML
    public Button button_history;
    /** @brief Boolea que indica si hi ha input path o no*/
    public boolean inputSet;

    /**
     * @brief Constructor a partir del controlador de presentacio i el controlador de domini
     * \pre true
     * \post S'ha creat una instancia de MainView amb els atributs necessaris
     * \param pc Controlador de presentacio, dc Controlador de domini
     */
    MainView(PresentationController pc, DomainController dc)
    {
        this.pc = pc;
        this.dc = dc;
        this.state = null;
    }

    /**
     * @brief Inicialitzacio de la vista en javafx
     * \pre true
     * \post S'ha inicialitzat la vista principal
     * \param location URL
     * \param resources ResourceBundle
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
       changeState(new FileCompressState(this, pc, dc));

       tv1_action.setCellValueFactory(new MapValueFactory("0"));
       tv1_algorithm.setCellValueFactory(new MapValueFactory("1"));
       tv1_date.setCellValueFactory(new MapValueFactory("2"));
       tv1_decompressedSize.setCellValueFactory(new MapValueFactory("3"));
       tv1_compressedSize.setCellValueFactory(new MapValueFactory("4"));
       tv1_time.setCellValueFactory(new MapValueFactory("5"));
       tv1_ratio.setCellValueFactory(new MapValueFactory("6"));
       tv1_speed.setCellValueFactory(new MapValueFactory("7"));
       
       tv2_name.setCellValueFactory(new MapValueFactory("0"));
       tv2_nc.setCellValueFactory(new MapValueFactory("1"));
       tv2_nd.setCellValueFactory(new MapValueFactory("2"));
       tv2_cr.setCellValueFactory(new MapValueFactory("3"));
       tv2_cs.setCellValueFactory(new MapValueFactory("4"));
       tv2_dr.setCellValueFactory(new MapValueFactory("5"));
       tv2_ds.setCellValueFactory(new MapValueFactory("6"));
    }

    /**
     * @brief Descriu l'accio que realitza el listener CompressFile
     * \pre true
     * \post Es canvia l'estat a estat FileCompressState
     */
    public void listener_CompressFile()
    {
        changeState(new FileCompressState(this, pc, dc));
    }

    /**
     * @brief Descriu l'accio que realitza el listener CompressFolder
     * \pre true
     * \post Es canvia l'estat a estat FolderCompressState
     */
    public void listener_CompressFolder()
    {
        changeState(new FolderCompressState(this, pc, dc));
    }

    /**
     * @brief Descriu l'accio que realitza el listener decompressFile
     * \pre true
     * \post Es canvia l'estat a estat DecompressState
     */
    public void listener_Decompress()
    {
        changeState(new DecompressState(this, pc, dc));
    }

    /**
     * @brief Descriu l'accio que realitza el listener Compare
     * \pre true
     * \post Es canvia l'estat a estat CompareState
     */
    public void listener_Compare()
    {
        changeState(new CompareState(this, pc, dc));
    }

    /**
     * @brief Descriu l'accio que realitza el listener GlobalStatistics
     * \pre true
     * \post Es canvia l'estat a estat GlobalStatisticsState
     */
    public void listener_GlobalStatistics()
    {
        if(state.getClass() != GlobalStatisticsState.class)
        {
            changeState(new GlobalStatisticsState(this, pc, dc));
        }
    }

    /**
     * @brief Descriu l'accio que realitza el listener History
     * \pre true
     * \post Es canvia l'estat a estat HistoryState
     */
    public void listsner_History()
    {
        if(state.getClass() != HistoryState.class)
        {
            changeState(new HistoryState(this, pc, dc));
        }
    }

    /**
     * @brief Descriu l'accio que realitza el listener Input
     * \pre true
     * \post L'estat state executa el corresponent comportament del listener Input
     */
    public void listener_Input()
    {
        state.specificlistener_Input();
    }

    /**
     * @brief Descriu l'accio que realitza el listener Output
     * \pre true
     * \post L'estat state executa el corresponent comportament del listener Output
     */
    public void listener_Output()
    {
        state.specificlistener_Output();
    }

    /**
     * @brief Descriu l'accio que realitza el listener Help
     * \pre true
     * \post L'estat state executa el corresponent comportament del listener Help
     */
    public void listener_Help()
    {
        state.specificlistener_Help();
    }

    /**
     * @brief Descriu l'accio que realitza el listener Action
     * \pre true
     * \post L'estat state executa el corresponent comportament del listener Action
     */
    public void listener_Action()
    {
        state.specificlistener_Action();
    }

    /**
     * @brief Canvia l'estat actual per l'estat que es desitja estar
     * \pre state valid
     * \post Es canvia l'estat actual per l'estat state
     * \param state L'estat que es desitja canviar
     */
    private void changeState(MainViewState state)
    {
        this.state = state;
        state.setLayout();
    }

    /**
     * @brief Activa la rodona de progres
     * \pre true
     * \post Es fa visible la rodona de progres i el anchorpane
     */
    void enableLoading()
    {
        pi_Loading.setVisible(true);
        anchorpane_Gray.setVisible(true);
    }

    /**
     * @brief Desactiva la rodona de progres
     * \pre true
     * \post Es fa invisible la rodona de progres i el anchorpane
     */
    void disableLoading()
    {
        pi_Loading.setVisible(false);
        anchorpane_Gray.setVisible(false);
    }
}

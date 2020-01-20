/**
 * @file GlobalStatisticsState.java
 */
package Presentation;

import java.util.ArrayList;
import java.util.HashMap;

import Domain.DomainController;
import Global.Pair;

/**
 * @class GlobalStatisticsState
 * @brief Estat de mostrar les estadístiques globals
 * Es defineix el comportament dels diferents listeners quan es troba en l'estat de mostrar estadístiques globals.
 */
public class GlobalStatisticsState extends MainViewState {
    /**
     * @brief Constructor a partir de la vista principal, el controlador de presentació i el controlador de domini
     * \pre true
     * \post S'ha creat l'estat GlobalStatistics passant a la seva superclasse els atributs necessaris
     * \param mv Vista principal
     * \param pc Controlador de presentació
     * \param dc Controlador de domini
     */
    GlobalStatisticsState(MainView mv, PresentationController pc, DomainController dc) {
        super(mv, pc, dc);
    }

    /**
     * @brief Funció modificadora que activa els elements que seran visibles
     * \pre true
     * \post S'ha activat els elements visibles de l'estat GlobalStatistics
     */
    @Override
    void specificSetLayout()
    {
    	mv.tableview_Tableview2.setVisible(true);
        mv.text_Title.setText("Global Statistics");
        
        ArrayList<Pair<String,ArrayList<String>>> stat = dc.getStatistics();
        for (Pair<String,ArrayList<String>> tmp : stat) {
        	HashMap<String, String> tmp2 = new HashMap<String,String>();
        	tmp2.put("0", tmp.first());
        	tmp2.put("1", tmp.second().get(0));
            tmp2.put("2", tmp.second().get(1));
            tmp2.put("3", PresentationController.makeHR(tmp.second().get(2), PresentationController.HRType.ratio));
            tmp2.put("4", PresentationController.makeHR(tmp.second().get(3), PresentationController.HRType.speed));
            tmp2.put("5", PresentationController.makeHR(tmp.second().get(4), PresentationController.HRType.ratio));
            tmp2.put("6", PresentationController.makeHR(tmp.second().get(5), PresentationController.HRType.speed));
        	mv.tableview_Tableview2.getItems().add(tmp2);
        }
        mv.button_globalStatistics.setDisable(true);
    }

    /**
     * @brief Descriu l'acció que realitza el listener Help
     * \pre true
     * \post Es realitza el comportament del listener Help,  mostra les instruccions de la forma de visualització de  Global Statistics.
     */
    @Override
    void specificlistener_Help()
    {
        String s;
        s = "The table shows the following features for each algorithm:" +
                "\n\n- Number of compressions" +
                "\n\n- Compression ratio" +
                "\n\n- Compression speed" +
                "\n\n- Number of decompressions" +
                "\n\n- Deompression ratio" +
                "\n\n- Deompression speed";
        pc.showHelp(s);
    }

}

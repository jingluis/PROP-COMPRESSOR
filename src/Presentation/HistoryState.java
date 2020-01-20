/**
 * @file HistoryState.java
 */
package Presentation;

import Domain.DomainController;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryState extends MainViewState {

	/**
	 * @brief Constructor a partir de la vista principal, el controlador de presentació i el controlador de domini
	 * \pre true
	 * \post S'ha creat l'estat History passant a la seva superclasse els atributs necessaris
	 * \param mv Vista principal
	 * \param pc Controlador de presentació
	 * \param dc Controlador de domini
	 */
    HistoryState(MainView mv, PresentationController pc, DomainController dc) {
        super(mv, pc, dc);
    }

	/**
	 * @brief Funció modificadora que activa els elements que seran visibles
	 * \pre true
	 * \post S'ha activat els elements visibles de l'estat History, si no hi ha cap entrada emmagatzemada, mostrara la pantalla d'error.
	 */
    @Override
    void specificSetLayout()
    {
    	mv.tableview_Tableview1.setVisible(true);
        mv.text_Title.setText("History Entry");
		mv.button_history.setDisable(true);

		Thread t = new Thread(new Runnable() {
			public void run()
			{
				try
				{
					ArrayList<ArrayList<String>> hist = dc.getHistory();
					for (ArrayList<String> tmp : hist) {
						HashMap<String, String> tmp2 = new HashMap<String,String>();
						tmp2.put(String.valueOf("0"), tmp.get(0));
						tmp2.put(String.valueOf("1"), tmp.get(1));
						tmp2.put(String.valueOf("2"), tmp.get(2));
						tmp2.put(String.valueOf("3"), PresentationController.makeHR(tmp.get(3), PresentationController.HRType.size));
						tmp2.put(String.valueOf("4"), PresentationController.makeHR(tmp.get(4), PresentationController.HRType.size));
						tmp2.put(String.valueOf("5"), PresentationController.makeHR(tmp.get(5), PresentationController.HRType.time));
						tmp2.put(String.valueOf("6"), PresentationController.makeHR(tmp.get(6), PresentationController.HRType.ratio));
						tmp2.put(String.valueOf("7"), PresentationController.makeHR(tmp.get(7), PresentationController.HRType.speed));
						Platform.runLater(() -> mv.tableview_Tableview1.getItems().add(tmp2));
					}
				}
				catch(DomainController.DomainControllerException e)
				{
					Platform.runLater(() -> pc.showError(e.getMessage()));
				}
			}
		});
		t.start();
    }

	/**
	 * @brief Descriu l'acció que realitza el listener Help
	 * \pre true
	 * \post Es realitza el comportament del listener Help,  mostra les instruccions de la forma de visualització de la funcionalitat History.
	 */
    @Override
    void specificlistener_Help()
    {
        String s;
        s = "The table shows the following features for each algorithm:" +
                "\n\n- Action: compression/decompression" +
                "\n\n- Algorithm used" +
                "\n\n- Date" +
                "\n\n- Statistics";
        pc.showHelp(s);
    }

}
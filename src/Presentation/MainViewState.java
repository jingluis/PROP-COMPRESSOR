/**
 * @file MainViewState.java
 */
package Presentation;

import Domain.DomainController;

/**
 * @class MainViewState
 * @brief Classe abstracta que representa l'estat de l'interfície
 * Es defineix l'estat que es troba l'interfície
 */
abstract class MainViewState
{
    /** @brief La vista principal */
    protected MainView mv;
    /** @brief El controlador de presentació */
    protected PresentationController pc;
    /** @brief El controlador de domini */
    protected DomainController dc;

    /**
     * @brief Constructor a partir de la vista principal, el controlador de presentació i el controlador de domini
     * \pre true
     * \post S'ha creat una instància de MainViewState amb els atributs necessaris
     * \param mv Vista princial
     * \param pc Controlador de presentació
     * \param dc Controlador de domini
     */
    MainViewState(MainView mv, PresentationController pc, DomainController dc)
    {
        this.mv = mv;
        this.pc = pc;
        this.dc = dc;
    }

    /**
     * @brief Funcio modificadora que activa els elements que seran visibles de forma comuna per tots els estats i els elements específics de cada estat
     * \pre true
     * \post S'ha activat els elements visibles comuns i específics de cada estat
     */
    void setLayout()
    {
        mv.tableview_Tableview1.setVisible(false);
        mv.tableview_Tableview2.setVisible(false);
        mv.label_Output.setVisible(false);
        mv.textfield_Output.setVisible(false);
        mv.button_Output.setVisible(false);
        mv.label_Algorithm1.setVisible(false);
        mv.label_Algorithm2.setVisible(false);
        mv.combobox_Algorithm1.setVisible(false);
        mv.combobox_Algorithm2.setVisible(false);
        mv.combobox_Algorithm1.setDisable(true);
        mv.combobox_Algorithm2.setDisable(true);
        mv.textfield_Input.clear();
        mv.textfield_Output.clear();
        mv.tableview_Tableview1.getItems().clear();
        mv.tableview_Tableview2.getItems().clear();
        mv.combobox_Algorithm1.getItems().clear();
        mv.combobox_Algorithm2.getItems().clear();

        mv.button_compressFile.setDisable(false);
        mv.button_compressFolder.setDisable(false);
        mv.button_decompress.setDisable(false);
        mv.button_compare.setDisable(false);
        mv.button_globalStatistics.setDisable(false);
        mv.button_history.setDisable(false);

        mv.inputSet = false;

        specificSetLayout();
    }

    /**
     * @brief Funció modificadora que activa els elements que seran visibles de l'estat específic
     * \pre true
     * \post S'ha activat els elements visibles de l'estat específic
     */
    void specificSetLayout() {}
    /**
     * @brief Descriu l'acció que realitza el listener Input
     * \pre true
     * \post Es realitza el comportament del listener Input
     */
    void specificlistener_Input() {}
    /**
     * @brief Descriu l'acció que realitza el listener Output
     * \pre true
     * \post Es realitza el comportament del listener Output
     */
    void specificlistener_Output() {}
    /**
     * @brief Descriu l'acció que realitza el listener Help
     * \pre true
     * \post Es realitza el comportament del listener Help
     */
    void specificlistener_Help() {}
    /**
     * @brief Descriu l'acció que realitza el listener Action
     * \pre true
     * \post Es realitza el comportament del listener Action
     */
    void specificlistener_Action() {}
}

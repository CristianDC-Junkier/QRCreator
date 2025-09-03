package junkier.qrcreator.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Controlador que controla el dialog de información, mostrandole al usuario la
 * información o errores
 *
 * @author Cristian Delgado Cruz
 * @since 2025-09-03
 * @version 1.0
 */
public class InfoController {

    @FXML
    private Label infoMessageLabel;

    private Stage dialogStage;

    @FXML
    private void acceptInfoDialog() {
        dialogStage.close();
    }

    private void setInfoMessageLabel(String infoMessageLabel) {
        this.infoMessageLabel.setText(infoMessageLabel);
    }

    private void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private Stage getDialogStage() {
        return dialogStage;
    }

    /**
     * Muestra el cuadro de diálogo de informacion de forma modal.
     *
     * @param parentStage El escenario principal (o la ventana que invoca este
     * diálogo)
     * @param infoMessage El mensaje de información que se quiere mostrar
     * información
     * @param error Comprueba si el mensaje es un error
     */
    public static void showInfoDialog(Stage parentStage, String infoMessage, boolean error) {
        try {
            FXMLLoader loader = new FXMLLoader(InfoController.class.getResource("/junkier/qrcreator/view/info.fxml"));
            AnchorPane page = loader.load();

            InfoController infoController = loader.getController();
            infoController.setInfoMessageLabel(infoMessage);

            infoController.setDialogStage(new Stage());
            if (error) {
                infoController.getDialogStage().setTitle("Error");
            } else {
                infoController.getDialogStage().setTitle("Información");
            }
            infoController.getDialogStage().initModality(javafx.stage.Modality.APPLICATION_MODAL);
            infoController.getDialogStage().initOwner(parentStage);
            infoController.getDialogStage().getIcons().add(new Image(InfoController.class.getResourceAsStream("/junkier/qrcreator/icons/icon-ayunt.png")));

            // Establecer la escena y mostrar el diálogo
            Scene scene = new Scene(page, 350, 250);
            infoController.getDialogStage().setScene(scene);
            infoController.getDialogStage().showAndWait();
        } catch (IOException ioE) {
            ioE.printStackTrace();
            System.out.println("Fallo en el Dialog");
        }
    }
}

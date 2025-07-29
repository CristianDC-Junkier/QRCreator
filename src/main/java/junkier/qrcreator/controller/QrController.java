package junkier.qrcreator.controller;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import junkier.qrcreator.services.QrGeneratorService;
import junkier.qrcreator.services.LuminaliaService;

/**
 * FXML Controller class
 *
 * @author Cristian
 */
public class QrController implements Initializable {

    @FXML
    private ColorPicker backGrPicker;
    @FXML
    private ColorPicker frontPatPicker;
    @FXML
    private TextField nameQrTF;
    @FXML
    private TextField adressQrTF;
    @FXML
    private Text imagePathField;

    private static final String INVALID_FILENAME_CHARS = "[\\\\/:*?\"<>|]";
    protected final String errorStyle
            = "-fx-background-color: "
            + "linear-gradient"
            + "(from 0% 0% to 100% 100%, #e52d27, #b31217);";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * Metodo que imprime el Qr, o muestra el error según donde el usuario se
     * equivocó
     */
    @FXML
    private void printQr() {
        if (nameQrTF.getText() == null || nameQrTF.getText().isBlank() || isNotValidFileName()) {
            nameQrTF.setStyle(errorStyle);
            return;
        }
        if (adressQrTF.getText() == null || adressQrTF.getText().isBlank()) {
            adressQrTF.setStyle(errorStyle);
            return;
        }
        if (!LuminaliaService.isContrastSufficient(frontPatPicker.getValue(), backGrPicker.getValue())) {
            frontPatPicker.setStyle(errorStyle);
            backGrPicker.setStyle(errorStyle);
            return;
        }
        String result = QrGeneratorService.generator(adressQrTF.getText(), nameQrTF.getText(),
                imagePathField.getText(), frontPatPicker.getValue(), backGrPicker.getValue());
        if (result != null) {
            try {
                File file = new File(result);
                if (file.exists()) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error al abrir el archivo");
            }
        } else {
            System.out.println("Resultado erroneo");
        }
    }

    @FXML
    private void serviceterms() {
        System.exit(0);
    }

    @FXML
    private void clean() {
        backGrPicker.setValue(Color.WHITE);
        frontPatPicker.setValue(Color.BLACK);

        nameQrTF.setText("");
        adressQrTF.setText("");
        imagePathField.setText("");
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    @FXML
    private void refreshName() {
        nameQrTF.setStyle("");
    }

    @FXML
    private void refreshAdress() {
        adressQrTF.setStyle("");
    }
    @FXML
    private void refreshPicker() {
        frontPatPicker.setStyle("");
        backGrPicker.setStyle("");
    }

    @FXML
    private void chooseImageFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona una imagen Central");

        // Filtros para tipos de archivo válidos
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png")
        );

        // Directorio por defecto
        String userHome = System.getProperty("user.home");
        File defaultDir = new File(userHome, "Pictures");
        if (defaultDir.exists()) {
            fileChooser.setInitialDirectory(defaultDir);
        }

        // Mostrar diálogo de selección
        File selectedFile = fileChooser.showOpenDialog(imagePathField.getScene().getWindow());
        if (selectedFile != null && selectedFile.getName().toLowerCase().endsWith(".png")) {
            imagePathField.setText(selectedFile.getAbsolutePath());
        } else {
            imagePathField.setText(null);
        }
    }

    private boolean isNotValidFileName() {
        return nameQrTF.getText().matches(".*" + INVALID_FILENAME_CHARS + ".*");
    }

}

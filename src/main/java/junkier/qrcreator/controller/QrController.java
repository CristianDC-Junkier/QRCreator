package junkier.qrcreator.controller;

import junkier.qrcreator.services.QrGeneratorService;
import junkier.qrcreator.services.QrReadService;
import junkier.qrcreator.services.QrTransformEyesService.EyeShape;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

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
    @FXML
    private ImageView qrImage;
    @FXML
    private ChoiceBox<EyeShape> eyesCB;

    private static final String INVALID_FILENAME_CHARS = "[\\\\/:*?\"<>|]";
    protected final String errorStyle
            = "-fx-background-color: "
            + "linear-gradient"
            + "(from 0% 0% to 100% 100%, #e52d27, #b31217);";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        eyesCB.setItems(FXCollections.observableArrayList(EyeShape.values()));
        eyesCB.setValue(eyesCB.getItems().getFirst());
        // Listener para cambios de selección
        eyesCB.valueProperty().addListener((observable, oldValue, newValue) -> {
            previewQr();
        });
        previewQr();
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    @FXML
    private void clean() {
        backGrPicker.setValue(Color.WHITE);
        frontPatPicker.setValue(Color.BLACK);
        eyesCB.setValue(eyesCB.getItems().getFirst());

        nameQrTF.setText("");
        adressQrTF.setText("");
        imagePathField.setText("");
        frontPatPicker.setStyle("");
        backGrPicker.setStyle("");

        previewQr();
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

        if (isNotReadeable()) {
            frontPatPicker.setStyle(errorStyle);
            backGrPicker.setStyle(errorStyle);
            return;
        }

        String result = QrGeneratorService.generator(
                adressQrTF.getText(),
                nameQrTF.getText(),
                imagePathField.getText(),
                frontPatPicker.getValue(),
                backGrPicker.getValue(),
                eyesCB.getValue()
        );
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
    private void previewQr() {
        String content = adressQrTF.getText().isBlank() ? "null" : adressQrTF.getText();

        BufferedImage qr = QrGeneratorService.generatorPreview(
                content,
                imagePathField.getText(),
                frontPatPicker.getValue(),
                backGrPicker.getValue(),
                eyesCB.getValue()
        );

        Image preview = SwingFXUtils.toFXImage(qr, null);

        if (preview != null) {
            qrImage.setImage(preview);
        }
    }

    @FXML
    private void refreshName() {
        nameQrTF.setStyle("");
    }

    @FXML
    private void refreshAdress() {
        adressQrTF.setStyle("");
        previewQr();
    }

    @FXML
    private void refreshPicker() {
        if (isNotReadeable()) {
            frontPatPicker.setStyle(errorStyle);
            backGrPicker.setStyle(errorStyle);
        } else {
            frontPatPicker.setStyle("");
            backGrPicker.setStyle("");
        }
        previewQr();
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

        previewQr();
    }

    @FXML
    private void dropImageFile() {
        imagePathField.setText(null);
        previewQr();
    }

    private boolean isNotReadeable() {
        BufferedImage qr = QrGeneratorService.generatorPreview(
                adressQrTF.getText(),
                imagePathField.getText(),
                frontPatPicker.getValue(),
                backGrPicker.getValue(),
                eyesCB.getValue()
        );
        return !QrReadService.isQrReadable(qr);
    }

    private boolean isNotValidFileName() {
        return nameQrTF.getText().matches(".*" + INVALID_FILENAME_CHARS + ".*");
    }

}

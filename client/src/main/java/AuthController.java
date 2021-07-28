import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.*;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class AuthController {
    public TextField loginTF;
    public PasswordField passwordTF;
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;


    @FXML
    private void initialize() throws IOException {
        try {
            Socket socket = new ConnectionServer().getSocket();
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        AbstractCommand command = (AbstractCommand) is.readObject();
                        switch (command.getType()) {
                            case AUTHOK:
                                Platform.runLater(() -> {
                                    Stage stage = (Stage) loginTF.getScene().getWindow();
                                    stage.close();
                                });
                                break;
                            case AUTHERR:
                                Platform.runLater(() -> {
                                    alertWin("Ошибка авторизации", "Что-то пошло не так!");
                                });
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void auth() throws IOException, SQLException {
        if (passwordTF.getText() != null && !passwordTF.getText().trim().isEmpty()) {
            os.writeObject(new AuthOk(loginTF.getText(), passwordTF.getText()));
            os.flush();
        } else {
            alertWin("Введите пароль!", "Password cannot be empty");
        }
    }

    private void alertWin(String s1, String s2){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(s1);
        alert.setHeaderText(null);
        alert.setContentText(s2);
        alert.showAndWait();
    }

    @FXML
    private String getLogin() throws IOException {
        return loginTF.getText();
    }
}
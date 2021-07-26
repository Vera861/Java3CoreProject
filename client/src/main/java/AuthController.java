import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

public class AuthController {
    public TextField loginTF;
    public PasswordField passwordTF;
    private DataInputStream in;
    private DataOutputStream out;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() throws IOException {

        Socket socket = new Socket("localhost", 8089);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String strFromServer = in.readUTF();
                        if (strFromServer.startsWith("/authok")) {
                            Platform.runLater(() -> {
                                Stage stage = (Stage) loginTF.getScene().getWindow();
                                stage.close();
                            });
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @FXML
    private void auth() throws IOException {
        try (Connection connection = DriverManager
                .getConnection("jdbc:postgresql://Localhost:5432/auth_users", "Evka", "123");) {
            PreparedStatement ps = connection.prepareStatement("select * from auth_user where login_us =? and pass_us=?");
//            String login = "vera";
            ps.setString(2, loginTF.getText());
            ps.setString(3, passwordTF.getText());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                System.out.println(rs.getString(1));
                is_auth_user(rs.getString(3), passwordTF.getText());

            }

        } catch (SQLException e) {
e.printStackTrace();
        }
        if (passwordTF.getText() == null && passwordTF.getText().trim().isEmpty()) {
//            String authString = "/auth " + loginTF.getText() + " " + passwordTF.getText();
//            out.writeUTF(authString);
//        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Password cannot be empty");
            alert.setHeaderText(null);
            alert.setContentText("Введите пароль!");
            alert.showAndWait();
        }
    }

    private boolean is_auth_user(String pass1,String pass2){
        if(pass1.equals(pass2))
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Авторизация прошла успешно");
            alert.setHeaderText(null);
            alert.setContentText("Авторизация");
            alert.showAndWait();
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Пользователь не найден");
            alert.setHeaderText(null);
            alert.setContentText("Авторизация");
            alert.showAndWait();

        }
        return false;
    }

    @FXML
    private String getLogin() throws IOException {
        return loginTF.getText();
    }
}
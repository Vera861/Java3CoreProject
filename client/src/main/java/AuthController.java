import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.AbstractCommand;
import model.AuthUser;
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
    public String nick;
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;

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
        os = new ObjectEncoderOutputStream(socket.getOutputStream());
        is = new ObjectDecoderInputStream(socket.getInputStream());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        AbstractCommand command = (AbstractCommand) is.readObject();
                        switch (command.getType()) {
                            case AUTOK:
                                Platform.runLater(() -> {
                                    Stage stage = (Stage) loginTF.getScene().getWindow();
                                    stage.close();
                                });
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
            ps.setString(1, loginTF.getText());
            ps.setString(2, passwordTF.getText());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                nick = rs.getString("nick_us");
                break;
        }
    } catch(SQLException e){
        e.printStackTrace();
    }
        if(passwordTF.getText()==null&&passwordTF.getText().trim().isEmpty())
    {
        os.writeObject(new AuthUser());
        os.flush();
    }
}
    @FXML
    private String getLogin() throws IOException {
        return loginTF.getText();
    }
}
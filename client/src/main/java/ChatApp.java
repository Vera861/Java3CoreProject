import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fileMng.fxml"));
        primaryStage.setTitle("File Manager");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}

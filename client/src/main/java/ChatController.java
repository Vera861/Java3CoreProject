import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.*;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


@Slf4j

public class ChatController implements Initializable {

    public TextField clientPath;
    public TextField serverPath;
    public ListView<String> clientList;
    public ListView<String> serverList;
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    private Path currentDir;

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
                openLoginWindow();
        try {
            String userDir = System.getProperty("user.name");
            currentDir = Paths.get("/Users", userDir).toAbsolutePath();
            log.info("Current user: {}", System.getProperty("user.name"));
            Socket socket = new ConnectionServer().getSocket();
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            refreshClientView();
            addNavigationListener();

            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        AbstractCommand command = (AbstractCommand) is.readObject();
                        switch (command.getType()) {
                            case LIST_MESSAGE:
                                ListResponse response = (ListResponse) command;
                                List<String> names = response.getNames();
                                refreshServerView(names);
                                break;
                            case PATH_RESPONSE:
                                PathUpResponse pathUpResponse = (PathUpResponse) command;
                                String path = pathUpResponse.getPath();
                                Platform.runLater(() -> serverPath.setText(path));
                                break;
                            case MESSAGE:
                                Message message = (Message) command;
                                Files.write(currentDir.resolve(message.getName()), message.getData());
                                refreshClientView();
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

    private void refreshServerView(List<String> names) {
        Platform.runLater(() -> {
            serverList.getItems().clear();
            serverList.getItems().addAll(names);
        });
    }

    private void addNavigationListener() {
        clientList.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                String item = clientList.getSelectionModel().getSelectedItem();
                Path newPath = currentDir.resolve(item);
                if (Files.isDirectory(newPath)) {
                    currentDir = newPath;
                    try {
                        refreshClientView();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });
        serverList.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                String item = serverList.getSelectionModel().getSelectedItem();
                try {
                    os.writeObject(new PathInRequest(item));
                    os.flush();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    private void refreshClientView() throws IOException {
        clientPath.setText(currentDir.toString());
        List<String> names = Files.list(currentDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(() -> {
            clientList.getItems().clear();
            clientList.getItems().addAll(names);
        });
    }

    private void openLoginWindow() throws IOException {
        Parent root = FXMLLoader.load(ClassLoader.getSystemResource("auth.fxml"));
        Stage loginStage = new Stage();
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setScene(new Scene(root));
        loginStage.setTitle("Авторизация");
        loginStage.showAndWait();
    }

    public void upFile() throws IOException {
        String fileName = clientList.getSelectionModel().getSelectedItem();
        Message message = new Message(currentDir.resolve(fileName));
        os.writeObject(message);
        os.flush();
    }

    public void downFile() throws IOException {
        String fileName = serverList.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileName));
        os.flush();
    }

    public void clientUp() throws IOException {
        currentDir =  currentDir.getParent();
        clientPath.setText(currentDir.toString());
        refreshClientView();

    }
    public void serverUp() throws IOException {
        os.writeObject(new PathUpRequest());
        os.flush();
    }

    public void delFile() throws IOException {
        String fileName = serverList.getSelectionModel().getSelectedItem();
        os.writeObject(new FileDel(currentDir.resolve(fileName)));
        os.flush();
    }
}
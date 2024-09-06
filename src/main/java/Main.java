import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * A GUI for Duke using FXML.
 */
public class Main extends Application {
    private BeeBot beebot;

    @Override
    public void start(Stage stage) {
        String taskFileUrl = "src/main/resources/data/BeeBot.txt";
        if (taskFileUrl != null) {
            beebot = new BeeBot(taskFileUrl);
        } else {
            System.out.println("File not found!");
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane ap = fxmlLoader.load();
            Scene scene = new Scene(ap);
            stage.setScene(scene);
            MainWindow controller = fxmlLoader.getController();
            controller.setBeeBot(beebot);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
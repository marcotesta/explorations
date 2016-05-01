package it.mondogrua.exploration;

import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.mondogrua.exploration.ReadNewlyAddedLinesToAFile.TailListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class FileUI extends Application {

    private static String fileName;

    private ReadNewlyAddedLinesToAFile tail;
    private WriteAtTheEndOfFile writer;

    private ExecutorService executorService;

    private TextArea textArea;
    private Scene scene;
    private Label lastLine;

    private TextField nextLine;

    public FileUI() throws Exception {

        RandomAccessFile input = new RandomAccessFile(fileName, "r");
        tail = new ReadNewlyAddedLinesToAFile(input, createTailListener());

        RandomAccessFile output = new RandomAccessFile(fileName, "rw");
        writer = new WriteAtTheEndOfFile(output);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        scene = createScene();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Prova");
        primaryStage.show();

        startReading();
    }

    @Override
    public void stop() throws Exception {
        stopReading();
    }

    private Scene createScene() {
        textArea = new TextArea();
        HBox nextLineBox = new HBox(10);
        nextLine = new TextField();
        nextLineBox.getChildren().add(nextLine);
        Button nextLineButton = new Button("Submit");
        nextLineButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String text = nextLine.getText();
                if ((text != null && !text.isEmpty())) {
                    writer.write(text);
                }
            }
        });

        nextLineBox.getChildren().add(nextLineButton);

        lastLine = new Label();
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));
        pane.setTop(nextLineBox);
        pane.setCenter(textArea);
        HBox lastLineBox = new HBox(10);
        lastLineBox.getChildren().add(new Label("Last inserted line: "));
        lastLine.setStyle("-fx-font-weight: bold");
        lastLineBox.getChildren().add(lastLine);
        pane.setBottom(lastLineBox);
        Scene scene = new Scene(pane);
        return scene;
    }

    private void startReading() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                tail.readNewLines();
            }
        });
    }

    private void stopReading() {
        tail.stopReading();
        executorService.shutdown();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println(
                    "Usage: java it.mondogrua.explorations.FileUI <filename>");
            return;
        }
        fileName = args[0];

        launch(args);
    }

    private TailListener createTailListener() {
        return new TailListener() {

            @Override
            public void handle(String line) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        textArea.appendText(line);
                        textArea.appendText("\n");
                        lastLine.setText(line);
                    }
                });
            }

            @Override
            public void handle(Exception ex) {
                System.out.println(ex.getMessage());
            }
        };
    }
}

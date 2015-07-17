package frames;

import sockets.SocketServer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ServerFrame extends Application {

	private BorderPane root;
    private Button serverStartButton;
    private Button serverStopButton;
    public TextArea textArea;
    
    public SocketServer server;
    public Thread serverThread;
        
	@Override
	public void start(Stage primaryStage) throws Exception {
		root = new BorderPane();
        Scene scene = new Scene(root, 500, 500);
        primaryStage.setTitle("Chat Server");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("icon.png"));
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				System.exit(0);
			}
		});

        HBox buttonRow = new HBox();
        buttonRow.setStyle("-fx-background-color:#add8e6");
        buttonRow.setPadding(new Insets(5,5,5,5));
        buttonRow.setSpacing(150);
        buttonRow.setAlignment(Pos.BASELINE_CENTER);
        root.setTop(buttonRow);

        serverStartButton = new Button("Start");
        serverStartButton.setTooltip(new Tooltip("This button starts server."));
        serverStartButton.setId("button");
        serverStartButton.getStylesheets().add("design.css");
        serverStartButton.setOnAction(event -> serverStartButtonActionPerformed());

        serverStopButton = new Button("Stop");
        serverStopButton.setTooltip(new Tooltip("This button stops server."));
        serverStopButton.setId("button");
        serverStopButton.setDisable(true);
        serverStopButton.getStylesheets().add("design.css");
        serverStopButton.setOnAction(event -> serverStopButtonActionPerformed());

        buttonRow.getChildren().addAll(serverStartButton, serverStopButton);
        
        textArea = new TextArea();
        textArea.setEditable(false);
        root.setCenter(textArea);
	}
	
	private void serverStopButtonActionPerformed() {
		textArea.appendText("Server is stopped. Push Start Button to start server.\n");
		server.stop();
		serverStartButton.setDisable(false);
		serverStopButton.setDisable(true);
	}
	
	private void serverStartButtonActionPerformed() {
		server = new SocketServer(this);
	    serverStartButton.setDisable(true);
	    serverStopButton.setDisable(false);
	}
	
	public void retryStart(int port) {
		if(server != null) {
			server.stop();
		}
		server = new SocketServer(this, port);
	}

	public static void main(String[] args) {
		launch(args);
	}
}

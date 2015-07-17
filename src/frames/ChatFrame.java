package frames;

import sockets.SocketClient;
import message.Message;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ChatFrame extends Application{

	private BorderPane root;
	public SocketClient client;
	public int port;
	public String serverAddr;
	public String username;
	public Thread clientThread;
	public ObservableList<String> listOfContacts;
	public Button connectButton; 
	public Button loginButton; 
	public Button sendButton; 
	private Label hostAddressLabel;
	private Label hostPortLabel;
	private Label userNameLabel;
	private Label messageLabel;
	public ListView<String> listView;
	public TextArea outputMessageArea;
	public TextField hostTextField;
	public TextField portTextField;
	public TextField userNameTextField;
	public TextArea inputMessageArea;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		root = new BorderPane();
        Scene scene = new Scene(root, 500, 600);
        
        primaryStage.setTitle("Chat by RK");
        primaryStage.getIcons().add(new Image("icon.png"));
        GridPane top = new GridPane();
        top.setStyle("-fx-background-color:#add8e6");
        top.setAlignment(Pos.CENTER);
        HBox center = new HBox(); 
        VBox bottom = new VBox(); 
        HBox bottom2 = new HBox(); 
        
        hostAddressLabel = new Label("Host Address : ");
        hostPortLabel = new Label("Host Port : ");
        userNameLabel = new Label("Username :");
        messageLabel = new Label("Message : ");
        
        hostTextField = new TextField();
        portTextField = new TextField();
        userNameTextField = new TextField();
        
        inputMessageArea = new TextArea();
        outputMessageArea = new TextArea();
        
        listView = new ListView<String>();
        
        connectButton = new Button();
        sendButton = new Button();
        loginButton = new Button();

        hostTextField.setText("localhost");
        portTextField.setText("20000");

        connectButton.setText("Connect");
        connectButton.setId("button");
        connectButton.setPrefWidth(200);
        connectButton.getStylesheets().add("design.css");
        connectButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				connectButtonActionPerformed(event);
			}
		}); 

        userNameTextField.setText("Anonymous");
        userNameTextField.setDisable(true);

        outputMessageArea.setFont(new Font("Consolas", 12));
        outputMessageArea.setEditable(false);
        outputMessageArea.setPrefSize(350, 350);
        inputMessageArea.setFont(new Font("Consolas", 12));
        inputMessageArea.setEditable(false);
        inputMessageArea.setPrefSize(350, 50);
        
        listOfContacts = FXCollections.observableArrayList("All");
        listView = new ListView<String>(listOfContacts);
        listView.setCursor(Cursor.HAND);
        listView.setOrientation(Orientation.VERTICAL);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setVisible(true);
        listView.setPrefSize(150, 350);
        listView.getSelectionModel().select(0);
        
        Image send = new Image("send.png");
        sendButton.setGraphic(new ImageView(send));
        sendButton.setPrefSize(150, 50);
        sendButton.setDisable(true);
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
			public void handle(ActionEvent event) {
                sendButtonActionPerformed(event);
            }
        });

        loginButton.setText("Login");
        loginButton.setPrefWidth(200);
        loginButton.setDisable(true);
        loginButton.setId("button");
        loginButton.getStylesheets().add("design.css");
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
			public void handle(ActionEvent event) {
                loginButtonActionPerformed(event);
            }
        });
        
        Group group = new Group();
        group.getChildren().add(listView);
        center.getChildren().addAll(outputMessageArea, group);
        bottom2.getChildren().addAll(inputMessageArea, sendButton);
        bottom2.setSpacing(5);
        bottom.getChildren().addAll(messageLabel, bottom2);
        bottom.setPadding(new Insets(5, 5, 5, 5));
        bottom.setStyle("-fx-background-color:#add8e6");
        top.add(hostAddressLabel, 0, 0);
        top.add(hostPortLabel, 0, 1);
        top.add(hostTextField, 1, 0);
        top.add(portTextField, 1, 1);
        top.add(connectButton, 2, 0, 1, 2);
        top.add(userNameLabel, 0, 2);
        top.add(userNameTextField, 1, 2);
        top.add(loginButton, 2, 2);
        top.setPadding(new Insets(5,5,5,5));
        top.setHgap(5);
        top.setVgap(5);
        
        root.setTop(top);
        root.setCenter(center);
        root.setBottom(bottom);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					client.send(new Message("message", username, ".bye", "SERVER"));
	                clientThread.interrupt();
	            } catch(Exception ex) {
                }
			}
        });
	}

	private void connectButtonActionPerformed(ActionEvent evt) {
		serverAddr = hostTextField.getText();
	    port = Integer.parseInt(portTextField.getText());

	    if (!serverAddr.isEmpty() && !portTextField.getText().isEmpty()) {
	    	try {
	    		client = new SocketClient(this);
	            clientThread = new Thread(client);
	            clientThread.start();
	            client.send(new Message("test", "testUser", "testContent", "SERVER"));
	        } catch(Exception ex) {
	        	outputMessageArea.appendText("[Application > Me] : Server not found\n");
	        }
	    }
	}

	private void loginButtonActionPerformed(ActionEvent evt) {
		username = userNameTextField.getText();
		for (int i = 0; i < listOfContacts.size(); i++) {
			if (username.equalsIgnoreCase(listOfContacts.get(i))) {
				outputMessageArea.setText("This user name is already exist!");
				return;
			}
		}
	    if (!username.isEmpty()) {
	    	client.send(new Message("login", username, "","SERVER"));
	    	inputMessageArea.setEditable(true);
	    } else {
	    	outputMessageArea.setText("Enter Your name!");
	    }
	}

	private void sendButtonActionPerformed(ActionEvent evt) {
		String msg = inputMessageArea.getText();
	    String target = listView.getSelectionModel().getSelectedItem();

	    if (!msg.isEmpty() && !target.isEmpty()) {
	    	inputMessageArea.setText("");
	        client.send(new Message("message", username, msg, target));
	    }
	}

	public static void main(String[] args) {
		launch(args);
	}
}

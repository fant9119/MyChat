package sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javafx.application.Platform;
import message.Message;
import frames.ChatFrame;


public class SocketClient implements Runnable {
	public int port;
    public String serverAddr;
    public Socket socket;
    public ChatFrame ui;
    public ObjectInputStream In;
    public ObjectOutputStream Out;

    public SocketClient(ChatFrame frame) throws IOException {
        ui = frame; this.serverAddr = ui.serverAddr; this.port = ui.port;
        socket = new Socket(InetAddress.getByName(serverAddr), port);

        Out = new ObjectOutputStream(socket.getOutputStream());
        Out.flush();
        In = new ObjectInputStream(socket.getInputStream());

    }

    @Override
    public void run() {
        boolean keepRunning = true;
        while(keepRunning){
            try {
                Message msg = (Message) In.readObject();
                System.out.println("Incoming : "+msg.toString());

                if (msg.type.equals("message")) {
                    if (msg.recipient.equals(ui.username)) {
                        ui.outputMessageArea.appendText("["+msg.sender +" > Me] : " + msg.content + "\n");
                    } else {
                        ui.outputMessageArea.appendText("["+ msg.sender +" > "+ msg.recipient +"] : " + msg.content + "\n");
                    }

                } else if (msg.type.equals("login")) {
                    ui.loginButton.setDisable(true);
                    ui.sendButton.setDisable(false);
                    ui.outputMessageArea.appendText("[SERVER > Me] : Login Successful\n");
                    ui.userNameTextField.setDisable(true);
                } else if(msg.type.equals("test")){
                    ui.connectButton.setDisable(true);
                    ui.loginButton.setDisable(false);
                    ui.userNameTextField.setDisable(false);
                    ui.hostTextField.setDisable(true);
                    ui.portTextField.setDisable(true);

                } else if(msg.type.equals("newuser")) {
                    if(!msg.content.equals(ui.username)) {
                        boolean exists = false;
                        for(int i = 0; i < ui.listOfContacts.size(); i++){
                            if(ui.listOfContacts.get(i).equals(msg.content)){
                                exists = true; 
                                break;
                            }
                        }
                        if(!exists){ 
                        	Platform.runLater(new Runnable() {
								@Override
								public void run() {
									ui.listOfContacts.add(msg.content); 	
								}
                        	}); 	
                        } 
                    }
                }
                else if(msg.type.equals("signup")){
                    if(msg.content.equals("TRUE")){
                        ui.loginButton.setDisable(true);
                        ui.sendButton.setDisable(false);
                        ui.outputMessageArea.appendText("[SERVER > Me] : Singup Successful\n");
                    }
                    else{
                        ui.outputMessageArea.appendText("[SERVER > Me] : Signup Failed\n");
                    }
                }
                else if(msg.type.equals("signout")) {
                    if (msg.content.equals(ui.username)) {
                        ui.outputMessageArea.appendText("["+ msg.sender +" > Me] : Bye\n");
                        ui.connectButton.setDisable(false);
                        ui.sendButton.setDisable(true);
                        ui.hostTextField.setEditable(true);
                        ui.portTextField.setEditable(true);
                        
                        Platform.runLater(new Runnable() {
								@Override
								public void run() {
									for(int i = 1; i < ui.listOfContacts.size(); i++){
										ui.listOfContacts.remove(i);
									}
								}
                        	}); 
                        ui.clientThread.stop();
                    } else {
                    	Platform.runLater(new Runnable() {
							@Override
							public void run() {
								 ui.listOfContacts.remove(msg.content);
			                     ui.outputMessageArea.appendText("["+ msg.sender +" > All] : "+ msg.content +" has signed out\n");
							}
                    	});
                    }
                } else {
                    ui.outputMessageArea.appendText("[SERVER > Me] : Unknown message type\n");
                }
            } catch(Exception ex) {
                keepRunning = false;
                ui.outputMessageArea.appendText("[Application > Me] : Connection Failure\n");
                ui.connectButton.setDisable(false);
                ui.hostTextField.setEditable(true);
                ui.portTextField.setEditable(true);
                ui.sendButton.setDisable(true);

                Platform.runLater(new Runnable() {
					@Override
					public void run() {
						for(int i = 1; i < ui.listOfContacts.size(); i++){
							ui.listOfContacts.remove(i);
						}
					}
            	}); 

                ui.clientThread.stop();

                System.out.println("Exception SocketClient run()");
                ex.printStackTrace();
            }
        }
    }

    public void send(Message msg){
        try {
            Out.writeObject(msg);
            Out.flush();
            System.out.println("Outgoing : " + msg.toString());

        }
        catch (IOException ex) {
            System.out.println("Exception SocketClient send()");
        }
    }

    public void closeThread(Thread t){
        t = null;
    }
}

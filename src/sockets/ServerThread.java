package sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javafx.application.Platform;
import message.Message;
import frames.ServerFrame;
import sockets.SocketServer;


public class ServerThread extends Thread {

    public SocketServer server = null;
    public Socket socket = null;
    public int ID = -1;
    public String username = "";
    public ObjectInputStream streamIn  =  null;
    public ObjectOutputStream streamOut = null;
    public ServerFrame frame;

    public ServerThread(SocketServer server, Socket socket) {
        super();
        this.server = server;
        this.socket = socket;
        ID = socket.getPort();
        this.frame = server.serverFrame;
    }

    public void send(Message msg) {
        try {
            streamOut.writeObject(msg);
            streamOut.flush();
        } catch (IOException ex) {
            System.out.println("Exception [SocketClient : send(...)]");
        }
    }

    public int getID(){
        return ID;
    }


    public void run() {
    	Platform.runLater(new Runnable() {
		
			@Override
			public void run() {
				 frame.textArea.appendText("\nServer Thread " + ID + " running.");
			}
		});
       
        while (true) {
            try {
                Message msg = (Message) streamIn.readObject();
                server.handle(ID, msg);
            } catch(Exception e) {
                System.out.println(ID + " ERROR reading: " + e.getMessage());
                server.remove(ID);
                interrupt();
                return;
            }
        }
    }

    public void open() throws IOException {
        streamOut = new ObjectOutputStream(socket.getOutputStream());
        streamOut.flush();
        streamIn = new ObjectInputStream(socket.getInputStream());
    }

    public void close() throws IOException {
        if (socket != null)    socket.close();
        if (streamIn != null)  streamIn.close();
        if (streamOut != null) streamOut.close();
    }
}
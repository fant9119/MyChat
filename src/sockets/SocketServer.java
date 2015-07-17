package sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.application.Platform;
import message.Message;
import frames.ServerFrame;
import sockets.ServerThread;


public class SocketServer implements Runnable {

    public ServerThread[] clients;
    public ServerSocket server = null;
    public Thread thread = null;
    public int clientCount = 0;
    public int port = 20000;
    public ServerFrame serverFrame;

    public SocketServer(ServerFrame frame) {
        clients = new ServerThread[50];
        serverFrame = frame;

        try {
            server = new ServerSocket(port);
            port = server.getLocalPort();
            Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					try {
						serverFrame.textArea.appendText("Server started. IP : " + 
								InetAddress.getLocalHost() + ", Port : " + server.getLocalPort() + "\n");
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
			});
            start();
        } catch (IOException e){
        	Platform.runLater(new Runnable() {
        		
				@Override
				public void run() {
					serverFrame.textArea.appendText("Can not bind to port : " + port + "\nRetrying...\n");
				}
			});
            serverFrame.retryStart(0);
        }
    }

    public SocketServer(ServerFrame frame, int port) {
        clients = new ServerThread[50];
        serverFrame = frame;
        this.port = port;


        try {
            server = new ServerSocket(port);
            port = server.getLocalPort();
            Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					try {
						serverFrame.textArea.appendText("Server started. IP : " + 
								InetAddress.getLocalHost() + ", Port : " + server.getLocalPort() + "\n");
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
			});
            start();
        } catch (IOException e) {
            serverFrame.textArea.appendText("\nCan not bind to port " + port + ": " + e.getMessage() + "\n");
        }
    }

    @Override
    public void run() {
        while (thread != null) {
            try {
                serverFrame.textArea.appendText("\nWaiting for a client ...");
                addThread(server.accept());
            } catch(Exception e){
                serverFrame.textArea.appendText("\nServer accept error: " + e.getMessage() + "\n");
                serverFrame.retryStart(0);
            }
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
            server = null;
        }
    }

    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    public synchronized void handle(int ID, Message msg) {
        if (msg.content.equals(".bye")) {
            announce("signout", "SERVER", msg.sender);
            remove(ID);
        } else {
            if (msg.type.equals("login")) {
                if (findUserThread(msg.sender) == null) {
                    clients[findClient(ID)].username = msg.sender;
                    clients[findClient(ID)].send(new Message("login", "SERVER", "TRUE", msg.sender));
                    announce("newuser", "SERVER", msg.sender);
                    SendUserList(msg.sender);
                } else {
                    clients[findClient(ID)].send(new Message("login", "SERVER", "FALSE", msg.sender));
                }
            }
            else if (msg.type.equals("message")) {
                if(msg.recipient.equals("All")) {
                    announce("message", msg.sender, msg.content);
                } else {
                    findUserThread(msg.recipient).send(new Message(msg.type, msg.sender, msg.content, msg.recipient));
                    clients[findClient(ID)].send(new Message(msg.type, msg.sender, msg.content, msg.recipient));
                }
            }
            else if(msg.type.equals("test")) {
                clients[findClient(ID)].send(new Message("test", "SERVER", "OK", msg.sender));
            }
        }
    }

    public void announce(String type, String sender, String content) {
        Message msg = new Message(type, sender, content, "All");
        for (int i = 0; i < clientCount; i++) {
            clients[i].send(msg);
        }
    }

    public void SendUserList(String toWhom) {
        for (int i = 0; i < clientCount; i++) {
            findUserThread(toWhom).send(new Message("newuser", "SERVER", clients[i].username, toWhom));
        }
    }

    public ServerThread findUserThread(String usr) {
        for(int i = 0; i < clientCount; i++) {
            if(clients[i].username.equals(usr)) {
                return clients[i];
            }
        }
        return null;
    }

    public synchronized void remove(int ID) {
        int pos = findClient(ID);
        if (pos >= 0) {
            ServerThread toTerminate = clients[pos];
            serverFrame.textArea.appendText("\nRemoving client thread " + ID + " at " + pos);
            if (pos < clientCount - 1){
                for (int i = pos + 1; i < clientCount; i++) {
                    clients[i-1] = clients[i];
                }
            }
            clientCount--;
            try {
                toTerminate.close();
            } catch(IOException e) {
                serverFrame.textArea.appendText("\nError closing thread: " + e);
            }
            toTerminate.interrupt();
        }
    }

    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            serverFrame.textArea.appendText("\nClient accepted: " + socket);
            clients[clientCount] = new ServerThread(this, socket);
            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;
            } catch(IOException e){
                serverFrame.textArea.appendText("\nError opening thread: " + e);
            }
        } else {
            serverFrame.textArea.appendText("\nClient refused: maximum " + clients.length + " reached.");
        }
    }
}
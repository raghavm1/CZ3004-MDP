package ntu.mdp.group.three.communication;


import ntu.mdp.group.three.config.RobotConfig;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ConnectionServer {
	private DataInputStream dataInputStream =  null;
    private DataOutputStream dataOutputStream = null;
    private static ConnectionServer cs = null;
    
	private ConnectionServer() { }
	
	public static ConnectionServer getInstance() {
		if (cs == null) {
			cs = new ConnectionServer();
			cs.startServer();
		}
		return cs;
	}
	
	public void startServer() {
        // starts server and waits for a connection.
        try
        {
			ServerSocket serverSocket = new ServerSocket(RobotConfig.PORT);
            System.out.println("Server started"); 
  
            System.out.println("Waiting for a client ...");

			Socket socket = serverSocket.accept();
            System.out.println("Client accepted"); 
            
            dataInputStream = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream())); 
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
    public void sendMessage(String message) {
    	try {
    		dataOutputStream.write(message.getBytes());
    		dataOutputStream.flush();
    		System.out.println('"' + message + '"' + " sent successfully");
    	} catch (Exception e) {
			System.out.println(e.getMessage());
		}
    }
    
    public String receiveMessage() {
    	String message = "";
    	byte[] byteData = new byte[RobotConfig.BUFFER_SIZE];
    	try {
    		int size = 0;
    		dataInputStream.read(byteData);
    		
    		// This is to get rid of junk bytes
    		while (size < RobotConfig.BUFFER_SIZE) {
    			if (byteData[size] == 0) {
    				break;
    			}
    			size++;
    		}
    		message = new String(byteData, 0, size, StandardCharsets.UTF_8);
    	} catch (Exception e) {
			System.out.println(e.getMessage());
		}
    	return message;
    }
}

package ntu.mdp.group.three.communication;

import ntu.mdp.group.three.config.RobotConfig;
import ntu.mdp.group.three.threadedTasks.SenseTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommunicationSocket {
	private static CommunicationSocket communicationSocket = null;
	private InputStream inputStream = null;
	private PrintStream printStream = null;
	private static AtomicBoolean connected = new AtomicBoolean(false);
	private final static AtomicBoolean isDebug = new AtomicBoolean(false);
	private Socket socket = null;

    private CommunicationSocket() { }

    public static CommunicationSocket getInstance() {
    	if (communicationSocket == null) {
    		communicationSocket = new CommunicationSocket();
    		communicationSocket.connectToRPI();
    	}
    	return communicationSocket;
    }

	public static boolean checkConnection() { return communicationSocket != null; }

    public boolean connectToRPI() {
    	if (socket == null) {
	    	try {
	    		socket = new Socket(RobotConfig.RPI_IP_ADDRESS, RobotConfig.PORT);
	    		System.out.println("Connected to " + RobotConfig.RPI_IP_ADDRESS + ":" + RobotConfig.PORT);
	    		inputStream = socket.getInputStream();
	    		printStream = new PrintStream(socket.getOutputStream());
	    		connected.set(true);
	    		return true;
	    	} catch(UnknownHostException UHEx) {
	    		System.out.println("UnknownHostException in ConnectionSocket connectToRPI Function");
	        } catch (IOException IOEx) {
	    		System.out.println("IOException in ConnectionSocket connectToRPI Function");
	    	}
    	}
    	return false;
    }

    public void sendMessage(String message) {
    	try {
			SenseTask.resetTimer();
    		printStream.write(message.getBytes());
    		printStream.flush();
    		
    		if (isDebug.get()) {
    			System.out.println('"' + message + '"' + " sent successfully");
    		}
    	}
    	catch (IOException IOEx) {
    		System.out.println("IOException in ConnectionSocket sendMessage Function");
    	}
    }
    
    public static void setDebugTrue() { isDebug.set(true); }

    public static boolean isDebug() {
//    	return debug.get();
		// Forced debug
    	return true;
    }

	public String receiveMessage() {
		byte[] byteData = new byte[RobotConfig.BUFFER_SIZE];
		try {
			int size = 0;
			while (inputStream.available() == 0 && connected.get()) {
				try {
					CommunicationManager.getInstance().join(1);
				}
				catch(Exception e) {
					System.out.println("Error in receive message");
				}
			}

			inputStream.read(byteData);

			while (size < RobotConfig.BUFFER_SIZE) {
				if (byteData[size] == 0) break;
				size++;
			}
			return new String(byteData, 0, size, StandardCharsets.UTF_8);
		} catch (IOException IOEx) {
			System.out.println("IOException in ConnectionSocket receiveMessage Function");
		}
		return "Error";
	}
}
package ntu.mdp.group.three.communication;
import java.util.Scanner;

// This is used to test your connection between PC and Rpi
public class Client {
	public static void main(String[] arg) {
		CommunicationSocket communicationSocket = CommunicationSocket.getInstance();
		String message = "";
		Scanner scanner = new Scanner (System.in);
		while (message.compareTo("quit") != 0) {
			System.out.print("Enter your message (\"quit\" to exit): ");
			message = scanner.nextLine();
			communicationSocket.sendMessage(message);
			System.out.println("Waiting for server to reply...");
			System.out.println("Server: " + communicationSocket.receiveMessage());
		}
//		connectionSocket.closeConnection();
	}
}

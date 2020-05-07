import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame{
	
	
	//Server Side
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	
	//constructor
	public Server() {
		super("Instant Messenger");
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					sendMessage(event.getActionCommand());
					userText.setText("");
				}
			}
		);
		add(userText, BorderLayout.SOUTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300,150);
		setVisible(true);
		}
	
	public void startRunning() {
		try {
			server = new ServerSocket(6789, 100);
			
			while(true) {
				try {
					waitForConnection();
					setupStreams();
					whileChatting();
				}catch(EOFException eofException) {
					showMessage("\n Server ended the connection! ");
				}finally {
					closeConnection();
				}
			}
		}catch(IOException ioException) {
			ioException.printStackTrace(); 
		}
	}
	
	//wait for connection then display connection information
	private void waitForConnection() throws IOException{
		showMessage("Waiting for someone to connect...\n");
		connection = server.accept();
		showMessage("Now connected to "+ connection.getInetAddress().getHostAddress());
	}
	
	//get stream to send and receive data
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input  = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are now setup \n");
	}
	
	//during the chat conversation
	private void whileChatting() throws IOException{
		String message = "You are now connected!";
		sendMessage(message);
		ableToType(true);
		do {
			//have conversation
			try {
				message = (String) input.readObject();
				showMessage("\n" + message);
			}catch(ClassNotFoundException classNotFoundException){
				showMessage("\n Could not recognize what user is trying to send!");
				
			}
		}while(!message.equals("Client - END"));
	}
	
	//close streams and sockets after you are done chatting
	private void closeConnection() {
		showMessage("\n Closing Connection... \n");
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	//send a message to client
	private void sendMessage(String message) {
		try {
			output.writeObject("Server -"+ message);
			output.flush();
			showMessage("\nServer - " + message);
		}catch(IOException ioException) {
			chatWindow.append("\n Error: server msg can't be sent");
		}
	}
	
	//updates chat window
	private void showMessage(final String text) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					chatWindow.append(text);
				}
			}
				
		);
	}
	
	//Let textfield be enabled
	private void ableToType(final boolean tof) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						userText.setEditable(tof);
					}
				}
				);
	}
}
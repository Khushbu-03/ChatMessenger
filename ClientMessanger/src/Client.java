import java.io.*;
import java.awt.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame{
	
	//Client Side
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	
	//constructor
	public Client(String host) {
		super("Client Messanger");
		serverIP = host;
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
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		setSize(300,150);
		setVisible(true);
	}
	
	//connect to Server
	public void startRunning() {
		try {
			connectToServer();
			setupStreams();
			whileChatting();
		}catch(EOFException eofException) {
			showMessage("\n CLient Terminated connection");
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}finally {
			closeConnection();
		}
	}
	
	//connect to Server
	private void connectToServer() throws IOException{
		showMessage("Attempting connection...");
		connection = new Socket(InetAddress.getByName(serverIP), 6789);
		showMessage("Connected to Server :" + connection.getInetAddress().getHostName());
	}
	
	//get stream to send and receive data
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input  = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are now setup \n");
	}
	
	//	while chatting with server
	private void whileChatting() throws IOException{
		ableToType(true);
		do {
			try {
				message = (String) input.readObject();
				showMessage("\n" + message);
			}catch(ClassNotFoundException classnotfoundException) {
				showMessage("\n Could not recognize object type");
			}
		}while(!message.equals("Server - END"));
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
	
	//send a message to server
		private void sendMessage(String message) {
			try {
				output.writeObject("Client - "+ message);
				output.flush();
				showMessage("\nClient - " + message);
			}catch(IOException ioException) {
				chatWindow.append("\n Error: client msg can't be sent");
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

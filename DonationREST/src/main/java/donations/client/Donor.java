package donations.client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JOptionPane;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import donations.serialization.User;

import donations.util.DonationException;

public class Donor implements Runnable {
	private JFrame frame;
	private JButton buttonEnd;
	private JButton buttonLogin;
	private JTextField usernameField;
	private JTextField passwordField;
	private JLabel message;
	private JTextField userText;

	private Client client;
	private WebTarget webTarget;

	private Thread thread;
	private final AtomicBoolean running = new AtomicBoolean(false);

	public Donor(String hostname, String port) {
		client = ClientBuilder.newClient();
		webTarget = client.target(String.format("http://%s:%s/rest", hostname, port));


		//buenoooo
		JFrame frame = new JFrame("Udeusto Login");
		frame.setSize(400, 200);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.add(panel);
		placeComponents(panel);

		frame.setVisible(true);

		thread = new Thread(this);
		thread.start();
	}

	private void placeComponents(JPanel panel) {

		panel.setLayout(null);

		//mensaje
		JLabel message = new JLabel("Trying");
		message.setOpaque(true);
		message.setForeground(Color.yellow);
		message.setBackground(Color.gray);
		panel.add(message);

		JLabel userLabel = new JLabel("User");
		userLabel.setBounds(10, 10, 80, 25);
		panel.add(userLabel);

		userText = new JTextField(20);
		userText.setBounds(100, 10, 160, 25);
		panel.add(userText);

		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setBounds(10, 40, 80, 25);
		panel.add(passwordLabel);

		JPasswordField passwordText = new JPasswordField(20);
		passwordText.setBounds(100, 40, 160, 25);
		panel.add(passwordText);

		JButton loginButton = new JButton("login");
		loginButton.setBounds(10, 80, 80, 25);
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Login !");
				try {
					String username=userText.getText();
					String password=String.valueOf(passwordText.getPassword());
					message.setText("Trying to login");
					try {
						login(username, password);
						System.out.println("Lanzando login desde boton");
					} catch (DonationException ex) {
						ex.printStackTrace();
					}
					message.setText("Login of  " + username +password+ " sent");
				} catch (NumberFormatException exc) {
					message.setText(" # Error login. Login must be strings");
				}
			}

		});
		panel.add(loginButton);

		JButton registerButton = new JButton("register");
		registerButton.setBounds(180, 80, 80, 25);
		registerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

		});
		panel.add(registerButton);
	}
	
	public void login(String username, String password) throws DonationException {
		WebTarget donationsWebTarget = webTarget.path("collector/donations");
		Invocation.Builder invocationBuilder = donationsWebTarget.request(MediaType.APPLICATION_JSON);

		User user = new User(username,password);
		Response response = invocationBuilder.post(Entity.entity(user, MediaType.APPLICATION_JSON));
		if (response.getStatus() != Status.OK.getStatusCode()) {
			throw new DonationException("" + response.getStatus());
		} else {
			System.out.println("Enviado con exito");
		}
	}

	public User getUserInfo() throws DonationException {
		WebTarget donationsWebTarget = webTarget.path("collector/donations");
		Invocation.Builder invocationBuilder = donationsWebTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.get();
		if (response.getStatus() == Status.OK.getStatusCode()) {
			User user = response.readEntity(User.class);
			return user;
		} else {
			throw new DonationException("" + response.getStatus());
		}
	}

	public void run() {
		running.set(true);
		while(running.get()) {
			try { 
				Thread.sleep(2000);
				System.out.println("Obtaining data from server...");
				try {
					//revisar
					User user = getUserInfo();
					System.out.println("Metodo run print: " + user.getUsername());
				} catch (DonationException e) {
					System.out.println(e.getMessage());
				}
            } catch (InterruptedException e){ 
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted, Failed to complete operation");
            }
		}
	}

	public void stop() {

		this.running.set(false);
	}

	public static void main(String[] args) {
		String hostname = args[0];
		String port = args[1];

		Donor donor = new Donor(hostname, port);
	}
}
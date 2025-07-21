import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import keyboardinput.Keyboard;

public class MainTest {

	private ObjectOutputStream out;
	private ObjectInputStream in;

	public MainTest(String ip, int port) throws IOException {
		InetAddress addr = InetAddress.getByName(ip);
		System.out.println("addr = " + addr);
		Socket socket = new Socket(addr, port);
		System.out.println(socket);
		out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(socket.getInputStream());
	}

	private int menu() {
		int answer;
		do {
			System.out.println("\n(1) Load clusters from file");
			System.out.println("(2) Load data from db");
			System.out.print("(1/2):");
			try {
				String line = Keyboard.readString();
				if (line == null) return -1;
				answer = Integer.parseInt(line.trim());
			} catch (NumberFormatException e) {
				System.err.println("Errore: Inserisci un numero.");
				answer = 0;
			}
		} while (answer <= 0 || answer > 2);
		return answer;
	}

	/**
	 * MODIFICA QUI: Questo metodo ora chiede solo il nome del file da caricare.
	 */
	private String learningFromFile() throws SocketException, ServerException, IOException, ClassNotFoundException {
		out.writeObject(3); // Invia il codice operazione 3
		
		System.out.print("Nome del file da cui caricare i cluster: ");
		String fileNameToLoad = Keyboard.readString();
		out.writeObject(fileNameToLoad); // Invia il nome del file al server
		
		// La gestione della risposta rimane la stessa
		String result = (String) in.readObject();
		if (result.equals("OK"))
			return (String) in.readObject();
		else
			throw new ServerException(result);
	}

	private void storeTableFromDb() throws SocketException, ServerException, IOException, ClassNotFoundException {
		out.writeObject(0);
		System.out.print("Table name:");
		String tabName = Keyboard.readString();
		out.writeObject(tabName);
		String result = (String) in.readObject();
		if (!result.equals("OK"))
			throw new ServerException(result);
	}

	private String learningFromDbTable() throws SocketException, ServerException, IOException, ClassNotFoundException {
		out.writeObject(1);
		double r = -1.0;
		do {
			System.out.print("Radius:");
			 try {
				 String line = Keyboard.readString();
				 if (line == null) throw new IOException("Input stream terminato.");
				 r = Double.parseDouble(line.trim());
			} catch(NumberFormatException e) {
				System.err.println("Errore: Inserisci un numero valido.");
				r = -1.0;
			}
		} while (r <= 0);
		out.writeObject(r);
		String result = (String) in.readObject();
		if (result.equals("OK")) {
			System.out.println("Number of Clusters:" + in.readObject());
			return (String) in.readObject();
		} else
			throw new ServerException(result);
	}

	private void storeClusterInFile(String fileName) throws SocketException, ServerException, IOException, ClassNotFoundException {
		out.writeObject(2);
		out.writeObject(fileName); 
		String result = (String) in.readObject();
		if (!result.equals("OK"))
			 throw new ServerException(result);
	}

	public static void main(String[] args) {
		String ip = args[0];
		int port = new Integer(args[1]).intValue();
		MainTest main = null;
		try {
			main = new MainTest(ip, port);
		} catch (IOException e) {
			System.out.println(e);
			return;
		}

		do {
			int menuAnswer = main.menu();
			if (menuAnswer == -1) break;

			switch (menuAnswer) {
				case 1:
					try {
						String kmeans = main.learningFromFile();
						System.out.println(kmeans);
					} catch (Exception e) {
						System.err.println("Errore durante l'operazione: " + e.getMessage());
					}
					break;
				case 2: // learning from db
					try {
						main.storeTableFromDb();
					} catch (Exception e) {
						System.err.println("Errore durante il caricamento della tabella: " + e.getMessage());
						continue;
					}

					char answer = 'y';
					do {
						try {
							String clusterSet = main.learningFromDbTable();
							System.out.println(clusterSet);
							
							System.out.print("Nome del file su cui salvare i cluster (es. 'clusters.dat'): ");
							String fileNameToSave = Keyboard.readString();
							
							main.storeClusterInFile(fileNameToSave);
							System.out.println("Richiesta di salvataggio inviata per il file: " + fileNameToSave);
									
						} catch (Exception e) {
							 System.err.println("Errore durante il clustering o salvataggio: " + e.getMessage());
							 break;
						}
						System.out.print("Would you repeat?(y/n)");
						String line = Keyboard.readString();
						if (line != null && !line.isEmpty()) {
							answer = line.charAt(0);
						} else {
							answer = 'n';
						}
					} while (Character.toLowerCase(answer) == 'y');
					break;
				default:
					System.out.println("Invalid option!");
			}
			
			System.out.print("would you choose a new operation from menu?(y/n)");
			String line = Keyboard.readString();
			if (line == null || line.isEmpty() || Character.toLowerCase(line.charAt(0)) != 'y') {
				break;
			}
		} while (true);
	}
}
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import keyboardinput.Keyboard;

/**
 * Classe client principale per interagire con un server di clustering.
 * <p>
 * Questa applicazione si connette a un server tramite socket per eseguire operazioni
 * di clustering. Può caricare dati da database, eseguire l'algoritmo di
 * clustering e salvare/caricare i risultati da file.
 * </p>
 *
 */
public class MainTest {

	/**
	 * Stream di output per inviare oggetti serializzati al server.
	 */
	private final ObjectOutputStream out;
	/**
	 * Stream di input per ricevere oggetti serializzati dal server.
	 */
	private final ObjectInputStream in;

	/**
	 * Stabilisce la connessione con il server e inizializza gli stream di comunicazione.
	 *
	 * @param ip L'indirizzo IP del server a cui connettersi.
	 * @param port La porta su cui il server è in ascolto.
	 * @throws IOException se si verifica un errore durante la creazione del socket o degli stream.
	 */
	public MainTest(String ip, int port) throws IOException {
		InetAddress addr = InetAddress.getByName(ip);
		System.out.println("Connecting to addr = " + addr);
		Socket socket = new Socket(addr, port);
		System.out.println("Connection established: " + socket);
		
		out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(socket.getInputStream());
	}

	/**
	 * Mostra il menu principale all'utente e acquisisce la sua scelta.
	 *
	 * @return L'intero che rappresenta la scelta dell'utente (1 o 2), oppure -1 se l'input termina.
	 */
	private int menu() {
		int answer;
		do {
			System.out.println("\nScegli un'opzione:");
			System.out.println("(1) Carica cluster da un file");
			System.out.println("(2) Esegui clustering da dati su database");
			System.out.print("Scelta (1/2): ");
			try {
				String line = Keyboard.readString();
				if (line == null) return -1;
				answer = Integer.parseInt(line.trim());
			} catch (NumberFormatException e) {
				System.err.println("Errore: Inserisci un numero intero valido.");
				answer = 0;
			}
		} while (answer <= 0 || answer > 2);
		return answer;
	}

	/**
	 * Richiede al server di caricare un cluster set da un file.
	 * Invia il codice operazione 3.
	 *
	 * @return Una stringa contenente la rappresentazione del cluster set caricato.
	 * @throws SocketException se si verifica un errore a livello di socket TCP.
	 * @throws ServerException se il server risponde con un messaggio di errore.
	 * @throws IOException se si verifica un errore generico di I/O.
	 * @throws ClassNotFoundException se un oggetto ricevuto non può essere deserializzato.
	 */
	private String learningFromFile() throws SocketException, ServerException, IOException, ClassNotFoundException {
		out.writeObject(3);
		
		System.out.print("Nome del file da cui caricare i cluster: ");
		String fileNameToLoad = Keyboard.readString();
		out.writeObject(fileNameToLoad);
		
		String result = (String) in.readObject();
		if (result.equals("OK"))
			return (String) in.readObject();
		else
			throw new ServerException(result);
	}

	/**
	 * Richiede al server di caricare una tabella dal database per il successivo clustering.
	 * Invia il codice operazione 0.
	 *
	 * @throws SocketException se si verifica un errore a livello di socket TCP.
	 * @throws ServerException se il server risponde con un messaggio di errore.
	 * @throws IOException se si verifica un errore generico di I/O.
	 * @throws ClassNotFoundException se un oggetto ricevuto non può essere deserializzato.
	 */
	private void storeTableFromDb() throws SocketException, ServerException, IOException, ClassNotFoundException {
		out.writeObject(0);
		System.out.print("Nome della tabella nel database: ");
		String tabName = Keyboard.readString();
		out.writeObject(tabName);
		String result = (String) in.readObject();
		if (!result.equals("OK"))
			throw new ServerException(result);
	}

	/**
	 * Richiede al server di eseguire l'algoritmo di clustering sui dati caricati dal DB.
	 * Invia il codice operazione 1.
	 *
	 * @return Una stringa contenente il numero di cluster e la loro rappresentazione.
	 * @throws SocketException se si verifica un errore a livello di socket TCP.
	 * @throws ServerException se il server risponde con un messaggio di errore.
	 * @throws IOException se si verifica un errore generico di I/O.
	 * @throws ClassNotFoundException se un oggetto ricevuto non può essere deserializzato.
	 */
	private String learningFromDbTable() throws SocketException, ServerException, IOException, ClassNotFoundException {
		out.writeObject(1);
		double r = -1.0;
		do {
			System.out.print("Inserisci il raggio (es. 1.5): ");
			 try {
				 String line = Keyboard.readString();
				 if (line == null) throw new IOException("Input stream terminato dall'utente.");
				 r = Double.parseDouble(line.trim());
			} catch(NumberFormatException e) {
				System.err.println("Errore: Inserisci un numero valido (es. 1.5).");
				r = -1.0;
			}
		} while (r <= 0);
		
		out.writeObject(r);
		String result = (String) in.readObject();
		if (result.equals("OK")) {
			System.out.println("Numero di Cluster trovati: " + in.readObject()); 
			return (String) in.readObject();
		} else
			throw new ServerException(result);
	}

	/**
	 * Richiede al server di salvare il cluster set calcolato su un file.
	 * Invia il codice operazione 2.
	 *
	 * @param fileName Il nome del file su cui il server salverà i dati.
	 * @throws SocketException se si verifica un errore a livello di socket TCP.
	 * @throws ServerException se il server risponde con un messaggio di errore.
	 * @throws IOException se si verifica un errore generico di I/O.
	 * @throws ClassNotFoundException se un oggetto ricevuto non può essere deserializzato.
	 */
	private void storeClusterInFile(String fileName) throws SocketException, ServerException, IOException, ClassNotFoundException {
		out.writeObject(2);
		out.writeObject(fileName); 
		String result = (String) in.readObject();
		if (!result.equals("OK"))
			 throw new ServerException(result);
	}

	/**
	 * Metodo di avvio dell'applicazione client.
	 *
	 * @param args Array di stringhe con `args[0]` = IP server e `args[1]` = porta server.
	 */
	public static void main(String[] args) {
		String ip;
		int port;
		MainTest main;

		try {
			ip = args[0];
			port = Integer.parseInt(args[1]);
			main = new MainTest(ip, port);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			System.err.println("Errore negli argomenti. Uso: java MainTest <server_ip> <server_port>");
			return;
		} catch (IOException e) {
			System.err.println("Impossibile connettersi al server: " + e.getMessage());
			return;
		}

		mainLoop:
		do {
			int menuAnswer = main.menu();
			if (menuAnswer == -1) break;

			switch (menuAnswer) {
				case 1:
					try {
						String kmeans = main.learningFromFile();
						System.out.println(kmeans);
					} catch (ClassNotFoundException | ServerException | IOException e) {
						System.err.println("ERRORE durante il caricamento da file: " + e.getMessage());
					}
					break;
				
				case 2:
					try {
						main.storeTableFromDb();
					} catch (ClassNotFoundException | ServerException | IOException e) {
						System.err.println("ERRORE durante il caricamento della tabella dal DB: " + e.getMessage());
						continue;
					}

					char answer;
					do {
						try {
							String clusterSet = main.learningFromDbTable();
							System.out.println(clusterSet);
							
							System.out.print("Vuoi salvare i cluster su file? (y/n): ");
							String choice = Keyboard.readString();
							if(choice != null && !choice.isEmpty() && Character.toLowerCase(choice.charAt(0)) == 'y'){
								System.out.print("Nome del file su cui salvare i cluster (es. 'clusters.dat'): ");
								String fileNameToSave = Keyboard.readString();
								main.storeClusterInFile(fileNameToSave);
								System.out.println("Richiesta di salvataggio inviata per il file: " + fileNameToSave);
							}
									
						} catch (ClassNotFoundException | ServerException | IOException e) {
							 System.err.println("ERRORE durante il clustering o il salvataggio: " + e.getMessage());
							 break;
						}
						
						System.out.print("Vuoi eseguire un nuovo clustering con un raggio diverso? (y/n): ");
						String line = Keyboard.readString();
						if (line != null && !line.isEmpty()) {
							answer = line.charAt(0);
						} else {
							answer = 'n';
						}
					} while (Character.toLowerCase(answer) == 'y');
					break;
				
				default:
					System.out.println("Opzione non valida!");
			}
			
			System.out.print("\nTornare al menu principale? (y/n): ");
			String line = Keyboard.readString();
			if (line == null || line.isEmpty() || Character.toLowerCase(line.charAt(0)) != 'y') {
				break mainLoop;
			}
		} while (true);
		
		System.out.println("Applicazione terminata.");
	}
}
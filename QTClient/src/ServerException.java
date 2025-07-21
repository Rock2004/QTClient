/**
 * Eccezione personalizzata per segnalare errori specifici che si verificano
 * durante l'interazione con il server QT o a seguito di una risposta di errore
 * da parte del server.
 * <p>
 * Estende {@link Exception}, rendendola una checked exception, il che significa
 * che il codice client ({@code MainTest}) dovrà gestirla esplicitamente.
 * </p>
 */
public class ServerException extends Exception {

    private static final long serialVersionUID = 1L; 

    /**
     * Costruisce una nuova {@code ServerException} con un messaggio di default.
     * Il messaggio di default che avevi era " (Da completare) ".
     * Possiamo renderlo più generico o specifico se preferisci.
     */
    public ServerException() {
        super("Errore comunicato dal server o durante la comunicazione."); // Messaggio aggiornato per maggiore chiarezza
    }

    /**
     * Costruisce una nuova {@code ServerException} con il messaggio di dettaglio specificato.
     * Questo è il costruttore più comunemente usato quando il server invia una stringa
     * che descrive l'errore.
     *
     * @param message Il messaggio di dettaglio che descrive l'errore.
     */
    public ServerException(String message) {
        super(message);
    }

    /**
     * Costruisce una nuova {@code ServerException} con la causa specificata.
     * Utile se un'eccezione di più basso livello (es. IOException) nel client
     * deve essere wrappata come {@code ServerException}.
     *
     * @param cause La causa originale (un'altra eccezione) che ha portato a questa ServerException.
     */
    public ServerException(Throwable cause) {
        super(cause);
    }

    /**
     * Costruisce una nuova {@code ServerException} con il messaggio di dettaglio
     * e la causa specificati.
     *
     * @param message Il messaggio di dettaglio che descrive l'errore.
     * @param cause   La causa originale (un'altra eccezione) che ha portato a questa ServerException.
     */
    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}

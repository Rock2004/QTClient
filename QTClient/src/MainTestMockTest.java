
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.SocketException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class MainTestMockTest {

    @Mock
    private ObjectOutputStream out;

    @Mock
    private ObjectInputStream in;

    // La classe da testare
    private MainTest mainTest;

    /**
     * Metodo di supporto per accedere al metodo privato "learningFromFile(String)" 
     * usando la riflessione Java.
     * @return Il metodo reso accessibile.
     * @throws NoSuchMethodException se il metodo non viene trovato.
     */
    private Method getLearningFromFileMethod() throws NoSuchMethodException {
        // Specifichiamo il nome del metodo e il tipo del suo parametro (String)
        Method method = MainTest.class.getDeclaredMethod("learningFromFile", String.class);
        // Rendiamo il metodo accessibile anche se è privato
        method.setAccessible(true);
        return method;
    }

    @BeforeEach
    void setUp() {
        // Inizializza l'oggetto da testare con i mock degli stream
        mainTest = new MainTest(out, in);
    }

    @Test
    @DisplayName("learningFromFile dovrebbe gestire correttamente una risposta 'OK' dal server")
    void testLearningFromFile_Success() throws Exception {
        // ========== 1. ARRANGE: Prepara il test ==========
        String fakeFileName = "clusters.dat";
        String serverResponse = "1:Centroid=(val1 val2)";
        
        // Configura il mock per simulare la risposta del server
        when(in.readObject())
            .thenReturn("OK")
            .thenReturn(serverResponse);

        // ========== 2. ACT: Esegui il metodo da testare ==========
        // Ottieni il metodo privato tramite il nostro helper
        Method learningMethod = getLearningFromFileMethod();
        // Invoca il metodo su `mainTest`, passando `fakeFileName` come argomento
        String result = (String) learningMethod.invoke(mainTest, fakeFileName);

        // ========== 3. ASSERT: Verifica i risultati ==========
        // Controlla che il risultato restituito sia corretto
        assertEquals(serverResponse, result, "Il risultato del metodo non corrisponde alla risposta del server.");

        // Verifica che il client abbia inviato i dati corretti al server
        verify(out).writeObject(3);
        verify(out).writeObject(fakeFileName);
    }
}
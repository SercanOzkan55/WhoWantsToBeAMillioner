import java.io.PrintWriter;
import java.net.Socket;

public interface Joker {
    void use();

    default void sendJokerSignal(String message) {
        new Thread(() -> {
            try (Socket s = new Socket("localhost", 4338);
                 PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
                out.println(message);
                // Yanıt beklemiyoruz, hemen kapatıyoruz
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

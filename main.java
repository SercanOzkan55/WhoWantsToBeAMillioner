import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class main {
    public static volatile boolean jokerServerReady = false; // Joker sunucu hazır mı?

    public static void main(String[] args) {
        // Firebase sorularını ön yükle (isteğe bağlı)
        FirebaseService.fetchQuestionsViaRest();

        // Ana Sunucu 4337 portu thread
        Thread mainServerThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(4337)) {
                System.out.println("Ana Sunucu 4337 portunda dinleniyor...");

                while (true) {
                    Socket client = serverSocket.accept();

                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Ana Sunucu mesaj aldı: " + message);

                        if ("S".equalsIgnoreCase(message)) {
                            System.out.println("Seyirci jokeri kullanıldı.");
                            out.println("Seyirci jokeri onaylandı.");
                        } else if ("Y".equalsIgnoreCase(message)) {
                            System.out.println("Yarı yarıya jokeri kullanıldı.");
                            out.println("Yarı yarıya jokeri onaylandı.");
                        } else if (message.startsWith("JOKER")) {
                            System.out.println("Joker sunucusundan mesaj: " + message);
                            out.println("Joker mesajı alındı.");
                        } else {
                            out.println("Mesaj alındı: " + message);
                        }
                    }
                    client.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Joker Sunucu 4338 portu thread
        Thread jokerServerThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(4338)) {
                System.out.println("Joker Sunucu 4338 portunda dinleniyor...");

                while (true) {
                    Socket client = serverSocket.accept();

                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String message = in.readLine();
                    System.out.println("Joker Sunucu mesaj aldı: " + message);

                    // Geri dönüş YOK, sadece ana sunucuya ilet
                    try (Socket mainSocket = new Socket("localhost", 4337);
                         PrintWriter mainOut = new PrintWriter(mainSocket.getOutputStream(), true)) {
                        mainOut.println("JOKER: " + message + " kullanıldı, ana sunucuya dönüldü");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    client.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Önce sunucuları başlat
        mainServerThread.start();
        jokerServerThread.start();

        // Sonra GUI'yi başlat
        SwingUtilities.invokeLater(() -> {
            FirstMenu firstMenu = new FirstMenu();
            firstMenu.setVisible(true);
        });
    }
}

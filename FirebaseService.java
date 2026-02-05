import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FirebaseService {

    public static class Question {
        String soru, a, b, c, d, dogru;
    }

    public static List<Question> fetchQuestionsViaRest() {
        try {
            // Firebase URL'si
            URL url = new URL("https://millioner-4d27d-default-rtdb.europe-west1.firebasedatabase.app/sorular.json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // JSON verisini okuma
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            System.out.println("ÇEKİLEN JSON:");
            System.out.println(jsonBuilder.toString());

            // JSON verisini Java nesnesine dönüştürme
            Gson gson = new Gson();
            // JSON dizisini List<Question> olarak parse et
            List<Question> questions = gson.fromJson(jsonBuilder.toString(), new TypeToken<List<Question>>(){}.getType());

            System.out.println("Sorular başarıyla çözümlendi.");
            return questions;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;


public class Frame extends JFrame implements ActionListener {
    public static final int FRAME_POSITION_X = 0;
    public static final int FRAME_POSITION_Y = 100;
    public static final int FRAME_SIZE_X = 1100;
    public static final int FRAME_SIZE_Y = 733;
    public static final String FRAME_TITLE = "WHO WANTS TO BE A MILLIONER";

    private JPanel menuPanel;
    private Panel mp;
    private RoundButton rb;

    // Soru ve cevap alanları
    private List<Question> questions;
    private List<Question> selectedQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;  // Doğru cevap sayısı
    private JLabel questionLabel;
    private JButton[] answerButtons = new JButton[4];
    private Timer timer;
    private int remainingTime = 30;  // Başlangıç süresi
    private int maxTime = 30;  // Maksimum süre (ilk 3 soru için)
    private TimeBar timeBar;  // Zaman barı

    // Jokerlerin kullanılma durumu
    private boolean halfHalfUsed = false;
    private boolean audienceUsed = false;
    private boolean isPaused = false;
    
    private HalfHalfJoker halfHalfJoker;
    private AudienceJoker audienceJoker;
    
    
    // Joker butonları
    private JButton halfHalfButton;
    private JButton audienceButton;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JLabel[] optionLabels = new JLabel[4];
    private JLabel pauseLabel;
 
    private List<List<Boolean>> disabledAnswers = new ArrayList<>(); // Her soru için şıkların devre dışı olma durumu

    
    public Frame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Bu satırları ekliyoruz
        setSize(FRAME_SIZE_X, FRAME_SIZE_Y);  // Frame boyutlarını ayarlıyoruz
        setLocation(FRAME_POSITION_X, FRAME_POSITION_Y);  // Ekrandaki pozisyonu belirliyoruzz

        mp = new Panel();
        mp.setLayout(null);  // Layout'u null yapıyoruz, manuel yerleştirme için
        add(mp);
        
        setLocationRelativeTo(null);
        setResizable(false);

        pauseLabel = new JLabel("PAUSED", SwingConstants.CENTER);
        pauseLabel.setFont(new Font("Arial", Font.BOLD, 100));
        pauseLabel.setForeground(new Color(255, 255, 255, 200));
        pauseLabel.setOpaque(true);
        pauseLabel.setBackground(new Color(0, 0, 0, 150));
        pauseLabel.setBounds(0, 0, FRAME_SIZE_X, FRAME_SIZE_Y);
        pauseLabel.setVisible(false);
        mp.add(pauseLabel);
        
        createComponents();
        setupKeyBindings();
        connectToServer();
        this.setContentPane(mp);
        this.setVisible(true);
        
        this.setContentPane(mp);
        this.setVisible(true);

        
        halfHalfJoker = new HalfHalfJoker(this);
        audienceJoker = new AudienceJoker(this);
        
        halfHalfButton.addActionListener(e -> halfHalfJoker.use());
        audienceButton.addActionListener(e -> audienceJoker.use());
        
        connectToServer();

        // Diğer bileşenler...
        timeBar = new TimeBar();
        timeBar.setBounds(150, 110, 800, 30);  // Zaman barının yeri
        mp.add(timeBar);

        // Soruları yükle...
        new Thread(() -> {
            questions = fetchQuestionsViaRest();
            if (questions != null && !questions.isEmpty()) {
                Collections.shuffle(questions);
                selectedQuestions = questions.subList(0, 6);
                SwingUtilities.invokeLater(this::showQuestion);
            } else {
                JOptionPane.showMessageDialog(this, "Soru verisi alınamadı!");
            }
        }).start();
    }


    private void createComponents() {
    	ImageIcon icon = null;
    	try {
    	    Image img = ImageIO.read(getClass().getResource("/images/Button.png"));
    	    icon = new ImageIcon(img.getScaledInstance(60, 60, Image.SCALE_SMOOTH));
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}

    	JButton hamburger = new JButton(icon);
    	hamburger.setBounds(1020, 10, 60, 60);
    	hamburger.setOpaque(false);
    	hamburger.setContentAreaFilled(false);
    	hamburger.setBorderPainted(false);
    	hamburger.addActionListener(this);
    	mp.add(hamburger);

    	// Yeni tasarımlı menuPanel
    	menuPanel = new JPanel() {
    	    @Override
    	    protected void paintComponent(Graphics g) {
    	        super.paintComponent(g);
    	        Graphics2D g2 = (Graphics2D) g.create();

    	        // Kenarları yumuşat
    	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    	        int strokeWidth = 10;
    	        int arc = 30;

    	        // Yarı saydam siyah iç dolgu
    	        g2.setColor(new Color(0, 0, 0, 180)); // Alpha: 180/255 ~ %70 opak
    	        g2.fillRoundRect(strokeWidth / 2, strokeWidth / 2, getWidth() - strokeWidth, getHeight() - strokeWidth, arc, arc);

    	        // Beyaz çerçeve
    	        g2.setStroke(new BasicStroke(strokeWidth));
    	        g2.setColor(Color.WHITE);
    	        g2.drawRoundRect(strokeWidth / 2, strokeWidth / 2, getWidth() - strokeWidth, getHeight() - strokeWidth, arc, arc);
    	        
    	        String[] rewards = {
    	                "1. Soru : 1000 TL",
    	                "2. Soru : 2000 TL",
    	                "3. Soru : 3000 TL",
    	                "4. Soru : 5000 TL",
    	                "5. Soru : 10000 TL",
    	                "6. Soru : 20000 TL"
    	            };
    	        Color[] colors = {
    	                new Color(255, 215, 0),    // Sarı
    	                new Color(144, 238, 144),  // Açık Yeşil
    	                new Color(255, 165, 0),    // Turuncu
    	                new Color(135, 206, 250),  // Açık Mavi
    	                new Color(255, 105, 180),  // Pembe
    	                new Color(255, 223, 0)     // Altın Sarı
    	            };

    	            g2.setFont(new Font("Arial", Font.BOLD, 35));
    	            int startY = 100;
    	            int spacing = 100;

    	            for (int i = 0; i < rewards.length; i++) {
    	                g2.setColor(colors[i]);
    	                g2.drawString(rewards[i], 20, startY + i * spacing);
    	            }

    	        g2.dispose();
    	    }
    	};

    	// Panel boyutu ayarı (daha dar yapıldı)
    	menuPanel.setBounds(0, 0, FRAME_SIZE_X/3, FRAME_SIZE_Y-40);  // İstersen 280 de yapabiliriz
    	menuPanel.setOpaque(false);
    	menuPanel.setVisible(false);
    	menuPanel.setLayout(null);
    	mp.add(menuPanel);


        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setBounds(233, 122, 617, 204);
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 22));
        questionLabel.setOpaque(false);
        questionLabel.setVerticalAlignment(SwingConstants.CENTER);
        mp.add(questionLabel);

        // Cevap Label'ları (A, B, C, D)
        int[] xCoords = {180, 560, 180, 560};
        int[] yCoords = {390, 390, 550, 550};
        int[] widths = {330, 330, 330, 330};
        int[] heights = {95, 95, 95, 95};
        String[] optionChars = {"A", "B", "C", "D"};

        for (int i = 0; i < 4; i++) {
            optionLabels[i] = new JLabel("", SwingConstants.CENTER);
            optionLabels[i].setBounds(xCoords[i], yCoords[i], widths[i], heights[i]);
            optionLabels[i].setForeground(Color.WHITE);
            optionLabels[i].setFont(new Font("Arial", Font.BOLD, 22));
            optionLabels[i].setOpaque(false);
            optionLabels[i].setVerticalAlignment(SwingConstants.CENTER);
            mp.add(optionLabels[i]);

            JButton btn = new JButton();
            btn.setBounds(xCoords[i], yCoords[i], widths[i], heights[i]);
            btn.setActionCommand(optionChars[i]);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);

            btn.addActionListener(e -> checkAnswer(btn.getActionCommand()));
            mp.add(btn);
            answerButtons[i] = btn;
        }



     // Resimleri yükle
        ImageIcon halfIcon = new ImageIcon(getClass().getResource("/images/50joker.png"));
        ImageIcon audienceIcon = new ImageIcon(getClass().getResource("/images/seyircijoker.png"));

        // Boyutlandır (isteğe bağlı)
        Image img1 = halfIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        Image img2 = audienceIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);

     // RoundButton doğrudan JButton gibi davranır, cast etmene gerek yok

        halfHalfButton = new RoundButton(new ImageIcon(img1));
        halfHalfButton.setBounds(10, 10, 60, 60);
        halfHalfButton.setBackground(new Color(255, 215, 0));
        mp.add(halfHalfButton);

        audienceButton = new RoundButton(new ImageIcon(img2));
        audienceButton.setBounds(80, 10, 60, 60);
        audienceButton.setBackground(new Color(0, 128, 0));
        mp.add(audienceButton);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        menuPanel.setVisible(!menuPanel.isVisible());
        mp.repaint();

        // Ana sunucu bağlantısını sadece menü açıldığında yap
        new Thread(() -> {
            try (Socket s = new Socket("localhost", 4337);
                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                 PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

                System.out.println("Sunucuya bağlanıldı (4337)");
                out.println("Ana menü etkileşimi");
                System.out.println("Cevap: " + in.readLine());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private Clip currentClip;

    private void playSound(String fileName, boolean loop) {
        if(isPaused) return;
        
        try {
            if (currentClip != null && currentClip.isRunning()) {
                currentClip.stop();
                currentClip.close();
            }

            InputStream audioSrc = getClass().getResourceAsStream("/sounds/" + fileName);
            if(audioSrc == null) return;
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioSrc);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);
            if (loop) currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            else currentClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void checkAnswer(String selected) {
        timer.stop();

        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }

        Question q = selectedQuestions.get(currentQuestionIndex);
        String correct = q.dogru.toUpperCase();

        if (selected.startsWith(correct)) {
            correctAnswers++;
            playSound("win.wav", false);
            showResultMessage(true); // Sadece burada mesajı göster
        } else {
            playSound("lose.wav", false);
            showResultMessage(false); // Hata mesajını göster
        }
    }

    private void startTimer() {
        if(timer != null && timer.isRunning()) timer.stop();
        
        timer = new Timer(1000, e -> {
            if(!isPaused) {
                remainingTime--;
                timeBar.updateBar(remainingTime, maxTime);
                
                if(remainingTime <= 0) {
                    timer.stop();
                    playSound("lose.wav", false);
                    JOptionPane.showMessageDialog(Frame.this, "Süre bitti! Kaybettiniz.");
                    System.exit(0);
                }
            }
        });
        timer.start();
        
        if(!isPaused) playSound("question.wav", true);
    }
    
    private final int[] rewards = {0, 1000, 2000, 3000, 5000, 10000, 20000};

    private void showResultMessage(boolean isCorrect) {
        String message = "";
        int questionNum = currentQuestionIndex + 1;

        if (isCorrect) {
            switch (questionNum) {
                case 1: message = "1. soruyu bildin! 1000 TL kazandın!"; break;
                case 2: message = "2. soruyu bildin! 2000 TL kazandın!"; break;
                case 3: message = "3. soruyu bildin! 3000 TL kazandın!"; break;
                case 4: message = "4. soruyu bildin! 5000 TL kazandın!"; break;
                case 5: message = "5. soruyu bildin! 10000 TL kazandın!"; break;
                case 6: message = "6. soruyu bildin! 20000 TL kazandın! Harika!"; break;
                default: message = questionNum + ". soruyu bildin! Tebrikler!"; break;
            }
        } else {
            switch (questionNum) {
                case 1: message = "1. soruda elendin. Vasat!"; break;
                case 2: message = "2. soruda elendin. Eh işte!"; break;
                case 3: message = "3. soruda elendin. İyi deneme!"; break;
                case 4: message = "4. soruda elendin. Yine de iyi ilerledim!"; break;
                case 5: message = "5. soruda elendin. Harika bir ilerlemeydi!"; break;
                case 6: message = "6. soruda elendin. Nerdeyse Oluyordu!"; break;
                default: message = questionNum + ". soruda elendin!"; break;
            }
        }

        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(new Font("Arial", Font.BOLD, 40));
        msgLabel.setForeground(isCorrect ? Color.GREEN : Color.RED);
        msgLabel.setOpaque(true);
        msgLabel.setBackground(new Color(0, 0, 0, 180));
        
        // Label boyutunu frame'e göre ayarla
        msgLabel.setBounds(0, 0, FRAME_SIZE_X, FRAME_SIZE_Y);
        msgLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        mp.add(msgLabel);
        mp.setComponentZOrder(msgLabel, 0); // En üste getir
        mp.revalidate();
        mp.repaint();

        // 4 saniye sonra mesajı kaldır ve işleme devam et
        new Timer(4000, e -> {
            mp.remove(msgLabel);
            mp.repaint();

            if (isCorrect) {
                currentQuestionIndex++;
                if (currentQuestionIndex < selectedQuestions.size()) {
                    showQuestion();
                } else {
                    JOptionPane.showMessageDialog(this, "Tebrikler! Tüm soruları bildin! Kazandığın para: " + rewards[rewards.length - 1] + " TL");
                    System.exit(0);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Oyun bitti! Kazandığın para: " + rewards[currentQuestionIndex] + " TL");
                System.exit(0);
            }

            ((Timer) e.getSource()).stop();
        }).start();
    }


    // Firebase'den REST API ile soru çeken metot
    private List<Question> fetchQuestionsViaRest() {
        try {
            URL url = new URL("https://millioner-4d27d-default-rtdb.europe-west1.firebasedatabase.app/sorular.json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            Gson gson = new Gson();
            return gson.fromJson(jsonBuilder.toString(), new TypeToken<List<Question>>(){}.getType());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Jokerlerin erişmesi gereken metodlar
    public Question getCurrentQuestion() {
        return selectedQuestions.get(currentQuestionIndex);
    }

    public JButton[] getAnswerButtons() {
        return answerButtons;
    }

    public void updateDisabledAnswers(List<Integer> disabledIndices) {
        // Mevcut soru için disabled listesini temizle veya oluştur
        while (disabledAnswers.size() <= currentQuestionIndex) {
            disabledAnswers.add(new ArrayList<>(Collections.nCopies(4, false)));
        }
        
        List<Boolean> currentDisabled = disabledAnswers.get(currentQuestionIndex);
        Collections.fill(currentDisabled, false); // Önceki değerleri temizle
        
        for (int index : disabledIndices) {
            if (index >= 0 && index < currentDisabled.size()) {
                currentDisabled.set(index, true);
            }
        }
    }


    // Firebase JSON içeriği ile eşleşen iç sınıf
    public static class Question {
        String soru, a, b, c, d, dogru;
    }

    class TimeBar extends JComponent {
        private int currentTime = 0;
        private int maxTime = 30;

        public TimeBar() {
            setBounds(207, 306, 600, 30); // x=207, y=306, width=600, height=30
            setOpaque(false);
        }

        public void updateBar(int currentTime, int maxTime) {
            this.currentTime = currentTime;
            this.maxTime = maxTime;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            // Arkaplan efekti
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            // Ana çizim
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            double progress = (double) currentTime / maxTime;

            int numCircles = 20; // Daire sayısını azalttık
            int padding = 4;
            int circleDiameter = 22; // Sabit çap
            int totalWidth = getWidth() - 2 * padding;
            int spacing = totalWidth / numCircles;

            for (int i = 0; i < numCircles; i++) {
                int x = padding + i * spacing;
                int y = (getHeight() - circleDiameter) / 2; // Dikeyde ortala

                // Renk hesaplama
                Color circleColor = (i < (numCircles * progress)) 
                    ? getColorForProgress((double)i/numCircles) 
                    : new Color(255, 255, 255, 60);

                // Dış daire
                g2d.setColor(circleColor);
                g2d.fillOval(x, y, circleDiameter, circleDiameter);

                // İç halka efekti
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillOval(x + 2, y + 2, circleDiameter - 4, circleDiameter - 4);
            }

            g2d.dispose();
        }

        private Color getColorForProgress(double ratio) {
            float hue = (float) (0.3 + (ratio * 0.7)); // Yeşil -> Mavi
            float saturation = 0.9f;
            float brightness = 0.8f + (float)(ratio * 0.2); // Parlaklık artışı
            return Color.getHSBColor(hue, saturation, brightness);
        }
    }

    private void showQuestion() {
        if (currentQuestionIndex >= selectedQuestions.size()) {
            String message = correctAnswers == 5 
                ? "Tebrikler! Tüm soruları doğru cevapladınız!" 
                : "Oyun bitti! Doğru cevaplar: " + correctAnswers;
            JOptionPane.showMessageDialog(this, message);
            System.exit(0);
        }

        // Zaman ayarlarını güncelle
        if (currentQuestionIndex >= 3) { // 4. soru ve sonrası
            maxTime = 15;
        } else {
            maxTime = 30;
        }
        remainingTime = maxTime;
        timeBar.updateBar(remainingTime, maxTime);

        // Soru ve şıkları resetle
        Question q = selectedQuestions.get(currentQuestionIndex);
        questionLabel.setText("<html><center>" + q.soru + "</center></html>");

        String[] options = {q.a, q.b, q.c, q.d};
        for(int i=0; i<4; i++) {
            optionLabels[i].setText((char)(65+i) + ": " + options[i]);
            optionLabels[i].setVisible(true);
            answerButtons[i].setEnabled(true);
            answerButtons[i].setVisible(true);
        }

        if(disabledAnswers.size() > currentQuestionIndex) {
            List<Boolean> disabled = disabledAnswers.get(currentQuestionIndex);
            for(int i=0; i<4; i++) {
                if(disabled.get(i)) {
                    optionLabels[i].setVisible(false);
                    answerButtons[i].setEnabled(false);
                    answerButtons[i].setVisible(false);
                }
            }
        }

        mp.revalidate();
        mp.repaint();

        halfHalfButton.setEnabled(true);
        audienceButton.setEnabled(true);
        halfHalfUsed = false;
        audienceUsed = false;

        startTimer();
    }
    
    public JLabel[] getOptionLabels() {
        return optionLabels;
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 4337);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        final String message = line;
                        System.out.println("Sunucudan mesaj: " + message);
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(Frame.this, "Sunucudan: " + message));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            System.out.println("Program sunucusuna bağlanıldı (port 4337)");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Sunucuya bağlanılamadı!");
        }
    }

    public void sendMessageToServer(String message) {
        if (out != null) {
            out.println(message);
        } else {
            JOptionPane.showMessageDialog(this, "Sunucu bağlantısı yok!");
        }
    }
    
    private void togglePause() {
        isPaused = !isPaused;
        
        if(isPaused) {
            timer.stop();
            if(currentClip != null && currentClip.isRunning()) {
                currentClip.stop();
            }
            pauseLabel.setVisible(true);
            disableButtons(true);
        } else {
            pauseLabel.setVisible(false);
            disableButtons(false);
            startTimer();
        }
        mp.repaint();
    }

    private void disableButtons(boolean disable) {
        for(JButton btn : answerButtons) btn.setEnabled(!disable);
        halfHalfButton.setEnabled(!disable);
        audienceButton.setEnabled(!disable);
    }
    
    private void setupKeyBindings() {
        InputMap inputMap = mp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mp.getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "pauseAction");
        actionMap.put("pauseAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });
    }
    
}

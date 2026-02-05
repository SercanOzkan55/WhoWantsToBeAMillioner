import java.awt.*;
import javax.swing.*;

public class FirstMenu extends JFrame {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                FirstMenu frame = new FirstMenu();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public FirstMenu() {
        setTitle("FirstMenu Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(400, 50, 500, 773);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon backgroundImageIcon = new ImageIcon(FirstMenu.class.getResource("/images/Giriş.png"));
                g.drawImage(backgroundImageIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(null);
        setContentPane(panel);

        ShadowButton startButton = new ShadowButton("BAŞLA");
        startButton.setBounds(95, 530, 310, 110);
        panel.add(startButton);

        // ActionListener to switch to the second screen (Frame class)
        startButton.addActionListener(e -> {
            dispose(); // Close the first screen
            Frame mg = new Frame("Öteki Ekran");
            mg.setVisible(true); // Show the second screen
        });
    }
}
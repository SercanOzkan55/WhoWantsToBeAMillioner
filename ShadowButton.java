import javax.swing.*;
import java.awt.*;

public class ShadowButton extends JButton {

    public ShadowButton(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(new Color(255, 215, 0)); // Altın sarısı
        setFont(new Font("Segoe UI Black", Font.BOLD, 60));
        setHorizontalAlignment(SwingConstants.CENTER);
        setMargin(new Insets(5, 5, 5, 5));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Gölge ayarı
        g2.setColor(new Color(0, 0, 0, 150)); // Siyah, yarı saydam
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

        // Gölgeyi biraz sağa ve aşağıya kaydır
        g2.drawString(getText(), x + 3, y + 3);

        // Asıl yazı
        g2.setColor(getForeground());
        g2.drawString(getText(), x, y);

        g2.dispose();
        // Butonun geri kalan davranışlarını koru
        super.paintComponent(g);
    }
}

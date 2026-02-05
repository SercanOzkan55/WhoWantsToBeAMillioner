import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Panel extends JPanel {
    private Image bg;

    public Panel() {
        setBounds(0, 0, Frame.FRAME_SIZE_X, Frame.FRAME_SIZE_Y);
        try {
            bg = ImageIO.read(getClass().getResource("/images/AnaTema.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
    }
}

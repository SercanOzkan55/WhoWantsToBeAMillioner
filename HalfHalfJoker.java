import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HalfHalfJoker implements Joker {
    private final Frame frame;
    private boolean used = false;

    public HalfHalfJoker(Frame frame) {
        this.frame = frame;
    }

    @Override
    public void use() {
        if (used) {
            JOptionPane.showMessageDialog(frame, "Yarı Yarıya Jokeri zaten kullanıldı!");
            return;
        }

        Frame.Question q = frame.getCurrentQuestion();
        String correct = q.dogru.toUpperCase();
        JButton[] answerButtons = frame.getAnswerButtons();
        
        frame.sendMessageToServer("H");  // Program sunucusuna sinyal gönder
        sendJokerSignal("Seyirci jokeri kullanıldı");

        List<Integer> wrongIndexes = new ArrayList<>();
        for (int i = 0; i < answerButtons.length; i++) {
            String option = answerButtons[i].getActionCommand();
            if (!option.equals(correct)) {
                wrongIndexes.add(i);
            }
        }

        Collections.shuffle(wrongIndexes);
        List<Integer> toDisable = wrongIndexes.subList(0, 2); // 2 yanlışı devre dışı bırak

        // Hem butonları disable et hem de listeyi güncelle
        for (int index : toDisable) {
            frame.getOptionLabels()[index].setVisible(false);
            frame.getAnswerButtons()[index].setVisible(false);
            frame.getAnswerButtons()[index].setEnabled(false);
        }
        
        frame.updateDisabledAnswers(toDisable); // Önemli: Listeyi güncelle
        used = true;

        JOptionPane.showMessageDialog(frame, "2 yanlış şık kaldırıldı!");
    }
}

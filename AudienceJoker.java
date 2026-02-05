import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class AudienceJoker implements Joker {
    private final Frame frame;
    private boolean used = false;

    public AudienceJoker(Frame frame) {
        this.frame = frame;
    }

    @Override
    public void use() {
        if (used) {
            JOptionPane.showMessageDialog(frame, "Seyirci Jokeri zaten kullanıldı!");
            return;
        }

        frame.sendMessageToServer("S");  // Program sunucusuna sinyal gönder
        sendJokerSignal("Seyirci jokeri kullanıldı");

        Frame.Question q = frame.getCurrentQuestion();
        String correct = q.dogru.toUpperCase();

        int correctVotes = (int) (Math.random() * 30) + 45;  // %45-%75
        int remaining = 100 - correctVotes;

        List<String> options = new java.util.ArrayList<>(List.of("A", "B", "C", "D"));
        options.remove(correct);
        Collections.shuffle(options);

        int vote1 = (int) (Math.random() * remaining);
        int vote2 = (int) (Math.random() * (remaining - vote1));
        int vote3 = remaining - vote1 - vote2;
        int[] votes = {vote1, vote2, vote3};

        String result = String.format("""
            Seyirciler şu şekilde cevap verdi:
            A: %d%%
            B: %d%%
            C: %d%%
            D: %d%%
            """,
            getVote("A", correct, correctVotes, options, votes),
            getVote("B", correct, correctVotes, options, votes),
            getVote("C", correct, correctVotes, options, votes),
            getVote("D", correct, correctVotes, options, votes)
        );

        JOptionPane.showMessageDialog(frame, result);
        used = true;
    }

    private int getVote(String option, String correct, int correctVotes,
                        List<String> wrongOptions, int[] votes) {
        if (option.equals(correct)) return correctVotes;
        int index = wrongOptions.indexOf(option);
        return (index >= 0 && index < votes.length) ? votes[index] : 0;
    }
}

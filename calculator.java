import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class calculator extends JFrame implements ActionListener, KeyListener {
    private JTextField display;
    private String input = "";

    public calculator() {
        setTitle("Calculator with Keyboard Interface");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        display = new JTextField();
        display.setFont(new Font("Arial", Font.BOLD, 28));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);
        add(display, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 4, 10, 10));
        String[] buttons = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "=", "+",
                "C"
        };

        for (String text : buttons) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Arial", Font.BOLD, 24));
            btn.addActionListener(this);
            btn.setFocusable(false);
            panel.add(btn);
        }

        add(panel, BorderLayout.CENTER);
        addKeyListener(this);
        setFocusable(true);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = ((JButton) e.getSource()).getText();
        processInput(cmd);
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        char keyChar = e.getKeyChar();
        String keyText = String.valueOf(keyChar);

        if ("0123456789.+-*/".contains(keyText)) {
            processInput(keyText);
        } else if (keyChar == KeyEvent.VK_ENTER || keyChar == '=') {
            processInput("=");
        } else if (keyChar == KeyEvent.VK_BACK_SPACE || keyChar == 'C' || keyChar == 'c') {
            processInput("C");
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    private void processInput(String cmd) {
        switch (cmd) {
            case "=":
                try {
                    input = input.replaceAll("[^0-9+\\-*/.]", ""); // sanitize
                    double result = eval(input);
                    display.setText(String.valueOf(result));
                    input = String.valueOf(result);
                } catch (Exception ex) {
                    display.setText("Error");
                    input = "";
                }
                break;
            case "C":
                input = "";
                display.setText("");
                break;
            default:
                input += cmd;
                display.setText(input);
        }
    }

    // Basic eval for + - * /
    public double eval(String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ')
                    nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expr.length())
                    throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar: expression = term | expression `+` term | expression `-` term
            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    if (eat('+'))
                        x += parseTerm();
                    else if (eat('-'))
                        x -= parseTerm();
                    else
                        return x;
                }
            }

            // term = factor | term `*` factor | term `/` factor
            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if (eat('*'))
                        x *= parseFactor();
                    else if (eat('/'))
                        x /= parseFactor();
                    else
                        return x;
                }
            }

            // factor = `+` factor | `-` factor | number | `(` expression `)`
            double parseFactor() {
                if (eat('+'))
                    return +parseFactor();
                if (eat('-'))
                    return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.')
                        nextChar();
                    x = Double.parseDouble(expr.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }
                return x;
            }
        }.parse();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new calculator());
    }
}

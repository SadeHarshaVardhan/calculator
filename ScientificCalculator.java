import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class ScientificCalculator extends JFrame implements ActionListener {
    private JTextField display;
    private String expression = "";

    String[] buttonLabels = {
            "CE", "⌫", "(", ")",
            "sin", "cos", "tan", "√",
            "log", "ln", "e^x", "10^x",
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "^", "+",
            "π", "!", "=", "C"
    };

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setSize(500, 600);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        display = new JTextField();
        display.setFont(new Font("Consolas", Font.BOLD, 28));
        display.setEditable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);
        add(display, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(8, 4, 5, 5));

        for (String label : buttonLabels) {
            JButton btn = new JButton(label);
            btn.setFont(new Font("Arial", Font.BOLD, 20));
            btn.addActionListener(this);
            panel.add(btn);
        }

        add(panel, BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = ((JButton) e.getSource()).getText();

        switch (cmd) {
            case "C":
                expression = "";
                display.setText("");
                break;
            case "⌫":
                if (!expression.isEmpty()) {
                    expression = expression.substring(0, expression.length() - 1);
                    display.setText(expression);
                }
                break;
            case "CE":
                expression = "";
                display.setText("");
                break;
            case "=":
                try {
                    double result = evaluateExpression(expression);
                    display.setText(String.valueOf(result));
                    expression = String.valueOf(result);
                } catch (Exception ex) {
                    display.setText("Error");
                    expression = "";
                }
                break;
            case "π":
                expression += Math.PI;
                display.setText(expression);
                break;
            case "sin":
            case "cos":
            case "tan":
            case "log":
            case "ln":
            case "√":
            case "e^x":
            case "10^x":
            case "!":
                expression += cmd + "(";
                display.setText(expression);
                break;
            default:
                expression += cmd;
                display.setText(expression);
        }
    }

    private double evaluateExpression(String expr) {
        expr = expr.replaceAll("π", String.valueOf(Math.PI));
        expr = expr.replaceAll("√", "sqrt");
        expr = expr.replaceAll("e\\^x", "exp");
        expr = expr.replaceAll("10\\^x", "10^");

        return evaluate(expr);
    }

    // Evaluate scientific expressions
    private double evaluate(String expr) {
        // NOTE: For full scientific support, consider integrating with exp4j, JEval, or
        // your own parser.
        // Below is a basic safe parser with some scientific support.
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

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+'))
                        x += parseTerm();
                    else if (eat('-'))
                        x -= parseTerm();
                    else
                        return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*'))
                        x *= parseFactor();
                    else if (eat('/'))
                        x /= parseFactor();
                    else
                        return x;
                }
            }

            double parseFactor() {
                if (eat('+'))
                    return parseFactor();
                if (eat('-'))
                    return -parseFactor();

                double x;
                int start = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.')
                        nextChar();
                    x = Double.parseDouble(expr.substring(start, this.pos));
                } else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z')
                        nextChar();
                    String func = expr.substring(start, this.pos);
                    x = parseFactor();
                    switch (func) {
                        case "sqrt":
                            x = Math.sqrt(x);
                            break;
                        case "sin":
                            x = Math.sin(Math.toRadians(x));
                            break;
                        case "cos":
                            x = Math.cos(Math.toRadians(x));
                            break;
                        case "tan":
                            x = Math.tan(Math.toRadians(x));
                            break;
                        case "log":
                            x = Math.log10(x);
                            break;
                        case "ln":
                            x = Math.log(x);
                            break;
                        case "exp":
                            x = Math.exp(x);
                            break;
                        default:
                            throw new RuntimeException("Unknown func: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected char: " + (char) ch);
                }

                if (eat('^'))
                    x = Math.pow(x, parseFactor());
                if (expr.length() > pos && expr.charAt(pos) == '!') {
                    pos++;
                    x = factorial((int) x);
                }

                return x;
            }

            int factorial(int n) {
                if (n < 0)
                    throw new RuntimeException("Factorial of negative");
                int res = 1;
                for (int i = 2; i <= n; i++)
                    res *= i;
                return res;
            }
        }.parse();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ScientificCalculator::new);
    }
}

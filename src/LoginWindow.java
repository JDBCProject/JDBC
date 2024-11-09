import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginWindow extends JFrame {
    private JTextField usrField; // 아이디 입력 필드
    private JPasswordField passwordField; // 비밀번호 입력 필드
    private JButton loginButton;
    private Connection connection;
    private JLabel messageLabel;

    public LoginWindow() {
        setTitle("MySQL Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel usrLabel = new JLabel("User name:");
        JLabel pwdLabel = new JLabel("Password:");
        messageLabel = new JLabel("", SwingConstants.CENTER);
        usrField = new JTextField();
        passwordField = new JPasswordField();

        formPanel.add(usrLabel);
        formPanel.add(usrField);
        formPanel.add(pwdLabel);
        formPanel.add(passwordField);

        loginButton = new JButton("Login To MySQL");
        loginButton.addActionListener(e -> performLogin());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(messageLabel, BorderLayout.NORTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }
    private void performLogin() {
        String usrname = usrField.getText();
        String password = new String(passwordField.getPassword());

        try {
            String url = "jdbc:mysql://localhost:3306/mydb?serverTimeZone=UTC"; // 접속 url
            connection = DriverManager.getConnection(url, usrname, password);
            JOptionPane.showMessageDialog(this, "로그인 성공!", "MySQL", JOptionPane.INFORMATION_MESSAGE);

            new InfoRetrieval(connection);
            dispose();
        } catch (SQLException e) {
            messageLabel.setText("로그인 실패, 아이디 및 비밀번호를 확인해주세요.");
            messageLabel.setForeground(Color.BLACK);
        }
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AdminLogin extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private AdminDAO adminDAO;

    public AdminLogin() {
        this.adminDAO = new AdminDAO();

        setTitle("Enterprise SMS - Secure Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- TITLE ---
        JLabel titleLabel = new JLabel("System Gateway");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        // --- EMAIL ---
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Email Address:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        emailField = new JTextField(15);
        add(emailField, gbc);

        // --- PASSWORD ---
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        passwordField = new JPasswordField(15);
        add(passwordField, gbc);

        // --- BUTTONS ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        JButton loginBtn = new JButton("Secure Login");
        loginBtn.setBackground(new Color(41, 128, 185));
        loginBtn.setForeground(Color.WHITE);

        JButton registerBtn = new JButton("Create Account");
        registerBtn.setBackground(new Color(149, 165, 166));
        registerBtn.setForeground(Color.WHITE);

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        add(buttonPanel, gbc);

        // --- ACTION LISTENERS ---

        // LOGIN LOGIC
        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both email and password.");
                return;
            }

            String adminName = adminDAO.login(email, password);

            if (adminName != null) {
                this.dispose(); 
                new MainDashboard(adminName).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        });

            
        registerBtn.addActionListener(e -> {

            JTextField regNameField = new JTextField(15);
            JTextField regEmailField = new JTextField(15);
            JTextField regDeptField = new JTextField(15);
            JTextField regSubField = new JTextField(15);
            JTextField regQualField = new JTextField(15);

            JPanel regPanel = new JPanel(new GridLayout(5, 2, 10, 10));
            regPanel.add(new JLabel("Full Name:"));
            regPanel.add(regNameField);
            regPanel.add(new JLabel("Official Email:"));
            regPanel.add(regEmailField);
            regPanel.add(new JLabel("Department:"));
            regPanel.add(regDeptField);
            regPanel.add(new JLabel("Subject Taught:"));
            regPanel.add(regSubField);
            regPanel.add(new JLabel("Qualifications:"));
            regPanel.add(regQualField);

            
            int result = JOptionPane.showConfirmDialog(this, regPanel, 
                     "Professor Registration Form", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

       
            if (result == JOptionPane.OK_OPTION) {
                String newName = regNameField.getText().trim();
                String newEmail = regEmailField.getText().trim();
                String newDept = regDeptField.getText().trim();
                String newSub = regSubField.getText().trim();
                String newQual = regQualField.getText().trim();

               
                if (newName.isEmpty() || newEmail.isEmpty() || newDept.isEmpty() || newSub.isEmpty() || newQual.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success = adminDAO.registerAdmin(newName, newEmail, newDept, newSub, newQual);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Account Created! Default password is: pass@123\nPlease login.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create account. Email may already exist.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AdminLogin().setVisible(true);
        });
    }
}
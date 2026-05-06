import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Login and registration frame for the UMAK Lost & Found system.
 * Handles user authentication, account creation, and password recovery.
 *
 * @author UMAK Lost & Found Team
 */
public class LoginFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainCardPanel = new JPanel(cardLayout);
    
    // Login Fields
    private JTextField loginUser = new JTextField();
    private JPasswordField loginPass = new JPasswordField();
    
    // Registration Fields
    private JTextField regName = new JTextField();
    private JTextField regID = new JTextField();
    private JTextField regEmail = new JTextField();
    private JPasswordField regPass = new JPasswordField();
    private JComboBox<String> regDept = new JComboBox<>(new String[]{
        "Select Department",
        "CCAPS: College of Continuing, Advanced and Professional Studies",
        "IAD: Institute of Arts and Design",
        "CBFS: College of Business and Financial Science",
        "IOA: Institute of Accountancy",
        "CCIS: College of Computing and Information Sciences",
        "CCSE: College of Construction Sciences and Engineering",
        "CHK: College of Human Kinetics",
        "CGPP: College of Governance and Public Policy",
        "ION: Institute of Nursing",
        "IOP: Institute of Pharmacy",
        "IIHS: Institute of Imaging Health Sciences",
        "CITE: College of Innovative Teacher Education",
        "IOPsy: Institute of Psychology",
        "CTHM: College of Tourism and Hospitality Management",
        "IDEM: Institute for Disaster and Emergency Management",
        "ISW: Institute for Social Work",
        "CET: College of Engineering Technology (Formerly CTM)",
        "SOL: School of Law",
        "CITE-HSU: Higher School ng UMak (Senior High School level)"
    });

    private final Color PRIMARY = new Color(0, 30, 64);
    private final Color SECONDARY = new Color(0, 106, 106);
    private final String BACKGROUND_PATH = "sample image/background.jpg"; 
    private final String LOGO_PATH = "sample image/logo.png";
    private final String PROJECT_LOGO_PATH = "sample image/project_logo.png";

    public LoginFrame() {
        setTitle("UMAK Lost & Found Inventory - Login");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        mainCardPanel.add(createLoginView(), "login");
        mainCardPanel.add(createRegisterView(), "register");

        add(mainCardPanel);
    }

    private JPanel createLoginView() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        
        // Left Panel: Background Image + Text
        mainPanel.add(new LeftImagePanel(BACKGROUND_PATH));
        
        // Right Panel: Login Form
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Logo and Header ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Color.WHITE);
        
        ImageIcon logoIcon = new ImageIcon(new ImageIcon(LOGO_PATH).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        JLabel logoLabel = new JLabel(logoIcon);
        
        ImageIcon projectLogoIcon = new ImageIcon(new ImageIcon(PROJECT_LOGO_PATH).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        JLabel projectLogoLabel = new JLabel(projectLogoIcon);
        
        JLabel brandText = new JLabel("<html><b style='font-size:16px;'>UNIVERSITY OF MAKATI</b><br>Lost & Found Inventory</html>");
        header.add(logoLabel);
        header.add(projectLogoLabel);
        header.add(brandText);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        rightPanel.add(header, gbc);

        // --- Email/User Field ---
        gbc.gridy = 1; gbc.insets = new Insets(30, 40, 5, 40);
        JLabel emailLabel = new JLabel("Student No / Email:");
        emailLabel.setFont(new Font("Inter", Font.BOLD, 12));
        rightPanel.add(emailLabel, gbc);
        
        gbc.gridy = 2; gbc.insets = new Insets(0, 50, 20, 50);
        loginUser.setPreferredSize(new Dimension(350, 45));
        loginUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your ID or Email");
        loginUser.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        rightPanel.add(loginUser, gbc);

        // --- Password Field ---
        gbc.gridy = 3; gbc.insets = new Insets(10, 50, 5, 50);
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Inter", Font.BOLD, 12));
        rightPanel.add(passLabel, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(0, 50, 20, 50);
        loginPass.setPreferredSize(new Dimension(350, 45));
        loginPass.putClientProperty(FlatClientProperties.STYLE, "arc: 10; showRevealButton: true");
        rightPanel.add(loginPass, gbc);

        // --- Forgot Password & Register Link ---
        gbc.gridy = 5; gbc.insets = new Insets(0, 40, 20, 40);
        JPanel linksPanel = new JPanel(new BorderLayout());
        linksPanel.setBackground(Color.WHITE);
        
        JLabel forgotPass = new JLabel("Forgot Password?");
        forgotPass.setForeground(Color.GRAY);
        forgotPass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPass.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleForgotPassword();
            }
        });
        
        JButton goReg = new JButton("Create account");
        styleLinkBtn(goReg);
        goReg.addActionListener(e -> cardLayout.show(mainCardPanel, "register"));
        
        linksPanel.add(forgotPass, BorderLayout.WEST);
        linksPanel.add(goReg, BorderLayout.EAST);
        rightPanel.add(linksPanel, gbc);

        // --- Sign In Button ---
        gbc.gridy = 6; gbc.insets = new Insets(10, 40, 10, 40);
        JButton signInBtn = new JButton("Sign In");
        stylePrimaryBtn(signInBtn);
        signInBtn.addActionListener(e -> handleLogin());
        rightPanel.add(signInBtn, gbc);

        // --- Horizontal Line ---
        gbc.gridy = 7; gbc.insets = new Insets(20, 40, 20, 40);
        rightPanel.add(new JSeparator(), gbc);

        // --- Google Sign In ---
        gbc.gridy = 8;
        JButton googleBtn = new JButton("Sign in with Google");
        googleBtn.setBackground(Color.WHITE);
        googleBtn.setFocusPainted(false);
        googleBtn.setPreferredSize(new Dimension(300, 40));
        googleBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        rightPanel.add(googleBtn, gbc);

        mainPanel.add(rightPanel);
        return mainPanel;
    }

    private JPanel createRegisterView() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(new LeftImagePanel(BACKGROUND_PATH));

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Public Sans", Font.BOLD, 24));
        title.setForeground(PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 40, 30, 40);
        rightPanel.add(title, gbc);

        gbc.insets = new Insets(5, 40, 5, 40);
        gbc.gridwidth = 2;
        
        gbc.gridy = 1; rightPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridy = 2; regName.setPreferredSize(new Dimension(300, 35)); rightPanel.add(regName, gbc);
        
        gbc.gridy = 3; rightPanel.add(new JLabel("Student Number:"), gbc);
        gbc.gridy = 4; regID.setPreferredSize(new Dimension(300, 35)); rightPanel.add(regID, gbc);
        
        gbc.gridy = 5; rightPanel.add(new JLabel("Department:"), gbc);
        gbc.gridy = 6; regDept.setPreferredSize(new Dimension(300, 35)); rightPanel.add(regDept, gbc);

        gbc.gridy = 7; rightPanel.add(new JLabel("Email:"), gbc);
        gbc.gridy = 8; regEmail.setPreferredSize(new Dimension(300, 35)); rightPanel.add(regEmail, gbc);
        
        gbc.gridy = 9; rightPanel.add(new JLabel("Password:"), gbc);
        gbc.gridy = 10; regPass.setPreferredSize(new Dimension(300, 35)); rightPanel.add(regPass, gbc);

        gbc.gridy = 11; gbc.insets = new Insets(30, 40, 10, 40);
        JButton registerBtn = new JButton("Register");
        stylePrimaryBtn(registerBtn);
        registerBtn.addActionListener(e -> handleRegister());
        rightPanel.add(registerBtn, gbc);

        gbc.gridy = 12; gbc.insets = new Insets(0, 40, 10, 40);
        JButton backBtn = new JButton("← Back to Login");
        styleLinkBtn(backBtn);
        backBtn.addActionListener(e -> cardLayout.show(mainCardPanel, "login"));
        rightPanel.add(backBtn, gbc);

        mainPanel.add(rightPanel);
        return mainPanel;
    }

    private void stylePrimaryBtn(JButton b) {
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Inter", Font.BOLD, 15));
        b.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderPainted: false; focusPainted: false");
        b.setPreferredSize(new Dimension(300, 45));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleLinkBtn(JButton b) {
        b.setForeground(SECONDARY);
        b.setFont(new Font("Inter", Font.BOLD, 13));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void handleLogin() {
        String user = loginUser.getText().trim();
        String pass = new String(loginPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        try {
            conn = DBConnection.connect();
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Cannot connect to database. Make sure XAMPP MySQL is running.", "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE (student_no = ? OR email = ?) AND password = ?");
            ps.setString(1, user);
            ps.setString(2, user);
            ps.setString(3, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int uid = rs.getInt("id");
                try (PreparedStatement updatePs = conn.prepareStatement("UPDATE users SET is_online = 1, last_seen = CURRENT_TIMESTAMP WHERE id = ?")) {
                    updatePs.setInt(1, uid);
                    updatePs.executeUpdate();
                }
                UserSession.init(
                    uid,
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getString("student_no"),
                    rs.getString("email"),
                    rs.getString("department")
                );

                // Create and show main window
                UMAKSystemMain mainWindow = new UMAKSystemMain();
                mainWindow.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Login Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }

    private void handleForgotPassword() {
        JTextField emailField = new JTextField();
        JTextField idField = new JTextField();
        Object[] message = {
            "Enter your registered Email:", emailField,
            "Enter your Student Number:", idField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Reset Password", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.connect();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ? AND student_no = ?")) {
                ps.setString(1, emailField.getText());
                ps.setString(2, idField.getText());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String newPass = JOptionPane.showInputDialog(this, "User verified! Enter new password:");
                    if (newPass != null && !newPass.isEmpty()) {
                        PreparedStatement updatePs = conn.prepareStatement("UPDATE users SET password = ? WHERE id = ?");
                        updatePs.setString(1, newPass);
                        updatePs.setInt(2, rs.getInt("id"));
                        updatePs.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Password updated successfully!");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "User not found with these details.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    private void handleRegister() {
        if(regName.getText().isEmpty() || regID.getText().isEmpty() || regEmail.getText().isEmpty() || regDept.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields and select a department");
            return;
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO users (full_name, student_no, email, password, department, role) VALUES (?,?,?,?,?,'Student')")) {
            ps.setString(1, regName.getText());
            ps.setString(2, regID.getText());
            ps.setString(3, regEmail.getText());
            ps.setString(4, new String(regPass.getPassword()));
            ps.setString(5, regDept.getSelectedItem().toString());
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.");
            cardLayout.show(mainCardPanel, "login");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration Error: " + ex.getMessage());
        }
    }

    class LeftImagePanel extends JPanel {
        private Image bgImage;

        public LeftImagePanel(String path) {
            this.bgImage = new ImageIcon(path).getImage();
            setLayout(new GridBagLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                int iw = bgImage.getWidth(this);
                int ih = bgImage.getHeight(this);
                if (iw > 0 && ih > 0) {
                    double imageAspect = (double) iw / ih;
                    double canvasAspect = (double) getWidth() / getHeight();
                    
                    int x, y, w, h;
                    if (canvasAspect > imageAspect) {
                        w = getWidth();
                        h = (int) (w / imageAspect);
                        x = 0;
                        y = (getHeight() - h) / 2;
                    } else {
                        h = getHeight();
                        w = (int) (h * imageAspect);
                        x = (getWidth() - w) / 2;
                        y = 0;
                    }
                    g.drawImage(bgImage, x, y, w, h, this);
                }
            }
            // Dark blue overlay (slight) - using Primary color with alpha
            g.setColor(new Color(0, 30, 64, 180)); 
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        @Override
        public void addNotify() {
            super.addNotify();
            JPanel textOverlay = new JPanel();
            textOverlay.setOpaque(false);
            textOverlay.setLayout(new BoxLayout(textOverlay, BoxLayout.Y_AXIS));

            JLabel title1 = new JLabel("Find What's Yours.");
            JLabel title2 = new JLabel("Return What's Theirs.");
            JLabel description = new JLabel("<html><div style='text-align: center; width: 300px;'>"
                + "The official UMak community hub to quickly report lost belongings and turn in found items.</div></html>");

            Font boldFont = new Font("SansSerif", Font.BOLD, 36);
            title1.setFont(boldFont); title1.setForeground(Color.WHITE);
            title2.setFont(boldFont); title2.setForeground(Color.WHITE);
            
            description.setFont(new Font("SansSerif", Font.PLAIN, 16));
            description.setForeground(new Color(240, 240, 240));
            description.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

            title1.setAlignmentX(Component.CENTER_ALIGNMENT);
            title2.setAlignmentX(Component.CENTER_ALIGNMENT);
            description.setAlignmentX(Component.CENTER_ALIGNMENT);

            textOverlay.add(title1);
            textOverlay.add(title2);
            textOverlay.add(description);
            add(textOverlay);
        }
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

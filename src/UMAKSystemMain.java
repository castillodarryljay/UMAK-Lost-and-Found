import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;

/**
 * Main application window for the UMAK Lost & Found Inventory System.
 * Provides the primary interface with navigation sidebar and content panels.
 * Features:
 * - Dark/Light theme switching
 * - User authentication and session management
 * - Item reporting and browsing
 * - Claim management (admin only)
 * - Activity tracking and profile management
 *
 * @author UMAK Lost & Found Team
 * @version 1.0
 */
public class UMAKSystemMain extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContent = new JPanel(cardLayout);
    private File selectedImageFile = null;
    private boolean isDarkMode = false;
    private String currentCard = "dash";
    private java.util.Map<String, JButton> navButtons = new java.util.HashMap<>();
    
    // Updated Theme Colors
    private final Color SIDEBAR_BG = new Color(13, 27, 42); 
    private Color PRIMARY = new Color(0, 30, 64);
    private Color SECONDARY = new Color(0, 106, 106);
    private Color SURFACE = new Color(247, 250, 252);
    private Color OUTLINE = new Color(115, 119, 128);
    private Color TEXT_MAIN = Color.BLACK;
    private Color CARD_BG = Color.WHITE;
    
    // Inventory Design Colors
    private final Color BORDER_LIGHT = new Color(200, 200, 200);
    private final Color STATUS_PENDING_BG = new Color(235, 155, 150);
    private final Color STATUS_APPROVED_BG = new Color(215, 245, 215);
    private final Color TEXT_GRAY = new Color(150, 150, 150);
    
    private final String BACKGROUND_PATH = "sample image/background.jpg"; 
    private final String LOGO_PATH = "sample image/logo.png";
    private final String PROJECT_LOGO_PATH = "sample image/project_logo.png";

    private UMAKDashboard dashboardPanel;
    private JPanel profilePanelContainer;
    public UMAKSystemMain() {
        FlatLightLaf.setup();
        setTitle("UMAK Lost & Found Inventory - Portal");
        setSize(1350, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        dashboardPanel = new UMAKDashboard(SURFACE, CARD_BG);
        mainContent.add(dashboardPanel, "dash");
        mainContent.add(createReportForm(), "report");
        mainContent.add(createProfilePanel(), "profile");
        mainContent.add(createMyActivityPanel(), "my_activity");
        
        if (UserSession.role != null && UserSession.role.equalsIgnoreCase("Admin")) {
            mainContent.add(createAdminStatusPanel(), "admin_status");
            mainContent.add(createAdminStatsPanel(), "admin_stats");
            mainContent.add(createAdminInventoryPanel(), "admin_inventory");
            mainContent.add(createAdminSessionsPanel(), "admin_sessions");
        }

        add(createSidebar(), BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                setOfflineStatus();
            }
        });

        refreshDashboard();
    }

    private void setOfflineStatus() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("UPDATE users SET is_online = 0 WHERE id = ?")) {
            ps.setInt(1, UserSession.userId);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            FlatDarkLaf.setup();
            SURFACE = new Color(18, 18, 18);
            CARD_BG = new Color(30, 30, 30);
            TEXT_MAIN = Color.WHITE;
            OUTLINE = new Color(180, 180, 180);
        } else {
            FlatLightLaf.setup();
            SURFACE = new Color(247, 250, 252);
            CARD_BG = Color.WHITE;
            TEXT_MAIN = Color.BLACK;
            OUTLINE = new Color(115, 119, 128);
        }
        SwingUtilities.updateComponentTreeUI(this);
        refreshFullUI();
    }

    private void refreshFullUI() {
        mainContent.removeAll();
        dashboardPanel = new UMAKDashboard(SURFACE, CARD_BG);
        mainContent.add(dashboardPanel, "dash");
        mainContent.add(createReportForm(), "report");
        mainContent.add(createProfilePanel(), "profile");
        mainContent.add(createMyActivityPanel(), "my_activity");
        if (UserSession.role != null && UserSession.role.equalsIgnoreCase("Admin")) {
            mainContent.add(createAdminStatusPanel(), "admin_status");
            mainContent.add(createAdminStatsPanel(), "admin_stats");
            mainContent.add(createAdminInventoryPanel(), "admin_inventory");
            mainContent.add(createAdminSessionsPanel(), "admin_sessions");
        }
        
        getContentPane().removeAll();
        add(createSidebar(), BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG); 
        sidebar.setPreferredSize(new Dimension(280, 900));

        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(30, 25, 30, 25));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brandPanel.setOpaque(false);
        brandPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        ImageIcon logoIcon = new ImageIcon(new ImageIcon(LOGO_PATH).getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
        JLabel logoLabel = new JLabel(logoIcon);
        
        ImageIcon projectLogoIcon = new ImageIcon(new ImageIcon(PROJECT_LOGO_PATH).getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
        JLabel projectLogoLabel = new JLabel(projectLogoIcon);
        
        JLabel brandLabel = new JLabel("<html><b style='color:white; font-size:17px;'>Lost & Found</b><br><font color='#006a6a'>UMAK Inventory</font></html>");
        
        brandPanel.add(logoLabel);
        brandPanel.add(projectLogoLabel);
        brandPanel.add(Box.createRigidArea(new Dimension(12, 0)));
        brandPanel.add(brandLabel);
        
        navPanel.add(brandPanel);
        navPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        navPanel.add(createNavBtn("🏠   Dashboard", "dash"));
        navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        navPanel.add(createNavBtn("➕   Report Item", "report"));
        navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        navPanel.add(createNavBtn("📋   My Activity", "my_activity"));
        navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        navPanel.add(createNavBtn("👤   Profile", "profile"));
        
        if (UserSession.role != null && UserSession.role.equalsIgnoreCase("Admin")) {
            navPanel.add(Box.createRigidArea(new Dimension(0, 35)));
            JLabel adminLbl = new JLabel("ADMINISTRATION");
            adminLbl.setForeground(new Color(100, 116, 139));
            adminLbl.setFont(new Font("Inter", Font.BOLD, 11));
            adminLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            navPanel.add(adminLbl);
            navPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            
            navPanel.add(createNavBtn("📊   Statistics", "admin_stats"));
            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            navPanel.add(createNavBtn("⚖️   Manage Claims", "admin_status"));
            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            navPanel.add(createNavBtn("📦   Inventory", "admin_inventory"));
            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            navPanel.add(createNavBtn("👥   Sessions", "admin_sessions"));
        }

        navPanel.add(Box.createVerticalGlue());
        
        JButton themeToggle = new JButton(isDarkMode ? "☀️   Light Mode" : "🌙   Dark Mode");
        themeToggle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        themeToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        themeToggle.setBackground(SIDEBAR_BG);
        themeToggle.setForeground(new Color(200, 200, 200));
        themeToggle.setFont(new Font("Inter", Font.BOLD, 14));
        themeToggle.setHorizontalAlignment(SwingConstants.LEFT);
        themeToggle.setFocusPainted(false);
        themeToggle.setBorderPainted(false);
        themeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeToggle.setToolTipText(isDarkMode ? "Switch to Light Mode for better visibility in bright environments" : "Switch to Dark Mode to reduce eye strain in low light");
        themeToggle.addActionListener(e -> toggleDarkMode());
        navPanel.add(themeToggle);

        sidebar.add(navPanel, BorderLayout.CENTER);

        JPanel profileCard = new JPanel(new BorderLayout());
        profileCard.setBackground(new Color(255, 255, 255, 10));
        profileCard.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);
        JLabel nameLbl = new JLabel(UserSession.fullName != null ? UserSession.fullName : "User");
        nameLbl.setFont(new Font("Inter", Font.BOLD, 14));
        nameLbl.setForeground(Color.WHITE);
        JLabel emailLbl = new JLabel(UserSession.email != null ? UserSession.email : "");
        emailLbl.setFont(new Font("Inter", Font.PLAIN, 11));
        emailLbl.setForeground(new Color(160, 160, 160));
        userInfo.add(nameLbl);
        userInfo.add(emailLbl);
        profileCard.add(userInfo, BorderLayout.CENTER);

        JButton logout = new JButton("Logout");
        logout.setBackground(new Color(255, 255, 255, 15));
        logout.setForeground(Color.WHITE);
        logout.setFocusPainted(false);
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> { 
            setOfflineStatus();
            FlatLightLaf.setup();
            new LoginFrame().setVisible(true); 
            this.dispose(); 
        });
        
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 10));
        bottomContainer.setOpaque(false);
        bottomContainer.add(profileCard, BorderLayout.CENTER);
        bottomContainer.add(logout, BorderLayout.SOUTH);

        sidebar.add(bottomContainer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavBtn(String text, String card) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(new Color(220, 220, 220));
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Store button reference for active state tracking
        navButtons.put(card, btn);

        // Add tooltips based on card type
        switch (card) {
            case "dash": btn.setToolTipText("View the dashboard and browse all items"); break;
            case "report": btn.setToolTipText("Report a new lost or found item"); break;
            case "my_activity": btn.setToolTipText("View your activity history"); break;
            case "profile": btn.setToolTipText("View and manage your profile"); break;
            case "admin_stats": btn.setToolTipText("View system statistics"); break;
            case "admin_status": btn.setToolTipText("Manage all claims"); break;
            case "admin_inventory": btn.setToolTipText("View and manage all items"); break;
            case "admin_sessions": btn.setToolTipText("View active user sessions"); break;
        }

        btn.addActionListener(e -> {
            setActiveNavButton(card);
            refreshCurrentPanel(card);
            cardLayout.show(mainContent, card);
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!card.equals(currentCard)) btn.setBackground(new Color(30, 41, 59));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!card.equals(currentCard)) btn.setBackground(SIDEBAR_BG);
            }
        });
        return btn;
    }

    private void setActiveNavButton(String card) {
        // Reset previous active button - only change background
        JButton prevBtn = navButtons.get(currentCard);
        if (prevBtn != null) {
            prevBtn.setBackground(SIDEBAR_BG);
        }
        // Set new active button - highlight without moving (background only)
        JButton currentBtn = navButtons.get(card);
        if (currentBtn != null) {
            currentBtn.setBackground(new Color(30, 41, 59));
        }
        currentCard = card;
    }

    private void refreshCurrentPanel(String card) {
        if(card.equals("dash")) refreshDashboard();
        else if(card.equals("admin_stats")) refreshAdminStats();
        else if(card.equals("admin_status")) refreshAdminClaimsTable();
        else if(card.equals("admin_inventory")) refreshAdminInventory();
        else if(card.equals("admin_sessions")) refreshAdminSessions();
        else if(card.equals("my_activity")) refreshMyActivity();
        else if(card.equals("profile")) refreshProfile();
    }

    public void navigateToClaims(int itemId) {
        cardLayout.show(mainContent, "admin_status");
        refreshAdminClaimsTable(itemId);
    }

    private DefaultTableModel sessionsModel;
    private JTable sessionsTable;
    private JPanel createAdminSessionsPanel() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(SURFACE);
        mainContent.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Title: "Sessions"
        JLabel title = new JLabel("User Sessions");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainContent.add(title, BorderLayout.NORTH);

        // Table Container (The Box)
        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBackground(CARD_BG);
        tableBox.setBorder(new javax.swing.border.LineBorder(OUTLINE, 1));
        
        String[] columns = {"Account", "Full Name", "Student ID", "Last Seen", "Status"};
        sessionsModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        sessionsTable = new JTable(sessionsModel);
        sessionsTable.setRowHeight(50);
        sessionsTable.setShowGrid(false);
        sessionsTable.setIntercellSpacing(new Dimension(0, 0));
        sessionsTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        sessionsTable.setBackground(CARD_BG);
        sessionsTable.setForeground(TEXT_MAIN);

        // Header Styling
        javax.swing.table.JTableHeader header = sessionsTable.getTableHeader();
        header.setBackground(CARD_BG);
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.setForeground(TEXT_MAIN);
        ((javax.swing.table.DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        // Custom Renderer for the Status column
        sessionsTable.getColumnModel().getColumn(4).setCellRenderer(new SessionStatusRenderer());

        JScrollPane scrollPane = new JScrollPane(sessionsTable);
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        tableBox.add(scrollPane, BorderLayout.CENTER);
        mainContent.add(tableBox, BorderLayout.CENTER);

        return mainContent;
    }

    private void refreshAdminSessions() {
        if (sessionsModel == null) return;
        sessionsModel.setRowCount(0);
        try (Connection conn = DBConnection.connect(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT email, full_name, student_no, last_seen, is_online FROM users ORDER BY is_online DESC, last_seen DESC")) {
            while (rs.next()) {
                sessionsModel.addRow(new Object[]{
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getBoolean(5) ? "Online" : "Offline"
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    class SessionStatusRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private final Color ONLINE_GREEN = new Color(40, 167, 69);
        private final Color OFFLINE_RED = new Color(220, 53, 69);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String status = (String) value;
            JLabel label = new JLabel();
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            
            if ("Online".equalsIgnoreCase(status)) {
                label.setForeground(ONLINE_GREEN);
                label.setText("● Online");
            } else {
                label.setForeground(OFFLINE_RED);
                label.setText("○ Offline");
            }
            
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            wrapper.add(label);
            return wrapper;
        }
    }

    private void refreshDashboard() {
        if (dashboardPanel != null) {
            dashboardPanel.refreshData();
        }
    }

    private JPanel createReportForm() {
        JPanel w = new JPanel(new GridBagLayout()); 
        w.setBackground(SURFACE);
        
        JPanel cd = new JPanel(new GridBagLayout());
        cd.setBackground(CARD_BG); 
        cd.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        cd.putClientProperty(FlatClientProperties.STYLE, "arc: 40; borderPainted: true; borderColor: " + (isDarkMode ? "#3c3c3c" : "#f1f5f9"));
        cd.setPreferredSize(new Dimension(800, 850));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- TITLE: Report Item ---
        JLabel title = new JLabel("Report Item");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(TEXT_MAIN);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 25, 0);
        cd.add(title, gbc);

        // --- ITEM NAME ---
        gbc.gridy = 2;
        JTextField nameField = createTextField("What did you lose?");
        addLabelAndFieldToPanel(cd, "Item Name", nameField, 2, 0, gbc, true);

        // --- STATUS SELECTION (Lost/Found) ---
        gbc.gridy = 1;
        JPanel statusPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        statusPanel.setOpaque(false);
        JRadioButton lostRadio = new JRadioButton("REPORT LOST", true);
        JRadioButton foundRadio = new JRadioButton("REPORT FOUND");
        ButtonGroup statusGroup = new ButtonGroup();
        statusGroup.add(lostRadio); statusGroup.add(foundRadio);
        
        lostRadio.addActionListener(e -> {
            nameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "What did you lose?");
            nameField.repaint();
        });
        foundRadio.addActionListener(e -> {
            nameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "What did you find?");
            nameField.repaint();
        });
        
        statusPanel.add(lostRadio); statusPanel.add(foundRadio);
        cd.add(statusPanel, gbc);

        // --- ROW 1: Category & Date Lost  ---
        gbc.gridy = 4;
        JComboBox<String> catCombo = createComboBox(new String[]{"Select Category", "ID Card", "Electronics", "Wallet", "Documents", "Keys", "Bags", "Other"});
        addLabelAndFieldToPanel(cd, "Category", catCombo, 3, 0, gbc, false);
        
        // Date Spinner for easier selection
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new java.util.Date()); // Default to today
        addLabelAndFieldToPanel(cd, "Date", dateSpinner, 3, 1, gbc, false);

        // --- ROW 2: Time & Location  ---
        gbc.gridy = 6;
        JTextField timeField = createTextField("hh:mm (Optional)");
        addLabelAndFieldToPanel(cd, "Time (Optional)", timeField, 4, 0, gbc, false);
        
        JTextField locField = createTextField("Location");
        addLabelAndFieldToPanel(cd, "Location", locField, 4, 1, gbc, false);

        // --- ROW 3: Description (Full Width)  ---
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 0, 5, 0);
        cd.add(new JLabel("Description") {{ setForeground(Color.GRAY); }}, gbc);

        JTextArea descArea = new JTextArea(6, 20);
        descArea.setText("Provide more details about the item.");
        descArea.setForeground(Color.GRAY);
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createLineBorder(OUTLINE));
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.BOTH;
        cd.add(new JScrollPane(descArea), gbc);

        // --- ROW 4: Upload Photo (Dashed Area)  ---
        gbc.gridy = 10; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 0, 10, 0);
        cd.add(new JLabel("Upload Photo") {{ setForeground(Color.GRAY); }}, gbc);

        JPanel uploadPanel = createUploadPanel();
        gbc.gridy = 11;
        cd.add(uploadPanel, gbc);
        
        // --- SUBMIT BUTTON ---
        JButton submitBtn = new JButton("Submit Report");
        // Consistent primary button styling across the app
        submitBtn.setBackground(PRIMARY);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Inter", Font.BOLD, 16));
        submitBtn.setPreferredSize(new Dimension(200, 50));
        submitBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 15; borderPainted: false; focusPainted: false");
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setToolTipText("Submit the loss/found report");
        
        submitBtn.addActionListener(e -> {
            String itemType = lostRadio.isSelected() ? "Lost" : "Found";
            String category = catCombo.getSelectedItem().toString();
            
            // Task 2: Category validation
            if (category.equals("Select Category")) {
                JOptionPane.showMessageDialog(this, "Please select a category.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Task 1: Time validation
            String timeText = timeField.getText().trim();
            Time validatedTime = null;
            if (!timeText.isEmpty() && !timeText.contains("Optional")) {
                if (!timeText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                    JOptionPane.showMessageDialog(this, "Please enter time in HH:mm format (e.g., 09:30 or 14:05) or leave it blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    validatedTime = Time.valueOf(timeText + ":00");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid time format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            try (Connection c = DBConnection.connect()) {
                if (c == null) {
                    JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO items (item_name, category, description, location, item_type, status, image_path, reporter_id, date_lost, time_lost) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                    ps.setString(1, nameField.getText());
                    ps.setString(2, category);
                    ps.setString(3, descArea.getText());
                    ps.setString(4, locField.getText());
                    ps.setString(5, itemType);
                    ps.setString(6, "Pending");
                    ps.setString(7, (selectedImageFile != null ? selectedImageFile.getAbsolutePath() : ""));
                    ps.setInt(8, UserSession.userId);
                    
                    // Get date from JSpinner
                    java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    ps.setString(9, sdf.format(selectedDate));
                    
                    if (validatedTime != null) {
                        ps.setTime(10, validatedTime);
                    } else {
                        ps.setNull(10, java.sql.Types.TIME);
                    }
                    
                    ps.executeUpdate();
                    
                    // Task 8: Clear selected image
                    selectedImageFile = null;
                    
                    JOptionPane.showMessageDialog(this, "Report submitted successfully!");
                    cardLayout.show(mainContent, "dash");
                    refreshDashboard();
                    refreshAdminStats();
                    refreshAdminInventory();
                    refreshMyActivity();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Submission Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 0, 0, 0);
        cd.add(submitBtn, gbc);

        w.add(cd); return w;
    }

    private void addLabelAndFieldToPanel(JPanel panel, String labelText, JComponent field, int row, int col, GridBagConstraints gbc, boolean fullWidth) {
        if (fullWidth) {
            gbc.gridwidth = 2;
            gbc.weightx = 1.0;
            gbc.gridx = 0;
        } else {
            gbc.gridwidth = 1;
            gbc.weightx = 0.5;
            gbc.gridx = col;
        }
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Label
        gbc.gridy = (row * 2) - 1;
        gbc.insets = new Insets(10, (col == 1 && !fullWidth) ? 20 : 0, 2, 0);
        panel.add(new JLabel(labelText) {{ setForeground(Color.GRAY); }}, gbc);
        
        // Field
        gbc.gridy = row * 2;
        gbc.insets = new Insets(0, (col == 1 && !fullWidth) ? 20 : 0, 10, 0);
        field.setPreferredSize(new Dimension(200, 40));
        panel.add(field, gbc);
    }

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        tf.setBackground(CARD_BG);
        tf.setForeground(TEXT_MAIN);
        tf.setBorder(BorderFactory.createLineBorder(OUTLINE));
        return tf;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(CARD_BG);
        cb.setForeground(TEXT_MAIN);
        return cb;
    }

    private JPanel createUploadPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD_BG);
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(OUTLINE, 3, 2),
            BorderFactory.createEmptyBorder(30, 0, 30, 0)
        ));

        JLabel icon = new JLabel("☁"); 
        icon.setFont(new Font("SansSerif", Font.PLAIN, 40));
        icon.setForeground(Color.LIGHT_GRAY);
        
        JLabel text = new JLabel("Click to Upload or Drag and Drop");
        text.setForeground(Color.GRAY);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; p.add(icon, c);
        c.gridy = 1; p.add(text, c);

        p.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    selectedImageFile = chooser.getSelectedFile();
                    text.setText(selectedImageFile.getName());
                    text.setForeground(SECONDARY);
                }
            }
        });
        
        return p;
    }

    private DefaultTableModel actModel;
    private JTable actTable;
    private JPanel createMyActivityPanel() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(SURFACE);
        mainContent.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Title
        JLabel title = new JLabel("Activity History");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainContent.add(title, BorderLayout.NORTH);

        // Table Container (The Box)
        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBackground(CARD_BG);
        tableBox.setBorder(new javax.swing.border.LineBorder(OUTLINE, 1));
        
        // Table Data
        String[] columns = {"Type", "Item", "Status", "Date"};
        actModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        actTable = new JTable(actModel);
        styleTable(actTable, 2);

        JScrollPane scrollPane = new JScrollPane(actTable);
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        tableBox.add(scrollPane, BorderLayout.CENTER);
        mainContent.add(tableBox, BorderLayout.CENTER);

        return mainContent;
    }

    private void refreshMyActivity() {
        actModel.setRowCount(0);
        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps1 = c.prepareStatement("SELECT 'Reported', item_name, status, date_added FROM items WHERE reporter_id = ?");
            ps1.setInt(1, UserSession.userId); ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()) actModel.addRow(new Object[]{rs1.getString(1), rs1.getString(2), rs1.getString(3), rs1.getTimestamp(4)});
            PreparedStatement ps2 = c.prepareStatement("SELECT 'Claimed', i.item_name, c.status, c.claim_date FROM claims c JOIN items i ON c.item_id = i.id WHERE c.user_id = ?");
            ps2.setInt(1, UserSession.userId); ResultSet rs2 = ps2.executeQuery();
            while(rs2.next()) actModel.addRow(new Object[]{rs2.getString(1), rs2.getString(2), rs2.getString(3), rs2.getTimestamp(4)});
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel createProfilePanel() {
        profilePanelContainer = new JPanel(new BorderLayout());
        profilePanelContainer.setBackground(SURFACE);
        return profilePanelContainer;
    }

    private void refreshProfile() {
        profilePanelContainer.removeAll();
        
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(SURFACE);
        mainContent.setBorder(new EmptyBorder(40, 50, 40, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        // 1. Title: "Profile"
        JLabel profileTitle = new JLabel("Profile");
        profileTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        profileTitle.setForeground(TEXT_MAIN);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.weightx = 1.0; gbc.weighty = 0;
        mainContent.add(profileTitle, gbc);

        // 2. User Info Card (Top Wide Card)
        gbc.gridy = 1; gbc.weighty = 0.3;
        mainContent.add(createUserInfoCard(), gbc);

        // 3. Bottom Left: Activity Summary
        gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.5; gbc.weighty = 0.5;
        mainContent.add(createActivitySummary(), gbc);

        // 4. Bottom Right: Privacy & Security
        gbc.gridx = 1;
        mainContent.add(createPrivacySecurity(), gbc);

        profilePanelContainer.add(new JScrollPane(mainContent) {{ setBorder(null); }}, BorderLayout.CENTER);
        profilePanelContainer.revalidate();
        profilePanelContainer.repaint();
    }

    private JPanel createUserInfoCard() {
        JPanel card = new JPanel(new BorderLayout(30, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(60,60,60) : new Color(200,200,200), 1), 
            new EmptyBorder(30, 30, 30, 30)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 15");

        // Left side: JD Avatar
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(SECONDARY);
                g2d.fill(new java.awt.geom.Ellipse2D.Double(0, 0, 160, 160));
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 60));
                String initials = "";
                if (UserSession.fullName != null && !UserSession.fullName.isEmpty()) {
                    String[] parts = UserSession.fullName.split(" ");
                    if (parts.length > 0) initials += parts[0].charAt(0);
                    if (parts.length > 1) initials += parts[parts.length - 1].charAt(0);
                }
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(initials, (160 - fm.stringWidth(initials)) / 2, (160 + fm.getAscent() / 2) / 2);
                g2d.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(160, 160));
        avatarPanel.setOpaque(false);
        card.add(avatarPanel, BorderLayout.WEST);

        // Right side: Details
        JPanel details = new JPanel(new GridLayout(4, 1, 0, 5));
        details.setOpaque(false);
        details.add(createLabelRow("Name:", UserSession.fullName));
        details.add(createLabelRow("Student No.:", UserSession.studentNo));
        details.add(createLabelRow("UMak Email:", UserSession.email));
        details.add(createLabelRow("Department:", UserSession.department));
        card.add(details, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActivitySummary() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(60,60,60) : new Color(200,200,200), 1), 
            new EmptyBorder(25, 30, 25, 30)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 15");

        JLabel title = new JLabel("Account Activity Summary");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_MAIN);
        
        JPanel list = new JPanel(new GridLayout(4, 1, 0, 10));
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(20, 0, 20, 0));
        list.add(new JLabel("Tracker Summary") {{ setForeground(OUTLINE); }});
        
        int lost = 0, returned = 0, claimed = 0;
        try (Connection c = DBConnection.connect()) {
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
                return card;
            }
            // Active Lost Reports: item_type = 'Lost' and status not finalized
            PreparedStatement psLost = c.prepareStatement("SELECT COUNT(*) FROM items WHERE reporter_id = ? AND item_type = 'Lost' AND status NOT IN ('Returned', 'Denied', 'Claimed')");
            psLost.setInt(1, UserSession.userId);
            ResultSet rsLost = psLost.executeQuery();
            if(rsLost.next()) lost = rsLost.getInt(1);

            // Returned Items: status = 'Returned'
            PreparedStatement psRet = c.prepareStatement("SELECT COUNT(*) FROM items WHERE reporter_id = ? AND status = 'Returned'");
            psRet.setInt(1, UserSession.userId);
            ResultSet rsRet = psRet.executeQuery();
            if(rsRet.next()) returned = rsRet.getInt(1);

            // Claimed Items: Approved claims
            PreparedStatement psClaimed = c.prepareStatement("SELECT COUNT(*) FROM claims WHERE user_id = ? AND status = 'Approved'");
            psClaimed.setInt(1, UserSession.userId);
            ResultSet rsClaimed = psClaimed.executeQuery();
            if(rsClaimed.next()) claimed = rsClaimed.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }

        list.add(createStatusRow("Active Lost Reports:", String.valueOf(lost)));
        list.add(createStatusRow("Returned Items:", String.valueOf(returned)));
        list.add(createStatusRow("Claimed Items:", String.valueOf(claimed)));

        JButton btn = createStyledButton("View Activity Log");
        btn.addActionListener(e -> { cardLayout.show(mainContent, "my_activity"); refreshMyActivity(); });

        card.add(title, BorderLayout.NORTH);
        card.add(list, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createPrivacySecurity() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(60,60,60) : new Color(200,200,200), 1), 
            new EmptyBorder(25, 30, 25, 30)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 15");

        JLabel title = new JLabel("Privacy & Security", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(0,0,20,0));
        
        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 0, 15));
        btnPanel.setOpaque(false);
        
        JButton cpBtn = createStyledButton("Change Password");
        cpBtn.addActionListener(e -> {
            String p = JOptionPane.showInputDialog(this, "Enter New Password:");
            if(p != null && !p.isEmpty()) { 
                try (Connection c = DBConnection.connect()) { 
                    PreparedStatement ps = c.prepareStatement("UPDATE users SET password = ? WHERE id = ?"); 
                    ps.setString(1, p); ps.setInt(2, UserSession.userId); 
                    ps.executeUpdate(); 
                    JOptionPane.showMessageDialog(this, "Password updated!"); 
                } catch(Exception ex) {} 
            }
        });
        
        btnPanel.add(cpBtn);
        btnPanel.add(createStyledButton("Report Suspicious Activity"));
        btnPanel.add(createStyledButton("Download Account Data"));

        card.add(title, BorderLayout.NORTH);
        card.add(btnPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createLabelRow(String key, String val) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        p.setOpaque(false);
        JLabel k = new JLabel(key);
        k.setFont(new Font("SansSerif", Font.BOLD, 18));
        k.setForeground(TEXT_MAIN);
        JLabel v = new JLabel(val != null ? val : "N/A");
        v.setFont(new Font("SansSerif", Font.PLAIN, 18));
        v.setForeground(TEXT_MAIN);
        p.add(k); p.add(v);
        return p;
    }

    private JPanel createStatusRow(String key, String val) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel k = new JLabel(key);
        k.setForeground(TEXT_MAIN);
        p.add(k, BorderLayout.WEST);
        JLabel v = new JLabel(val);
        v.setFont(new Font("SansSerif", Font.BOLD, 16));
        v.setForeground(SECONDARY);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private JButton createStyledButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(isDarkMode ? new Color(45, 45, 45) : new Color(240, 242, 245));
        b.setForeground(TEXT_MAIN);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDarkMode ? new Color(80,80,80) : new Color(200,200,200)),
            new EmptyBorder(10, 20, 10, 20)
        ));
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setToolTipText(text + " button");
        return b;
    }

    private JPanel statsCardsPanel;
    private JPanel createAdminStatsPanel() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(SURFACE);
        mainContent.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("System Statistics");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainContent.add(title, BorderLayout.NORTH);

        statsCardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        statsCardsPanel.setOpaque(false);
        mainContent.add(statsCardsPanel, BorderLayout.CENTER);

        return mainContent;
    }

    private void refreshAdminStats() {
        if (statsCardsPanel == null) return;
        statsCardsPanel.removeAll();
        int lost = 0, found = 0, claimed = 0, pending = 0;
        try (Connection conn = DBConnection.connect()) {
            String itemSql = "SELECT item_type, COUNT(*) as count FROM items GROUP BY item_type";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(itemSql)) {
                while (rs.next()) {
                    String type = rs.getString("item_type");
                    int count = rs.getInt("count");
                    if (type.equalsIgnoreCase("Lost")) lost = count;
                    else if (type.equalsIgnoreCase("Found")) found = count;
                }
            }
            String claimSql = "SELECT COUNT(*) FROM claims WHERE status = 'Pending'";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(claimSql)) {
                if (rs.next()) pending = rs.getInt(1);
            }
            String resolvedSql = "SELECT COUNT(*) FROM items WHERE status IN ('Claimed', 'Returned')";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(resolvedSql)) {
                if (rs.next()) claimed = rs.getInt(1);
            }
            statsCardsPanel.add(createStatCard("FOUND ITEMS", found, new Color(16, 185, 129))); 
            statsCardsPanel.add(createStatCard("LOST REPORTS", lost, new Color(249, 115, 22))); 
            statsCardsPanel.add(createStatCard("RESOLVED", claimed, new Color(34, 197, 94)));
            statsCardsPanel.add(createStatCard("PENDING CLAIMS", pending, new Color(59, 130, 246)));
            statsCardsPanel.add(createStatCard("TOTAL RECORDS", (found + lost), PRIMARY));
        } catch (Exception e) { e.printStackTrace(); }
        statsCardsPanel.revalidate(); statsCardsPanel.repaint();
    }

    private JPanel createStatCard(String label, int val, Color c) {
        JPanel cd = new JPanel(new BorderLayout()); cd.setPreferredSize(new Dimension(280, 160));
        cd.setBackground(CARD_BG); cd.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderPainted: true; borderColor: " + (isDarkMode ? "#3c3c3c" : "#e0e3e5"));
        cd.setBorder(new EmptyBorder(25, 30, 25, 30));
        JLabel l = new JLabel(label); l.setFont(new Font("SansSerif", Font.BOLD, 12)); l.setForeground(OUTLINE);
        cd.add(l, BorderLayout.NORTH);
        JLabel v = new JLabel(String.valueOf(val)); v.setFont(new Font("SansSerif", Font.BOLD, 48)); v.setForeground(c);
        cd.add(v, BorderLayout.CENTER);
        return cd;
    }

    private DefaultTableModel claimsModel;
    private JTable claimsTable;
    private JPanel createAdminStatusPanel() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(SURFACE);
        mainContent.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Title
        JLabel title = new JLabel("Manage Claims");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainContent.add(title, BorderLayout.NORTH);

        // Table Container (The Box)
        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBackground(CARD_BG);
        tableBox.setBorder(new javax.swing.border.LineBorder(OUTLINE, 1));
        
        // Table Data
        String[] columns = {"ID", "Item", "Claimant", "Justification", "Status"};
        claimsModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        claimsTable = new JTable(claimsModel);
        styleTable(claimsTable, 4); // Status is at index 4
        
        // Hide ID column
        claimsTable.getColumnModel().getColumn(0).setMinWidth(0);
        claimsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        claimsTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(claimsTable);
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        tableBox.add(scrollPane, BorderLayout.CENTER);
        mainContent.add(tableBox, BorderLayout.CENTER);

        // Functional Buttons (Approve/Deny)
        JPanel bPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); 
        bPanel.setOpaque(false);
        bPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton approve = new JButton("APPROVE"); 
        approve.setBackground(SECONDARY); 
        approve.setForeground(Color.WHITE);
        approve.setFont(new Font("SansSerif", Font.BOLD, 14));
        approve.setPreferredSize(new Dimension(150, 40));
        approve.addActionListener(e -> handleClaimStatus("Approved"));
        
        JButton deny = new JButton("REJECT"); 
        deny.setBackground(new Color(186, 26, 26)); 
        deny.setForeground(Color.WHITE);
        deny.setFont(new Font("SansSerif", Font.BOLD, 14));
        deny.setPreferredSize(new Dimension(150, 40));
        deny.addActionListener(e -> handleClaimStatus("Rejected"));
        
        bPanel.add(deny); bPanel.add(approve); 
        mainContent.add(bPanel, BorderLayout.SOUTH);
        
        return mainContent;
    }

    private void refreshAdminClaimsTable() {
        refreshAdminClaimsTable(-1);
    }

    private void refreshAdminClaimsTable(int filterItemId) {
        if (claimsModel == null) return;
        claimsModel.setRowCount(0);
        
        // MODIFY THIS: Display ONLY items with claims using SQL JOIN
        String sql = "SELECT CONCAT('C:', c.claim_id), i.item_name, c.student_name, c.justification, c.status " +
                     "FROM claims c JOIN items i ON c.item_id = i.id ";
        
        if (filterItemId != -1) {
            sql += "WHERE c.item_id = " + filterItemId + " ";
        }
        
        sql += "ORDER BY c.claim_date DESC";

        try (Connection conn = DBConnection.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) claimsModel.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleClaimStatus(String status) {
        int row = claimsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a claim first.");
            return;
        }
        String rawId = (String) claimsModel.getValueAt(row, 0);
        if (!rawId.startsWith("C:")) return;
        int claimId = Integer.parseInt(rawId.substring(2));
        
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);
            try {
                // Update claims.status
                try (PreparedStatement ps = conn.prepareStatement("UPDATE claims SET status = ? WHERE claim_id = ?")) {
                    ps.setString(1, status);
                    ps.setInt(2, claimId);
                    ps.executeUpdate();
                }

                // If APPROVED: update items.status = 'Claimed'
                if (status.equalsIgnoreCase("Approved")) {
                    try (PreparedStatement ps2 = conn.prepareStatement("UPDATE items SET status = 'Claimed' WHERE id = (SELECT item_id FROM claims WHERE claim_id = ?)")) {
                        ps2.setInt(1, claimId);
                        ps2.executeUpdate();
                    }
                }
                
                conn.commit();
                JOptionPane.showMessageDialog(this, "Claim " + status + " successfully!");
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
            refreshAdminClaimsTable(); refreshDashboard(); refreshAdminStats();
        } catch (Exception ex) { 
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing claim: " + ex.getMessage());
        }
    }

    private DefaultTableModel invModel;
    private JTable invTable;
    private JPanel createAdminInventoryPanel() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(SURFACE);
        mainContent.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Title
        JLabel title = new JLabel("Inventory");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainContent.add(title, BorderLayout.NORTH);

        // Table Container (The Box)
        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBackground(CARD_BG);
        tableBox.setBorder(new javax.swing.border.LineBorder(OUTLINE, 1));
        
        // Table Data
        String[] columns = {"ID", "Items", "Type", "Date", "Status"};
        invModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        invTable = new JTable(invModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String status = (String) getValueAt(row, 4);
                    if ("Pending".equalsIgnoreCase(status)) {
                        c.setBackground(new Color(255, 255, 200)); // Light Yellow for Pending
                    } else {
                        c.setBackground(getBackground());
                    }
                }
                return c;
            }
        };
        styleTable(invTable, 4); 
        
        invTable.getColumnModel().getColumn(0).setMinWidth(0);
        invTable.getColumnModel().getColumn(0).setMaxWidth(0);
        invTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(invTable);
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        tableBox.add(scrollPane, BorderLayout.CENTER);
        mainContent.add(tableBox, BorderLayout.CENTER);

        JButton delBtn = new JButton("Delete Item");
        delBtn.setBackground(new Color(186, 26, 26));
        delBtn.setForeground(Color.WHITE);
        delBtn.setFocusPainted(false);
        delBtn.addActionListener(e -> {
            int row = invTable.getSelectedRow(); if (row == -1) return;
            int id = Integer.parseInt(invModel.getValueAt(row, 0).toString());
            if (JOptionPane.showConfirmDialog(this, "Permanently delete item?", "Warning", 0) == 0) {
                try (Connection conn = DBConnection.connect()) {
                    PreparedStatement ps1 = conn.prepareStatement("DELETE FROM claims WHERE item_id = ?"); ps1.setInt(1, id); ps1.executeUpdate();
                    PreparedStatement ps2 = conn.prepareStatement("DELETE FROM items WHERE id = ?"); ps2.setInt(1, id); ps2.executeUpdate();
                    refreshAdminInventory();
                    refreshAdminStats();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setOpaque(false);
        southPanel.add(delBtn);
        mainContent.add(southPanel, BorderLayout.SOUTH);

        return mainContent;
    }

    private void styleTable(JTable table, int statusCol) {
        table.setRowHeight(60); 
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_MAIN);

        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setBackground(CARD_BG);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setForeground(TEXT_MAIN);
        ((javax.swing.table.DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        if (statusCol != -1) {
            table.getColumnModel().getColumn(statusCol).setCellRenderer(new StatusBadgeRenderer());
        }
    }

    private void refreshAdminInventory() {
        if (invModel == null) return;
        invModel.setRowCount(0);
        try (Connection conn = DBConnection.connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, item_name, item_type, date_added, status FROM items")) {
            while (rs.next()) {
                invModel.addRow(new Object[]{
                    rs.getInt(1), 
                    rs.getString(2), 
                    rs.getString(3), 
                    rs.getString(4), 
                    rs.getString(5)
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    class StatusBadgeRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (value != null) ? value.toString() : "";
            JLabel label = new JLabel(status, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(new Font("SansSerif", Font.BOLD, 13));
            label.setPreferredSize(new Dimension(110, 32));
            
            // MODIFIED COLORS
            if ("Pending".equalsIgnoreCase(status)) {
                label.setBackground(new Color(255, 193, 7)); // Yellow
                label.setForeground(Color.BLACK);
            } else if ("Approved".equalsIgnoreCase(status) || "Claimed".equalsIgnoreCase(status) || "Returned".equalsIgnoreCase(status) || "Found".equalsIgnoreCase(status)) {
                label.setBackground(new Color(40, 167, 69)); // Green
                label.setForeground(Color.WHITE);
            } else if ("Rejected".equalsIgnoreCase(status) || "Denied".equalsIgnoreCase(status) || "Lost".equalsIgnoreCase(status)) {
                label.setBackground(new Color(220, 53, 69)); // Red
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(UIManager.getColor("Table.background"));
                label.setForeground(UIManager.getColor("Label.disabledForeground"));
            }
            
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            
            // HIGHLIGHT NEW CLAIMS (ADMIN) - Highlight rows where status = 'Pending'
            if (!isSelected && "Pending".equalsIgnoreCase(status)) {
                wrapper.setBackground(new Color(255, 255, 225)); // Light Yellow
            }

            wrapper.add(label);
            return wrapper;
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new UMAKSystemMain().setVisible(true)); }
}

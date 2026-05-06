import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.io.File;

/**
 * Dashboard panel displaying lost and found items in a grid layout.
 * Allows users to browse, search, and filter items by category.
 *
 * @author UMAK Lost & Found Team
 */
public class UMAKDashboard extends JPanel {
    private JPanel grid;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private Color surfaceColor;
    private Color cardColor;

    public UMAKDashboard(Color surfaceColor, Color cardColor) {
        this.surfaceColor = surfaceColor;
        this.cardColor = cardColor;

        setBackground(surfaceColor);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // --- TITLE ---
        JLabel pageTitle = new JLabel("Browse Lost & Found Items");
        pageTitle.setFont(new Font("Inter", Font.BOLD, 32));
        pageTitle.setForeground(UIManager.getColor("Label.foreground"));

        // --- TOP BAR ---
        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);

        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search Items...");
        searchField.setPreferredSize(new Dimension(400, 40));
        searchField.addActionListener(e -> refreshData());
        gbc.weightx = 1.0; gbc.gridx = 0; topBar.add(searchField, gbc);

        categoryCombo = new JComboBox<>(new String[]{"All Categories", "ID Card", "Electronics", "Wallet", "Documents", "Keys", "Bags", "Other"});
        categoryCombo.setPreferredSize(new Dimension(160, 40));
        categoryCombo.addActionListener(e -> refreshData());
        gbc.weightx = 0; gbc.gridx = 1; topBar.add(categoryCombo, gbc);

        JButton filterBtn = new JButton("Refresh");
        filterBtn.setPreferredSize(new Dimension(100, 40));
        filterBtn.addActionListener(e -> refreshData());
        gbc.gridx = 2; topBar.add(filterBtn, gbc);

        JPanel headerContainer = new JPanel(new BorderLayout(0, 20));
        headerContainer.setOpaque(false);
        headerContainer.add(pageTitle, BorderLayout.NORTH);
        headerContainer.add(topBar, BorderLayout.CENTER);
        add(headerContainer, BorderLayout.NORTH);

        // --- ITEM GRID ---
        grid = new JPanel(new GridLayout(0, 3, 25, 25));
        grid.setOpaque(false);

        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setOpaque(false);
        gridContainer.add(grid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(gridContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        grid.removeAll();
        String search = searchField.getText();
        String cat = categoryCombo.getSelectedItem().toString();

        String sql = "SELECT * FROM items WHERE status NOT IN ('Returned', 'Denied')";
        if (!search.isEmpty()) sql += " AND (item_name LIKE ? OR description LIKE ?)";
        if (!cat.equals("All Categories")) sql += " AND category = ?";
        sql += " ORDER BY date_added DESC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (!search.isEmpty()) {
                ps.setString(idx++, "%" + search + "%");
                ps.setString(idx++, "%" + search + "%");
            }
            if (!cat.equals("All Categories")) ps.setString(idx++, cat);

            boolean hasItems = false;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    grid.add(createItemCard(rs));
                    hasItems = true;
                }
            }
            if (!hasItems) {
                JLabel emptyLabel = new JLabel("No items found. Try adjusting the filter or add a new report.", SwingConstants.CENTER);
                emptyLabel.setFont(new Font("Inter", Font.ITALIC, 16));
                emptyLabel.setForeground(new Color(120, 120, 120));
                grid.add(emptyLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        grid.revalidate();
        grid.repaint();
    }

    private JPanel createItemCard(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("item_name");
        String time = rs.getString("date_added");
        String loc = rs.getString("location");
        String path = rs.getString("image_path");
        String category = rs.getString("category");
        String description = rs.getString("description");
        String status = rs.getString("status");
        String itemType = rs.getString("item_type");
        int reporterId = rs.getInt("reporter_id");

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(cardColor);
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderPainted: true; borderColor: #e2e8f0");

        // Add subtle drop shadow for depth
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1),
            BorderFactory.createEmptyBorder(2, 2, 4, 4)
        ));

        // Image Section with proper scaling
        JLabel picLabel = new JLabel("", SwingConstants.CENTER);
        picLabel.setPreferredSize(new Dimension(200, 180));
        picLabel.setOpaque(true);
        picLabel.setBackground(cardColor);

        File imgFile = DBConnection.resolveImagePath(path);
        if (imgFile != null && imgFile.exists()) {
            ImageIcon originalIcon = new ImageIcon(imgFile.getAbsolutePath());
            Image originalImage = originalIcon.getImage();
            int maxW = 200, maxH = 180;
            int w = originalImage.getWidth(null);
            int h = originalImage.getHeight(null);
            if (w > 0 && h > 0) {
                double ratio = Math.min((double) maxW / w, (double) maxH / h);
                int newW = (int) (w * ratio);
                int newH = (int) (h * ratio);
                picLabel.setIcon(new ImageIcon(originalImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH)));
            } else {
                picLabel.setText("No Image");
                picLabel.setForeground(Color.LIGHT_GRAY);
            }
        } else {
            picLabel.setText("No Image");
            picLabel.setForeground(Color.LIGHT_GRAY);
        }

        // Type Tag Overlay
        JLabel typeTag = new JLabel(itemType.toUpperCase(), SwingConstants.CENTER);
        typeTag.setOpaque(true);
        typeTag.setFont(new Font("Inter", Font.BOLD, 10));
        typeTag.setForeground(Color.WHITE);
        typeTag.setBackground(itemType.equalsIgnoreCase("Found") ? new Color(16, 185, 129) : new Color(249, 115, 22));
        typeTag.setPreferredSize(new Dimension(65, 22));

        JPanel overlay = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        overlay.setOpaque(false);
        overlay.add(typeTag);
        picLabel.setLayout(new BorderLayout());
        picLabel.add(overlay, BorderLayout.NORTH);

        // Info Section
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Inter", Font.BOLD, 16));

        JLabel metaLbl = new JLabel(category + " • " + loc);
        metaLbl.setFont(new Font("Inter", Font.PLAIN, 12));
        metaLbl.setForeground(Color.GRAY);

        JLabel statusLbl = new JLabel("Status: " + status);
        statusLbl.setFont(new Font("Inter", Font.BOLD, 12));
        statusLbl.setForeground(new Color(59, 130, 246));

        info.add(titleLbl);
        info.add(Box.createVerticalStrut(5));
        info.add(metaLbl);
        info.add(Box.createVerticalStrut(5));
        info.add(statusLbl);

        card.add(picLabel, BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new ItemDetailModal((JFrame) SwingUtilities.getWindowAncestor(card), id, title, category, time, loc, description, path, reporterId, status, itemType, surfaceColor, cardColor, UMAKDashboard.this).setVisible(true);
            }
            public void mouseEntered(MouseEvent e) {
                card.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderPainted: true; borderColor: #3b82f6");
            }
            public void mouseExited(MouseEvent e) {
                card.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderPainted: true; borderColor: #e2e8f0");
            }
        });

        return card;
    }
}

class ItemDetailModal extends JDialog {
    private UMAKDashboard dashboard;

    public ItemDetailModal(JFrame parent, int itemId, String title, String category, String time, String loc, String description, String path, int reporterId, String status, String itemType, Color surfaceColor, Color cardColor, UMAKDashboard dashboard) {
        super(parent, "Item Details", true);
        this.dashboard = dashboard;
        setSize(500, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBackground(cardColor);
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- TOP (Status Tag) ---
        JLabel typeTag = new JLabel(itemType, SwingConstants.CENTER);
        typeTag.setOpaque(true);
        if (itemType.equalsIgnoreCase("Found")) {
            typeTag.setBackground(new Color(0, 150, 136));
        } else {
            typeTag.setBackground(new Color(240, 128, 128));
        }
        typeTag.setForeground(Color.WHITE);
        typeTag.setPreferredSize(new Dimension(80, 25));
        typeTag.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tagPanel.setOpaque(false);
        tagPanel.add(typeTag);
        container.add(tagPanel, BorderLayout.NORTH);

        // --- IMAGE ---
        JLabel bigPic = new JLabel("", SwingConstants.CENTER);
        bigPic.setPreferredSize(new Dimension(400, 250));
        bigPic.setOpaque(true);
        bigPic.setBackground(surfaceColor);
        bigPic.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

        File imgFile = DBConnection.resolveImagePath(path);
        if (imgFile != null && imgFile.exists()) {
            bigPic.setIcon(new ImageIcon(new ImageIcon(imgFile.getAbsolutePath()).getImage().getScaledInstance(450, 250, Image.SCALE_SMOOTH)));
        } else {
            bigPic.setText("PIC");
            bigPic.setForeground(UIManager.getColor("Label.disabledForeground"));
            bigPic.setFont(new Font("SansSerif", Font.BOLD, 80));
        }
        container.add(bigPic, BorderLayout.CENTER);

        // --- DETAILS ---
        JPanel details = new JPanel(new GridLayout(5, 2, 5, 5));
        details.setOpaque(false);

        String reporterName = "Unknown";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT full_name FROM users WHERE id = ?")) {
            ps.setInt(1, reporterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) reporterName = rs.getString("full_name");
            }
        } catch (Exception e) {}

        details.add(new JLabel("Category") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(category));
        details.add(new JLabel("Posted By") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(reporterName));
        details.add(new JLabel("Date Added") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(time));
        details.add(new JLabel("Location") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(loc));
        details.add(new JLabel("Status") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(status));

        // Description & Claim Box
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);

        JLabel descHeader = new JLabel("Description");
        descHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel descBody = new JLabel("<html>" + description + "</html>");

        JPanel claimBox = new JPanel(new BorderLayout(10, 10));
        claimBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")), "Claim this Item?"));
        ((javax.swing.border.TitledBorder)claimBox.getBorder()).setTitleColor(UIManager.getColor("Label.foreground"));
        claimBox.setOpaque(false);

        JLabel claimInstruction = new JLabel("<html><small>If you believe this item is yours, file a claim and our admin will verify it.</small></html>");
        JButton claimBtn = new JButton("File a claim");
        claimBtn.setBackground(new Color(16, 185, 129));
        claimBtn.setForeground(Color.WHITE);

        // CHECK IF ALREADY CLAIMED OR IF ITEM IS CLAIMED
        boolean alreadyClaimed = false;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT status FROM claims WHERE item_id = ? AND user_id = ?")) {
            ps.setInt(1, itemId);
            ps.setInt(2, UserSession.userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) alreadyClaimed = true;
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        if (alreadyClaimed || "Claimed".equalsIgnoreCase(status) || "Returned".equalsIgnoreCase(status)) {
            claimBtn.setEnabled(false);
            claimBtn.setText("Already Claimed");
            claimBtn.setBackground(Color.GRAY);
        }

        claimBtn.addActionListener(e -> {
            // Re-check just in case
            try (Connection conn = DBConnection.connect();
                 PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM claims WHERE item_id = ? AND user_id = ?")) {
                ps.setInt(1, itemId);
                ps.setInt(2, UserSession.userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this, "You have already filed a claim for this item.", "Duplicate Claim", JOptionPane.WARNING_MESSAGE);
                        claimBtn.setEnabled(false);
                        claimBtn.setText("Already Claimed");
                        return;
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); }

            String justification = JOptionPane.showInputDialog(this, "Prove ownership by describing the item:");
            if (justification != null && !justification.trim().isEmpty()) {
                try (Connection conn = DBConnection.connect();
                     PreparedStatement ps = conn.prepareStatement("INSERT INTO claims (item_id, user_id, student_name, student_email, justification, status, claim_date) VALUES (?,?,?,?,?,?,NOW())")) {
                    ps.setInt(1, itemId);
                    ps.setInt(2, UserSession.userId);
                    ps.setString(3, UserSession.fullName);
                    ps.setString(4, UserSession.email);
                    ps.setString(5, justification);
                    ps.setString(6, "Pending");
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Claim submitted successfully");
                    claimBtn.setEnabled(false);
                    claimBtn.setText("Already Claimed");
                    if (dashboard != null) dashboard.refreshData();
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Claim Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        claimBox.add(claimInstruction, BorderLayout.CENTER);
        claimBox.add(claimBtn, BorderLayout.SOUTH);

        bottom.add(descHeader); bottom.add(descBody);
        bottom.add(Box.createVerticalStrut(20));

        // Owner/Admin Actions
        if ("Admin".equalsIgnoreCase(UserSession.role) || UserSession.userId == reporterId) {
            JPanel actionPanel = new JPanel(new GridLayout(1, 0, 10, 0));
            actionPanel.setOpaque(false);

            // For Admins, if there are claims, show MANAGE button
            if ("Admin".equalsIgnoreCase(UserSession.role)) {
                boolean hasClaims = false;
                try (Connection conn = DBConnection.connect();
                     PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM claims WHERE item_id = ?")) {
                    ps.setInt(1, itemId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) hasClaims = true;
                } catch (Exception e) {}

                if (hasClaims) {
                    JButton manageBtn = new JButton("MANAGE");
                    manageBtn.setBackground(new Color(13, 27, 42));
                    manageBtn.setForeground(Color.WHITE);
                    manageBtn.addActionListener(e -> {
                        dispose();
                        if (parent instanceof UMAKSystemMain) {
                            ((UMAKSystemMain) parent).navigateToClaims(itemId);
                        }
                    });
                    actionPanel.add(manageBtn);
                }
            }

            JButton editBtn = new JButton("Edit Item");
            editBtn.setBackground(new Color(59, 130, 246));
            editBtn.setForeground(Color.WHITE);
            editBtn.addActionListener(e -> {
                dispose();
                new EditItemModal(parent, itemId, title, category, loc, description, status, itemType, surfaceColor, cardColor, dashboard).setVisible(true);
            });

            JButton deleteBtn = new JButton("Delete Item");
            deleteBtn.setBackground(new Color(239, 68, 68));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
                    try (Connection conn = DBConnection.connect()) {
                        if (conn == null) {
                            JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        conn.setAutoCommit(false);
                        try (PreparedStatement psClaims = conn.prepareStatement("DELETE FROM claims WHERE item_id = ?");
                             PreparedStatement psItem = conn.prepareStatement("DELETE FROM items WHERE id = ?")) {
                            psClaims.setInt(1, itemId);
                            psClaims.executeUpdate();

                            psItem.setInt(1, itemId);
                            psItem.executeUpdate();

                            conn.commit();
                            JOptionPane.showMessageDialog(this, "Item Deleted!");
                            dashboard.refreshData(); dispose();
                        } catch (Exception ex) {
                            conn.rollback();
                            throw ex;
                        } finally {
                            conn.setAutoCommit(true);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            });

            actionPanel.add(editBtn);
            actionPanel.add(deleteBtn);
            bottom.add(actionPanel);
        } else {
            bottom.add(claimBox);
        }

        JPanel infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setOpaque(false);
        infoWrapper.add(details, BorderLayout.NORTH);
        infoWrapper.add(bottom, BorderLayout.CENTER);

        container.add(infoWrapper, BorderLayout.SOUTH);
        add(new JScrollPane(container) {{ setBorder(null); }});
    }
}

class EditItemModal extends JDialog {
    public EditItemModal(JFrame parent, int itemId, String title, String category, String loc, String description, String status, String itemType, Color surfaceColor, Color cardColor, UMAKDashboard dashboard) {
        super(parent, "Edit Item", true);
        setSize(450, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(cardColor);
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(0, 0, 15, 0);

        // Modernized Edit Fields
        JTextField titleF = new JTextField(title);
        titleF.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        JComboBox<String> catC = new JComboBox<>(new String[]{"ID Card", "Electronics", "Wallet", "Documents", "Keys", "Bags", "Other"});
        catC.setSelectedItem(category);
        catC.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        JTextField locF = new JTextField(loc);
        locF.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        JComboBox<String> statC = new JComboBox<>(new String[]{"Pending", "Approved", "Claimed", "Returned", "Denied"});
        statC.setSelectedItem(status);
        statC.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        JTextArea descA = new JTextArea(description, 5, 20);
        descA.setLineWrap(true); descA.setWrapStyleWord(true);
        descA.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        int row = 0;
        addEditField(p, "Item Title", titleF, c, row++);
        addEditField(p, "Category", catC, c, row++);
        addEditField(p, "Location", locF, c, row++);
        addEditField(p, "Status", statC, c, row++);
        addEditField(p, "Description", new JScrollPane(descA), c, row++);

        JButton save = new JButton("Save Changes");
        save.setBackground(new Color(59, 130, 246));
        save.setForeground(Color.WHITE);
        save.setFont(new Font("Inter", Font.BOLD, 14));
        save.setPreferredSize(new Dimension(0, 45));
        save.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        save.addActionListener(e -> {
            if (titleF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title Required!"); return;
            }
            try (Connection conn = DBConnection.connect();
                 PreparedStatement ps = conn.prepareStatement("UPDATE items SET item_name=?, category=?, location=?, description=?, status=? WHERE id=?")) {
                ps.setString(1, titleF.getText()); ps.setString(2, catC.getSelectedItem().toString());
                ps.setString(3, locF.getText()); ps.setString(4, descA.getText());
                ps.setString(5, statC.getSelectedItem().toString()); ps.setInt(6, itemId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Item Updated Successfully!");
                dashboard.refreshData(); dispose();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        c.gridy = row * 2; c.insets = new Insets(10, 0, 0, 0); p.add(save, c);
        add(new JScrollPane(p) {{ setBorder(null); }});
    }

    private void addEditField(JPanel p, String label, JComponent f, GridBagConstraints c, int row) {
        c.gridy = row * 2;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Inter", Font.BOLD, 12));
        l.setForeground(Color.GRAY);
        p.add(l, c);

        c.gridy = row * 2 + 1;
        p.add(f, c);
    }
}

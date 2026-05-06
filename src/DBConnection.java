import java.sql.*;

/**
 * Database connection manager for the UMAK Lost & Found system.
 * Handles MySQL connections and automatic schema migrations.
 *
 * @author UMAK Lost & Found Team
 */
public class DBConnection {
    public static Connection connect() {
        try {
            // Standard XAMPP connection string
            Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/UMAK_LostFound", "root", "");
            
            // Ensure schema is up to date
            try (Statement stmt = conn.createStatement()) {
                // Check if department column exists
                ResultSet rs = conn.getMetaData().getColumns(null, null, "users", "department");
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE users ADD COLUMN department VARCHAR(255) AFTER password");
                }
                
                // Check if is_online column exists
                rs = conn.getMetaData().getColumns(null, null, "users", "is_online");
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE users ADD COLUMN is_online BOOLEAN DEFAULT 0");
                }
                
                // Check if last_seen column exists
                rs = conn.getMetaData().getColumns(null, null, "users", "last_seen");
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE users ADD COLUMN last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
                }

                // --- ITEMS TABLE MIGRATION ---
                // Check if item_type exists
                rs = conn.getMetaData().getColumns(null, null, "items", "item_type");
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE items ADD COLUMN item_type ENUM('Lost', 'Found') AFTER reporter_id");
                    // Migrate existing status data to item_type
                    stmt.execute("UPDATE items SET item_type = 'Lost' WHERE status = 'Lost'");
                    stmt.execute("UPDATE items SET item_type = 'Found' WHERE status = 'Found'");
                    stmt.execute("UPDATE items SET item_type = 'Found' WHERE status = 'Claimed'"); // Assumption
                }

                // Change status to lifecycle status
                // We'll use VARCHAR(50) for flexibility or update the ENUM
                stmt.execute("ALTER TABLE items MODIFY COLUMN status VARCHAR(50) DEFAULT 'Pending'");
                // Set default status for existing items if they were just Lost/Found
                stmt.execute("UPDATE items SET status = 'Pending' WHERE status IN ('Lost', 'Found')");
                
                // Check if date_lost exists
                rs = conn.getMetaData().getColumns(null, null, "items", "date_lost");
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE items ADD COLUMN date_lost DATE AFTER reporter_id");
                }
                
                // Check if time_lost exists
                rs = conn.getMetaData().getColumns(null, null, "items", "time_lost");
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE items ADD COLUMN time_lost TIME AFTER date_lost");
                }
                
                // --- CLAIMS TABLE MIGRATION ---
                rs = conn.getMetaData().getTables(null, null, "claims", null);
                if (!rs.next()) {
                    stmt.execute("CREATE TABLE claims (" +
                                "claim_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                "item_id INT, " +
                                "user_id INT, " +
                                "student_name VARCHAR(100), " +
                                "student_email VARCHAR(100), " +
                                "claim_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "status VARCHAR(20) DEFAULT 'Pending', " +
                                "justification TEXT, " +
                                "FOREIGN KEY (item_id) REFERENCES items(id), " +
                                "FOREIGN KEY (user_id) REFERENCES users(id))");
                } else {
                    // 1. Check for user_id, if not found, check if claimant_id exists to rename it
                    rs = conn.getMetaData().getColumns(null, null, "claims", "user_id");
                    if (!rs.next()) {
                        rs = conn.getMetaData().getColumns(null, null, "claims", "claimant_id");
                        if (rs.next()) {
                            stmt.execute("ALTER TABLE claims CHANGE COLUMN claimant_id user_id INT");
                        } else {
                            stmt.execute("ALTER TABLE claims ADD COLUMN user_id INT AFTER item_id");
                        }
                    }

                    // 2. Check if student_name exists
                    rs = conn.getMetaData().getColumns(null, null, "claims", "student_name");
                    if (!rs.next()) {
                        stmt.execute("ALTER TABLE claims ADD COLUMN student_name VARCHAR(100) AFTER user_id");
                    }

                    // 3. Check if student_email exists
                    rs = conn.getMetaData().getColumns(null, null, "claims", "student_email");
                    if (!rs.next()) {
                        stmt.execute("ALTER TABLE claims ADD COLUMN student_email VARCHAR(100) AFTER student_name");
                    }

                    // 4. Check if justification exists
                    rs = conn.getMetaData().getColumns(null, null, "claims", "justification");
                    if (!rs.next()) {
                        stmt.execute("ALTER TABLE claims ADD COLUMN justification TEXT AFTER status");
                    }

                    // 5. Check if claim_date exists
                    rs = conn.getMetaData().getColumns(null, null, "claims", "claim_date");
                    if (!rs.next()) {
                        stmt.execute("ALTER TABLE claims ADD COLUMN claim_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER student_email");
                    }

                    // 6. Ensure PK is claim_id if it was 'id'
                    rs = conn.getMetaData().getColumns(null, null, "claims", "claim_id");
                    if (!rs.next()) {
                        rs = conn.getMetaData().getColumns(null, null, "claims", "id");
                        if (rs.next()) {
                            stmt.execute("ALTER TABLE claims CHANGE COLUMN id claim_id INT AUTO_INCREMENT");
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("Schema update notice: " + e.getMessage());
            }
            
            return conn;
        } catch (Exception e) {
            System.out.println("XAMPP Connection Error: " + e.getMessage());
            return null;
        }
    }

    public static java.io.File resolveImagePath(String path) {
        if (path == null || path.isEmpty()) return null;
        java.io.File f = new java.io.File(path);
        if (f.exists()) return f;
        java.io.File[] checks = { 
            new java.io.File("website/" + path), 
            new java.io.File("C:/xampp/htdocs/umak/" + path), 
            new java.io.File("uploads/" + f.getName()) 
        };
        for (java.io.File c : checks) if (c.exists()) return c;
        return null;
    }
}
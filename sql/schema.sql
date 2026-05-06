CREATE DATABASE IF NOT EXISTS UMAK_LostFound;
USE UMAK_LostFound;

-- Table for users
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    full_name VARCHAR(100),
    password VARCHAR(255),
    department VARCHAR(255),
    role ENUM('Student', 'Admin') DEFAULT 'Student',
    is_online BOOLEAN DEFAULT 0,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table for items
CREATE TABLE IF NOT EXISTS items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(100),
    category VARCHAR(50),
    description TEXT,
    location VARCHAR(100),
    item_type ENUM('Lost', 'Found') DEFAULT 'Found',
    status VARCHAR(50) DEFAULT 'Pending',
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    image_path VARCHAR(255),
    reporter_id INT,
    date_lost DATE,
    time_lost TIME
);

-- Table for claims
CREATE TABLE IF NOT EXISTS claims (
    claim_id INT PRIMARY KEY AUTO_INCREMENT,
    item_id INT,
    user_id INT,
    student_name VARCHAR(100),
    student_email VARCHAR(100),
    justification TEXT,
    status VARCHAR(50) DEFAULT 'Pending',
    claim_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Insert Default Admin
INSERT IGNORE INTO users (student_no, full_name, password, role) 
VALUES ('admin', 'Admin User', 'admin123', 'Admin');

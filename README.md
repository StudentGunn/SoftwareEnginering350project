# SoftwareEnginering350project - Food Dash
**Version:** 1.1

# Description
**Goal:** To create a Food Delivery App called Food Dash that makes it easier to deliver food to people.

Food Dash is a food delivery management system that connects customers, drivers, and administrators through an intuitive desktop application. The system handles user authentication, order management, payment processing, and delivery tracking basically.

# Features
- **Multi-User System**: Support for Customers, Drivers, and Administrators
- **Secure Authentication**: SHA-256 password hashing and admin verification codes
- **Database Management**: SQLite-based data persistence for users, orders, drivers, and payments
- **Order Tracking**: Real-time order status updates and delivery management
- **Payment Processing**: Support for card and bank payment methods
- **Admin Dashboard**: User management and system administration tools
- **Driver Interface**: Order acceptance, delivery tracking, and payment method setup
- **Custom UI**: Background image support and intuitive graphical interface *will change in future updates*

# Libraries and Dependencies 

# Core Java Libraries
- **Swing (`javax.swing`)** - Main GUI framework for desktop application
- **AWT (`java.awt.`)** - Graphics and layout management
  - (`java.awt.event`) - Event handling for user interactions, 
- **SQL (`java.sql`)** - Database connectivity and operations,
  - `java.sql.Connection`
  - `java.sql.DriverManager`
  - `java.sql.PreparedStatement`
  - `java.sql.ResultSet`
  - `java.sql.SQLException`
  - `java.sql.Statement`

# Standard Java APIs
- **I/O (`java.io.`)** - File operations and image loading
  - `java.io.IOException`
- **NIO (`java.nio.*`)** - Modern file path handling
  - `java.nio.file.Path`
  - `java.nio.charset.StandardCharsets`
- **Collections (`java.util.*`)** - Data structures
  - `java.util.ArrayList`
- **Security (`java.security.*`)** - Password hashing
  - `java.security.MessageDigest`
  - `java.security.NoSuchAlgorithmException`
- **Time (`java.time.*`)** - Date and time handling
  - `java.time.Instant`
  - `java.time.LocalDateTime`
- **Text (`java.text.*`)** - Formatting and parsing
  - `java.text.DecimalFormat`
  - `java.text.ParseException`

# Swing Components, (Used so far)
- **Tables (`javax.swing.table.*`)** - Data display in tabular format
  - `javax.swing.table.DefaultTableModel`
  - `javax.swing.table.TableCellRenderer`
- **Text (`javax.swing.text.*`)** - Advanced text input components
  - `javax.swing.text.MaskFormatter`
- **Image I/O (`javax.imageio.*`)** - Image loading and processing
  - `javax.imageio.ImageIO`

# External Dependencies
- **SQLite JDBC Driver** (`sqlite-jdbc-3.42.0.0.jar`)
  - Version: 3.42.0.0
  - Purpose: For Database connectivity for SQLite databases
  - Location: `/lib/sqlite-jdbc-3.42.0.0.jar`

# System Requirements

# Software Requirements
- **Java Development Kit (JDK)** - Version 8 or higher (Latest Recommneded) 
- **Windows 11** (Latest version recommended)
- **IDE** - Any Java-compatible IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)

# Database Requirements 
(Need SQL Eexntension )
- **SQLite** - Embedded database (no separate installation required)
- **SQLite JDBC Driver** - Included in project (`lib/sqlite-jdbc-3.42.0.0.jar`)

# Installation and Setup

# Remove UNesary files
1. Remove Phase 4 from file directory as it is not needed, *Duplicate Files*

1. **Clone the Repository**, ("If using github Desktop or Website")
   ```bash
   git clone https://github.com/[username]/SoftwareEnginering350project.git
   cd SoftwareEnginering350project
   ```

2. **Verify Dependencies**
   - Ensure the SQLite JDBC driver is present in the `lib/` directory
   - Check that `sqlite-jdbc-3.42.0.0.jar` exists

3. **Compile the Project**
   ```bash
   javac -cp "lib/*" *.java
   ```

4. **Run the Application**
   ```bash
   java -cp ".;lib/*" MainApp
   ```

# How to Run

1. **Navigate to the project directory**
2. **Open your IDE** and load the project
3. **Run the main class**: Execute `MainApp.java`
4. **Alternative**: Use command line compilation and execution as shown above

# Default Admin Account 
- **Username:** `FoodDashAdmin`
- **Password:** `admin123`
- **Admin Hash Code:** `ADMIN2024`

# Project Structure _ AS of "Version 1.1"
```
SoftwareEnginering350project/
├── lib/
│   └── sqlite-jdbc-3.42.0.0.jar
├── *.java                     # Main application files
├── *.db                       # SQLite database files 
├── MANIFEST.MF                # JAR manifest file
└── README.md                  # This file
```

# Troubleshooting

# Common Issues 

1. **SQL Exceptions / File Path Issues** (Will often see this issue due to cloning github sadly, edit json to configure)
   - **Cause**: Incorrect classpath or missing SQLite JDBC driver
   - **Solution**: Ensure `sqlite-jdbc-3.42.0.0.jar` is in the `lib/` directory and included in classpath

2. **Database Initialization Errors** (very unlikely to see this issue unless, you deltete the db files)
   - **Cause**: Database files may have outdated schema
   - **Solution**: Delete existing `.db` files to trigger fresh database creation

3. **Image Loading Errors** ('Ignore Image is not public info; will see this issue on start up)
   - **Cause**: Background image path not found
   - **Solution**: Update the image path in `MainApp.java` or remove/comment the background image code

4. **Compilation Errors** (Unless Java files missing, wont see this issue)
   - **Cause**: Missing dependencies or incorrect classpath
   - **Solution**: Verify all `.java` files are present and SQLite JDBC is in classpath

# JSON Configuration Issues**
If you encounter issues with IDE configuration, ensure your IDE's project settings include:
- Source path pointing to the project root
- Classpath including `lib/sqlite-jdbc-3.42.0.0.jar`
- Main class set to `MainApp`

# Future Updates
Version 1.1 includes core functionality. Future updates will focus on:
- Quality of life improvements
- Enhanced user experience
- Additional features based on user feedback * Not implmented yet*

## Authors
- Alex
- Mark
- Sky
- Josiah
- Anthony











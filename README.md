# SoftwareEnginering350project - Food Dash
**Version:** 1.1

# Description
**Goal:** To create a Food Delivery App called Food Dash that makes it easier to deliver food to people.

Food Dash is a food delivery management system that connects customers, drivers, and administrators through an intuitive desktop application. The system handles user authentication, order management, payment processing, and delivery tracking.

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

1. **Clone the Repository**
   ```bash
   git clone https://github.com/[username]/SoftwareEnginering350project.git
   cd SoftwareEnginering350project
   ```

2. **Verify Dependencies**
   - Ensure the SQLite JDBC driver is present: `sqlite-jdbc-3.42.0.0.jar`
   - Check that database files exist: `users.db`, `orders.db`, `drivers.db`, `payments.db`

3. **Open in Your IDE**
   - Open the project folder in VS Code, IntelliJ IDEA, Eclipse, or your preferred Java IDE
   - The IDE will automatically detect the Java files

4. **Run the Application**
   - Locate `MainApp.java` in the project root
   - Click "Run" or press F5 in your IDE
   - The application window will appear

# How to Run

**Using Your IDE (Recommended):**
1. Open the project in your IDE (VS Code, IntelliJ, Eclipse)
2. Open `MainApp.java`
3. Click the "Run" button or use the Run/Debug configuration
4. The IDE handles compilation and execution automatically

# Default Admin Account 
- **Username:** `FoodDashAdmin`
- **Password:** `admin123`
- **Admin Hash Code:** `ADMIN2024`

# Project Structure

```
SoftwareEnginering350project/
│
├── *.java                          # All Java source files in root directory
│   ├── MainApp.java               # Application entry point
│   ├── UserDataBase.java          # Database management
│   ├── OrderDatabase.java
│   ├── PaymentDatabase.java
│   ├── DriverDatabase.java
│   ├── OrderingSystem.java
│   ├── User.java                  # Model classes
│   ├── Customer.java
│   ├── Driver.java
│   ├── Admin.java
│   ├── Store.java
│   ├── Orders.java
│   ├── Item.java
│   ├── Address.java
│   ├── PaymentInformation.java
│   ├── ETA.java
│   ├── FoodDeliveryLoginUI.java   # UI components
│   ├── LoginUI.java
│   ├── MainScreen.java
│   ├── AdminScreen.java
│   ├── ResturantScreen.java
│   ├── DriverScreen.java
│   ├── AddressScreen.java
│   ├── DriverGetOrder.java
│   ├── DriverPaymentHistory.java
│   ├── DriverSetPaymentMethod.java
│   ├── DriveryHistory.java
│   ├── CustomerOrderHistory.java
│   ├── SceneSorter.java
│   ├── Logger.java                # Utility classes
│   ├── MapCalculator.java
│   └── ... (other utility files)
│
├── *.db                            # SQLite database files
│   ├── users.db
│   ├── orders.db
│   ├── drivers.db
│   └── payments.db
│
├── *.log                           # Log files
│   ├── errors.log
│   ├── bugs.log
│   └── UITesting.log
│
├── lib/                            # External libraries (optional folder)
│   └── sqlite-jdbc-3.42.0.0.jar   # (or in root if not in lib/)
│
├── *.class                         # Compiled files (generated by IDE)
├── MANIFEST.MF                     # JAR manifest file
├── README.md                       # This file
└── .gitignore                      # Git ignore rules
```

## File Organization

All Java source files, database files, and the SQLite JAR are in the project root directory. Your IDE will automatically handle compilation and place `.class` files alongside the source files.

# Troubleshooting

## Common Issues 

1. **Compilation Errors**
   - **Cause**: Missing package imports or incorrect classpath
   - **Solution**: Use `.\compile.ps1` script which handles all classpath configuration automatically
   
2. **Database Path Issues**
   - **Cause**: Database files not found in expected location
   - **Solution**: Ensure database files are in the `data/` directory. The application looks for:
     - `data/users.db`
     - `data/orders.db`
     - `data/drivers.db`
     - `data/payments.db`

3. **SQLite JDBC Driver Not Found**
   - **Cause**: Missing or incorrectly located SQLite JDBC driver
   - **Solution**: Verify `lib/sqlite-jdbc-3.42.0.0.jar` exists and is included in classpath

4. **Class Not Found Errors**
   - **Cause**: Project not compiled or bin directory missing
   - **Solution**: Run `.\compile.ps1` to compile all source files to `bin/` directory

5. **Image Loading Errors** 
   - **Cause**: Background image path not found (non-critical)
   - **Solution**: This is a cosmetic issue and won't prevent the app from running. The image path can be updated in the UI classes if needed.

## IDE Configuration

**VS Code:**
1. Install the Java Extension Pack (if not already installed)
2. Open the project folder
3. VS Code will automatically detect all `.java` files
4. The SQLite JAR will be added to the classpath automatically
5. To run: Open `MainApp.java` and click "Run" above the main method

**IntelliJ IDEA:**
1. Open the project folder (File → Open)
2. IntelliJ will auto-detect the Java files
3. Add `sqlite-jdbc-3.42.0.0.jar` to libraries if needed (usually automatic)
4. To run: Right-click `MainApp.java` → Run 'MainApp.main()'

**Eclipse:**
1. Import project (File → Import → Existing Projects into Workspace)
2. Eclipse will add all `.java` files to the build path
3. Verify `sqlite-jdbc-3.42.0.0.jar` is in the build path
4. To run: Right-click `MainApp.java` → Run As → Java Application

# Future Updates
Version 1.1 includes core functionality. Future updates will focus on:
- Quality of life improvements
- Enhanced user experience
- Additional features based on user feedback

# Content Added Since Version 1.0
- Added back buttons for logout functionality
- Added Customer Order History button
- Added solid color UI for better readability
- Added "Welcome" differentiation between screens
- Added Confirm Food Drop-off button on Driver side
- Added Delivery History
- Added Collect Payment option (Driver Side)
- Improved Javadoc documentation across all classes
- Enhanced error logging and bug tracking




## Authors
- Alex
- Mark
- Sky
- Josiah
- Anthony











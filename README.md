# Online Food Order System

## Student Details
- Name    : Muhammad Awais
- CMS     : 023-25-0156
- Section : (C)

## GitHub Repository
https://github.com/awaisghani42/FoodOrderSystem

## Video Demo
(YouTube link )

---

---

## Project Structure

```
FoodOrderSystem/
├── sql/
│   └── schema.sql              ← Run this first in MySQL
├── src/
│   ├── util/
│   │   └── DBConnection.java   ← JDBC connection singleton
│   ├── model/
│   │   ├── User.java
│   │   ├── MenuItem.java
│   │   └── Order.java          ← includes OrderDetail inner class
│   ├── dao/
│   │   ├── UserDAO.java        ← CRUD for Users table
│   │   ├── MenuDAO.java        ← CRUD for Menu/Categories
│   │   └── OrderDAO.java       ← Orders + Order_Details (with transactions)
│   ├── service/
│   │   └── OrderService.java   ← Cart logic + bill calculation
│   └── ui/
│       └── MainApp.java        ← Console application entry point
└── web/
    └── index.html              ← Full web frontend (open in browser)
```

---

## Setup Instructions

### Step 1 — Database
1. Open **MySQL Workbench** or any MySQL client
2. Run the entire `sql/schema.sql` file
3. This creates the database, all tables, and inserts sample data

### Step 2 — Configure JDBC
Open `src/util/DBConnection.java` and update:
```java
private static final String PASSWORD = "your_mysql_password";
```

### Step 3 — Add MySQL Connector
Download `mysql-connector-j-*.jar` from:
https://dev.mysql.com/downloads/connector/j/

Add it to your project classpath in your IDE (Eclipse / IntelliJ).

### Step 4 — Compile & Run (Console App)
```bash
# From project root
javac -cp ".;path/to/mysql-connector.jar" src/**/*.java
java  -cp ".;path/to/mysql-connector.jar" ui.MainApp
```

### Step 5 — Web Frontend
Simply open `web/index.html` in any browser. No server needed.
The web version runs with in-memory data (mirrors the DB structure).

---

## Demo Credentials

| Role     | Email              | Password   |
|----------|--------------------|------------|
| Admin    | admin@food.com     | admin123   |
| Customer | awais@gmail.com    | pass123    |

---

## Database Schema (Fixed & Complete)

```
Users          → Orders         (One-to-Many)
Orders         → Order_Details  (One-to-Many)
Menu           → Order_Details  (Many-to-Many bridge)
Categories     → Menu           (One-to-Many)
```

### Key Fixes Applied
- `Order_Details` now has composite PK `(order_id, item_id)`
- `Order_Details.unit_price` snapshots price at time of order
- `Orders.status` tracks order lifecycle
- `Menu.is_available` allows toggling items on/off
- `Menu.category_id` FK links to Categories table
- All passwords stored as MD5 hashes
- All Java SQL uses `PreparedStatement` (no SQL injection)
- Order insertion wrapped in a **transaction** (atomic commit/rollback)

---

## DBMS Concepts Covered

| Concept                | Where Used                                      |
|------------------------|-------------------------------------------------|
| Primary Keys           | All 5 tables                                    |
| Foreign Keys           | Orders→Users, Order_Details→Orders/Menu         |
| Composite Primary Key  | Order_Details (order_id + item_id)              |
| One-to-Many            | Users→Orders, Orders→Order_Details              |
| Many-to-Many (bridge)  | Menu ↔ Orders via Order_Details                 |
| Transactions           | placeOrder() in OrderDAO                        |
| ENUM type              | Orders.status                                   |
| CHECK constraint       | quantity > 0, price > 0                         |
| CRUD Operations        | All DAO classes                                 |
| PreparedStatement      | All DAO queries (SQL Injection prevention)       |
| Aggregate Functions    | Revenue report (SUM, COUNT, GROUP BY)           |

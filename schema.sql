-- ============================================================
--  Online Food Order System — Full Database Schema
--  Student: Muhammad Awais | CMS: 023-25-0156
-- ============================================================

CREATE DATABASE IF NOT EXISTS food_order_system;
USE food_order_system;

-- ─────────────────────────────────────────────
--  1. USERS
-- ─────────────────────────────────────────────
CREATE TABLE Users (
    user_id    INT          AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,          -- MD5 hashed
    phone      VARCHAR(15),
    role       ENUM('customer','admin') DEFAULT 'customer',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
--  2. MENU CATEGORIES
-- ─────────────────────────────────────────────
CREATE TABLE Categories (
    category_id   INT         AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE
);

-- ─────────────────────────────────────────────
--  3. MENU ITEMS
-- ─────────────────────────────────────────────
CREATE TABLE Menu (
    item_id      INT            AUTO_INCREMENT PRIMARY KEY,
    category_id  INT            NOT NULL,
    item_name    VARCHAR(100)   NOT NULL,
    description  TEXT,
    price        DECIMAL(10,2)  NOT NULL CHECK (price > 0),
    image_url    VARCHAR(255)   DEFAULT 'default.jpg',
    is_available BOOLEAN        DEFAULT TRUE,
    FOREIGN KEY (category_id) REFERENCES Categories(category_id)
);

-- ─────────────────────────────────────────────
--  4. ORDERS
-- ─────────────────────────────────────────────
CREATE TABLE Orders (
    order_id     INT           AUTO_INCREMENT PRIMARY KEY,
    user_id      INT           NOT NULL,
    order_date   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    status       ENUM('pending','confirmed','preparing','delivered','cancelled')
                               DEFAULT 'pending',
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- ─────────────────────────────────────────────
--  5. ORDER DETAILS  (fixed: composite PK + unit_price snapshot)
-- ─────────────────────────────────────────────
CREATE TABLE Order_Details (
    order_id   INT           NOT NULL,
    item_id    INT           NOT NULL,
    quantity   INT           NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL,          -- price at time of order
    PRIMARY KEY (order_id, item_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id)  REFERENCES Menu(item_id)
);

-- ============================================================
--  SEED DATA
-- ============================================================

-- Categories
INSERT INTO Categories (category_name) VALUES
    ('Burgers'), ('Pizza'), ('Drinks'), ('Desserts'), ('Sides');

-- Admin user  (password = "admin123" MD5 hashed)
INSERT INTO Users (name, email, password, phone, role) VALUES
    ('Admin', 'admin@food.com', MD5('admin123'), '0300-0000000', 'admin');

-- Sample customer  (password = "pass123")
INSERT INTO Users (name, email, password, phone, role) VALUES
    ('Muhammad Awais', 'awais@gmail.com', MD5('pass123'), '0312-1234567', 'customer');

-- Menu Items
INSERT INTO Menu (category_id, item_name, description, price, image_url) VALUES
-- Burgers
(1, 'Classic Beef Burger',  'Juicy beef patty with lettuce, tomato & special sauce', 350.00, 'burger1.jpg'),
(1, 'Zinger Burger',        'Crispy fried chicken with coleslaw and mayo',           320.00, 'burger2.jpg'),
(1, 'Double Smash Burger',  'Double patty, double cheese, caramelised onions',       480.00, 'burger3.jpg'),
-- Pizza
(2, 'Margherita Pizza',     'Classic tomato sauce, fresh mozzarella, basil',         650.00, 'pizza1.jpg'),
(2, 'BBQ Chicken Pizza',    'Smoky BBQ base, grilled chicken, peppers',              750.00, 'pizza2.jpg'),
(2, 'Veggie Supreme',       'Bell peppers, olives, mushrooms, onions',               600.00, 'pizza3.jpg'),
-- Drinks
(3, 'Fresh Lemonade',       'Chilled homemade lemonade with mint',                   120.00, 'drink1.jpg'),
(3, 'Mango Shake',          'Thick fresh mango milkshake',                           150.00, 'drink2.jpg'),
(3, 'Soft Drink (Can)',     'Pepsi / 7UP / Mountain Dew',                             80.00, 'drink3.jpg'),
-- Desserts
(4, 'Chocolate Lava Cake',  'Warm chocolate cake with molten centre',                220.00, 'dessert1.jpg'),
(4, 'Vanilla Ice Cream',    'Two scoops of classic vanilla ice cream',               150.00, 'dessert2.jpg'),
-- Sides
(5, 'Crispy Fries',         'Golden french fries with dipping sauce',                180.00, 'side1.jpg'),
(5, 'Onion Rings',          'Beer-battered crispy onion rings',                      160.00, 'side2.jpg');

-- ============================================================
--  USEFUL QUERIES  (for reference / testing)
-- ============================================================

-- Full order receipt for a given order
-- SELECT u.name, o.order_id, o.order_date, m.item_name,
--        od.quantity, od.unit_price, (od.quantity * od.unit_price) AS subtotal,
--        o.total_amount, o.status
-- FROM Orders o
-- JOIN Users u        ON o.user_id  = u.user_id
-- JOIN Order_Details od ON o.order_id = od.order_id
-- JOIN Menu m         ON od.item_id  = m.item_id
-- WHERE o.order_id = 1;

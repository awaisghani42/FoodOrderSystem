package ui;

import dao.MenuDAO;
import dao.OrderDAO;
import model.MenuItem;
import model.Order;
import model.OrderDetail;
import model.User;
import dao.UserDAO;
import service.OrderService;
import util.DBConnection;

import java.util.List;
import java.util.Scanner;

/**
 * Console-based UI for the Online Food Order System.
 * Run this class as the entry point.
 */
public class MainApp {

    static final Scanner    sc      = new Scanner(System.in);
    static final UserDAO    userDAO = new UserDAO();
    static final MenuDAO    menuDAO = new MenuDAO();
    static final OrderDAO   orderDAO = new OrderDAO();

    static User         loggedInUser    = null;
    static OrderService orderService    = null;

    // ══════════════════════════════════════════════════════════
    public static void main(String[] args) {
        banner();
        boolean running = true;
        while (running) {
            if (loggedInUser == null) {
                running = showAuthMenu();
            } else if ("admin".equals(loggedInUser.getRole())) {
                running = showAdminMenu();
            } else {
                running = showCustomerMenu();
            }
        }
        DBConnection.closeConnection();
        System.out.println("\nThank you for using Food Order System. Goodbye!\n");
    }

    // ── AUTH MENU ─────────────────────────────────────────────
    static boolean showAuthMenu() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║       MAIN MENU              ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  1. Login                    ║");
        System.out.println("║  2. Register                 ║");
        System.out.println("║  3. Exit                     ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("Choice: ");
        String choice = sc.nextLine().trim();
        switch (choice) {
            case "1": doLogin();    break;
            case "2": doRegister(); break;
            case "3": return false;
            default:  System.out.println("Invalid option.");
        }
        return true;
    }

    static void doLogin() {
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        User u = userDAO.loginUser(email, password);
        if (u != null) {
            loggedInUser = u;
            orderService = new OrderService(u.getUserId());
            System.out.println("\n✔ Welcome back, " + u.getName() + "! [" + u.getRole() + "]");
        } else {
            System.out.println("✘ Invalid email or password.");
        }
    }

    static void doRegister() {
        System.out.println("\n── Register New Account ──");
        System.out.print("Full Name : "); String name  = sc.nextLine().trim();
        System.out.print("Email     : "); String email = sc.nextLine().trim();
        if (userDAO.emailExists(email)) {
            System.out.println("✘ Email already registered.");
            return;
        }
        System.out.print("Password  : "); String pass  = sc.nextLine().trim();
        System.out.print("Phone     : "); String phone = sc.nextLine().trim();

        User newUser = new User(name, email, pass, phone);
        if (userDAO.registerUser(newUser)) {
            System.out.println("✔ Registration successful! Please login.");
        } else {
            System.out.println("✘ Registration failed.");
        }
    }

    // ── CUSTOMER MENU ─────────────────────────────────────────
    static boolean showCustomerMenu() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║   CUSTOMER MENU              ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  1. Browse Menu              ║");
        System.out.println("║  2. View / Edit Cart         ║");
        System.out.println("║  3. Place Order (Checkout)   ║");
        System.out.println("║  4. My Order History         ║");
        System.out.println("║  5. Logout                   ║");
        System.out.println("║  6. Exit                     ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("Choice: ");
        String c = sc.nextLine().trim();
        switch (c) {
            case "1": browseMenu();     break;
            case "2": viewCart();       break;
            case "3": checkout();       break;
            case "4": orderHistory();   break;
            case "5": doLogout();       break;
            case "6": return false;
            default:  System.out.println("Invalid option.");
        }
        return true;
    }

    static void browseMenu() {
        List<MenuItem> items = menuDAO.getAllMenuItems();
        if (items.isEmpty()) { System.out.println("No items available."); return; }

        String currentCat = "";
        System.out.println("\n══════════════ MENU ══════════════════");
        for (MenuItem m : items) {
            if (!m.getCategoryName().equals(currentCat)) {
                currentCat = m.getCategoryName();
                System.out.println("\n  ── " + currentCat + " ──");
            }
            System.out.printf("  [%2d] %-26s Rs.%.2f%n",
                m.getItemId(), m.getItemName(), m.getPrice());
            if (m.getDescription() != null && !m.getDescription().isEmpty())
                System.out.println("       " + m.getDescription());
        }
        System.out.println("══════════════════════════════════════");
        System.out.print("\nEnter item ID to add to cart (0 to go back): ");
        try {
            int itemId = Integer.parseInt(sc.nextLine().trim());
            if (itemId == 0) return;
            System.out.print("Quantity: ");
            int qty = Integer.parseInt(sc.nextLine().trim());
            orderService.addToCart(itemId, qty);
            System.out.println("✔ Added to cart.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    static void viewCart() {
        if (orderService.isCartEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        orderService.printBill();
        System.out.println("Options: [R] Remove item  [C] Clear cart  [Enter] Back");
        System.out.print("Choice: ");
        String c = sc.nextLine().trim().toUpperCase();
        if ("R".equals(c)) {
            System.out.print("Enter item ID to remove: ");
            try {
                int id = Integer.parseInt(sc.nextLine().trim());
                orderService.removeFromCart(id);
                System.out.println("✔ Item removed.");
            } catch (NumberFormatException e) { System.out.println("Invalid."); }
        } else if ("C".equals(c)) {
            orderService.clearCart();
            System.out.println("✔ Cart cleared.");
        }
    }

    static void checkout() {
        if (orderService.isCartEmpty()) {
            System.out.println("Your cart is empty!");
            return;
        }
        orderService.printBill();
        System.out.print("Confirm order? (y/n): ");
        String confirm = sc.nextLine().trim().toLowerCase();
        if ("y".equals(confirm)) {
            Order placed = orderService.placeOrder();
            if (placed != null) {
                System.out.println("✔ Order #" + placed.getOrderId() + " placed successfully!");
                System.out.println("  Total charged: Rs." + String.format("%.2f", placed.getTotalAmount()));
                System.out.println("  Status: " + placed.getStatus());
            } else {
                System.out.println("✘ Order placement failed.");
            }
        } else {
            System.out.println("Order cancelled.");
        }
    }

    static void orderHistory() {
        List<Order> orders = orderService.getMyOrderHistory();
        if (orders.isEmpty()) { System.out.println("No orders yet."); return; }
        System.out.println("\n── Your Order History ──────────────────");
        for (Order o : orders) {
            System.out.printf("\nOrder #%-4d | %s | Rs.%.2f | %s%n",
                o.getOrderId(), o.getOrderDate(), o.getTotalAmount(), o.getStatus().toUpperCase());
            for (OrderDetail d : o.getDetails()) {
                System.out.printf("   %-24s x%d @ Rs.%.2f%n",
                    d.getItemName(), d.getQuantity(), d.getUnitPrice());
            }
        }
        System.out.println("────────────────────────────────────────");
    }

    // ── ADMIN MENU ────────────────────────────────────────────
    static boolean showAdminMenu() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║   ADMIN PANEL                ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  1. View All Orders          ║");
        System.out.println("║  2. Update Order Status      ║");
        System.out.println("║  3. View All Menu Items      ║");
        System.out.println("║  4. Toggle Item Availability ║");
        System.out.println("║  5. Add New Menu Item        ║");
        System.out.println("║  6. Revenue Report           ║");
        System.out.println("║  7. View All Users           ║");
        System.out.println("║  8. Logout                   ║");
        System.out.println("║  9. Exit                     ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("Choice: ");
        String c = sc.nextLine().trim();
        switch (c) {
            case "1": adminViewOrders();         break;
            case "2": adminUpdateOrderStatus();  break;
            case "3": adminViewMenu();           break;
            case "4": adminToggleItem();         break;
            case "5": adminAddItem();            break;
            case "6": orderDAO.printRevenueReport(); break;
            case "7": adminViewUsers();          break;
            case "8": doLogout();                break;
            case "9": return false;
            default:  System.out.println("Invalid option.");
        }
        return true;
    }

    static void adminViewOrders() {
        List<Order> orders = orderDAO.getAllOrders();
        if (orders.isEmpty()) { System.out.println("No orders found."); return; }
        System.out.println("\n── All Orders ──────────────────────────────");
        for (Order o : orders) {
            System.out.printf("Order #%-4d | %-15s | %s | Rs.%-8.2f | %s%n",
                o.getOrderId(), o.getUserName(), o.getOrderDate(),
                o.getTotalAmount(), o.getStatus().toUpperCase());
        }
    }

    static void adminUpdateOrderStatus() {
        System.out.print("Enter Order ID: ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.println("New status: [1] confirmed  [2] preparing  [3] delivered  [4] cancelled");
            System.out.print("Choice: ");
            String[] statuses = {"confirmed","preparing","delivered","cancelled"};
            int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (idx < 0 || idx >= statuses.length) { System.out.println("Invalid."); return; }
            boolean ok = orderDAO.updateOrderStatus(id, statuses[idx]);
            System.out.println(ok ? "✔ Status updated." : "✘ Update failed.");
        } catch (NumberFormatException e) { System.out.println("Invalid input."); }
    }

    static void adminViewMenu() {
        List<MenuItem> items = menuDAO.getAllMenuItems();
        System.out.println("\n── Menu Items ──────────────────────────────");
        for (MenuItem m : items) {
            System.out.printf("[%2d] %-26s Rs.%-8.2f %-10s %s%n",
                m.getItemId(), m.getItemName(), m.getPrice(),
                m.getCategoryName(), m.isAvailable() ? "✔ Available" : "✘ Unavailable");
        }
    }

    static void adminToggleItem() {
        System.out.print("Enter Item ID to toggle availability: ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            boolean ok = menuDAO.toggleAvailability(id);
            System.out.println(ok ? "✔ Availability toggled." : "✘ Failed.");
        } catch (NumberFormatException e) { System.out.println("Invalid."); }
    }

    static void adminAddItem() {
        System.out.println("\n── Add New Menu Item ──");
        System.out.print("Category ID (1=Burgers 2=Pizza 3=Drinks 4=Desserts 5=Sides): ");
        try {
            int catId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Item Name   : "); String name = sc.nextLine().trim();
            System.out.print("Description : "); String desc = sc.nextLine().trim();
            System.out.print("Price (Rs.) : "); double price = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Image file  : "); String img  = sc.nextLine().trim();
            model.MenuItem item = new model.MenuItem(catId, name, desc, price, img);
            boolean ok = menuDAO.addMenuItem(item);
            System.out.println(ok ? "✔ Item added successfully." : "✘ Failed.");
        } catch (NumberFormatException e) { System.out.println("Invalid input."); }
    }

    static void adminViewUsers() {
        List<User> users = userDAO.getAllUsers();
        System.out.println("\n── Registered Users ────────────────────────");
        for (User u : users) {
            System.out.printf("[%2d] %-20s %-30s %s%n",
                u.getUserId(), u.getName(), u.getEmail(), u.getRole().toUpperCase());
        }
    }

    static void doLogout() {
        System.out.println("✔ Logged out successfully.");
        loggedInUser = null;
        orderService = null;
    }

    static void banner() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║        ONLINE FOOD ORDER SYSTEM              ║");
        System.out.println("║  Student: Muhammad Awais | CMS: 023-25-0156  ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }
}

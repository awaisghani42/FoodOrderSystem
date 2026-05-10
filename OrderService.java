package service;

import dao.MenuDAO;
import dao.OrderDAO;
import model.MenuItem;
import model.Order;
import model.OrderDetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic layer — sits between UI and DAO.
 * Handles cart management, bill calculation, and order placement.
 */
public class OrderService {

    private final MenuDAO  menuDAO  = new MenuDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    // Cart: itemId → quantity
    private final Map<Integer, Integer> cart = new HashMap<>();
    private final int userId;

    public OrderService(int userId) {
        this.userId = userId;
    }

    // ── CART OPERATIONS ───────────────────────────────────────

    /** Add item to cart (or increase quantity if already there). */
    public void addToCart(int itemId, int quantity) {
        if (quantity <= 0) return;
        cart.merge(itemId, quantity, Integer::sum);
        System.out.println("[Cart] Item #" + itemId + " qty=" + cart.get(itemId));
    }

    /** Remove one unit from cart. */
    public void decreaseQty(int itemId) {
        if (!cart.containsKey(itemId)) return;
        int qty = cart.get(itemId) - 1;
        if (qty <= 0) cart.remove(itemId);
        else          cart.put(itemId, qty);
    }

    /** Remove item entirely from cart. */
    public void removeFromCart(int itemId) {
        cart.remove(itemId);
    }

    /** Clear cart completely. */
    public void clearCart() {
        cart.clear();
    }

    /** Returns current cart snapshot. */
    public Map<Integer, Integer> getCart() {
        return new HashMap<>(cart);
    }

    public boolean isCartEmpty() {
        return cart.isEmpty();
    }

    // ── BILL CALCULATION ──────────────────────────────────────

    /** Calculates total from current cart items. */
    public double calculateTotal() {
        double total = 0;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            MenuItem item = menuDAO.getItemById(entry.getKey());
            if (item != null) {
                total += item.getPrice() * entry.getValue();
            }
        }
        return total;
    }

    /** Prints itemized bill to console. */
    public void printBill() {
        System.out.println("\n══════════════════════════════════════");
        System.out.println("          FOOD ORDER BILL             ");
        System.out.println("══════════════════════════════════════");
        System.out.printf("%-22s %4s %10s%n", "Item", "Qty", "Amount");
        System.out.println("──────────────────────────────────────");
        double total = 0;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            MenuItem item = menuDAO.getItemById(entry.getKey());
            if (item != null) {
                double sub = item.getPrice() * entry.getValue();
                total += sub;
                System.out.printf("%-22s %4d %10.2f%n",
                    item.getItemName(), entry.getValue(), sub);
            }
        }
        System.out.println("──────────────────────────────────────");
        System.out.printf("%-22s %4s %10.2f%n", "TOTAL (Rs.)", "", total);
        System.out.println("══════════════════════════════════════\n");
    }

    // ── PLACE ORDER ───────────────────────────────────────────

    /**
     * Converts current cart into an Order and persists it.
     * @return the new Order with its generated ID, or null on failure.
     */
    public Order placeOrder() {
        if (isCartEmpty()) {
            System.out.println("[OrderService] Cart is empty!");
            return null;
        }

        Order order = new Order(userId, 0);
        double total = 0;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            MenuItem item = menuDAO.getItemById(entry.getKey());
            if (item == null || !item.isAvailable()) continue;

            int    qty       = entry.getValue();
            double unitPrice = item.getPrice();
            total += unitPrice * qty;

            // snapshot unit_price — not a live FK reference
            order.addDetail(new OrderDetail(item.getItemId(), item.getItemName(), qty, unitPrice));
        }

        order.setTotalAmount(total);

        boolean success = orderDAO.placeOrder(order);
        if (success) {
            clearCart();
            return order;
        }
        return null;
    }

    // ── ORDER HISTORY ─────────────────────────────────────────

    public List<Order> getMyOrderHistory() {
        return orderDAO.getOrdersByUser(userId);
    }

    public Order getOrderById(int orderId) {
        return orderDAO.getOrderById(orderId);
    }
}

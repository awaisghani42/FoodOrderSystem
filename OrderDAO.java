package dao;

import model.Order;
import model.OrderDetail;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Orders and Order_Details tables.
 * Uses transactions to ensure order + details are saved atomically.
 */
public class OrderDAO {

    // ── PLACE a new order (atomic transaction) ────────────────
    public boolean placeOrder(Order order) {
        String insertOrder  = "INSERT INTO Orders (user_id, total_amount, status) VALUES (?, ?, 'pending')";
        String insertDetail = "INSERT INTO Order_Details (order_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);   // BEGIN TRANSACTION

            // 1. Insert order header
            PreparedStatement psOrder = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, order.getUserId());
            psOrder.setDouble(2, order.getTotalAmount());
            psOrder.executeUpdate();

            ResultSet keys = psOrder.getGeneratedKeys();
            if (!keys.next()) throw new SQLException("Order insert failed, no generated key.");
            int generatedOrderId = keys.getInt(1);
            order.setOrderId(generatedOrderId);

            // 2. Insert each order detail line
            PreparedStatement psDetail = conn.prepareStatement(insertDetail);
            for (OrderDetail d : order.getDetails()) {
                psDetail.setInt(1, generatedOrderId);
                psDetail.setInt(2, d.getItemId());
                psDetail.setInt(3, d.getQuantity());
                psDetail.setDouble(4, d.getUnitPrice());
                psDetail.addBatch();
            }
            psDetail.executeBatch();

            conn.commit();   // COMMIT TRANSACTION
            System.out.println("[OrderDAO] Order #" + generatedOrderId + " placed successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println("[OrderDAO] Transaction failed — rolling back.");
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ── GET order history for a user ──────────────────────────
    public List<Order> getOrdersByUser(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders WHERE user_id = ? ORDER BY order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order o = mapOrderRow(rs);
                o.setDetails(getDetailsByOrderId(o.getOrderId()));
                orders.add(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // ── GET single order by ID (with details) ─────────────────
    public Order getOrderById(int orderId) {
        String sql = "SELECT o.*, u.name AS user_name " +
                     "FROM Orders o JOIN Users u ON o.user_id = u.user_id " +
                     "WHERE o.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order o = mapOrderRow(rs);
                o.setUserName(rs.getString("user_name"));
                o.setDetails(getDetailsByOrderId(orderId));
                return o;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── GET all orders (admin) ────────────────────────────────
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.name AS user_name " +
                     "FROM Orders o JOIN Users u ON o.user_id = u.user_id " +
                     "ORDER BY o.order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order o = mapOrderRow(rs);
                o.setUserName(rs.getString("user_name"));
                o.setDetails(getDetailsByOrderId(o.getOrderId()));
                orders.add(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // ── UPDATE order status (admin) ───────────────────────────
    public boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── CANCEL order ──────────────────────────────────────────
    public boolean cancelOrder(int orderId) {
        return updateOrderStatus(orderId, "cancelled");
    }

    // ── GET line items for an order ───────────────────────────
    public List<OrderDetail> getDetailsByOrderId(int orderId) {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT od.*, m.item_name " +
                     "FROM Order_Details od JOIN Menu m ON od.item_id = m.item_id " +
                     "WHERE od.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderDetail d = new OrderDetail(
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price")
                );
                details.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    // ── REVENUE REPORT (total per day) ───────────────────────
    public void printRevenueReport() {
        String sql = "SELECT DATE(order_date) AS day, COUNT(*) AS total_orders, " +
                     "SUM(total_amount) AS revenue " +
                     "FROM Orders WHERE status != 'cancelled' " +
                     "GROUP BY DATE(order_date) ORDER BY day DESC LIMIT 30";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n── Revenue Report (last 30 days) ─────────────");
            System.out.printf("%-12s %-14s %s%n", "Date", "Orders", "Revenue (Rs.)");
            while (rs.next()) {
                System.out.printf("%-12s %-14d %.2f%n",
                    rs.getString("day"),
                    rs.getInt("total_orders"),
                    rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Private helper ────────────────────────────────────────
    private Order mapOrderRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("order_date");
        if (ts != null) o.setOrderDate(ts.toLocalDateTime());
        return o;
    }
}

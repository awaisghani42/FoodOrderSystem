package dao;

import model.MenuItem;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Menu and Categories tables.
 */
public class MenuDAO {

    // ── GET all available menu items (with category name) ─────
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT m.*, c.category_name " +
                     "FROM Menu m " +
                     "JOIN Categories c ON m.category_id = c.category_id " +
                     "WHERE m.is_available = TRUE " +
                     "ORDER BY c.category_name, m.item_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── GET menu items by category ────────────────────────────
    public List<MenuItem> getMenuByCategory(int categoryId) {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT m.*, c.category_name " +
                     "FROM Menu m " +
                     "JOIN Categories c ON m.category_id = c.category_id " +
                     "WHERE m.category_id = ? AND m.is_available = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── GET single item by ID ─────────────────────────────────
    public MenuItem getItemById(int itemId) {
        String sql = "SELECT m.*, c.category_name " +
                     "FROM Menu m " +
                     "JOIN Categories c ON m.category_id = c.category_id " +
                     "WHERE m.item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── SEARCH menu items by name ─────────────────────────────
    public List<MenuItem> searchMenu(String keyword) {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT m.*, c.category_name " +
                     "FROM Menu m " +
                     "JOIN Categories c ON m.category_id = c.category_id " +
                     "WHERE m.item_name LIKE ? AND m.is_available = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── ADD new menu item (admin) ─────────────────────────────
    public boolean addMenuItem(MenuItem item) {
        String sql = "INSERT INTO Menu (category_id, item_name, description, price, image_url) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getCategoryId());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getDescription());
            ps.setDouble(4, item.getPrice());
            ps.setString(5, item.getImageUrl());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── UPDATE menu item (admin) ──────────────────────────────
    public boolean updateMenuItem(MenuItem item) {
        String sql = "UPDATE Menu SET item_name=?, description=?, price=?, " +
                     "is_available=? WHERE item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getItemName());
            ps.setString(2, item.getDescription());
            ps.setDouble(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());
            ps.setInt(5, item.getItemId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── TOGGLE availability ───────────────────────────────────
    public boolean toggleAvailability(int itemId) {
        String sql = "UPDATE Menu SET is_available = NOT is_available WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── GET all categories ────────────────────────────────────
    public List<String> getAllCategories() {
        List<String> cats = new ArrayList<>();
        String sql = "SELECT category_name FROM Categories ORDER BY category_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) cats.add(rs.getString("category_name"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cats;
    }

    // ── Private helper ────────────────────────────────────────
    private MenuItem mapRow(ResultSet rs) throws SQLException {
        MenuItem m = new MenuItem();
        m.setItemId(rs.getInt("item_id"));
        m.setCategoryId(rs.getInt("category_id"));
        m.setCategoryName(rs.getString("category_name"));
        m.setItemName(rs.getString("item_name"));
        m.setDescription(rs.getString("description"));
        m.setPrice(rs.getDouble("price"));
        m.setImageUrl(rs.getString("image_url"));
        m.setAvailable(rs.getBoolean("is_available"));
        return m;
    }
}

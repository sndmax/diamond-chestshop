package com.gmail.sneakdevs.diamondchestshop.sql;

import com.gmail.sneakdevs.diamondeconomy.sql.SQLiteDatabaseManager;

import java.sql.*;

public class ChestshopSQLiteDatabaseManager implements ChestshopDatabaseManager {
    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(SQLiteDatabaseManager.url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public int addShop(String item, String nbt) {
        String sql = "INSERT INTO chestshop(item,nbt) VALUES(?,?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, item);
            pstmt.setString(2, nbt);
            pstmt.executeUpdate();
            return getMostRecentId();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getItem(int id) {
        String sql = "SELECT item FROM chestshop WHERE id = " + id;
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            return rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNbt(int id) {
        String sql = "SELECT nbt FROM chestshop WHERE id = " + id;
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            rs.next();
            return rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeShop(int id) {
        String sql = "DELETE FROM chestshop WHERE id = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getMostRecentId() {
        String sql = "SELECT id FROM chestshop ORDER BY id DESC";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void logTrade(String item, String nbt, int amount, int price, String buyer, String seller, String type, String date) {
        String sql = "INSERT INTO chestshop_trades(item,nbt,amount,price,buyer,seller,type,date) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, item);
            pstmt.setString(2, nbt);
            pstmt.setInt(3, amount);
            pstmt.setInt(4, price);
            pstmt.setString(5, buyer);
            pstmt.setString(6, seller);
            pstmt.setString(7, type);
            pstmt.setString(8, date);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
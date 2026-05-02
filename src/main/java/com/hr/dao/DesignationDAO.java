package com.hr.dao;

import com.hr.model.Designation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DesignationDAO {
    private final Connection conn;

    public DesignationDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public List<Designation> getAll() throws SQLException {
        List<Designation> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM designations ORDER BY title")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Designation getById(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM designations WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void insert(Designation d) throws SQLException {
        String sql = "INSERT INTO designations (title, description, salary_grade) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getTitle());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getSalaryGrade());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) d.setId(keys.getInt(1));
            }
        }
    }

    public void update(Designation d) throws SQLException {
        String sql = "UPDATE designations SET title=?, description=?, salary_grade=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getTitle());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getSalaryGrade());
            ps.setInt(4, d.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM designations WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Designation map(ResultSet rs) throws SQLException {
        Designation d = new Designation();
        d.setId(rs.getInt("id"));
        d.setTitle(rs.getString("title"));
        d.setDescription(rs.getString("description"));
        d.setSalaryGrade(rs.getString("salary_grade"));
        return d;
    }
}

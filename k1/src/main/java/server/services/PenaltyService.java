package server.services;

import Database.Connector;
import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Rabota s tablitsey Penalties (shtrafy). */
public final class PenaltyService {

    public Response listAll(Request req) {
        return query("SELECT * FROM Penalties", null);
    }

    public Response listByUser(Request req) {
        return query("SELECT * FROM Penalties WHERE Username = ?", req.get("username"));
    }

    public Response existsForUser(Request req) {
        String sql = "SELECT 1 FROM Penalties WHERE Username = ?";
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("username"));
            try (ResultSet rs = ps.executeQuery()) {
                return Response.success(rs.next() ? "true" : "false");
            }
        } catch (SQLException e) {
            return Response.failure("Ошибка БД: " + e.getMessage());
        }
    }

    public Response add(Request req) {
        String check = "SELECT 1 FROM Users WHERE Username = ?";
        String insert = "INSERT INTO Penalties (Username, FineAmount, PenaltyDate, Description) "
                + "VALUES (?, ?, CURRENT_DATE, ?)";
        try (Connection c = Connector.connect()) {
            try (PreparedStatement ps = c.prepareStatement(check)) {
                ps.setString(1, req.get("username"));
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Response.failure("Пользователь не найден");
                }
            }
            try (PreparedStatement ps = c.prepareStatement(insert)) {
                ps.setString(1, req.get("username"));
                ps.setBigDecimal(2, new BigDecimal(req.get("amount")));
                ps.setString(3, req.get("description"));
                ps.executeUpdate();
            }
            return Response.success("Штраф успешно добавлен");
        } catch (SQLException | NumberFormatException e) {
            return Response.failure("Ошибка добавления штрафа: " + e.getMessage());
        }
    }

    public Response deleteByUser(Request req) {
        String sql = "DELETE FROM Penalties WHERE Username = ?";
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("username"));
            int n = ps.executeUpdate();
            return Response.success("Удалено записей: " + n);
        } catch (SQLException e) {
            return Response.failure("Ошибка удаления: " + e.getMessage());
        }
    }

    private Response query(String sql, String username) {
        List<String> rows = new ArrayList<>();
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (username != null) ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add("Номер штрафа: " + rs.getInt("PenaltyID") + "\n"
                            + "Логин: " + rs.getString("Username") + "\n"
                            + "Сумма: " + rs.getBigDecimal("FineAmount") + "\n"
                            + "Дата: " + rs.getDate("PenaltyDate") + "\n"
                            + "Описание: " + rs.getString("Description") + "\n");
                }
            }
            return Response.success(JsonCodec.writeStringArray(rows));
        } catch (SQLException e) {
            return Response.failure("Ошибка БД: " + e.getMessage());
        }
    }
}

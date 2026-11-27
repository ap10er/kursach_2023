package server.services;

import Database.Connector;
import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Rabota s tablitsey Messages (soobshcheniya / uvedomleniya). */
public final class MessageService {

    public Response list(Request req) {
        String sql = "SELECT * FROM Messages WHERE Username = ?";
        List<String> rows = new ArrayList<>();
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("username"));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add("Отправитель: " + rs.getString("Sender") + "\n"
                            + "Сообщение: " + rs.getString("Message") + "\n");
                }
            }
            return Response.success(JsonCodec.writeStringArray(rows));
        } catch (SQLException e) {
            return Response.failure("Ошибка БД: " + e.getMessage());
        }
    }

    public Response listAll(Request req) {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT * FROM Messages";
        try (Connection c = Connector.connect(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add("Получатель: " + rs.getString("Username") + "\n"
                        + "Отправитель: " + rs.getString("Sender") + "\n"
                        + "Сообщение: " + rs.getString("Message") + "\n");
            }
            return Response.success(JsonCodec.writeStringArray(rows));
        } catch (SQLException e) {
            return Response.failure("Ошибка БД: " + e.getMessage());
        }
    }

    public Response send(Request req) {
        String check = "SELECT 1 FROM Employees WHERE Username = ?";
        String insert = "INSERT INTO Messages (Username, Sender, Message) VALUES (?, ?, ?)";
        try (Connection c = Connector.connect()) {
            try (PreparedStatement ps = c.prepareStatement(check)) {
                ps.setString(1, req.get("receiver"));
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Response.failure("Получатель не найден");
                }
            }
            try (PreparedStatement ps = c.prepareStatement(insert)) {
                ps.setString(1, req.get("receiver"));
                ps.setString(2, req.get("sender"));
                ps.setString(3, req.get("message"));
                ps.executeUpdate();
            }
            return Response.success("Сообщение отправлено");
        } catch (SQLException e) {
            return Response.failure("Ошибка отправки: " + e.getMessage());
        }
    }

    public Response delete(Request req) {
        String sql = "DELETE FROM Messages WHERE Username = ?";
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("username"));
            int n = ps.executeUpdate();
            return Response.success("Удалено сообщений: " + n);
        } catch (SQLException e) {
            return Response.failure("Ошибка удаления: " + e.getMessage());
        }
    }
}

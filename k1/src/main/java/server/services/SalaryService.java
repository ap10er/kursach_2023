package server.services;

import Database.Connector;
import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Rabota s tablitsey Salaries i Positions. */
public final class SalaryService {

    public Response positionExists(Request req) {
        String sql = "SELECT 1 FROM Positions WHERE PositionName=?";
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("positionName"));
            try (ResultSet rs = ps.executeQuery()) {
                return Response.success(rs.next() ? "true" : "false");
            }
        } catch (SQLException e) {
            return Response.failure("Ошибка БД: " + e.getMessage());
        }
    }

    public Response add(Request req) {
        String sql = "INSERT INTO Salaries (PositionName, BaseRate, Bonus, WorkedHours, RatePerHour, Result) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = Connector.connect();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, req.get("positionName"));
            ps.setDouble(2, Double.parseDouble(req.get("baseRate")));
            ps.setDouble(3, Double.parseDouble(req.get("bonus")));
            ps.setDouble(4, Double.parseDouble(req.get("workedHours")));
            ps.setDouble(5, Double.parseDouble(req.get("ratePerHour")));
            ps.setDouble(6, Double.parseDouble(req.get("result")));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : -1;
                return Response.success("Запись добавлена, ID=" + id);
            }
        } catch (SQLException | IllegalArgumentException e) {
            return Response.failure("Ошибка добавления зарплаты: " + e.getMessage());
        }
    }

    public Response list(Request req) {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT * FROM Salaries";
        try (Connection c = Connector.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add("Номер: " + rs.getInt("SalaryID") + "\n"
                        + "Должность: " + rs.getString("PositionName") + "\n"
                        + "Базовая ставка: " + rs.getDouble("BaseRate") + "\n"
                        + "Премия: " + rs.getDouble("Bonus") + "\n"
                        + "Кол-во отработанных часов: " + rs.getInt("WorkedHours") + "\n"
                        + "Оплата в час: " + rs.getDouble("RatePerHour") + "\n"
                        + "Дата получения зарплаты: " + rs.getString("EffectiveDate") + "\n"
                        + "Итого: " + rs.getDouble("Result") + "\n");
            }
            return Response.success(JsonCodec.writeStringArray(rows));
        } catch (SQLException e) {
            return Response.failure("Ошибка БД: " + e.getMessage());
        }
    }

    /** Dannye dlya grafika: pary "data|summa" po polzovatelyu. */
    public Response chartData(Request req) {
        String sql = "SELECT EffectiveDate, Result FROM Salaries WHERE PositionName = "
                + "(SELECT Position FROM Employees WHERE Username = ?)";
        List<String> points = new ArrayList<>();
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("username"));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    points.add(rs.getString("EffectiveDate") + "|" + rs.getDouble("Result"));
                }
            }
            return Response.success(JsonCodec.writeStringArray(points));
        } catch (SQLException e) {
            return Response.failure("Ошибка БД: " + e.getMessage());
        }
    }
}

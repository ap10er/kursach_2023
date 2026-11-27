package server.services;

import Database.Connector;
import protocol.JsonCodec;
import protocol.Request;
import protocol.Response;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Rabota s tablitsey Employees: spisok, dobavlenie, poisk, udalenie, redaktirovanie FIO. */
public final class EmployeeService {

    public Response list(Request req) {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT * FROM Employees";
        try (Connection c = Connector.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(format(rs));
            }
            return Response.success(JsonCodec.writeStringArray(rows));
        } catch (SQLException e) {
            return Response.failure("Ошибка БД: " + e.getMessage());
        }
    }

    public Response add(Request req) {
        String sql = "INSERT INTO Employees (Lastname, Firstname, Middlename, Number, Position, Salary, HireDate, DepartmentName) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("lastName"));
            ps.setString(2, req.get("firstName"));
            ps.setString(3, req.get("middleName"));
            ps.setString(4, req.get("number"));
            ps.setString(5, req.get("position"));
            ps.setDouble(6, Double.parseDouble(req.get("salary").replace(',', '.')));
            ps.setDate(7, Date.valueOf(req.get("hireDate")));
            ps.setString(8, req.get("department"));
            ps.executeUpdate();
            return Response.success("Сотрудник успешно добавлен!");
        } catch (SQLException | IllegalArgumentException e) {
            return Response.failure("Ошибка добавления сотрудника: " + e.getMessage());
        }
    }

    public Response search(Request req) {
        String fullName = req.get("fullName");
        String sql = "SELECT * FROM Employees WHERE CONCAT(Lastname, ' ', Firstname, ' ', Middlename) = ?";
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fullName);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> rows = new ArrayList<>();
                while (rs.next()) rows.add(format(rs));
                return Response.success(JsonCodec.writeStringArray(rows));
            }
        } catch (SQLException e) {
            return Response.failure("Ошибка поиска: " + e.getMessage());
        }
    }

    public Response delete(Request req) {
        String fullName = req.get("fullname");
        String select = "SELECT CONCAT(Lastname,' ',Firstname,' ',Middlename) AS fio, Username FROM Employees";
        String delEmp = "DELETE FROM Employees WHERE CONCAT(Lastname,' ',Firstname,' ',Middlename)=?";
        String delUser = "DELETE FROM users WHERE Username=?";
        try (Connection c = Connector.connect()) {
            c.setAutoCommit(false);
            String username = null;
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(select)) {
                while (rs.next()) {
                    if (fullName.equalsIgnoreCase(rs.getString("fio"))) {
                        username = rs.getString("Username");
                        break;
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement(delEmp)) {
                ps.setString(1, fullName);
                ps.executeUpdate();
            }
            if (username != null && !username.isBlank()) {
                try (PreparedStatement ps = c.prepareStatement(delUser)) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                }
            }
            c.commit();
            return Response.success("Сотрудник удалён");
        } catch (SQLException e) {
            return Response.failure("Ошибка удаления: " + e.getMessage());
        }
    }

    public Response deleteByUsername(Request req) {
        String username = req.get("username");
        String delEmp = "DELETE FROM Employees WHERE Username = ?";
        String delUser = "DELETE FROM users WHERE Username = ?";
        try (Connection c = Connector.connect()) {
            c.setAutoCommit(false);
            int n;
            try (PreparedStatement ps = c.prepareStatement(delEmp)) {
                ps.setString(1, username);
                n = ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(delUser)) {
                ps.setString(1, username);
                ps.executeUpdate();
            }
            c.commit();
            return Response.success("Удалено записей: " + n);
        } catch (SQLException e) {
            return Response.failure("Ошибка удаления: " + e.getMessage());
        }
    }

    public Response updateField(Request req) {
        String field = req.get("field");
        String allowed = switch (field) {
            case "Number" -> "Number";
            case "Middlename" -> "Middlename";
            case "Firstname" -> "Firstname";
            case "Lastname" -> "Lastname";
            default -> null;
        };
        if (allowed == null) return Response.failure("Недопустимое поле: " + field);
        String sql = "UPDATE Employees SET " + allowed + "=? WHERE Username=?";
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("value"));
            ps.setString(2, req.get("username"));
            ps.executeUpdate();
            return Response.success("Данные обновлены");
        } catch (SQLException e) {
            return Response.failure("Ошибка обновления: " + e.getMessage());
        }
    }

    public Response profile(Request req) {
        String sql = "SELECT * FROM Employees WHERE Username=?";
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("username"));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Response.success(format(rs));
                return Response.failure("Сотрудник не найден");
            }
        } catch (SQLException e) {
            return Response.failure("Ошибка БД: " + e.getMessage());
        }
    }

    private static String format(ResultSet rs) throws SQLException {
        return "Номер сотрудника: " + rs.getInt("EmployeeID") + "\n"
                + "Фамилия: " + rs.getString("LastName") + "\n"
                + "Имя: " + rs.getString("FirstName") + "\n"
                + "Отчество: " + rs.getString("MiddleName") + "\n"
                + "Номер телефона: " + rs.getString("Number") + "\n"
                + "Должность: " + rs.getString("Position") + "\n"
                + "Зарплата: " + rs.getDouble("Salary") + "\n"
                + "Дата начала работы: " + rs.getDate("HireDate") + "\n"
                + "Логин: " + rs.getString("Username") + "\n"
                + "Отдел: " + rs.getString("DepartmentName") + "\n\n";
    }
}

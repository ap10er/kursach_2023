package server.services;

import Database.Connector;
import protocol.Request;
import protocol.Response;

import java.sql.*;

/** Vsya rabota s tablitsey users i svyazannymi FIO — tolko na servere. */
public final class UserService {

    public Response login(Request req) {
        String username = req.get("username");
        String password = req.get("password");
        String sql = "SELECT role FROM users WHERE Username=? AND Password=?";
        try (Connection c = Connector.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Response.success(rs.getString("role"));
                }
                return Response.failure("Неверный логин или пароль");
            }
        } catch (SQLException e) {
            return Response.failure("Ошибка БД при входе: " + e.getMessage());
        }
    }

    /** Registratsiya: sverka FIO s Employees, sozdanie usera i privyazka username. */
    public Response register(Request req) {
        String login = req.get("login");
        String password = req.get("password");
        String lastname = req.get("lastname");
        String firstname = req.get("firstname");
        String middlename = req.get("middlename");
        String department = req.get("department");
        String role = "it".equalsIgnoreCase(department) ? "Admin" : "User";

        String find = "SELECT DepartmentName, Username FROM Employees WHERE Lastname=? AND Firstname=? AND Middlename=?";
        String insert = "INSERT INTO users (Username, Password, Role) VALUES (?, ?, ?)";
        String update = "UPDATE Employees SET Username=? WHERE Lastname=? AND Firstname=? AND Middlename=?";

        try (Connection c = Connector.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(find)) {
                ps.setString(1, lastname);
                ps.setString(2, firstname);
                ps.setString(3, middlename);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        c.rollback();
                        return Response.failure("Пользователь с такими ФИО не найден в таблице Employees");
                    }
                    String existing = rs.getString("Username");
                    if (existing != null && !existing.isBlank()) {
                        c.rollback();
                        return Response.failure("Этот пользователь уже зарегистрирован");
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement(insert)) {
                ps.setString(1, login);
                ps.setString(2, password);
                ps.setString(3, role);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(update)) {
                ps.setString(1, login);
                ps.setString(2, lastname);
                ps.setString(3, firstname);
                ps.setString(4, middlename);
                ps.executeUpdate();
            }
            c.commit();
            return Response.success("Регистрация прошла успешно!");
        } catch (SQLException e) {
            return Response.failure("Ошибка БД при регистрации: " + e.getMessage());
        }
    }

    public Response changeUsername(Request req) {
        String oldName = req.get("oldUsername");
        String newName = req.get("newUsername");
        String sql1 = "UPDATE Users SET Username=? WHERE Username=?";
        String sql2 = "UPDATE Employees SET Username=? WHERE Username=?";
        String sql3 = "UPDATE Penalties SET Username=? WHERE Username=?";
        try (Connection c = Connector.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement p1 = c.prepareStatement(sql1);
                 PreparedStatement p2 = c.prepareStatement(sql2);
                 PreparedStatement p3 = c.prepareStatement(sql3)) {
                p1.setString(1, newName); p1.setString(2, oldName); p1.executeUpdate();
                p2.setString(1, newName); p2.setString(2, oldName); p2.executeUpdate();
                p3.setString(1, newName); p3.setString(2, oldName); p3.executeUpdate();
            }
            c.commit();
            return Response.success("Логин изменён");
        } catch (SQLException e) {
            return Response.failure("Ошибка смены логина: " + e.getMessage());
        }
    }

    public Response getPassword(Request req) {
        String sql = "SELECT Password FROM Users WHERE Username=?";
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("username"));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Response.success(rs.getString("Password"))
                        : Response.failure("Пользователь не найден");
            }
        } catch (SQLException e) {
            return Response.failure("Ошибка БД: " + e.getMessage());
        }
    }

    public Response changePassword(Request req) {
        String sql = "UPDATE Users SET Password=? WHERE Username=?";
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("newPassword"));
            ps.setString(2, req.get("username"));
            return Response.success("Пароль обновлён: " + ps.executeUpdate());
        } catch (SQLException e) {
            return Response.failure("Ошибка смены пароля: " + e.getMessage());
        }
    }

    public Response userExists(Request req) {
        String sql = "SELECT 1 FROM Employees WHERE Username=?";
        try (Connection c = Connector.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.get("username"));
            try (ResultSet rs = ps.executeQuery()) {
                return Response.success(rs.next() ? "true" : "false");
            }
        } catch (SQLException e) {
            return Response.failure("Ошибка БД: " + e.getMessage());
        }
    }
}

package server;

import protocol.Request;
import protocol.Response;
import server.services.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Dispatcher zaprosov: Map<String, RequestHandler> vmesto if/else.
 * Kazhdyy action svyazan so svoim handlerom (chistaya funkcionalnaya korotkaya operatsiya).
 */
public final class RequestDispatcher {

    @FunctionalInterface
    public interface RequestHandler {
        Response handle(Request request);
    }

    private final Map<String, RequestHandler> handlers = new HashMap<>();

    public RequestDispatcher() {
        UserService users = new UserService();
        EmployeeService employees = new EmployeeService();
        SalaryService salaries = new SalaryService();
        PenaltyService penalties = new PenaltyService();
        MessageService messages = new MessageService();

        register("LOGIN", users::login);
        register("REGISTER", users::register);
        register("CHANGE_USERNAME", users::changeUsername);
        register("GET_PASSWORD", users::getPassword);
        register("CHANGE_PASSWORD", users::changePassword);
        register("USER_EXISTS", users::userExists);

        register("GET_EMPLOYEES", employees::list);
        register("ADD_EMPLOYEE", employees::add);
        register("SEARCH_EMPLOYEE", employees::search);
        register("DELETE_EMPLOYEE", employees::delete);
        register("DELETE_EMPLOYEE_BY_USERNAME", employees::deleteByUsername);
        register("UPDATE_EMPLOYEE_FIELD", employees::updateField);
        register("EMPLOYEE_PROFILE", employees::profile);

        register("POSITION_EXISTS", salaries::positionExists);
        register("ADD_SALARY", salaries::add);
        register("GET_SALARIES", salaries::list);
        register("SALARY_CHART", salaries::chartData);

        register("GET_PENALTIES", penalties::listByUser);
        register("GET_ALL_PENALTIES", penalties::listAll);
        register("PENALTY_EXISTS", penalties::existsForUser);
        register("ADD_PENALTY", penalties::add);
        register("DELETE_PENALTIES", penalties::deleteByUser);

        register("GET_MESSAGES", messages::list);
        register("GET_ALL_MESSAGES", messages::listAll);
        register("SEND_MESSAGE", messages::send);
        register("DELETE_MESSAGES", messages::delete);

        register("PING", r -> Response.success("pong"));
    }

    public void register(String action, RequestHandler handler) {
        handlers.put(action.toUpperCase(), handler);
    }

    public Response dispatch(Request request) {
        RequestHandler handler = handlers.get(request.getAction().toUpperCase());
        if (handler == null) {
            return Response.failure("Неизвестное действие: " + request.getAction());
        }
        try {
            return handler.handle(request);
        } catch (Exception e) {
            return Response.failure("Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    public Map<String, RequestHandler> getHandlers() {
        return Map.copyOf(handlers);
    }
}

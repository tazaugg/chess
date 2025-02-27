package handlers;

import com.google.gson.Gson;
import service.ClearService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

public class ClearHandler implements Route {
    @Override
    public Object handle(Request req, Response res) {
        ClearService service = new ClearService();
        service.clearAll();  // Call the service to clear everything

        res.status(200);  // HTTP 200 OK
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Database cleared successfully.");
        return new Gson().toJson(responseBody);
    }
}
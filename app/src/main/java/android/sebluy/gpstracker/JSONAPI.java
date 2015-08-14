package android.sebluy.gpstracker;

import org.json.JSONArray;

public class JSONAPI {

    public static void addPath(Path path, StatusListener listener) {
        try {
            JSONArray actions = singleActionCall(makeAction("add-path", path.toJSON()));
            send(actions, listener);
        } catch (Exception e) {
            listener.onStatusChanged("Error (" + e.getMessage() + ")");
        }
    }

    private static void send(JSONArray actions, StatusListener listener) {
        new Remote(listener).postAPI(actions.toString());
    }

    private static JSONArray makeAction(String action, JSONArray args) {
        JSONArray apiCall = new JSONArray();
        apiCall.put(action);
        apiCall.put(args);
        return apiCall;
    }

    private static JSONArray singleActionCall(JSONArray action) {
        JSONArray apiCallList = new JSONArray();
        apiCallList.put(action);
        return apiCallList;
    }

}



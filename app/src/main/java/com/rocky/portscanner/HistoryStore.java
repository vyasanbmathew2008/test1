package com.rocky.portscanner;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

final class HistoryStore {
    private final SharedPreferences prefs;
    HistoryStore(Context c) { prefs = c.getSharedPreferences("rocky_history", Context.MODE_PRIVATE); }

    List<JSONObject> getAll() {
        List<JSONObject> list = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(prefs.getString("items", "[]"));
            for (int i = a.length() - 1; i >= 0; i--) list.add(a.getJSONObject(i));
        } catch (Exception ignored) { }
        return list;
    }

    void add(ScanRecord record) {
        try {
            JSONArray old = new JSONArray(prefs.getString("items", "[]"));
            JSONArray next = new JSONArray();
            next.put(record.toJson());
            for (int i = 0; i < old.length() && i < 49; i++) next.put(old.getJSONObject(i));
            prefs.edit().putString("items", next.toString()).apply();
        } catch (Exception ignored) { }
    }
}

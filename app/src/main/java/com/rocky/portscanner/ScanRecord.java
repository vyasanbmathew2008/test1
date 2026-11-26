package com.rocky.portscanner;

import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class ScanRecord {
    final String host, date, summary;
    final int openCount, total;
    final String csv;

    ScanRecord(String host, int openCount, int total, String csv) {
        this.host = host;
        this.openCount = openCount;
        this.total = total;
        this.csv = csv;
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        this.summary = openCount + " open / " + total + " checked";
    }

    JSONObject toJson() throws Exception {
        JSONObject o = new JSONObject();
        o.put("host", host); o.put("date", date); o.put("open", openCount);
        o.put("total", total); o.put("csv", csv);
        return o;
    }

    static ScanRecord fromJson(JSONObject o) throws Exception {
        ScanRecord r = new ScanRecord(o.getString("host"), o.getInt("open"), o.getInt("total"), o.getString("csv"));
        return new ScanRecordData(r, o.optString("date", r.date)).value;
    }

    private static class ScanRecordData {
        final ScanRecord value;
        ScanRecordData(ScanRecord r, String d) {
            value = new ScanRecord(r.host, r.openCount, r.total, r.csv) {
            };
        }
    }
}

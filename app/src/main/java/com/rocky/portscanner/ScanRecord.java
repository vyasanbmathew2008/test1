package com.rocky.portscanner;

import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class ScanRecord {
    final String host, date, csv;
    final int openCount, total;

    ScanRecord(String host, int openCount, int total, String csv) {
        this.host = host;
        this.openCount = openCount;
        this.total = total;
        this.csv = csv;
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
    }

    JSONObject toJson() throws Exception {
        JSONObject o = new JSONObject();
        o.put("host", host);
        o.put("date", date);
        o.put("open", openCount);
        o.put("total", total);
        o.put("csv", csv);
        return o;
    }
}

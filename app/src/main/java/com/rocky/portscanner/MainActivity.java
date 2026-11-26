package com.rocky.portscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends Activity {
    private static final int CREATE_EXPORT = 71;
    private final int bg = 0xFF101820, panel = 0xFF1A2933, mint = 0xFF66E3C4, white = 0xFFF3F7F8, muted = 0xFF9EB0B8;
    private LinearLayout root, results;
    private EditText hostInput, portsInput;
    private TextView status, progressText, countText;
    private ProgressBar progress;
    private Button scanButton;
    private HistoryStore history;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> running;
    private volatile boolean cancelled;
    private final List<String[]> currentRows = new ArrayList<>();
    private String currentHost = "";
    private int openCount = 0;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        history = new HistoryStore(this);
        buildHome();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(18), dp(12), dp(18), dp(12)); return l;
    }
    private Button button(String label) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextColor(white); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel)); return b;
    }
    private void base(String title) {
        root = column(); root.setBackgroundColor(bg);
        TextView brand = text("ROCKY  /  NETWORK TOOLS", 12, mint); brand.setLetterSpacing(.12f);
        root.addView(brand);
        TextView heading = text(title, 27, white); heading.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2); hp.topMargin = dp(8); root.addView(heading, hp);
        setContentView(root);
    }
    private void buildHome() {
        base("Port Scanner");
        TextView intro = text("TCP connect checks • Fast • Simple", 14, muted);
        root.addView(intro);
        addGap(root, 14);
        TextView hostLabel = text("TARGET HOST", 12, mint); root.addView(hostLabel);
        hostInput = new EditText(this); hostInput.setSingleLine(true); hostInput.setTextColor(white); hostInput.setHintTextColor(muted); hostInput.setHint("example.com or 192.168.1.1"); hostInput.setText("127.0.0.1"); hostInput.setPadding(dp(12), dp(10), dp(12), dp(10)); hostInput.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mint)); root.addView(hostInput);
        addGap(root, 12);
        TextView portsLabel = text("CUSTOM PORTS / RANGES", 12, mint); root.addView(portsLabel);
        portsInput = new EditText(this); portsInput.setSingleLine(true); portsInput.setTextColor(white); portsInput.setHintTextColor(muted); portsInput.setHint("22,80,443,8000-8100"); portsInput.setPadding(dp(12), dp(10), dp(12), dp(10)); portsInput.setBackgroundTintList(android.content.res.ColorStateList.valueOf(mint)); root.addView(portsInput);
        addGap(root, 8);
        TextView hint = text("Up to 4,096 ports per scan. Timeout: 900 ms.", 12, muted); root.addView(hint);
        LinearLayout actions = new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL);
        Button common = button("Common ports"); common.setOnClickListener(v -> startScan(PortScanner.commonPorts()));
        Button custom = button("Scan custom"); custom.setOnClickListener(v -> {
            try { startScan(PortScanner.parsePorts(portsInput.getText().toString())); }
            catch (Exception e) { toast(e.getMessage()); }
        });
        actions.addView(common, new LinearLayout.LayoutParams(0, -2, 1)); actions.addView(custom, new LinearLayout.LayoutParams(0, -2, 1)); root.addView(actions);
        scanButton = button("Stop scan"); scanButton.setVisibility(View.GONE); scanButton.setOnClickListener(v -> stopScan()); root.addView(scanButton);
        addGap(root, 12);
        status = text("Ready when you are.", 14, muted); root.addView(status);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal); progress.setMax(100); root.addView(progress, new LinearLayout.LayoutParams(-1, dp(6)));
        progressText = text("", 12, muted); root.addView(progressText);
        countText = text("", 14, mint); countText.setTypeface(null, Typeface.BOLD); root.addView(countText);
        addGap(root, 10);
        LinearLayout nav = new LinearLayout(this);
        Button historyButton = button("History"); historyButton.setOnClickListener(v -> showHistory());
        Button exportButton = button("Export latest"); exportButton.setOnClickListener(v -> exportLatest());
        nav.addView(historyButton, new LinearLayout.LayoutParams(0, -2, 1)); nav.addView(exportButton, new LinearLayout.LayoutParams(0, -2, 1)); root.addView(nav);
        ScrollView scroll = new ScrollView(this); results = column(); results.setPadding(0, dp(8), 0, dp(8)); scroll.addView(results);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, 0, 1); root.addView(scroll, sp);
        addResult("No results yet. Open ports will appear here.");
    }

    private void addGap(LinearLayout parent, int h) { View v = new View(this); parent.addView(v, new LinearLayout.LayoutParams(1, dp(h))); }
    private void addResult(String line) {
        TextView t = text(line, 14, white); t.setPadding(dp(12), dp(10), dp(12), dp(10)); t.setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel)); results.addView(t);
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }

    private void startScan(List<Integer> ports) {
        String host = hostInput.getText().toString().trim();
        if (host.isEmpty() || host.contains(" ")) { toast("Enter a valid hostname or IP address."); return; }
        if (running != null && !running.isDone()) { toast("A scan is already running."); return; }
        currentHost = host; currentRows.clear(); openCount = 0; cancelled = false;
        results.removeAllViews(); status.setText("Scanning " + host); progress.setProgress(0);
        progressText.setText("0 / " + ports.size()); countText.setText("Open: 0");
        scanButton.setVisibility(View.VISIBLE);
        final List<Integer> scanPorts = new ArrayList<>(ports);
        running = executor.submit(() -> {
            try {
                PortScanner.scan(host, scanPorts, 900, new PortScanner.Listener() {
                    @Override public void onProgress(int done, int total) {
                        runOnUiThread(() -> { progress.setMax(total); progress.setProgress(done); progressText.setText(done + " / " + total); });
                    }
                    @Override public void onPort(int port, boolean open, String service) {
                        synchronized (currentRows) { currentRows.add(new String[]{String.valueOf(port), service, open ? "OPEN" : "NOT OPEN"}); }
                        if (open) {
                            runOnUiThread(() -> { openCount++; addResult("●  " + port + "   " + service + "   OPEN"); countText.setText("Open: " + openCount); });
                        }
                    }
                });
                String csv = makeCsv();
                if (!cancelled) {
                    history.add(new ScanRecord(host, countOpenRows(), scanPorts.size(), csv));
                    runOnUiThread(() -> { status.setText("Scan complete"); scanButton.setVisibility(View.GONE); if (openCount == 0) addResult("No open ports found in this scan."); });
                }
            } catch (Exception e) {
                runOnUiThread(() -> status.setText("Scan stopped or interrupted."));
            } finally {
                runOnUiThread(() -> scanButton.setVisibility(View.GONE));
            }
        });
    }

    private int countOpenRows() {
        int count = 0;
        synchronized (currentRows) {
            for (String[] row : currentRows) if ("OPEN".equals(row[2])) count++;
        }
        return count;
    }

    private String makeCsv() {
        StringBuilder b = new StringBuilder("host,port,service,state\n");
        synchronized (currentRows) {
            for (String[] r : currentRows) b.append(csv(currentHost)).append(',').append(r[0]).append(',').append(csv(r[1])).append(',').append(r[2]).append('\n');
        }
        return b.toString();
    }
    private String csv(String s) { return "\"" + s.replace("\"", "\"\"") + "\""; }

    private void stopScan() {
        cancelled = true;
        if (running != null) running.cancel(true);
        status.setText("Stopping scan…");
        scanButton.setVisibility(View.GONE);
    }

    private void showHistory() {
        base("Scan History");
        Button back = button("← Back to scanner"); back.setOnClickListener(v -> buildHome()); root.addView(back);
        ScrollView scroll = new ScrollView(this); LinearLayout list = column(); scroll.addView(list); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        List<JSONObject> records = history.getAll();
        if (records.isEmpty()) list.addView(text("No saved scans yet.", 15, muted));
        for (JSONObject o : records) {
            String host = o.optString("host", "Unknown");
            String date = o.optString("date", "");
            String summary = o.optInt("open") + " open / " + o.optInt("total") + " checked";
            Button item = button(host + "\n" + date + "  •  " + summary);
            item.setOnClickListener(v -> requestExport(o.optString("csv", ""), "rocky-" + host.replaceAll("[^A-Za-z0-9.-]", "_") + ".csv"));
            list.addView(item);
        }
        TextView note = text("Tap a scan to export its CSV report.", 12, muted); list.addView(note);
    }

    private void exportLatest() {
        String csv = makeCsv();
        if (currentRows.isEmpty()) {
            List<JSONObject> saved = history.getAll();
            if (saved.isEmpty()) { toast("Complete a scan before exporting."); return; }
            JSONObject last = saved.get(0); requestExport(last.optString("csv", ""), "rocky-" + last.optString("host", "scan") + ".csv");
        } else requestExport(csv, "rocky-" + currentHost.replaceAll("[^A-Za-z0-9.-]", "_") + ".csv");
    }

    private void requestExport(String csv, String filename) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        pendingCsv = csv;
        startActivityForResult(intent, CREATE_EXPORT);
    }
    private String pendingCsv = "";
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_EXPORT && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                out.write(pendingCsv.getBytes(StandardCharsets.UTF_8));
                toast("CSV exported successfully.");
            } catch (Exception e) { toast("Export failed: " + e.getMessage()); }
        }
    }

    @Override protected void onDestroy() {
        stopScan();
        executor.shutdownNow();
        super.onDestroy();
    }
}

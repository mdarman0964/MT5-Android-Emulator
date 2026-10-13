package com.winlator.cmod;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** In-app issue notes, Android logcat viewer, and app-accessible lock-file inspector. */
public class DebugCenterFragment extends Fragment {
    private LinearLayout content;
    private String currentSection = "issues";
    private static final String PREFS = "ras_debug_center";
    private static final String ISSUES_KEY = "issues_json";

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ScrollView scroll = new ScrollView(requireContext());
        scroll.setFillViewport(true);
        content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        content.setPadding(pad, pad, pad, pad);
        scroll.addView(content);
        render();
        return scroll;
    }

    private void render() {
        content.removeAllViews();
        addText("DEBUG CENTER", 23, true);
        addText("Issue notes • Android logs • Lock-file inspection", 13, false);
        LinearLayout nav = new LinearLayout(requireContext());
        nav.setOrientation(LinearLayout.HORIZONTAL);
        addButton(nav, "Issues", () -> { currentSection = "issues"; render(); });
        addButton(nav, "Logs", () -> { currentSection = "logs"; render(); });
        addButton(nav, "Lock files", () -> { currentSection = "locks"; render(); });
        content.addView(nav);
        addDivider();
        if ("issues".equals(currentSection)) renderIssues();
        else if ("logs".equals(currentSection)) renderLogs();
        else renderLocks();
    }

    private void renderIssues() {
        addText("Issue history", 19, true);
        addText("Notes are saved locally on this device and remain available after restarting the app.", 14, false);
        addButton(content, "+ Record a problem", this::showAddIssueDialog);
        JSONArray issues = readIssues();
        if (issues.length() == 0) {
            addText("No issues recorded yet.", 15, false);
            return;
        }
        for (int i = issues.length() - 1; i >= 0; i--) {
            JSONObject issue = issues.optJSONObject(i);
            if (issue == null) continue;
            String title = issue.optString("title", "Untitled issue");
            String description = issue.optString("description", "");
            String severity = issue.optString("severity", "Normal");
            String status = issue.optString("status", "Open");
            String date = issue.optString("date", "");
            addCardText(title + "\n" + severity + " • " + status + "\n" + date + "\n" + description);
            LinearLayout actions = new LinearLayout(requireContext());
            actions.setOrientation(LinearLayout.HORIZONTAL);
            final int index = i;
            addButton(actions, "Change status", () -> showStatusDialog(index));
            addButton(actions, "Delete", () -> confirmDelete(index));
            content.addView(actions);
        }
        addButton(content, "Export issue notes", this::shareIssues);
    }

    private void showAddIssueDialog() {
        LinearLayout form = new LinearLayout(requireContext());
        form.setPadding(dp(8), dp(4), dp(8), dp(4));
        form.setOrientation(LinearLayout.VERTICAL);
        EditText title = new EditText(requireContext());
        title.setHint("Problem title");
        title.setSingleLine(true);
        form.addView(title);
        EditText description = new EditText(requireContext());
        description.setHint("What happened? Steps to reproduce, error message…");
        description.setMinLines(3);
        description.setGravity(android.view.Gravity.TOP);
        form.addView(description);
        final String[] severity = {"Normal"};
        Button severityButton = new Button(requireContext());
        severityButton.setText("Severity: Normal");
        severityButton.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Choose severity")
                .setItems(new String[]{"Low", "Normal", "High", "Critical"}, (d, which) -> {
                    severity[0] = new String[]{"Low", "Normal", "High", "Critical"}[which];
                    severityButton.setText("Severity: " + severity[0]);
                }).show());
        form.addView(severityButton);
        new AlertDialog.Builder(requireContext())
                .setTitle("Record a problem")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String t = title.getText().toString().trim();
                    if (t.isEmpty()) {
                        Toast.makeText(requireContext(), "Enter a problem title.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONArray list = readIssues();
                    JSONObject item = new JSONObject();
                    try {
                        item.put("title", t);
                        item.put("description", description.getText().toString().trim());
                        item.put("severity", severity[0]);
                        item.put("status", "Open");
                        item.put("date", DateFormat.getDateTimeInstance().format(new Date()));
                        list.put(item);
                        saveIssues(list);
                        render();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Could not save the issue.", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private void showStatusDialog(int index) {
        new AlertDialog.Builder(requireContext()).setTitle("Issue status")
                .setItems(new String[]{"Open", "In Progress", "Resolved"}, (d, which) -> {
                    JSONArray list = readIssues();
                    JSONObject item = list.optJSONObject(index);
                    if (item != null) {
                        try { item.put("status", new String[]{"Open", "In Progress", "Resolved"}[which]); }
                        catch (Exception ignored) {}
                        saveIssues(list);
                        render();
                    }
                }).show();
    }

    private void confirmDelete(int index) {
        new AlertDialog.Builder(requireContext()).setTitle("Delete issue?")
                .setMessage("This removes the saved issue note from this device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    JSONArray old = readIssues(), next = new JSONArray();
                    for (int i = 0; i < old.length(); i++) if (i != index) next.put(old.opt(i));
                    saveIssues(next);
                    render();
                }).show();
    }

    private void renderLogs() {
        addText("Log Viewer", 19, true);
        addText("Reads Android logcat available to this app. Some system or other-app messages may be restricted by Android.", 14, false);
        addButton(content, "Load recent logs", this::loadLogs);
        addButton(content, "Copy displayed logs", () -> {
            CharSequence text = content.getTag() instanceof CharSequence ? (CharSequence) content.getTag() : "";
            if (text.length() == 0) { toast("Load logs first."); return; }
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Android logs", text));
            toast("Logs copied.");
        });
        TextView hint = addText("Tap “Load recent logs” to read the latest available entries.", 14, false);
        content.setTag("");
    }

    private void loadLogs() {
        StringBuilder out = new StringBuilder();
        try {
            Process process = new ProcessBuilder("logcat", "-d", "-t", "500", "-v", "threadtime").redirectErrorStream(true).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) out.append(line).append('\n');
            reader.close();
            process.waitFor();
        } catch (Exception e) {
            out.append("Unable to read logcat: ").append(e.getClass().getSimpleName()).append(": ").append(e.getMessage())
                    .append("\nAndroid may restrict access to system logs on this device.");
        }
        content.setTag(out.toString());
        renderLogsResult(out.toString());
    }

    private void renderLogsResult(String logs) {
        content.removeAllViews();
        addText("DEBUG CENTER", 23, true);
        addText("Log Viewer • recent available entries", 14, false);
        addButton(content, "Issues", () -> { currentSection = "issues"; render(); });
        addButton(content, "Lock files", () -> { currentSection = "locks"; render(); });
        addButton(content, "Refresh logs", this::loadLogs);
        addButton(content, "Copy logs", () -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Android logs", logs));
            toast("Logs copied.");
        });
        addDivider();
        addCardText(logs.isEmpty() ? "No log output available." : logs);
    }

    private void renderLocks() {
        addText("Lock File Inspector", 19, true);
        addText("Scans app-accessible internal files and app-specific external storage for names containing “lock” or ending in .lck / .lock. It does not bypass Android storage permissions.", 14, false);
        addButton(content, "Scan accessible files", this::scanLocks);
        addText("Tip: lock files can indicate a running process or stale state. Do not delete them unless you know what owns them.", 14, false);
    }

    private void scanLocks() {
        List<File> matches = new ArrayList<>();
        collectLocks(requireContext().getFilesDir(), matches, 5);
        collectLocks(requireContext().getCacheDir(), matches, 5);
        File external = requireContext().getExternalFilesDir(null);
        if (external != null) collectLocks(external, matches, 5);
        content.removeAllViews();
        addText("DEBUG CENTER", 23, true);
        addText("Lock File Inspector", 19, true);
        addButton(content, "Issues", () -> { currentSection = "issues"; render(); });
        addButton(content, "Logs", () -> { currentSection = "logs"; render(); });
        addButton(content, "Rescan", this::scanLocks);
        addDivider();
        if (matches.isEmpty()) addText("No matching lock files found in the locations this app can inspect.", 15, false);
        for (File f : matches) {
            addCardText(f.getAbsolutePath() + "\n" + readableSize(f.length()) + " • modified " + DateFormat.getDateTimeInstance().format(new Date(f.lastModified())));
            addButton(content, "View details", () -> new AlertDialog.Builder(requireContext())
                    .setTitle("Lock file details")
                    .setMessage("Path: " + f.getAbsolutePath() + "\nSize: " + f.length() + " bytes\nReadable: " + f.canRead() + "\nWritable: " + f.canWrite())
                    .setPositiveButton("OK", null).show());
        }
    }

    private void collectLocks(File dir, List<File> matches, int depth) {
        if (dir == null || depth < 0 || !dir.isDirectory() || !dir.canRead()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) collectLocks(file, matches, depth - 1);
            else {
                String name = file.getName().toLowerCase(Locale.ROOT);
                if (name.contains("lock") || name.endsWith(".lck")) matches.add(file);
            }
        }
    }

    private JSONArray readIssues() {
        String raw = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ISSUES_KEY, "[]");
        try { return new JSONArray(raw); } catch (Exception e) { return new JSONArray(); }
    }

    private void saveIssues(JSONArray issues) {
        requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(ISSUES_KEY, issues.toString()).apply();
    }

    private void shareIssues() {
        String text = readIssues().toString(2);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "MT5 Android Emulator - Issue Notes");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Export issue notes"));
    }

    private TextView addText(String value, int size, boolean bold) {
        TextView text = new TextView(requireContext());
        text.setText(value);
        text.setTextSize(size);
        text.setPadding(dp(4), dp(7), dp(4), dp(7));
        if (bold) text.setTypeface(null, android.graphics.Typeface.BOLD);
        content.addView(text);
        return text;
    }

    private void addCardText(String value) {
        TextView text = addText(value, 14, false);
        text.setBackgroundColor(0x142196F3);
        text.setPadding(dp(12), dp(12), dp(12), dp(12));
    }

    private void addButton(LinearLayout parent, String label, Runnable action) {
        Button button = new Button(requireContext());
        button.setText(label);
        button.setOnClickListener(v -> action.run());
        parent.addView(button, new LinearLayout.LayoutParams(-2, -2));
    }

    private void addDivider() {
        View line = new View(requireContext());
        line.setBackgroundColor(0x443F51B5);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(1));
        lp.setMargins(0, dp(8), 0, dp(8));
        content.addView(line, lp);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String readableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}

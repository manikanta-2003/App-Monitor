package com.example.devicemonitor;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devicemonitor.AppAdapter;
import com.example.devicemonitor.AppModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


public class MainActivity extends AppCompatActivity {

    private List<AppModel> allAppsList = new ArrayList<>();
    private List<AppModel> displayList = new ArrayList<>();
    private AppAdapter adapter;

    private TextView txtRootStatus, txtPlayStatus, txtFridaStatus;
    private LinearProgressIndicator progressBar;
    private int currentTab = 0; // 0: All, 1: 3rd Party, 2: System
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Edge-to-Edge Padding
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize UI
        txtRootStatus = findViewById(R.id.txtRootStatus);
        txtPlayStatus = findViewById(R.id.txtPlayStatus);
        progressBar = findViewById(R.id.progressBar);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        SearchView searchView = findViewById(R.id.searchView);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ImageButton btnRefresh = findViewById(R.id.btnRefresh);
        ImageButton btnTerminal = findViewById(R.id.btnTerminal);
        txtFridaStatus = findViewById(R.id.txtFridaStatus);

        // Adapter Setup
        adapter = new AppAdapter(this, displayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        performSecurityScan();

        // Initial Scan
        startScan();

        // 🔄 Refresh Button - Top Left
        btnRefresh.setOnClickListener(v -> startScan());

        // 🖥️ Terminal Button - Top Right
        if (btnTerminal != null) {
            btnTerminal.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CommandExecutorActivity.class);
                startActivity(intent);
            });
        }

        // Tab Filtering
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                refreshDisplay(searchView.getQuery().toString());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Search Logic
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                refreshDisplay(newText);
                return true;
            }
        });


    }

    private void startScan() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        executorService.execute(() -> {
            new Thread(() -> {
                checkDeviceIntegrity();
            }).start();
            new Thread(() -> {
                loadInstalledApps();
            }).start();

            
            // Artificial delay to show loading if scan is too fast
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

            mainHandler.post(() -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                refreshDisplay("");
            });
        });
    }

    private void checkDeviceIntegrity() {
        boolean isRooted = isRooted();
        boolean hasPlayServices = isPackageInstalled("com.google.android.gms");

        mainHandler.post(() -> {
            txtRootStatus.setText(isRooted ? "ROOTED / INSECURE" : "UNROOTED / SECURE");
            txtRootStatus.setTextColor(isRooted ? Color.parseColor("#D93025") : Color.parseColor("#1E8E3E"));

            txtPlayStatus.setText(hasPlayServices ? "AVAILABLE" : "MISSING");
            txtPlayStatus.setTextColor(hasPlayServices ? Color.parseColor("#1E8E3E") : Color.parseColor("#F9AB00"));
        });
    }

    private boolean isRooted() {
        // 1. Build Tags Check
        String buildTags = Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) return true;

        // 2. Common Root Binaries Check
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/system/xbin/my_root",
                          "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                          "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }

        // 3. System Properties Check
        if (getSystemProperty("ro.secure").equals("0")) return true;
        if (getSystemProperty("ro.debuggable").equals("1")) return true;

        Log.d("SystemProperty", "Property check: " + getSystemProperty("ro.build.id"));

        String buildType = Build.TYPE;
        if (buildType != null && (buildType.contains("dev") || buildType.contains("eng"))) return true;

        return false;
    }

    private String getSystemProperty(String propName) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("getprop " + propName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            Log.d("SystemProperty", "Line: for property: " + propName + " is " + line);
            return line != null ? line : "";
        } catch (Exception e) {
            return "";
        } finally {
            if (process != null) process.destroy();
        }
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        allAppsList.clear();

        for (ApplicationInfo appInfo : packages) {
            boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            String source = getInstallerSource(pm, appInfo.packageName, isSystem);

            allAppsList.add(new AppModel(
                    appInfo.loadLabel(pm).toString(),
                    appInfo.packageName,
                    appInfo.loadIcon(pm),
                    isSystem,
                    source
            ));
        }
    }

    private String getInstallerSource(PackageManager pm, String pkg, boolean isSystem) {
        if (isSystem) return "System Pre-installed";
        try {
            String installer;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                InstallSourceInfo info = pm.getInstallSourceInfo(pkg);
                installer = info.getInstallingPackageName();
            } else {
                installer = pm.getInstallerPackageName(pkg);
            }

            if (installer == null) return "Sideloaded";
            if (installer.equals("com.android.vending")) return "Google Play Store";
            return "Installer: " + installer;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void refreshDisplay(String query) {
        displayList.clear();
        String q = (query != null) ? query.toLowerCase() : "";
        for (AppModel app : allAppsList) {
            boolean categoryMatch = (currentTab == 0) || (currentTab == 1 && !app.isSystemApp) || (currentTab == 2 && app.isSystemApp);
            boolean searchMatch = app.appName.toLowerCase().contains(q) || app.packageName.toLowerCase().contains(q);
            if (categoryMatch && searchMatch) displayList.add(app);
        }
        mainHandler.post(() -> adapter.updateData(displayList));
    }
    private boolean detectFridaListener() {
        // We scan common Frida ports first to be fast, then a wider range
        int[] commonPorts = {27042, 27043, 27047};

        for (int port : commonPorts) {
            if (checkPortForFrida(port)) return true;
        }

        // Optional: Scan a wider range (e.g., 27000 to 28000)
        // Scanning all 65535 ports in Java is slow; 27000-28000 is usually enough.
        for (int i = 0; i <= 65535; i++) {
            if (i == 27042 || i == 27043 || i == 27047) continue; // skip already checked
            if (checkPortForFrida(i)) return true;
        }

        return false;
    }

    private boolean checkPortForFrida(int port) {
        try (Socket socket = new Socket()) {
            // Set a very short timeout so the scan doesn't take forever
            socket.connect(new InetSocketAddress("127.0.0.1", port), 50);

            OutputStream os = socket.getOutputStream();
            // The specific WebSocket handshake request provided in your C++ logic
            String req = "GET /ws HTTP/1.1\r\n" +
                    "Upgrade: websocket\r\n" +
                    "Connection: Upgrade\r\n" +
                    "Sec-WebSocket-Key: CpxD2C5REVLHvsUC9YAoqg==\r\n" +
                    "Sec-WebSocket-Version: 13\r\n" +
                    "Host: 127.0.0.1:" + port + "\r\n" +
                    "User-Agent: Frida/16.1.7\r\n\r\n";

            os.write(req.getBytes());
            os.flush();

            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = is.read(buffer);

            if (bytesRead > 0) {
                String response = new String(buffer, 0, bytesRead);
                // This is the static fingerprint Frida returns for that specific key
                if (response.contains("tyZql/Y8dNFFyopTrHadWzvbvRs=")) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Port closed or connection refused, ignore and move on
        }
        return false;
    }
    private boolean checkFridaFiles() {
        String[] paths = {
                "/data/local/tmp/frida-server",
                "/data/local/tmp/re.frida.server",
                "/usr/bin/frida-server",
                "/data/local/frida-server",
                "/data/local/tmp/frida"
        };
        for (String path : paths) {
            Log.d("Frida", "Checking file: " + path);
            if (new File(path).exists()) return true;
        }
        return false;
    }

    // 2. Check for Frida's default listening port (27042)
    private boolean checkFridaPort() {
        // This must be run on a background thread to avoid NetworkOnMainThreadException
        try (Socket socket = new Socket("localhost", 27042)) {
            return true; // If connection succeeds, Frida server is active
        } catch (Exception e) {
            return false;
        }
    }

    // 3. Scan /proc/self/maps for injected libraries (Most reliable)
    private boolean checkFridaThreads() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/self/maps"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("frida") || line.contains("gum-js") || line.contains("gadget")) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Log error or ignore
        }
        return false;
    }
    private void performSecurityScan() {
        // Show the progress bar from your layout
        progressBar.setVisibility(View.VISIBLE);
        txtFridaStatus.setText("Scanning...");

        new Thread(() -> {
            // 1. Run all Frida and instrumentation checks
            boolean fridaFound = detectFridaListener() || checkFridaFiles() || checkFridaPort() || checkFridaThreads();

            // 2. Check for Developer Options
            boolean devOptionsEnabled = isDeveloperOptionsEnabled();

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);

                // --- Frida Status UI ---
                if (fridaFound) {
                    txtFridaStatus.setText("DETECTED");
                    txtFridaStatus.setTextColor(android.graphics.Color.RED);
                } else {
                    txtFridaStatus.setText("SECURE");
                    txtFridaStatus.setTextColor(android.graphics.Color.parseColor("#1E8E3E"));
                    //txtFridaStatus.setTextColor(Color.GREEN);
                }

                // --- Developer Options Toast ---
                if (devOptionsEnabled) {
                    Toast.makeText(MainActivity.this,
                            "Security Warning: Please disable Developer Options for better security.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
    private boolean isDeveloperOptionsEnabled() {
        return android.provider.Settings.Global.getInt(
                getContentResolver(),
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
    }
}
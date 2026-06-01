package com.example.devicemonitor;

import android.content.pm.ApplicationInfo;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.PermissionModel;

import java.util.ArrayList;
import java.util.List;

public class AppDetailsActivity extends AppCompatActivity {

    ImageView imgAppIcon;
    TextView txtAppName;
    TextView txtPackageName;
    TextView txtBasicInfo;
    RecyclerView recyclerNormalPermissions;
    RecyclerView recyclerDangerousPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        imgAppIcon = findViewById(R.id.imgAppIcon);
        txtAppName = findViewById(R.id.txtAppName);
        txtPackageName = findViewById(R.id.txtPackageName);
        txtBasicInfo = findViewById(R.id.txtBasicInfo);
        recyclerNormalPermissions = findViewById(R.id.recyclerNormalPermissions);
        recyclerDangerousPermissions = findViewById(R.id.recyclerDangerousPermissions);

        String packageName = getIntent().getStringExtra("packageName");

        loadAppDetails(packageName);
    }

    private void loadAppDetails(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            ApplicationInfo appInfo = packageInfo.applicationInfo;

            Drawable icon = appInfo.loadIcon(pm);
            imgAppIcon.setImageDrawable(icon);
            txtAppName.setText(appInfo.loadLabel(pm).toString());
            txtPackageName.setText(packageName);

            // Determine Installation Source
            boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            String installSource = getInstallSource(pm, packageName, isSystem);

            // Basic Information
            String basicInfo =
                    "Installation Source : " + installSource + "\n\n" +
                    "Version Name : " + packageInfo.versionName + "\n\n" +
                    "Version Code : " + packageInfo.getLongVersionCode() + "\n\n" +
                    "Target SDK : " + appInfo.targetSdkVersion + "\n\n" +
                    "UID : " + appInfo.uid + "\n\n" +
                    "APK Path : \n" + appInfo.sourceDir + "\n\n" +
                    "Data Directory : \n" + appInfo.dataDir;

            txtBasicInfo.setText(basicInfo);

            // Process Permissions
            setupPermissions(packageInfo);

        } catch (Exception e) {
            txtBasicInfo.setText("Error: " + e.getMessage());
        }
    }

    private String getInstallSource(PackageManager pm, String pkg, boolean isSystem) {
        if (isSystem) return "System (Pre-installed)";
        try {
            String installer;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                InstallSourceInfo info = pm.getInstallSourceInfo(pkg);
                installer = info.getInstallingPackageName();
            } else {
                installer = pm.getInstallerPackageName(pkg);
            }

            if (installer == null) return "Sideloaded (Manual APK)";
            if (installer.equals("com.android.vending")) return "Google Play Store";
            return "Installer: " + installer;
        } catch (Exception e) {
            return "Unknown Source";
        }
    }

    private void setupPermissions(PackageInfo packageInfo) {
        String[] permissions = packageInfo.requestedPermissions;
        List<PermissionModel> normalPermissions = new ArrayList<>();
        List<PermissionModel> dangerousPermissions = new ArrayList<>();

        if (permissions != null) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                boolean granted = false;

                if (packageInfo.requestedPermissionsFlags != null) {
                    granted = (packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0;
                }

                boolean dangerous = isDangerousPermission(permission);
                PermissionModel model = new PermissionModel(permission, granted, dangerous);

                if (dangerous) dangerousPermissions.add(model);
                else normalPermissions.add(model);
            }
        }

        recyclerNormalPermissions.setLayoutManager(new LinearLayoutManager(this));
        recyclerNormalPermissions.setAdapter(new PermissionAdapter(normalPermissions));

        recyclerDangerousPermissions.setLayoutManager(new LinearLayoutManager(this));
        recyclerDangerousPermissions.setAdapter(new PermissionAdapter(dangerousPermissions));
    }

    private boolean isDangerousPermission(String permission) {
        return  permission.equals(android.Manifest.permission.CAMERA)
                || permission.equals(android.Manifest.permission.ACCESS_FINE_LOCATION)
                || permission.equals(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                || permission.equals(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                || permission.equals(android.Manifest.permission.READ_CONTACTS)
                || permission.equals(android.Manifest.permission.WRITE_CONTACTS)
                || permission.equals(android.Manifest.permission.GET_ACCOUNTS)
                || permission.equals(android.Manifest.permission.READ_CALENDAR)
                || permission.equals(android.Manifest.permission.WRITE_CALENDAR)
                || permission.equals(android.Manifest.permission.RECORD_AUDIO)
                || permission.equals(android.Manifest.permission.READ_PHONE_STATE)
                || permission.equals(android.Manifest.permission.READ_PHONE_NUMBERS)
                || permission.equals(android.Manifest.permission.CALL_PHONE)
                || permission.equals(android.Manifest.permission.ANSWER_PHONE_CALLS)
                || permission.equals(android.Manifest.permission.ADD_VOICEMAIL)
                || permission.equals(android.Manifest.permission.USE_SIP)
                || permission.equals(android.Manifest.permission.PROCESS_OUTGOING_CALLS)
                || permission.equals(android.Manifest.permission.READ_CALL_LOG)
                || permission.equals(android.Manifest.permission.WRITE_CALL_LOG)
                || permission.equals(android.Manifest.permission.SEND_SMS)
                || permission.equals(android.Manifest.permission.RECEIVE_SMS)
                || permission.equals(android.Manifest.permission.READ_SMS)
                || permission.equals(android.Manifest.permission.RECEIVE_WAP_PUSH)
                || permission.equals(android.Manifest.permission.RECEIVE_MMS)
                || permission.equals(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                || permission.equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || permission.equals(android.Manifest.permission.READ_MEDIA_IMAGES)
                || permission.equals(android.Manifest.permission.READ_MEDIA_VIDEO)
                || permission.equals(android.Manifest.permission.READ_MEDIA_AUDIO)
                || permission.equals(android.Manifest.permission.POST_NOTIFICATIONS)
                || permission.equals(android.Manifest.permission.BODY_SENSORS)
                || permission.equals(android.Manifest.permission.BODY_SENSORS_BACKGROUND)
                || permission.equals(android.Manifest.permission.ACTIVITY_RECOGNITION)
                || permission.equals(android.Manifest.permission.BLUETOOTH_SCAN)
                || permission.equals(android.Manifest.permission.BLUETOOTH_CONNECT)
                || permission.equals(android.Manifest.permission.BLUETOOTH_ADVERTISE)
                || permission.equals(android.Manifest.permission.NEARBY_WIFI_DEVICES)
                || permission.equals(android.Manifest.permission.UWB_RANGING);
    }
}

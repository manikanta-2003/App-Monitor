package com.example.devicemonitor;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppViewHolder extends RecyclerView.ViewHolder {

    ImageView imgIcon;
    TextView txtAppName;
    TextView txtPackageName;
    TextView txtInstallSource;

    public AppViewHolder(@NonNull View itemView) {
        super(itemView);

        imgIcon = itemView.findViewById(R.id.imgIcon);
        txtAppName = itemView.findViewById(R.id.txtAppName);
        txtPackageName = itemView.findViewById(R.id.txtPackageName);
        txtInstallSource = itemView.findViewById(R.id.txtInstallSource);
    }
}

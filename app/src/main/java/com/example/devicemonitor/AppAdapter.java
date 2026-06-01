package com.example.devicemonitor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppViewHolder> {

    private Context context;
    private List<AppModel> appList;

    public AppAdapter(Context context, List<AppModel> appList) {
        this.context = context;
        this.appList = appList;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppModel app = appList.get(position);

        holder.txtAppName.setText(app.appName);
        holder.txtPackageName.setText(app.packageName);
        holder.txtInstallSource.setText(app.installSource);
        holder.imgIcon.setImageDrawable(app.appIcon);

        // Color coding for Installation Source
        String source = app.installSource.toLowerCase();
        if (source.contains("system")) {
            // System -> Blue
            holder.txtInstallSource.setTextColor(Color.parseColor("#1A73E8"));
        } else if (source.contains("play store")) {
            // Play Store -> Green
            holder.txtInstallSource.setTextColor(Color.parseColor("#2E7D32"));
        } else if (source.contains("sideloaded")) {
            // Sideloaded -> Red
            holder.txtInstallSource.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            holder.txtInstallSource.setTextColor(Color.parseColor("#5F6368"));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AppDetailsActivity.class);
            intent.putExtra("packageName", app.packageName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public void updateData(List<AppModel> newList) {
        appList = newList;
        notifyDataSetChanged();
    }
}

package com.example.devicemonitor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.PermissionModel;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PermissionAdapter
        extends RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder> {

    List<PermissionModel> permissionList;

    public PermissionAdapter(List<PermissionModel> permissionList) {
        this.permissionList = permissionList;
    }

    @NonNull
    @Override
    public PermissionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.permission_item, parent, false);

        return new PermissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PermissionViewHolder holder,
            int position) {

        PermissionModel permission = permissionList.get(position);

        holder.txtPermission.setText(permission.permissionName);

        if (permission.isDangerous) {
            holder.txtType.setText("Dangerous Permission");
        } else {
            holder.txtType.setText("Normal Permission");
        }

        if (permission.isGranted) {
            holder.txtStatus.setText("GRANTED");
            // Set text color to Green
            holder.txtStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.cardView.setStrokeColor(Color.parseColor("#2E7D32"));
        } else {
            holder.txtStatus.setText("NOT GRANTED");
            // Set text color to Red
            holder.txtStatus.setTextColor(Color.parseColor("#D32F2F"));
            holder.cardView.setStrokeColor(Color.parseColor("#D32F2F"));
        }
    }

    @Override
    public int getItemCount() {
        return permissionList.size();
    }

    class PermissionViewHolder extends RecyclerView.ViewHolder {

        TextView txtPermission;
        TextView txtType;
        TextView txtStatus;

        MaterialCardView cardView;

        public PermissionViewHolder(@NonNull View itemView) {
            super(itemView);

            txtPermission = itemView.findViewById(R.id.txtPermission);
            txtType = itemView.findViewById(R.id.txtType);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            cardView = itemView.findViewById(R.id.permissionCard);
        }
    }
}

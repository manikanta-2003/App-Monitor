package com.example.myapplication;

public class PermissionModel {

    public String permissionName;
    public boolean isGranted;
    public boolean isDangerous;

    public PermissionModel(String permissionName,
                           boolean isGranted,
                           boolean isDangerous) {

        this.permissionName = permissionName;
        this.isGranted = isGranted;
        this.isDangerous = isDangerous;
    }
}
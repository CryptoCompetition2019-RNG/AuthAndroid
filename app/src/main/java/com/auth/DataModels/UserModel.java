package com.auth.DataModels;

public class UserModel extends AbstractModel {
    public String username;
    public String password;
    public String salt;
    public Integer biologic;
    public String imei;

    @Override
    public void saveToFile(String filename) {
        this.password = null;
        this.biologic = null;
        super.saveToFile(filename);
    }
}

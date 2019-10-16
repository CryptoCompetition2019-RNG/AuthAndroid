package com.auth.DataModels;

public class UserModel extends AbstractModel {
    public String username;
    public String password;
    public String salt;
    public Integer biologic;
    public String imei;

    public UserModel(String username) {
        super(username);
    }

    @Override
    protected String getUniqueIdent() {
        return username;
    }

    @Override
    protected void setUniqueIdent(String id) {
        this.username = id;
    }

    @Override
    public void saveToFile() {
        try {
            this.modelData.put("username", username);
            this.modelData.put("salt", salt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.saveToFile();
    }

    @Override
    public void loadFromFile() {
        super.loadFromFile();
        try {
            username = this.modelData.getString("username");
            salt = this.modelData.getString("salt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

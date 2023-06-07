package com.paj.pajbustelpo.model;

import java.util.List;

public class HttpResponse {

    public boolean isSuccess() {
        return success;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isUpgradable() {
        return upgradable;
    }

    public boolean isLogged() {
        return logged;
    }


    public String getMessage() {
        return message;
    }

    public String getApkUrl() {
        return url;
    }

    public String getApkName() {
        return apk;
    }

    public int iss() {
        return s;
    }

    public int success() {return s;}
    public List<Integer> l() {return l;}
    public List<Integer> r() {return r;}

    //
    private boolean success;
    private boolean update;
    private boolean upgradable;
    private boolean logged;
    private String message;
    private String url;
    private String apk;
    //
    private int s;
    private List<Integer> l;
    private List<Integer> r;

}

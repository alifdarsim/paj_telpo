package com.paj.pajbustelpo;

public class User {

    public String uuid;
    public String username;
    public String mykad_uid;
    public String qrcode_uid;
    public String blacklist;
    public String expired;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMykad_uid() {
        return mykad_uid;
    }

    public void setMykad_uid(String mykad_uid) {
        this.mykad_uid = mykad_uid;
    }

    public String getQrcode_uid() {
        return qrcode_uid;
    }

    public void setQrcode_uid(String qrcode_uid) {
        this.qrcode_uid = qrcode_uid;
    }

    public String isBlacklist() {
        return blacklist;
    }

    public void setBlacklist(String blacklist) {
        this.blacklist = blacklist;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    public boolean isUuidExist(){
        return uuid != null;
    }

}

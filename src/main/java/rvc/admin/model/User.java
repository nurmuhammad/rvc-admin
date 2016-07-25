package rvc.admin.model;

/**
 * @author nurmuhammad
 */

public class User extends SettingsModel {

    public Long id() {
        return (Long) get("id");
    }

    public void id(Long id) {
        set("id", id);
    }

    public String roles() {
        return (String) get("roles");
    }

    public void roles(String roles) {
        set("roles", roles);
    }

    public String email() {
        return (String) get("email");
    }

    public void email(String email) {
        set("email", email);
    }

    public String password() {
        return (String) get("password");
    }

    public void password(String password) {
        set("password", password);
    }

    public Integer created() {
        return (Integer) get("created");
    }

    public void created(Integer created) {
        set("created", created);
    }

    public Integer changed() {
        return (Integer) get("changed");
    }

    public void changed(Long changed) {
        set("changed", changed);
    }

    public Long lastLogin() {
        return (Long) get("last_login");
    }

    public void lastLogin(int lastLogin) {
        set("last_login", lastLogin);
    }

    public String lastIp() {
        return (String) get("last_ip");
    }

    public void lastIp(String lastIp) {
        set("last_ip", lastIp);
    }

    public boolean status() {
        return Boolean.TRUE.equals(get("status"));
    }

    public void status(boolean status) {
        set("status", status);
    }

    public String data() {
        return getString("data");
    }

    public void data(String data) {
        setString("data", data);
    }
}
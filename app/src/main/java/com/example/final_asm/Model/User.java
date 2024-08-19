package com.example.final_asm.Model;

public class User {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String repeatPassword;

    public User(int id, String name, String email, String phone, String password, String repeatPassword) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.repeatPassword = repeatPassword;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeatPassword(){return  repeatPassword;}
    public void setRepeatPassword(String password) {this.repeatPassword = repeatPassword;}
}
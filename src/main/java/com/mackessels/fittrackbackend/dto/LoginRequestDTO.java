package com.mackessels.fittrackbackend.dto;

public class LoginRequestDTO {
    private String name;
    private String password;

    public LoginRequestDTO(){

    }

    public String getName(){return name;}
    public String getPassword(){return password;}

    public void setName(String name){this.name = name;}
    public void setPassword(String password){this.password = password;}
}

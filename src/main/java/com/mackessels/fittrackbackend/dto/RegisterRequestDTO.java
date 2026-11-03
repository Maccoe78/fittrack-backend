package com.mackessels.fittrackbackend.dto;

public class RegisterRequestDTO {
    private String name;
    private String password;

    public RegisterRequestDTO(){

    }

    public String getName(){return name;}
    public String getPassword(){return password;}

    public void setName(String name){this.name = name;}
    public void setPassword(String password){this.password = password;}
}

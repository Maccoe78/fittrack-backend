package com.mackessels.fittrackbackend.dto;

public class LoginResponseDTO {
    private Long id;
    private String name;
    private String token;

    public LoginResponseDTO(){

    }

    public Long getId(){return id;}
    public String getName(){return name;}
    public String getToken(){return token;}

    public void setId(Long id){this.id = id;}
    public void setName(String name){this.name = name;}
    public void setToken(String token){this.token = token;}
}

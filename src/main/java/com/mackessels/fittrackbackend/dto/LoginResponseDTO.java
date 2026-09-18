package com.mackessels.fittrackbackend.dto;

public class LoginResponseDTO {
    private Long id;
    private String name;

    public LoginResponseDTO(){

    }

    public Long getId(){return id;}
    public String getName(){return name;}

    public void setId(Long id){this.id = id;}
    public void setName(String name){this.name = name;}
}

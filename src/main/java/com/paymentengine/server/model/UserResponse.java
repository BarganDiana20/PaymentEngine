package com.paymentengine.server.model;

public class UserResponse {

    private Integer id;
    private String username;
    private String email;
    private String role;
    private String firstName;
    private String lastName;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();  
    }

    public Integer getId() {return id;}
    public String getUsername() { return username;}
    public String getEmail() { return email;}
    public String getRole() {return role;}
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

}
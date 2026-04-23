package module;

import java.util.UUID;

public abstract class User {
	
	private String userID;
	private String name;
	private String email;
	private String password;
	private String role;
	
	public User(String name, String email, String password, String role) {
		this.userID = UUID.randomUUID().toString(); //generate unique ID
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
	}
	
	public String getUserID() { //getter for User ID
		return userID;
	}
	
	public String getName() {
		return name;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setRole(String role) { // role code implemented just now
		this.role = role;
	}
	public String getRole() {
		return role;
	}

}

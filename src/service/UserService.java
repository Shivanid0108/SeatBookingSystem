package service;
import module.User;
import module.ClientUser;
import module.Admin;
import module.BusinessOwner;
import java.util.List;  
import java.util.ArrayList;
//import java.math.BigDecimal;


public class UserService {
	
	private List<User> users = new ArrayList<>();
	
	public boolean isValidName(String name) {
		
		if(name == null || name.length() < 1 || name.trim().isEmpty()) {
			return false;
		}
		return true;
	}
	
	public boolean isValidEmail(String email) {
		if(email == null) {
			return false;
		}
		if (!email.contains("@") ) {
			return false;
		}
		
		if(email.endsWith("@gmail.com") || email.endsWith("@yahoo.com") || email.endsWith("@outlook.com")) {
			return true; 
		}
		
		return false; 
	}
	
	public boolean isValidPassword(String password) {
		if (password == null || password.length() < 8) {
			return false;
		}
		
		if(Character.isDigit(password.charAt(0))) {
			return false;
		}
		
		boolean hasUpper = false;
		boolean hasLower = false;
		boolean hasDigit = false;
		boolean hasSpecial = false;
		
		for (char ch : password.toCharArray()) {
			
			if(Character.isUpperCase(ch)) {
				hasUpper = true;
			}
			
			else if(Character.isLowerCase(ch)) {
				hasLower = true;
			}
			
			else if(Character.isDigit(ch)) {
				hasDigit = true;
			}
			
			else if(ch == '#' || ch == '&' || ch == '@') {
				hasSpecial = true;
			}
		}
		return hasUpper && hasLower && hasDigit && hasSpecial; 
	}
	
	public boolean emailExists(String email) {
		for(User user: users ) {
			if(user.getEmail().equals(email)) {
				return true;
			}
		}
		return false;
	}
	
	public String registerAdmin(String name, String email, String password) {
		if(!isValidName(name)) {
			return "Invalid name! Please enter valid name";
		}

		if(!isValidEmail(email)) {
			return "Invalid email address";
		}
		
		if(emailExists(email) ) {
			return "User already registered \n Please enter another email address"; 
		}
		
		if(!isValidPassword(password)) {
			return "Enter valid password";
		}
		Admin admin =new Admin(name, email, password);
		users.add(admin);
		return "Registered successfully";
	}
	
	public String registerBusinessOwner(String name, String email, String password) {  
		
		if(!isValidName(name)) {
			return "Invalid name! Please enter valid name";
		}

		if(!isValidEmail(email)) {
			return "Invalid email address";
		}
		
		if(emailExists(email) ) {
			return "User already registered \n Please enter another email address"; 
		}
		
		if(!isValidPassword(password)) {
			return "Enter valid password";
		}
		BusinessOwner owner = new BusinessOwner(name, email, password);
		users.add(owner);
		return "Registered successfully";
	}
	
	public String registerClientUser(String name, String email, String password) {
		
		if(!isValidName(name)) {
			return "Invalid name! Please enter valid name";
		}

		if(!isValidEmail(email)) {
			return "Invalid email address";
		}
		
		if(emailExists(email)) {
			return "User already registered \n Please enter another email address"; 
		}
		
		if(!isValidPassword(password)) {
			return "Enter valid password";
		}
		ClientUser client = new ClientUser(name, email, password);
		users.add(client);
		return "Registered successfully";
	}
	
	private User findUserByEmail(String email) {
		for(User user: users) {
			if(user.getEmail().equals(email)) {
				return user;
				
			}
		}
		return null;
	}
	
	public User loginUser(String email, String password, String role) {
		User foundUser = findUserByEmail(email);
		if(foundUser == null) {
			return null;
		}
		if(!foundUser.getPassword().equals(password) ) { 
				return null;	
		}
		if(!foundUser.getRole().equals(role)) {
			return null;
		}
	return foundUser;
	}
		
			

}

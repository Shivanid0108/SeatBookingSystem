package ui;

import java.util.Scanner;
//import java.math.BigDecimal;

import module.User;
import service.UserService;

public class Main {

	public static void main(String[] args) {
		UserService userservice = new UserService();
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println(" 1. Register \n 2. Login");
			System.out.println("Enter your choice: ");
			int mainChoice = scanner.nextInt();
			scanner.nextLine();
			if (mainChoice == 1) {
				handleRegistration(scanner, userservice);
			} else if (mainChoice == 2) {
				handleLogin(scanner, userservice);
			}
		}
	}

	private static void handleRegistration(Scanner scanner, UserService userservice) {
		boolean registering = true;
		while (registering) {
			System.out.println("Register as: \n 1. Admin \n 2. Business Owner \n 3. Client");
			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
			case 1: {
				System.out.println("Enter your name: ");
				String name = scanner.nextLine();

				System.out.println("Enter your email: ");
				String email = scanner.nextLine();

				System.out.println("Enter your password: ");
				String password = scanner.nextLine();

				String result = userservice.registerAdmin(name, email, password);
				System.out.println(result);
				if (result.equals("Registered successfully")) {
					registering = false;
				}
				break;
			}

			case 2: {
				System.out.println("Enter your name: ");
				String name = scanner.nextLine();

				System.out.println("\n Enter your email: ");
				String email = scanner.nextLine();

				System.out.println("Enter your password: ");
				String password = scanner.nextLine();

				String result = userservice.registerBusinessOwner(name, email, password);
				System.out.println(result);
				if (result.equals("Registered successfully")) {
					registering = false;
				}
				break;
			}

			case 3: {
				System.out.println("Enter your name: ");
				String name = scanner.nextLine();

				System.out.println("\n Enter your email: ");
				String email = scanner.nextLine();

				System.out.println("Enter your password: ");
				String password = scanner.nextLine();

				String result = userservice.registerClientUser(name, email, password);
				System.out.println(result);
				if (result.equals("Registered successfully")) {
					registering = false;
				}
				break;
			}

			case 4: {
				registering = false;
				break;
			}
			default:
				System.out.println("Invalid choice. Try again");
			}

		}

	}

	private static void handleLogin(Scanner scanner, UserService userservice) {
		boolean loggingIn = true;
		while (loggingIn) {
			System.out.println("Login as: \n 1. Admin \n 2. Business Owner \n 3. Client");
			int choice = scanner.nextInt();
			scanner.nextLine();
			String role;

			switch (choice) {

			case 1: {
				role = "Admin";

				System.out.println("Enter your email: ");
				String email = scanner.nextLine();

				System.out.println("Enter your password: ");
				String password = scanner.nextLine();

				User loggedInUser = userservice.loginUser(email, password, role);
				if (loggedInUser != null) {
					System.out.println("Welcome " + loggedInUser.getName());
					loggingIn = false;
				} else {
					System.out.println("Invalid email or password");
				}
				break;
			}

			case 2: {
				role = "BusinessOwner";

				System.out.println("Enter your email: ");
				String email = scanner.nextLine();

				System.out.println("Enter your password: ");
				String password = scanner.nextLine();

				User loggedInUser = userservice.loginUser(email, password, role); // ✅
				if (loggedInUser != null) {
					System.out.println("Welcome " + loggedInUser.getName());
					loggingIn = false;
				} else {
					System.out.println("Invalid email or password");
				}
				break;
			}

			case 3: {
				role = "ClientUser";

				System.out.println("Enter your email: ");
				String email = scanner.nextLine();

				System.out.println("Enter your password: ");
				String password = scanner.nextLine();

				User loggedInUser = userservice.loginUser(email, password, role); // ✅
				if (loggedInUser != null) {
					System.out.println("Welcome " + loggedInUser.getName());
					loggingIn = false;
				} else {
					System.out.println("Invalid email or password");
				}
				break;
			}

			case 4: {
				loggingIn = false;
				break;
			}
			default:
				System.out.println("Invalid choice. Try again");
			}

		} // while logginIn closing bracket

	}

}
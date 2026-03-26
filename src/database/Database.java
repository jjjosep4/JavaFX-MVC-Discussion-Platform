package database;

import java.sql.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import entityClasses.EvaluationParameter;
import entityClasses.Post;
import entityClasses.Reply;
import entityClasses.StaffRequest;
import entityClasses.User;

/*******
 * <p> Title: Database Class. </p>
 * 
 * <p> Description: This is an in-memory database built on H2.  Detailed documentation of H2 can
 * be found at https://www.h2database.com/html/main.html (Click on "PDF (2MP) for a PDF of 438 pages
 * on the H2 main page.)  This class leverages H2 and provides numerous special supporting methods.
 * </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 2.00		2025-04-29 Updated and expanded from the version produce by on a previous
 * 							version by Pravalika Mukkiri and Ishwarya Hidkimath Basavaraj
 */

/*
 * The Database class is responsible for establishing and managing the connection to the database,
 * and performing operations such as user registration, login validation, handling invitation 
 * codes, and numerous other database related functions.
 */
public class Database {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	//  Shared variables used within this class
	private Connection connection = null;		// Singleton to access the database 
	private Statement statement = null;			// The H2 Statement is used to construct queries
	
	// These are the easily accessible attributes of the currently logged-in user
	// This is only useful for single user applications
	private String currentUsername;
	private String currentPassword;
	private String currentFirstName;
	private String currentMiddleName;
	private String currentLastName;
	private String currentPreferredFirstName;
	private String currentEmailAddress;
	private boolean currentAdminRole;
	private boolean currentNewRole1;
	private boolean currentNewRole2;

	/*******
	 * <p> Method: Database </p>
	 * 
	 * <p> Description: The default constructor used to establish this singleton object.</p>
	 * 
	 */
	
	public Database () {
		
	}
	
	
/*******
 * <p> Method: connectToDatabase </p>
 * 
 * <p> Description: Used to establish the in-memory instance of the H2 database from secondary
 *		storage.</p>
 *
 * @throws SQLException when the DriverManager is unable to establish a connection
 * 
 */
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	
/*******
 * <p> Method: createTables </p>
 * 
 * <p> Description: Used to create new instances of the two database tables used by this class.</p>
 * 
 */
	private void createTables() throws SQLException {
		// Create the user database
		String userTable = "CREATE TABLE IF NOT EXISTS userDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "firstName VARCHAR(255), "
				+ "middleName VARCHAR(255), "
				+ "lastName VARCHAR (255), "
				+ "preferredFirstName VARCHAR(255), "
				+ "emailAddress VARCHAR(255), "
				+ "adminRole BOOL DEFAULT FALSE, "
				+ "newRole1 BOOL DEFAULT FALSE, "
				+ "newRole2 BOOL DEFAULT FALSE)";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	    		+ "emailAddress VARCHAR(255), "
	            + "role VARCHAR(10),"
	    		+ "used BOOLEAN DEFAULT FALSE,"
	            + "timeCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
	    statement.execute(invitationCodesTable);
	
	// Create posts table (supports soft-delete via 'deleted' boolean)
		String postsTable = "CREATE TABLE IF NOT EXISTS postDB ("
	        + "postID VARCHAR(36) PRIMARY KEY, "
	        + "author VARCHAR(255), "
	        + "thread VARCHAR(255) DEFAULT 'General', "
	        + "title VARCHAR(1024), "
	        + "content CLOB, "
	        + "timeCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	        + "isPrivate BOOLEAN DEFAULT FALSE, "
	        + "deleted BOOLEAN DEFAULT FALSE)";
		statement.execute(postsTable);

	// Create replies table (no cascade delete; posts soft-delete keeps replies)
		String repliesTable =
			    "CREATE TABLE IF NOT EXISTS replyDB ("
			  + "replyID VARCHAR(36) PRIMARY KEY, "
			  + "postID VARCHAR(36), "
			  + "author VARCHAR(255), "
			  + "message CLOB, "
			  + "timeCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
			  + "isPrivate BOOLEAN DEFAULT FALSE, "
			  + "FOREIGN KEY (postID) REFERENCES postDB(postID))";	    
		statement.execute(repliesTable);
		
		// Create evaluation parameter table for staff grading configuration
		String evalParamTable = "CREATE TABLE IF NOT EXISTS evaluationParameterDB ("
		        + "id INT AUTO_INCREMENT PRIMARY KEY, "
		        + "name VARCHAR(255) UNIQUE, "
		        + "description CLOB, "
		        + "weight INT)";
		statement.execute(evalParamTable);
		
		// New table to store per-parameter evaluation scores for each student
		String createEvaluationRecordTable = 
		    "CREATE TABLE IF NOT EXISTS evaluationRecordDB (" +
		    " id INT AUTO_INCREMENT PRIMARY KEY," +
		    " studentUsername VARCHAR(255) NOT NULL," +
		    " parameterName VARCHAR(255) NOT NULL," +
		    " score INT NOT NULL," +
		    " weight INT NOT NULL," +
		    " evaluatedBy VARCHAR(255) NOT NULL," +
		    " evaluatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
		    ");";

		try (Statement stmt = connection.createStatement()) {
		    stmt.execute(createEvaluationRecordTable);
		} catch (SQLException e) {
		    e.printStackTrace();
		}
		
		// Create Staff Request table
		String staffRequestTable = "CREATE TABLE IF NOT EXISTS staffRequestDB ("
		        + "id INT AUTO_INCREMENT PRIMARY KEY, "
		        + "sender VARCHAR(255) NOT NULL, "
		        + "subject VARCHAR(255) NOT NULL, "
		        + "body CLOB, "
		        + "adminReply CLOB, "
		        + "isClosed BOOLEAN DEFAULT FALSE, "
		        + "isRead BOOLEAN DEFAULT FALSE, "
		        + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		statement.execute(staffRequestTable);




	}



/*******
 * <p> Method: isDatabaseEmpty </p>
 * 
 * <p> Description: If the user database has no rows, true is returned, else false.</p>
 * 
 * @return true if the database is empty, else it returns false
 * 
 */
	public boolean isDatabaseEmpty() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count") == 0;
			}
		}  catch (SQLException e) {
	        return false;
	    }
		return true;
	}
	
	
/*******
 * <p> Method: getNumberOfUsers </p>
 * 
 * <p> Description: Returns an integer .of the number of users currently in the user database. </p>
 * 
 * @return the number of user records in the database.
 * 
 */
	public int getNumberOfUsers() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch (SQLException e) {
	        return 0;
	    }
		return 0;
	}

/*******
 * <p> Method: register(User user) </p>
 * 
 * <p> Description: Creates a new row in the database using the user parameter. </p>
 * 
 * @throws SQLException when there is an issue creating the SQL command or executing it.
 * 
 * @param user specifies a user object to be added to the database.
 * 
 */
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO userDB (userName, password, firstName, middleName, "
				+ "lastName, preferredFirstName, emailAddress, adminRole, newRole1, newRole2) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			currentUsername = user.getUserName();
			pstmt.setString(1, currentUsername);
			
			currentPassword = user.getPassword();
			pstmt.setString(2, currentPassword);
			
			currentFirstName = user.getFirstName();
			pstmt.setString(3, currentFirstName);
			
			currentMiddleName = user.getMiddleName();			
			pstmt.setString(4, currentMiddleName);
			
			currentLastName = user.getLastName();
			pstmt.setString(5, currentLastName);
			
			currentPreferredFirstName = user.getPreferredFirstName();
			pstmt.setString(6, currentPreferredFirstName);
			
			currentEmailAddress = user.getEmailAddress();
			pstmt.setString(7, currentEmailAddress);
			
			currentAdminRole = user.getAdminRole();
			pstmt.setBoolean(8, currentAdminRole);
			
			currentNewRole1 = user.getRole1();
			pstmt.setBoolean(9, currentNewRole1);
			
			currentNewRole2 = user.getRole2();
			pstmt.setBoolean(10, currentNewRole2);
			
			pstmt.executeUpdate();
		}
		
	}
	
/*******
 *  <p> Method: List getUserList() </p>
 *  
 *  <P> Description: Generate an List of Strings, one for each user in the database,
 *  starting with "<Select User>" at the start of the list. </p>
 *  
 *  @return a list of userNames found in the database.
 */
	public List<String> getUserList () {
		List<String> userList = new ArrayList<String>();
		userList.add("<Select a User>");
		String query = "SELECT userName FROM userDB";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				userList.add(rs.getString("userName"));
			}
		} catch (SQLException e) {
	        return null;
	    }
//		System.out.println(userList);
		return userList;
	}

/*******
 * <p> Method: boolean loginAdmin(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Admin role.
 * 
 * @return true if the specified user has been logged in as an Admin else false.
 * 
 */
	public boolean loginAdmin(User user){
		// Validates an admin user's login credentials so the user can login in as an Admin.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "adminRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();	// If a row is returned, rs.next() will return true		
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
/*******
 * <p> Method: boolean loginRole1(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Student role.
 * 
 * @return true if the specified user has been logged in as an Student else false.
 * 
 */
	public boolean loginRole1(User user) {
		// Validates a student user's login credentials.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "newRole1 = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}

	/*******
	 * <p> Method: boolean loginRole2(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username, password, and role
	 * 		is the same as a row in the table for the username, password, and role. </p>
	 * 
	 * @param user specifies the specific user that should be logged in playing the Reviewer role.
	 * 
	 * @return true if the specified user has been logged in as an Student else false.
	 * 
	 */
	// Validates a reviewer user's login credentials.
	public boolean loginRole2(User user) {
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "newRole2 = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: boolean doesUserExist(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username is  in the table. </p>
	 * 
	 * @param userName specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return true if the specified user is in the table else false.
	 * 
	 */
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	public boolean deleteUser(String userName) {
		String query = "DELETE FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	     
	        pstmt.setString(1, userName);
	        int rows = pstmt.executeUpdate();
	        return rows > 0;				//True if a row is deleted
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	    return false; 
		}
	}
	
	
	
	/*******
	 * <p> Method: int getNumberOfRoles(User user) </p>
	 * 
	 * <p> Description: Determine the number of roles a specified user plays. </p>
	 * 
	 * @param user specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return the number of roles this user plays (0 - 5).
	 * 
	 */	
	// Get the number of roles that this user plays
	public int getNumberOfRoles (User user) {
		int numberOfRoles = 0;
		if (user.getAdminRole()) numberOfRoles++;
		if (user.getRole1()) numberOfRoles++;
		if (user.getRole2()) numberOfRoles++;
		return numberOfRoles;
	}	

	
	/*******
	 * <p> Method: String generateInvitationCode(String emailAddress, String role) </p>
	 * 
	 * <p> Description: Given an email address and a roles, this method establishes and invitation
	 * code and adds a record to the InvitationCodes table.  When the invitation code is used, the
	 * stored email address is used to establish the new user and the record is removed from the
	 * table.</p>
	 * 
	 * @param emailAddress specifies the email address for this new user.
	 * 
	 * @param role specified the role that this new user will play.
	 * 
	 * @return the code of six characters so the new user can use it to securely setup an account.
	 * 
	 */
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode(String emailAddress, String role) {
	    String code = UUID.randomUUID().toString().substring(0, 6); // Generate a random 6-character code
	    String query = "INSERT INTO InvitationCodes (code, emailaddress, role) VALUES (?, ?, ?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.setString(2, emailAddress);
	        pstmt.setString(3, role);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return code;
	}
	

	
	/*******
	 * <p> Method: int getNumberOfInvitations() </p>
	 * 
	 * <p> Description: Determine the number of outstanding invitations in the table.</p>
	 *  
	 * @return the number of invitations in the table.
	 * 
	 */
	// Number of invitations in the database
	public int getNumberOfInvitations() {
		String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE used = FALSE";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return 0;
	}
	
	/** true iff the code exists and has not been used yet */
	public boolean isInvitationValid(String code) {
	    String q = "SELECT used, timeCreated FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, code);
	        ResultSet rs = ps.executeQuery();
	       if (rs.next()) {
	    	   boolean used = rs.getBoolean("used");
	    	   Timestamp timeCreated = rs.getTimestamp("timeCreated");
	    	   long now = System.currentTimeMillis();
	    	   long ageMilliseconds = now - timeCreated.getTime();
	    	   
	            if (!used && ageMilliseconds <= 60000) {
	            	return true;
	            } else {
	            	removeInvitationAfterUse(code);
	            	return false;
	            }
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return false;
	}
	/**
	 * Mark an invitation code as used and return {email, role}.
	 * If the code doesn't exist or is already used, returns null.
	 */
	public String[] consumeInvitation(String code) {
	    try {
	        connection.setAutoCommit(false);
	        // check current state
	        String sel = "SELECT used, emailAddress, role FROM InvitationCodes WHERE code = ? FOR UPDATE";
	        try (PreparedStatement ps = connection.prepareStatement(sel)) {
	            ps.setString(1, code);
	            try (ResultSet rs = ps.executeQuery()) {
	                if (!rs.next() || rs.getBoolean("used")) {
	                    connection.rollback();
	                    return null;
	                }
	                String email = rs.getString("emailAddress");
	                String role  = rs.getString("role");
	                // mark used
	                String upd = "UPDATE InvitationCodes SET used = TRUE WHERE code = ?";
	                try (PreparedStatement up = connection.prepareStatement(upd)) {
	                    up.setString(1, code);
	                    up.executeUpdate();
	                }
	                connection.commit();
	                return new String[]{ email, role };
	            }
	        }
	    } catch (SQLException e) {
	        try { connection.rollback(); } catch (SQLException ignore) {}
	        e.printStackTrace();
	    } finally {
	        try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
	    }
	    return null;
	}

	/*******
	 * <p> Method: boolean emailaddressHasBeenUsed(String emailAddress) </p>
	 * 
	 * <p> Description: Determine if an email address has been user to establish a user.</p>
	 * 
	 * @param emailAddress is a string that identifies a user in the table
	 *  
	 * @return true if the email address is in the table, else return false.
	 * 
	 */
	// Check to see if an email address is already in the database
	public boolean emailaddressHasBeenUsed(String emailAddress) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE emailAddress = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        ResultSet rs = pstmt.executeQuery();
	        System.out.println(rs);
	        if (rs.next()) {
	            // Mark the code as used
	        	return rs.getInt("count")>0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
	/*******
	 * <p> Method: String getRoleGivenAnInvitationCode(String code) </p>
	 * 
	 * <p> Description: Get the role associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the role for the code or an empty string.
	 * 
	 */
	// Obtain the roles associated with an invitation code.
	public String getRoleGivenAnInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

	
	/*******
	 * <p> Method: String getEmailAddressUsingCode (String code ) </p>
	 * 
	 * <p> Description: Get the email addressed associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the email address for the code or an empty string.
	 * 
	 */
	// For a given invitation code, return the associated email address of an empty string
	public String getEmailAddressUsingCode (String code ) {
	    String query = "SELECT emailAddress FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("emailAddress");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return "";
	}
	
	
	/*******
	 * <p> Method: void removeInvitationAfterUse(String code) </p>
	 * 
	 * <p> Description: Remove an invitation record once it is used.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 */
	// Remove an invitation using an email address once the user account has been setup
	public void removeInvitationAfterUse(String code) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	int counter = rs.getInt(1);
	            // Only do the remove if the code is still in the invitation table
	        	if (counter > 0) {
        			query = "DELETE FROM InvitationCodes WHERE code = ?";
	        		try (PreparedStatement pstmt2 = connection.prepareStatement(query)) {
	        			pstmt2.setString(1, code);
	        			pstmt2.executeUpdate();
	        		}catch (SQLException e) {
	        	        e.printStackTrace();
	        	    }
	        	}
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return;
	}
	

	private String currentOneTimePassword = null;

	//One Time Password generation
	public String generateOneTimePassword() {
	    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	    StringBuilder sb = new StringBuilder();
	    java.util.Random rand = new java.util.Random();
	    for (int i = 0; i < 10; i++) {
	        sb.append(chars.charAt(rand.nextInt(chars.length())));
	    }
	    currentOneTimePassword = sb.toString();
	    return currentOneTimePassword;
	}
	
	//Checking to see if one time password is the generated one
	public boolean validateOneTimePassword(String attempt) {
	    if (currentOneTimePassword != null && currentOneTimePassword.equals(attempt)) {
	        currentOneTimePassword = null; 
	        return true;
	    }
	    return false;
	}
	
	//Setting new password after using one time password
	public void setNewPassword(String username, String newPassword) {
	    String query = "UPDATE userDB SET password = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newPassword);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}




	

	
	public boolean updateUsername(String currentUsername, String newUsername) {
	    String query = "UPDATE userDB SET userName = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newUsername);
	        pstmt.setString(2, currentUsername);
	        int rows = pstmt.executeUpdate();
	        if (rows > 0) {
	            this.currentUsername = newUsername; 
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	
	public boolean updatePassword(String username, String password) {
	    String query = "UPDATE userDB SET password = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, password);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentFirstName = password;
	        return true;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	/*******
	 * <p> Method: String getFirstName(String username) </p>
	 * 
	 * <p> Description: Get the first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the first name of a user given that user's username 
	 *  
	 */
	public String getFirstName(String username) {
		String query = "SELECT firstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	

	/*******
	 * <p> Method: void updateFirstName(String username, String firstName) </p>
	 * 
	 * <p> Description: Update the first name of a user given that user's username and the new
	 *		first name.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @param firstName is the new first name for the user
	 *  
	 */
	// update the first name
	public void updateFirstName(String username, String firstName) {
	    String query = "UPDATE userDB SET firstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, firstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentFirstName = firstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	/*******
	 * <p> Method: String getMiddleName(String username) </p>
	 * 
	 * <p> Description: Get the middle name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the middle name of a user given that user's username 
	 *  
	 */
	// get the middle name
	public String getMiddleName(String username) {
		String query = "SELECT MiddleName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("middleName"); // Return the middle name if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}

	
	/*******
	 * <p> Method: void updateMiddleName(String username, String middleName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param middleName is the new middle name for the user
	 *  
	 */
	// update the middle name
	public void updateMiddleName(String username, String middleName) {
	    String query = "UPDATE userDB SET middleName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, middleName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentMiddleName = middleName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getLastName(String username) </p>
	 * 
	 * <p> Description: Get the last name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the last name of a user given that user's username 
	 *  
	 */
	// get he last name
	public String getLastName(String username) {
		String query = "SELECT LastName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("lastName"); // Return last name role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateLastName(String username, String lastName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param lastName is the new last name for the user
	 *  
	 */
	// update the last name
	public void updateLastName(String username, String lastName) {
	    String query = "UPDATE userDB SET lastName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, lastName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentLastName = lastName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getPreferredFirstName(String username) </p>
	 * 
	 * <p> Description: Get the preferred first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the preferred first name of a user given that user's username 
	 *  
	 */
	// get the preferred first name
	public String getPreferredFirstName(String username) {
		String query = "SELECT preferredFirstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the preferred first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updatePreferredFirstName(String username, String preferredFirstName) </p>
	 * 
	 * <p> Description: Update the preferred first name of a user given that user's username and
	 * 		the new preferred first name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param preferredFirstName is the new preferred first name for the user
	 *  
	 */
	// update the preferred first name of the user
	public void updatePreferredFirstName(String username, String preferredFirstName) {
	    String query = "UPDATE userDB SET preferredFirstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, preferredFirstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPreferredFirstName = preferredFirstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getEmailAddress(String username) </p>
	 * 
	 * <p> Description: Get the email address of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the email address of a user given that user's username 
	 *  
	 */
	// get the email address
	public String getEmailAddress(String username) {
		String query = "SELECT emailAddress FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("emailAddress"); // Return the email address if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateEmailAddress(String username, String emailAddress) </p>
	 * 
	 * <p> Description: Update the email address name of a user given that user's username and
	 * 		the new email address.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param emailAddress is the new preferred first name for the user
	 *  
	 */
	// update the email address
	public boolean updateEmailAddress(String username, String emailAddress) {
	    String query = "UPDATE userDB SET emailAddress = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentEmailAddress = emailAddress;
	        return true;
	    } catch (SQLException e) {
	        e.printStackTrace();	        
	    }
	    return false;
	}
	
	
	/*******
	 * <p> Method: boolean getUserAccountDetails(String username) </p>
	 * 
	 * <p> Description: Get all the attributes of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return true of the get is successful, else false
	 *  
	 */
	// get the attributes for a specified user
	public boolean getUserAccountDetails(String username) {
		String query = "SELECT * FROM userDB WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();			
			rs.next();
	    	currentUsername = rs.getString(2);
	    	currentPassword = rs.getString(3);
	    	currentFirstName = rs.getString(4);
	    	currentMiddleName = rs.getString(5);
	    	currentLastName = rs.getString(6);
	    	currentPreferredFirstName = rs.getString(7);
	    	currentEmailAddress = rs.getString(8);
	    	currentAdminRole = rs.getBoolean(9);
	    	currentNewRole1 = rs.getBoolean(10);
	    	currentNewRole2 = rs.getBoolean(11);
			return true;
	    } catch (SQLException e) {
			return false;
	    }
	}
	
	
	/*******
	 * <p> Method: boolean updateUserRole(String username, String role, String value) </p>
	 * 
	 * <p> Description: Update a specified role for a specified user's and set and update all the
	 * 		current user attributes.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param role is string that specifies the role to update
	 * 
	 * @param value is the string that specified TRUE or FALSE for the role
	 * 
	 * @return true if the update was successful, else false
	 *  
	 */
	// Update a users role
	public boolean updateUserRole(String username, String role, String value) {
		if (role.compareTo("Admin") == 0) {
			String query = "UPDATE userDB SET adminRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentAdminRole = true;
				else
					currentAdminRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Student") == 0) {
			String query = "UPDATE userDB SET newRole1 = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentNewRole1 = true;
				else
					currentNewRole1 = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Staff") == 0) {
			String query = "UPDATE userDB SET newRole2 = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentNewRole2 = true;
				else
					currentNewRole2 = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		return false;
	}
	
	
	/*********
	 * Create a new Post
	 */
	public boolean createPost(Post p) {
		// Input validation before insert
	    if (p.getTitle() == null || p.getTitle().trim().isEmpty()) return false;
	    if (p.getTitle().trim().length() > 50) return false;
	    if (p.getContent() == null || p.getContent().trim().isEmpty()) return false;
	    
	    		
	    String q = "INSERT INTO postDB (postID, author, thread, title, content, timeCreated, deleted) VALUES (?, ?, ?, ?, ?, ?, ?)";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, p.getPostID());
	        ps.setString(2, p.getAuthor());
	        ps.setString(3, p.getThread() == null ? "General" : p.getThread());
	        ps.setString(4, p.getTitle());
	        ps.setString(5, p.getContent());
	        ps.setTimestamp(6, Timestamp.valueOf(p.getTimestamp()));
	        ps.setBoolean(7, p.isDeleted());
	        ps.executeUpdate();
	        return true;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	
	/*********
	 * Update an existing post's title and content
	 */
	public boolean updatePost(String postID, String newTitle, String newContent) {
	    String q = "UPDATE postDB SET title = ?, content = ? WHERE postID = ?";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, newTitle);
	        ps.setString(2, newContent);
	        ps.setString(3, postID);
	        int rows = ps.executeUpdate();
	        return rows > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}


	/*********
	 * Get all posts (most recent first)
	 */
	public java.util.List<Post> getAllPosts() {
	    String q = "SELECT * FROM postDB ORDER BY timeCreated DESC";
	    java.util.List<Post> list = new ArrayList<>();
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) {
	            String id = rs.getString("postID");
	            String author = rs.getString("author");
	            String thread = rs.getString("thread");
	            String title = rs.getString("title");
	            String content = rs.getString("content");
	            Timestamp ts = rs.getTimestamp("timeCreated");
	            LocalDateTime time = ts.toLocalDateTime();
	            boolean deleted = rs.getBoolean("deleted");
	            list.add(new Post(id, author, thread, title, content, time, deleted));
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return list;
	}

	/*********
	 * Get posts for a specific author (my posts)
	 */
	public java.util.List<Post> getMyPosts(String author) {
	    String q = "SELECT * FROM postDB WHERE author = ? ORDER BY timeCreated DESC";
	    java.util.List<Post> list = new ArrayList<>();
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, author);
	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) {
	            String id = rs.getString("postID");
	            String title = rs.getString("title");
	            String thread = rs.getString("thread");
	            String content = rs.getString("content");
	            Timestamp ts = rs.getTimestamp("timeCreated");
	            LocalDateTime time = ts.toLocalDateTime();
	            boolean deleted = rs.getBoolean("deleted");
	            list.add(new Post(id, author, thread, title, content, time, deleted));
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return list;
	}

	/*********
	 * Get a single post by ID
	 */
	public Post getPostById(String postID) {
	    String q = "SELECT * FROM postDB WHERE postID = ?";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, postID);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            return new Post(
	                rs.getString("postID"),
	                rs.getString("author"),
	                rs.getString("thread"),
	                rs.getString("title"),
	                rs.getString("content"),
	                rs.getTimestamp("timeCreated").toLocalDateTime(),
	                rs.getBoolean("deleted")
	            );
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return null;
	}


	
	/*********
	 * Search posts by keyword (title or content)
	 */
	public java.util.List<Post> searchPostsByKeyword(String keyword) {
		java.util.List<Post> list = new ArrayList<>();
		if (keyword == null) keyword = "";
		keyword = keyword.trim();
		if (keyword.isEmpty()) return list;
	    String q = "SELECT * FROM postDB WHERE LOWER(title) LIKE ? OR LOWER(content) LIKE ? ORDER BY timeCreated DESC";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        String like = "%" + keyword.toLowerCase() + "%";
	        ps.setString(1, like);
	        ps.setString(2, like);
	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) {
	            list.add(new Post(
	                rs.getString("postID"),
	                rs.getString("author"),
	                rs.getString("thread"),
	                rs.getString("title"),
	                rs.getString("content"),
	                rs.getTimestamp("timeCreated").toLocalDateTime(),
	                rs.getBoolean("deleted")
	            ));
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return list;
	}

	/*********
	 * soft delete a post (mark deleted = true); leave replies in place
	 */
	public boolean softDeletePost(String postID) {
	    String q = "UPDATE postDB SET deleted = TRUE, title = ?, content = ? WHERE postID = ?";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        // Replace title/content with deleted marker
	        ps.setString(1, "(deleted)");
	        ps.setString(2, "This post has been deleted by the author.");
	        ps.setString(3, postID);
	        int rows = ps.executeUpdate();
	        return rows > 0;
	    } catch (SQLException e) { e.printStackTrace(); }
	    return false;
	}

	/*********
	 * Reply methods
	 */

	/** Create a reply */
	public boolean createReply(Reply r) {
		// Input validation before inserting
	    if (r.getPostID() == null || r.getPostID().isBlank()) return false;
	    if (r.getMessage() == null || r.getMessage().trim().isEmpty()) return false;
	    String q = "INSERT INTO replyDB (replyID, postID, author, message, timeCreated, isPrivate) VALUES (?, ?, ?, ?, ?, ?)";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, r.getReplyID());
	        ps.setString(2, r.getPostID());
	        ps.setString(3, r.getAuthor());
	        ps.setString(4, r.getMessage());
	        ps.setTimestamp(5, Timestamp.valueOf(r.getTimestamp()));
	        ps.setBoolean(6,  r.isPrivate());
	        ps.executeUpdate();
	        return true;
	    } catch (SQLException e) { e.printStackTrace(); }
	    return false;
	}

	/** Get replies for a post (oldest first) */
	public java.util.List<Reply> getRepliesForPost(String postID) {
	    String q = "SELECT * FROM replyDB WHERE postID = ? ORDER BY timeCreated ASC";
	    java.util.List<Reply> list = new ArrayList<>();

	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, postID);
	        ResultSet rs = ps.executeQuery();

	        while (rs.next()) {

	            // Construct reply using your existing Reply constructor
	            Reply r = new Reply(
	                rs.getString("replyID"),
	                rs.getString("postID"),
	                rs.getString("author"),
	                rs.getString("message"),
	                rs.getTimestamp("timeCreated").toLocalDateTime(),
	                rs.getBoolean("isPrivate")
	            );

	            // Load the "isPrivate" column from the database
	            boolean priv = false;
	            try {
	                priv = rs.getBoolean("isPrivate");
	            } catch (SQLException ignored) {
	                // Column might not exist yet — safe fallback
	            }

	            r.setPrivate(priv);

	            list.add(r);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}


	/** Get reply count for a post */
	public int getReplyCountForPost(String postID) {
	    String q = "SELECT COUNT(*) AS cnt FROM replyDB WHERE postID = ?";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, postID);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("cnt");
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return 0;
	}

	/** Get a reply by id */
	public Reply getReplyById(String replyID) {
	    String q = "SELECT * FROM replyDB WHERE replyID = ?";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, replyID);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            return new Reply(
	                rs.getString("replyID"),
	                rs.getString("postID"),
	                rs.getString("author"),
	                rs.getString("message"),
	                rs.getTimestamp("timeCreated").toLocalDateTime(),
	                rs.getBoolean("isPrivate")
	            );
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return null;
	}

	/** Update a reply's message */
	public boolean updateReply(String replyID, String newMessage) {
	    String q = "UPDATE replyDB SET message = ? WHERE replyID = ?";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, newMessage);
	        ps.setString(2, replyID);
	        int rows = ps.executeUpdate();
	        return rows > 0;
	    } catch (SQLException e) { e.printStackTrace(); }
	    return false;
	}

	/** Delete a reply */
	public boolean deleteReply(String replyID) {
	    String q = "DELETE FROM replyDB WHERE replyID = ?";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, replyID);
	        int rows = ps.executeUpdate();
	        return rows > 0;
	    } catch (SQLException e) { e.printStackTrace(); }
	    return false;
	}
	
	/*******
	 * Return all distinct thread names (categories) currently used by posts.
	 * Empty/null categories are ignored.
	 */
	public List<String> getAllThreadNames() {
	    List<Post> posts = getAllPosts();
	    List<String> result = new ArrayList<>();

	    for (Post p : posts) {
	        if (p == null || p.isDeleted()) continue;
	        String cat = p.getThread();
	        if (cat == null) continue;
	        String trimmed = cat.trim();
	        if (trimmed.isEmpty()) continue;
	        if (!result.contains(trimmed)) {
	            result.add(trimmed);
	        }
	    }
	    return result;
	}
	
	/*******
	 * Rename a thread by updating the category for all posts using the old name.
	 *
	 * @param oldName the current thread/category name
	 * @param newName the new thread/category name
	 * @return true if at least one post was updated
	 */
	public boolean renameThread(String oldName, String newName) {
	    if (oldName == null || newName == null) return false;
	    String oldTrim = oldName.trim();
	    String newTrim = newName.trim();
	    if (oldTrim.isEmpty() || newTrim.isEmpty()) return false;

	    boolean updatedAtLeastOne = false;
	    List<Post> posts = getAllPosts();

	    for (Post p : posts) {
	        if (p == null || p.isDeleted()) continue;
	        String cat = p.getThread();
	        if (cat != null && cat.trim().equalsIgnoreCase(oldTrim)) {
	            p.setThread(newTrim);
	            // If you have a DB update method, call it; if not, this may already be in-memory.
	            updatedAtLeastOne = true;
	        }
	    }
	    return updatedAtLeastOne;
	}
	
	/*******
	 * Delete a thread by soft-deleting all posts in that category.
	 *
	 * @param threadName the thread/category name
	 * @return true if at least one post was soft deleted
	 */
	public boolean deleteThread(String threadName) {
	    if (threadName == null) return false;
	    String nameTrim = threadName.trim();
	    if (nameTrim.isEmpty()) return false;

	    boolean deletedAtLeastOne = false;
	    List<Post> posts = getAllPosts();

	    for (Post p : posts) {
	        if (p == null || p.isDeleted()) continue;
	        String cat = p.getThread();
	        if (cat != null && cat.trim().equalsIgnoreCase(nameTrim)) {
	            if (softDeletePost(p.getPostID())) {
	                deletedAtLeastOne = true;
	            }
	        }
	    }
	    return deletedAtLeastOne;
	}

	/*******
	 * <p> Method: boolean createEvaluationParameter </p>
	 * 
	 * <p> Description: Create a new evaluation parameter for staff grading.  The name
	 * must be non-empty, weight must be between 1 and 100 (inclusive), and the sum of
	 * all existing parameter weights plus this one must not exceed 100. </p>
	 * 
	 * @param name the short name of the parameter (e.g., "Participation")
	 * @param description a free-text description of what this parameter means
	 * @param weight the integer weight 1–100
	 * 
	 * @return true if the parameter was created, else false
	 */
	public boolean createEvaluationParameter(String name, String description, int weight) {
	    if (name == null || name.trim().isEmpty()) return false;
	    if (weight < 1 || weight > 100) return false;

	    int currentTotal = getTotalParameterWeight();
	    if (currentTotal + weight > 100) {
	        // Would exceed total allowed weight
	        return false;
	    }

	    String q = "INSERT INTO evaluationParameterDB (name, description, weight) VALUES (?, ?, ?)";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ps.setString(1, name.trim());
	        ps.setString(2, description == null ? "" : description.trim());
	        ps.setInt(3, weight);
	        ps.executeUpdate();
	        return true;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	/*******
	 * <p> Method: boolean updateEvaluationParameter </p>
	 * 
	 * <p> Description: Update the name, description, and/or weight for an evaluation
	 * parameter identified by its ID.  Weight must be 1–100, and the total of all
	 * other weights plus the new weight must not exceed 100. </p>
	 * 
	 * @param id the primary key of the parameter record
	 * @param newName the new name to set (must be non-empty)
	 * @param newDescription the new description (may be empty)
	 * @param newWeight the new weight 1–100
	 * 
	 * @return true if the parameter was updated, else false
	 */
	public boolean updateEvaluationParameter(int id, String name, String description, int weight) {
	    String sql = "UPDATE evaluationParameterDB SET name = ?, description = ?, weight = ? WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {

	        stmt.setString(1, name);
	        stmt.setString(2, description);
	        stmt.setInt(3, weight);
	        stmt.setInt(4, id);

	        return stmt.executeUpdate() > 0;

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}



	/*******
	 * <p> Method: boolean deleteEvaluationParameter </p>
	 * 
	 * <p> Description: Delete a grading parameter given its ID. </p>
	 * 
	 * @param id the primary key of the parameter
	 * 
	 * @return true if a row was deleted, else false
	 */
	public boolean deleteEvaluationParameter(int id) {
	    String sql = "DELETE FROM evaluationParameterDB WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {

	        stmt.setInt(1, id);
	        return stmt.executeUpdate() > 0;

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}



	/*******
	 * <p> Method: List getAllEvaluationParameters </p>
	 * 
	 * <p> Description: Return a list of all grading parameters.  This is used by
	 * the Staff Parameters GUI to populate the table / list view. </p>
	 * 
	 * @return a List of EvaluationParameter objects
	 */
	public List<EvaluationParameter> getAllEvaluationParameters() {
	    List<EvaluationParameter> list = new ArrayList<>();
	    String sql = "SELECT id, name, description, weight FROM evaluationParameterDB";

	    try (PreparedStatement stmt = connection.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	            EvaluationParameter rec = new EvaluationParameter(
	                rs.getInt("id"),
	                rs.getString("name"),
	                rs.getString("description"),
	                rs.getInt("weight")
	            );
	            list.add(rec);
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}




	/*******
	 * <p> Method: int getTotalParameterWeight </p>
	 * 
	 * <p> Description: Computes the sum of all evaluation parameter weights currently
	 * stored in the database.  This is used to enforce the 100-point total limit. </p>
	 * 
	 * @return the integer sum of all weights (0 if no parameters exist)
	 */
	public int getTotalParameterWeight() {
	    String q = "SELECT COALESCE(SUM(weight), 0) AS totalWeight FROM evaluationParameterDB";
	    try (PreparedStatement ps = connection.prepareStatement(q)) {
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("totalWeight");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return 0;
	}

	// Insert a single evaluation row (one parameter for one student)
	public boolean createEvaluationRecord(String studentUsername,
	                                      String parameterName,
	                                      int score,
	                                      int weight,
	                                      String evaluatedBy) {
	    String sql = "INSERT INTO evaluationRecordDB " +
	                 "(studentUsername, parameterName, score, weight, evaluatedBy) " +
	                 "VALUES (?, ?, ?, ?, ?)";

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, studentUsername);
	        stmt.setString(2, parameterName);
	        stmt.setInt(3, score);
	        stmt.setInt(4, weight);
	        stmt.setString(5, evaluatedBy);
	        return stmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public List<User> getAllStudents() {
	    List<User> students = new ArrayList<>();
	    String sql = "SELECT * FROM userDB WHERE newRole1 = TRUE";

	    try (PreparedStatement ps = connection.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {

	        while (rs.next()) {
	            User u = new User(
	                rs.getString("userName"),
	                rs.getString("password"),
	                rs.getString("firstName"),
	                rs.getString("middleName"),
	                rs.getString("lastName"),
	                rs.getString("preferredFirstName"),
	                rs.getString("emailAddress"),
	                rs.getBoolean("adminRole"),
	                rs.getBoolean("newRole1"),
	                rs.getBoolean("newRole2")
	            );
	            students.add(u);
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return students;
	}

    /*******
     * <p> Method: insertStaffRequest </p>
     * 
     * <p> Description: Create a new staff → admin request in the
     * staffRequestDB table.  Returns the fully populated StaffRequest
     * as reloaded from the database, or null on failure. </p>
     */
    public StaffRequest insertStaffRequest(String sender,
                                           String subject,
                                           String body) {
        String sql = "INSERT INTO staffRequestDB "
                   + "(sender, subject, body, adminReply, isClosed, isRead) "
                   + "VALUES (?, ?, ?, '', FALSE, FALSE)";
        try (PreparedStatement ps = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, sender);
            ps.setString(2, subject);
            ps.setString(3, body);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return getStaffRequestById(id);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*******
     * <p> Method: getStaffRequestById </p>
     */
    public StaffRequest getStaffRequestById(int id) {
        String sql = "SELECT * FROM staffRequestDB WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStaffRequest(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*******
     * <p> Method: getStaffRequestsForUser </p>
     * 
     * <p> Description: Retrieve all requests created by the specified
     * staff user, ordered by newest first. </p>
     */
    public List<StaffRequest> getStaffRequestsForUser(String sender) {
        List<StaffRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM staffRequestDB "
                   + "WHERE sender = ? ORDER BY createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sender);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToStaffRequest(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /*******
     * <p> Method: getAllStaffRequests </p>
     * 
     * <p> Description: Retrieve all staff requests for admin access. </p>
     */
    public List<StaffRequest> getAllStaffRequests() {
        List<StaffRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM staffRequestDB ORDER BY createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToStaffRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /*******
     * <p> Method: updateStaffRequestBody </p>
     * 
     * <p> Description: Update subject/body of a request. Used when a
     * staff member edits an open request. </p>
     */
    public boolean updateStaffRequestBody(int id,
                                          String newSubject,
                                          String newBody) {
        String sql = "UPDATE staffRequestDB "
                   + "SET subject = ?, body = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newSubject);
            ps.setString(2, newBody);
            ps.setInt(3, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*******
     * <p> Method: updateStaffRequestAdminReply </p>
     * 
     * <p> Description: Update the adminReply field for a request. </p>
     */
    public boolean updateStaffRequestAdminReply(int id, String adminReply) {
        String sql = "UPDATE staffRequestDB SET adminReply = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, adminReply);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*******
     * <p> Method: setStaffRequestClosed </p>
     */
    public boolean setStaffRequestClosed(int id, boolean closed) {
        String sql = "UPDATE staffRequestDB SET isClosed = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, closed);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*******
     * <p> Method: setStaffRequestRead </p>
     */
    public boolean setStaffRequestRead(int id, boolean read) {
        String sql = "UPDATE staffRequestDB SET isRead = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, read);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*******
     * <p> Method: mapRowToStaffRequest </p>
     * 
     * <p> Description: Helper to convert a ResultSet row into a
     * StaffRequest object. </p>
     */
    private StaffRequest mapRowToStaffRequest(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String sender = rs.getString("sender");
        String subject = rs.getString("subject");
        String body = rs.getString("body");
        String adminReply = rs.getString("adminReply");
        boolean closed = rs.getBoolean("isClosed");
        boolean read = rs.getBoolean("isRead");

        Timestamp ts = rs.getTimestamp("createdAt");
        java.time.LocalDateTime createdAt = null;
        if (ts != null) {
            createdAt = ts.toLocalDateTime();
        }

        return new StaffRequest(id, sender, subject, body, adminReply, closed, read, createdAt);
    }






	// Attribute getters for the current user
	/*******
	 * <p> Method: String getCurrentUsername() </p>
	 * 
	 * <p> Description: Get the current user's username.</p>
	 * 
	 * @return the username value is returned
	 *  
	 */
	public String getCurrentUsername() { return currentUsername;};

	
	/*******
	 * <p> Method: String getCurrentPassword() </p>
	 * 
	 * <p> Description: Get the current user's password.</p>
	 * 
	 * @return the password value is returned
	 *  
	 */
	public String getCurrentPassword() { return currentPassword;};

	
	/*******
	 * <p> Method: String getCurrentFirstName() </p>
	 * 
	 * <p> Description: Get the current user's first name.</p>
	 * 
	 * @return the first name value is returned
	 *  
	 */
	public String getCurrentFirstName() { return currentFirstName;};

	
	/*******
	 * <p> Method: String getCurrentMiddleName() </p>
	 * 
	 * <p> Description: Get the current user's middle name.</p>
	 * 
	 * @return the middle name value is returned
	 *  
	 */
	public String getCurrentMiddleName() { return currentMiddleName;};

	
	/*******
	 * <p> Method: String getCurrentLastName() </p>
	 * 
	 * <p> Description: Get the current user's last name.</p>
	 * 
	 * @return the last name value is returned
	 *  
	 */
	public String getCurrentLastName() { return currentLastName;};

	
	/*******
	 * <p> Method: String getCurrentPreferredFirstName( </p>
	 * 
	 * <p> Description: Get the current user's preferred first name.</p>
	 * 
	 * @return the preferred first name value is returned
	 *  
	 */
	public String getCurrentPreferredFirstName() { return currentPreferredFirstName;};

	
	/*******
	 * <p> Method: String getCurrentEmailAddress() </p>
	 * 
	 * <p> Description: Get the current user's email address name.</p>
	 * 
	 * @return the email address value is returned
	 *  
	 */
	public String getCurrentEmailAddress() { return currentEmailAddress;};

	
	/*******
	 * <p> Method: boolean getCurrentAdminRole() </p>
	 * 
	 * <p> Description: Get the current user's Admin role attribute.</p>
	 * 
	 * @return true if this user plays an Admin role, else false
	 *  
	 */
	public boolean getCurrentAdminRole() { return currentAdminRole;};

	
	/*******
	 * <p> Method: boolean getCurrentNewRole1() </p>
	 * 
	 * <p> Description: Get the current user's Student role attribute.</p>
	 * 
	 * @return true if this user plays a Student role, else false
	 *  
	 */
	public boolean getCurrentNewRole1() { return currentNewRole1;};

	
	/*******
	 * <p> Method: boolean getCurrentNewRole2() </p>
	 * 
	 * <p> Description: Get the current user's Reviewer role attribute.</p>
	 * 
	 * @return true if this user plays a Reviewer role, else false
	 *  
	 */
	public boolean getCurrentNewRole2() { return currentNewRole2;};

	
	/*******
	 * <p> Debugging method</p>
	 * 
	 * <p> Description: Debugging method that dumps the database of the console.</p>
	 * 
	 * @throws SQLException if there is an issues accessing the database.
	 * 
	 */
	// Dumps the database.
	public void dump() throws SQLException {
		String query = "SELECT * FROM userDB";
		ResultSet resultSet = statement.executeQuery(query);
		ResultSetMetaData meta = resultSet.getMetaData();
		while (resultSet.next()) {
		for (int i = 0; i < meta.getColumnCount(); i++) {
		System.out.println(
		meta.getColumnLabel(i + 1) + ": " +
				resultSet.getString(i + 1));
		}
		System.out.println();
		}
		resultSet.close();
	}


	/*******
	 * <p> Method: void closeConnection()</p>
	 * 
	 * <p> Description: Closes the database statement and connection.</p>
	 * 
	 */
	// Closes the database statement and connection.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
}

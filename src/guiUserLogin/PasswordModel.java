package guiUserLogin;

import javafx.scene.paint.Color;

/*******
 * <p> Title: Model Class - establishes the required GUI data and the computations.
 * </p>
 *
 * <p> Description: This Model class is a major component of a Model View Controller (MVC)
 * application design that provides the user with a Graphical User Interface using JavaFX
 * widgets as opposed to a command line interface.
 * 
 * In this case the Model deals with an input from the user and checks to see if it conforms to
 * the requirements specified by a graphical representation of a finite state machine.
 * 
 * This is a purely static component of the MVC implementation.  There is no need to instantiate
 * the class.
 *
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 *
 * @author Lynn Robert Carter
 *
 * @version 2.00	2025-07-30 Rewrite of this application for the Fall 2025 offering of CSE 360
 * and other ASU courses.
 */

public class PasswordModel {
		
	/*******
	 * <p> Title: updatePassword - Protected Method </p>
	 * 
	 * <p> Description: This method is called every time the user changes the password (e.g., with 
	 * every key pressed) using the GUI from the PasswordEvaluationGUITestbed.  It resets the 
	 * messages associated with each of the requirements and then evaluates the current password
	 * with respect to those requirements.  The results of that evaluation are display via the View
	 * to the user and via the console.</p>
	 */

	
	/*-********************************************************************************************
	 * 
	 * Attributes used by the Finite State Machine to inform the user about what was and was not
	 * valid and point to the character of the error.  This will enhance the user experience.
	 * 
	 */

	public static String passwordErrorMessage = "";		// The error message text
	public static String passwordInput = "";			// The input being processed
	public static int passwordIndexofError = -1;		// The index where the error was located
	public static boolean foundUpperCase = false;
	public static boolean foundLowerCase = false;
	public static boolean foundNumericDigit = false;
	public static boolean foundSpecialChar = false;
	public static boolean foundLongEnough = false;
	public static boolean foundShortEnough = false;
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running

	/*
	 * This private method displays the input line and then on a line under it displays the input
	 * up to the point of the error.  At that point, a question mark is place and the rest of the 
	 * input is ignored. This method is designed to be used to display information to make it clear
	 * to the user where the error in the input can be found, and show that on the console 
	 * terminal.
	 * 
	 */


	/**********
	 * <p> Title: evaluatePassword - Public Method </p>
	 * 
	 * <p> Description: This method is a mechanical transformation of a Directed Graph diagram 
	 * into a Java method. This method is used by both the GUI version of the application as well
	 * as the testing automation version.
	 * 
	 * @param input		The input string evaluated by the directed graph processing
	 * @return			An output string that is empty if every things is okay or it will be
	 * 						a string with a helpful description of the error follow by two lines
	 * 						that shows the input line follow by a line with an up arrow at the
	 *						point where the error was found.
	 */
	
	public static String evaluatePassword(String input) {
		// The following are the local variable used to perform the Directed Graph simulation
		passwordErrorMessage = "";
		passwordIndexofError = 0;			// Initialize the IndexofError
		inputLine = input;					// Save the reference to the input line as a global
		currentCharNdx = 0;					// The index of the current character
		
		if(input.length() <= 0) {
			return "*** Error *** The password is empty!";
		}
		
		// The input is not empty, so we can access the first character
		currentChar = input.charAt(0);		// The current character from the above indexed position

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state.  This
		// local variable is a working copy of the input.
		passwordInput = input;				// Save a copy of the input
		
		// The following are the attributes associated with each of the requirements
		foundUpperCase = false;				// Reset the Boolean flag
		foundLowerCase = false;				// Reset the Boolean flag
		foundNumericDigit = false;			// Reset the Boolean flag
		foundSpecialChar = false;			// Reset the Boolean flag
		foundNumericDigit = false;			// Reset the Boolean flag
		foundLongEnough = false;			// Reset the Boolean flag
		foundShortEnough = false;
		
		
		// This flag determines whether the directed graph (FSM) loop is operating or not
		running = true;						// Start the loop

		// The Directed Graph simulation continues until the end of the input is reached or at some
		// state the current character does not match any valid transition
		while (running) {
			// The cascading if statement sequentially tries the current character against all of
			// the valid transitions, each associated with one of the requirements
			if (currentChar >= 'A' && currentChar <= 'Z') {
				System.out.println("Upper case letter found");
				foundUpperCase = true;
			} else if (currentChar >= 'a' && currentChar <= 'z') {
				System.out.println("Lower case letter found");
				foundLowerCase = true;
			} else if (currentChar >= '0' && currentChar <= '9') {
				System.out.println("Digit found");
				foundNumericDigit = true;
			} else if ("~`!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/".indexOf(currentChar) >= 0) {
				System.out.println("Special character found");
				foundSpecialChar = true;
			} else {
				passwordIndexofError = currentCharNdx;
				return "*** Error *** An invalid character has been found!";
			}
			if (currentCharNdx >= 7) {		//checks for length to be above 8 
				System.out.println("More than 8 characters found");	
				foundLongEnough = true;
			}
			if (inputLine.length() <= 32) { //checks for length to be 32 or less
				System.out.println("32 characters or less found");	//gives feedback of "32 chars or less found"
				foundShortEnough = true;
			}
			
			
			// Go to the next character if there is one
			currentCharNdx++;
			if (currentCharNdx >= inputLine.length())
				running = false;
			else
				currentChar = input.charAt(currentCharNdx);
			
			System.out.println();
		}
		
		// Construct a String with a list of the requirement elements that were found.
		String errMessage = "";
		if (!foundUpperCase)
			errMessage += "Upper case; ";
		
		if (!foundLowerCase)
			errMessage += "Lower case; ";
		
		if (!foundNumericDigit)
			errMessage += "Numeric digits; ";
			
		if (!foundSpecialChar)
			errMessage += "Special character; ";
			
		if (!foundLongEnough)
			errMessage += "Long Enough; ";
		
		if (!foundShortEnough)
			errMessage += "Short Enough; ";
		
		if (errMessage == "")
			return "";
		
		// If it gets here, there something was not found, so return an appropriate message
		passwordIndexofError = currentCharNdx;
		return errMessage + "conditions were not satisfied";
	}
}
 
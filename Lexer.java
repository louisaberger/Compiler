
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;


@SuppressWarnings("unused")
public class Lexer {
    
    static final String[][] types = {
	{"Type", "(int|float|void)"}, 
	{"Keyword", "(if|else|while|for|return)"},
	{"Hex", "(0x[0-9a-fA-F]+)"},
	{"Id", "([a-zA-Z][0-9a-zA-Z_]*)"},
	{"Relop", "(<=|>=|<|>|==|!=)"},
	{"Logicalop", "(\\&\\&|\\|\\|)"}, 
	{"Unaryop", "(!|\\&)"}, 
	{"Mulop", "(/|%)"},
	{"Assignop", "(=|\\+=|-=)"},
	{"Postfixop", "(\\+\\+|--)"},
	{"Plusop", "(\\+)"}, 
	{"Minusop", "(-)"},
	{"Punctuation", "(\\(|\\)|\\[|\\]|\\{|\\}|;|,)"},
	{"Real", "([0-9]+\\.[0-9]+[eE][+-]?[0-9]+)"},
	{"Real", "([0-9]+\\.[0-9]+)"},
	{"Real", "(\\.[0-9]+[eE][+-]?[0-9]+)"},
	{"Real", "(\\.[0-9]+)"},
	{"Real", "([0-9]+[eE][+-]?[0-9]+)"},
	{"Int", "([0-9]+)"}, //maximum of 2147483648, add exception? 
	{"Starop", "(\\*)"}, 
	{"Comment", "(\\/\\*.*\\*\\/)"},
	{"Commenterror", "(\\/\\*.*)"},
	{"Newline", "(\n|\r|\f)"},
	//Catch-all for errors.
	{"Error", "(^[a-zA-z].+)"}




    };
    Pattern pattern;
    String input = "";
    Matcher m;
    Token currentToken;
    Token nextToken;
    // Keeps track of what line we're on for better error messages.
    int currline = 1;
    
    public Lexer(File f) throws IOException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
	
	FileReader reader = new FileReader(f);
	int x = reader.read();
	while(x != -1) {
	    input += (char)x;
	    x = reader.read();
	}
	System.out.println("input is " + input);
	//Regular expression string of all entries in types array, to be passed into the matcher.
	String regExprString = types[0][1];
	for(int i = 1 ; i < types.length; i ++) {
	    regExprString += "|" + types[i][1];
	}
	pattern = Pattern.compile(regExprString);
	m = pattern.matcher(input);
	// To set the first token as nextToken before reading anything in.  
	getNextToken();
    }

    // Returns a Token object of the next token in the input string.
    // Sets nextToken to the next token if one exists. Returns currentToken.
    public Token getNextToken() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
	currentToken = nextToken;
	if(m.find()) {
	    for(int i = 1 ; i <= types.length; i++) {
		if(m.group(i) != null) {
		    if (types[i-1][0] == "Error") {
			nextToken = new Error(m.group(i), currline);
			i = types.length + 1;
		    } else {		
			nextToken = (Token)Class.forName( types[i-1][0] ) 
			    .getConstructor(String.class)
			.newInstance(m.group(i));
			nextToken.currline = currline;
			i = types.length + 1;
		    }
		}
	    }
	}
	else {
	    nextToken = null;
	}
	// Increment our current line if we've just read a newline.
	if ((currentToken != null) && currentToken.getClass() == Newline.class) {
	    currline++;
	}
	// Make sure we're on the right line
	if ((currentToken != null) && currentToken.getClass() == Error.class) {
	    currentToken.setCurrline(currline);
	}
	return currentToken;
    }

    // Returns a Token object of the next token in the input string.
    public Token peekNextToken() {
	return nextToken;

    }

    // Returns true if we have another token, false otherwise.
    public boolean hasNextToken() {
	return (nextToken!= null);
    }
    
    // Returns the current line.
    public int getCurrLine() {
	return currline;
    }

    public static void main(String []args) throws IOException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
	Lexer lex;
	if (args.length !=0) {
	         lex = new Lexer(new File(args[0]));
	         System.out.println(lex.pattern);}
	
	/* This would allow the user to enter lines in the console until CTRL-D, that are then written to a file that is passed to the lexer. 
	  else {
	    //Scanner console = new Scanner(System.in);
	    //File stdin = new File("input");
	    try {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		PrintStream stream = new PrintStream("input.txt");
		String testLine = null;
		//String line = "";
		while((testLine = reader.readLine()) != null) {
		    //line = line + "" + testLine;
		    stream.println(testLine);
		    stream.flush();
		}
		stream.close();
	    }
	    catch(IOException e){
		System.out.println("Error during reading/writing");
	    }
	    lex = new Lexer(new File("input.txt"));
	}*/
	else {
	    System.out.println("Please enter the name of the file to read from: ");
	    Scanner console = new Scanner(System.in);
	    String filename = console.nextLine();
	    lex = new Lexer(new File(filename));
	    System.out.println(lex.pattern);
	    }
    while(lex.hasNextToken()){
	    Token returnToken = lex.getNextToken();
	    if(returnToken.getClass()==(Error.class)) {
		System.out.println(returnToken.toString());
		return;
	    }
	    else if (returnToken.getClass()==(Int.class)){
		try{
		    Integer.parseInt(returnToken.name);
		    System.out.println(returnToken.toString() + " ");
		}
		catch(NumberFormatException e){
		    Token errorToken = new Error("Out of bounds integer", returnToken.currline);
		    System.out.println(errorToken.toString());
		}
	    }
	    else {
		System.out.print(returnToken.toString() + " ");
	    }
	}
    }

}

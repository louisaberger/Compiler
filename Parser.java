import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Stack;

//TODO: Consistency with when we pop the top of the stack re: errors.
//TODO: LOOK AT THE NEXTS

public class Parser {
    public static Lexer lex;
    String[][] parseTable = new String[5][];
    static Stack<ParseNode> parseStack = new Stack<ParseNode>();
    public static String errorMessage = "";
    public static Token curr;
    public static Token next;
    public static ParseNode top;
    //public static int numTabs = 0;

    public static void main(String[] args) throws IllegalArgumentException,
	    SecurityException, IOException, InstantiationException,
	    IllegalAccessException, InvocationTargetException,
	    NoSuchMethodException, ClassNotFoundException {

	if (args.length != 0) {
	    lex = new Lexer(new File(args[0]));
	    System.out.println(lex.pattern);
	} else {
	    System.out.println("Please enter the name of the file to read from: ");
	    Scanner console = new Scanner(System.in);
	    String filename = console.nextLine();
	    lex = new Lexer(new File(filename));
	    System.out.println(lex.pattern);
	}
	// initialize stack and input pointers
	parseStack.push(new ParseNode("$"));
	ParseNode root = new ParseNode("PROG");
	parseStack.push(root);
	top = parseStack.peek();
	curr = lex.getNextToken();
	next = lex.peekNextToken();
	
	while (! (top.name.equals( "$")) ){
	    top = parseStack.peek();
	    parse();
	}
	
	printTree(root, 0);

    }

    public static void parse() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {

	printStack();
	errorMessage = "ERROR at line " + lex.currline + ":";

	// Match terminals
	if( top.isTerminal ) {
	    if( top.name.equals(curr.name )) {
		System.out.println("MATCh TERMINAL " + top.name + " ON STACK!");
		parseStack.pop();
		updateInputPointers();		
	    }
	    else if (top.name.equals( "num" )) {
		if(contains(curr.tokenType,"hex","int")) {
		    System.out.println("MATCHED TERminAL " + top.name + " ON STACK!");
		    parseStack.pop();
		    updateInputPointers();
		}
	    }
	    else if (top.name.equals("id") ){
		if (curr.tokenType.equals( "id")) {
		    System.out.println("MATCHED TERminAL " + top.name + " ON STACK!");
		    parseStack.pop();
		    updateInputPointers();
		}
	    }
	    else {
		System.out.println(errorMessage + "terminal " + top.name + " on stack does not match input string. Ignoring this terminal.");
		parseStack.pop();
		updateInputPointers();
	    }
	}
	// Reduce
	else {
	    System.out.println("pop " + top);
	    if(curr != null)
		System.out.println("curr is " + curr.name);
	    else System.out.println("curr is null");
	    parseStack.pop();
	    printStack();
		if(top.name == "PROG") {
		    // curr is in first(declist)
		    if (contains(curr.name,"int","float","void"))
			addChildrenAndPush(top, "DECLIST");
		    else
			System.out.println(errorMessage + " programs must start with a declarative statement");
		}
		else if (top.name == "DECLIST") {
		    // curr in first(dec)?
		    if(contains(curr.name, "int", "float", "void"))
			addChildrenAndPush(top, "DECLISTROE", "DEC");
		    else 
			System.out.println(errorMessage + " a declarative list must start with a declaration");
		}
		else if (top.name == "DECLISTROE") {
		    // TODO: weird... curr is in follow(declistROE) = follow(DECLIST) = follow(PROG) = $
		    // So we check if there is no curr
		    if (curr == null) {
			// at the end of our input and stack.
		    }
		    // curr is in first(declist)
		    else if(contains(curr.name, "int", "float", "void"))
			addChildrenAndPush(top, "DECLIST");
		    else
			System.out.println(errorMessage + "declist can only contain decs");
		}
		
		else if (top.name == "DEC") {
		    // curr is in first(type-spec)?
		    if (contains(curr.name, "int","float","void"))
			addChildrenAndPush(top, "DECROE", "TYPE");
		    else
			System.out.println(errorMessage + " declarations must start with a type");
		}
		
		else if (top.name == "DECROE") {
		    if (contains(curr.name,"*")) {
			addChildrenAndPush(top, "DECROE2");
			addIdRealChildAndPush(top, next);
			addTerminalChildrenAndPush(top, curr);
		    }

		    else if (curr.tokenType.equals( "id")) {
			addChildrenAndPush(top, "DECROE3");
			addIdRealChildAndPush(top, curr);
		    }
		    else {
			System.out.println(errorMessage + " declarations must start with a type and then a * or id");
		    }
		}
		else if (top.name == "DECROE2") {
		    if (curr.name.equals( ";"))
			addTerminalChildrenAndPush(top, curr);
		    else if (curr.name.equals( "(")) {
			addChildrenAndPush(top, "COMPOUND");
			addTerminalChildrenAndPush(top, new Punctuation(")"));
			addChildrenAndPush(top, "PARAMS");
			addTerminalChildrenAndPush(top, new Punctuation("("));
		    }
		    else
			System.out.println(errorMessage + " should be a ';' or ')'");
		}
		else if (top.name == "DECROE3") {
		    if (curr.name.equals( ";"))
			addTerminalChildrenAndPush(top, new Punctuation(";"));
		    else if (curr.name.equals( "[")) {
			addTerminalChildrenAndPush(top, new Punctuation(";"), new Punctuation("]"));
			addNumChildAndPush(top, next);
			addTerminalChildrenAndPush(top, new Punctuation("["));
		    }
		    else if  (curr.name.equals( "(")) {
			addChildrenAndPush(top, "COMPOUND");
			addTerminalChildrenAndPush(top, new Punctuation(")"));
			addChildrenAndPush(top, "PARAMS");
			addTerminalChildrenAndPush(top, new Punctuation("("));
		    }
		    else
			System.out.println(errorMessage + " declarations must end with a ; or [num]; or (params) compound-stmt");
		}
		else if (top.name == "TYPE") {
		    if (contains(curr.name, "int","float","void"))
			addTerminalChildrenAndPush(top, curr);
		    else 
			System.out.println(errorMessage + " type must be int, float, or void");
		}
		
		else if (top.name == "PARAMS") {
		    // special case: either params --> void paramROE or params--> void check the next:
		    if (curr.name.equals( "void")) {
			// next is in first(paramROE) ?
			if (contains (next.name, "id", "*"))
			    addChildrenAndPush(top, "PARAMLIST");
			// next is in follow(params) ?
			else if (contains(next.name, ")"))
			    addTerminalChildrenAndPush(top, curr);
			else
			    System.out.println(errorMessage +  " void in a parameter must be followed by id, *id, or be the end of a paramter");
		    }
		    else if (contains(curr.name,"int","float"))
			addChildrenAndPush(top, "PARAMLIST");
		    else
			System.out.println(errorMessage + " params must be a param list or void");
		}

		else if (top.name == "PARAMLIST") {
		    // curr in first(param)?
		    if (contains(curr.name, "int", "float", "void"))
			    addChildrenAndPush(top, "PARAMLISTROE", "PARAM");
		    else 
			System.out.println(errorMessage + " paramlist must contain only params");
		}
		else if (top.name == "PARAMLISTROE") {
		    if(contains(curr.name, ",")) {
			addChildrenAndPush(top, "PARAMLIST");
			addTerminalChildrenAndPush(top, curr);
		    }
		    // EPSILON curr is in follow(paramlistROE) ?
		    else if (contains(curr.name, ")"));
		    else
			System.out.println(errorMessage + " param list can only contain params");
		}
			
		else if (top.name == "PARAM") {
		    // curr in first(type-spec) ?
		    if (contains(curr.name,"int","float","void"))
			addChildrenAndPush(top, "PARAMROE", "TYPE");
		    else
			System.out.println(errorMessage + " param must start with a type specifier");		
		}
		    
		else if (top.name == "PARAMROE") {
		    if (curr.tokenType.equals( "id")) {
			addChildrenAndPush(top, "PARAMROE2");
			addIdRealChildAndPush(top, curr);
		    }
		    else if (curr.name.equals( "*")) {
			addIdRealChildAndPush(top, next);
			addTerminalChildrenAndPush(top, curr);
		    }
		    else
			System.out.println(errorMessage + " param must be type spec. followed by id or *");
		}
		else if (top.name == "PARAMROE2") {
		    if (curr.name.equals( "["))
			addTerminalChildrenAndPush(top, next, curr);
		    // EPSILON curr is in follow(paramROE2) ?
		    else if (contains(curr.name, ")", ","));
		    else 
			System.out.println(errorMessage + " type spec must be followed by [] or epsilon");
		}
		    
		else if (top.name == "COMPOUND") {
		    if (curr.name.equals( "{") ){
			addTerminalChildrenAndPush(top, new Punctuation("}"));
			addChildrenAndPush(top, "STMT-LIST", "LOCAL-DEC");
			addTerminalChildrenAndPush(top, curr);
		    }
		    else
			System.out.println(errorMessage + " compound statement must start with {");
		}
		    
		else if (top.name == "LOCAL-DEC") {
		    // curr in first(type-spec) ?
		    if (contains(curr.name,"int","float","void"))
			addChildrenAndPush(top, "LOCALROE", "TYPE");
		    // curr is in follow(locdec) ?
		    else if (contains(curr.name, "{", "if", "while", "for","return","+","-","!","&","*","(")||idNumRealCheck(curr.tokenType));
		    else 
			System.out.println("local declarations must be a variable declaration or epsilon");
		}
		    
		else if (top.name == "LOCALROE") {
		    if(curr.tokenType.equals( "id")) {
			addChildrenAndPush(top, "LOCALROE2");
			addIdRealChildAndPush(top, curr);
		    }
		    else if (curr.name.equals( "*") ){
			addTerminalChildrenAndPush(top, new Punctuation(";"));
			addIdRealChildAndPush(top, next);
			addTerminalChildrenAndPush(top, curr);
		    }
		    else
			System.out.println(errorMessage + " must have a local declaration or epsilon");
		}
		else if (top.name == "LOCALROE2") {
		    if (curr.name.equals( ";"))
			addTerminalChildrenAndPush(top, curr);
		    else if (curr.name.equals( "[")) {
			addTerminalChildrenAndPush(top, new Punctuation(";"), new Punctuation("]"));
			addNumChildAndPush(top, next);
			addTerminalChildrenAndPush(top, curr);
		    }
		    else
			System.out.println(errorMessage + " must end a local-declaration with a ; or [num];");
		}

		else if (top.name == "STMT-LIST") {
		    // curr is in first (stmt)
		    if (contains(curr.name, "(", "+", "-", "!", "&", "*", "{", "if", "while", "for", "return") || idNumRealCheck(curr.tokenType))
			addChildrenAndPush(top, "STMTLISTROE", "STMT");
		    // cur is in follow(stmtlist)
		    else if (contains(curr.name, "}" ));
		    else
			System.out.println(errorMessage + " statement-list must contain statements");		    
		}
		else if (top.name == "STMT") {
		    if (curr.name.equals( "return")) {
			addChildrenAndPush(top, "RETURN-STMT");
		    }
		    else if (curr.name.equals( "for") ){
			addChildrenAndPush(top, "FOR-STMT");
		    }
		    else if (curr.name.equals( "while") ){
			addChildrenAndPush(top, "WHILE-STMT");
		    }
		    else if (curr.name.equals( "if")) {
			addChildrenAndPush(top, "IF-STMT");
		    }
		    else if (curr.name.equals( "{") ){
			addChildrenAndPush(top, "COMPOUND");
		    }
		    // curr in first(expr-stmt) ?
		    else if (contains(curr.name, "+","-","!","&","*","(", ";") || idNumRealCheck(curr.tokenType))
			addChildrenAndPush(top, "EXPR-STMT");
		    else
			System.out.println(errorMessage + " illegal beginning of a statement");
		}
 
		else if (top.name == "EXPR-STMT") {
		    //curr in first(or-expr) ?
		    if (contains(curr.name, "+", "-", "!", "&", "*", "(") || idNumRealCheck(curr.tokenType)) {
			addTerminalChildrenAndPush(top, new Punctuation(";"));
			addChildrenAndPush(top, "OPT-EXPR");
		    }
		    else
			System.out.println(errorMessage + " expressional statement must begin with an optional expression");
		}
		    
		else if (top.name == "IF-STMT") {
		    if(curr.name.equals( "if")) {
			addChildrenAndPush(top, "IFROE","STMT");
			addTerminalChildrenAndPush(top, new Punctuation(")"));
			addChildrenAndPush(top, "EXPR");
			addTerminalChildrenAndPush(top, new Punctuation("("), curr);
		    }
		    else
			System.out.println(errorMessage + " if statment must begin with 'if'");
		}
		
		else if (top.name == "IFROE") {
		    // if curr=else, could be IFROE--> else stmt OR IFROE --> \eps, followed by an else.
		    // But in the second case, we must be in an ifROE anyways! so we just add it.
		    if (curr.name.equals( "else" ) ){
			addChildrenAndPush(top, "STMT");
			addTerminalChildrenAndPush(top, curr);
		    }
		    // curr in follow(ifROE) ?
		    else if (contains(curr.name, "}","+","-","!","&","*","(", ";","{","if","while","for","return")||idNumRealCheck(curr.tokenType));
		    else
			System.out.println(errorMessage + " if statement must end in else or have the right follow");
		}
		else if (top.name == "WHILE-STMT") {
		    if (curr.name.equals( "while")) {
			addChildrenAndPush(top, "STMT");
			addTerminalChildrenAndPush(top, new Punctuation(")"));
			addChildrenAndPush(top, "EXPR");
			addTerminalChildrenAndPush(top, new Punctuation("("), curr);
		    }
		    else
			System.out.println(errorMessage + " while stmt must begin with while");
		}
		else if (top.name == "FOR-STMT") {
		    if (curr.name.equals( "for") ){
			addChildrenAndPush(top, "STMT");
			addTerminalChildrenAndPush(top, new Punctuation(")"));
			addChildrenAndPush(top, "OPT-EXPR");
			addTerminalChildrenAndPush(top, new Punctuation(";"));
			addChildrenAndPush(top, "OPT-EXPR");
			addTerminalChildrenAndPush(top, new Punctuation(";"));
			addChildrenAndPush(top, "OPT-EXPR");
			addTerminalChildrenAndPush(top, new Punctuation("("), curr);
		    }
		    else
			System.out.println(errorMessage + " for stmt must begin with for");
		}
		else if  (top.name == "RETURN-STMT") {
		    if (curr.name.equals( "return")) {
			if(next.tokenType.equals( ";")) {
			    addTerminalChildrenAndPush(top, next, curr);
			}
			// next in first(expr) ?
			else if (contains(next.name,"+","-","!","&","*","(")||idNumRealCheck(curr.tokenType)) {
			    addTerminalChildrenAndPush(top, new Punctuation (";"));
			    addChildrenAndPush(top, "EXPR");
			    addTerminalChildrenAndPush(top, curr);
			}
			else
			    System.out.println(errorMessage + " return statement must have an expression or ;");
		    }
		    else
			System.out.println(errorMessage + " return stmt must begin with return");
		}
		    
		else if (top.name == "OPT-EXPR") {
		    // if curr.name is in firsT(expr)
		    if(contains(curr.name,"+","-","!","&","*","(")||idNumRealCheck(curr.tokenType))
			addChildrenAndPush(top, "EXPR");
		    
		    // if curr.name is in follow of opt-expr--> EPSILON 
		    else if (curr.name.equals( ";") || curr.name.equals( ")"));
		    else
			System.out.println(errorMessage + " optional expr must be epsilon or expression");
		}
		else if (top.name == "EXPR") {
		    // curr is in first(or-expr) ?
		    if (contains(curr.name,"+","-","!","&","*","(")||idNumRealCheck(curr.tokenType))
			addChildrenAndPush(top, "EXPRROE", "OR-EXPR");
		    else System.out.println(errorMessage+" expression must begin with or expression");
		}
		else if (top.name == "EXPRROE") {
		    // curr is in first(assignop) ?
		    if(curr.name.equals("=")||curr.name.equals("+=")||curr.name.equals("-="))
			addChildrenAndPush(top, "EXPR", "ASSIGNOP");
		    //curr is in follow (exprROE) ?
		    else if (curr.name.equals(")")||curr.name.equals(";"));
		    else 
			System.out.println(errorMessage + " expression must end with assignop expression or epsilon");
		}
		else if(top.name=="ASSIGNOP" ) {
		    if(curr.name.equals("=")||curr.name.equals("+=")||curr.name.equals("-="))
			addTerminalChildrenAndPush(top, curr);
		    else 
			System.out.println(errorMessage + " need an assignop");
		}
		else if (top.name=="OR-EXPR") {
		    // curr is in first(and-expr) ?
		    if (contains( curr.name, "(", "+", "-", "!", "&", "*") || idNumRealCheck(curr.tokenType))
			addChildrenAndPush(top, "ORROE", "AND-EXPR");
		    else 
			System.out.println(errorMessage + " or expression must start with a primary expression or unary op");
		}
		else if (top.name == "ORROE") {
		    if (contains(curr.name, "||")) {
			addChildrenAndPush(top, "AND-EXPR");
			addTerminalChildrenAndPush(top, curr);
		    }
		    //curr is in follow(orROE) ?
		    else if (contains(curr.name, "=", "+=","-=", ")", ";"));
		    else
			System.out.println(errorMessage + " or expression must contain ||");
		}
		    
		else if (top.name=="AND-EXPR") {
		    //curr in first(rel-expr) ?
		    if(contains(curr.name, "+","-","!","&","*","(")||idNumRealCheck(curr.tokenType) )
			addChildrenAndPush(top, "ANDROE","REL-EXPR");
		    else
			System.out.println(errorMessage + " and-expr must start with a relational expr");
		}
		
		else if (top.name=="ANDROE") {
		    if(contains(curr.name, "&&")) {
			addChildrenAndPush(top, "REL-EXPR");
			addTerminalChildrenAndPush(top, curr);
		    }
		    // curr in follow(andROE) ? EPSILON
		    else if (contains(curr.name, "||","=","+=","-=",")",";"));
		    else 
			System.out.println(errorMessage + " and expression must be a list of relational expr && relational expr &&...");
		}

		else if (top.name=="REL-EXPR") {
		    // curr in first(add-expr)
		    if(contains(curr.name, "+","-","!","&","*","(")||idNumRealCheck(curr.tokenType) )
			addChildrenAndPush(top, "RELROE", "ADD-EXPR");
		    else
			System.out.println(errorMessage + " rel-expr must start with an additive expr");
		}
		
		else if (top.name =="RELROE") {
		    // curr in first(relop)
		    if(contains(curr.name, "<=",">=","<",">","==","!="))
			addChildrenAndPush(top, "ADD-EXPR","RELOP");
		    //curr in follow(relROE) ?
		    else if (contains(curr.name, "&&","||","=","+=","-=",")",";"));
		    else
			System.out.println(errorMessage + " relational expr must be additive expr relop additive-expr relop ...");
		}
		
		else if (top.name=="RELOP") {
		    if(contains(curr.name, "<=",">=","<",">","==","!="))
			addTerminalChildrenAndPush(top, curr);
		    else 
			System.out.println(errorMessage + " not a relop");
		}
		else if (top.name == "ADD-EXPR") {
		    // curr in first of term?
		    if (contains(curr.name, "+","-","!","&","*","(")|| idNumRealCheck(curr.tokenType))
			addChildrenAndPush(top, "ADDROE","TERM");
		    else 
			System.out.println(errorMessage + " must start with a term");
		}
		
		else if (top.name == "ADDROE") {
		    // curr in first(addop)
		    if (contains(curr.name, "+","-"))
			addChildrenAndPush(top, "TERM","ADD-EXPR");
		    //curr in follow(addROE) EPSILON
		    else if(contains(curr.name,"<=", ">=","<", ">", "==", "!=", "&&", "||",  "=", "+=", "-=", ")", ";"));
		    else 
			System.out.println(errorMessage + " additive exprs must be term add-expr term add-expr ...");
		}
		    
		else if (top.name == "ADDOP") {
		    if (contains(curr.name, "+", "-")) 
			addTerminalChildrenAndPush(top, curr);
		    else 
			System.out.println(errorMessage + " need an addop");
		}

		else if (top.name == "TERM") {
		    //cur is in first(unary-expr)
		    if(contains(curr.name, "+","-","!","&","*","(")||idNumRealCheck(curr.tokenType) )
			addChildrenAndPush(top, "TERMROE","UNARYOP");
		    else
			System.out.println(errorMessage + " term must begin with a unary-expr");		    
		}
		
		else if (top.name=="TERMROE") {
		    //curr in first(mulop)
		    if(contains (curr.name, "*", "/", "%"))
			addChildrenAndPush(top, "UNARYEXPR","MULOP");
		    // curr in follow(termROE) ?
		    else if(contains(curr.name, "+","-","<=", ">=","<", ">", "==", "!=", "&&", "||",  "=", "+=", "-=", ")", ";"));
		    else
			System.out.println(errorMessage + " term must be made up of unary exprs and mulops");
		}
		
		else if (top.name == "MULOP ") {
		    if(contains (curr.name, "*", "/", "%"))
			addTerminalChildrenAndPush(top, curr);
		    else 
			System.out.println(errorMessage + " need a mulop");
		}
		else if (top.name == "UNARYEXPR") {
		    //curr is in first (postfix)
		    if (contains(curr.name, "(")||idNumRealCheck(curr.tokenType))
			addChildrenAndPush(top, "POSTFIX-EXPR");		    
		    // curr in first(unop)
		    else if (contains(curr.name, "+","-","!","&","*"))
			addChildrenAndPush(top, "UNEXROE", "UNARYOP");
		    else
			System.out.println(errorMessage + " unary expression must begin with postfix expr or unary op");
		}
		else if (top.name == "UNEXROE") {
		    //curr in first(un-expr)
		    if (contains (curr.name, "+","-","!","&","*","(")|| idNumRealCheck(curr.tokenType)) 
			addChildrenAndPush(top, "UNARYEXPR");
		    //curr in follow(un-exROE)
		    else if(contains(curr.name, "+","-","<=", ">=","<", ">", "==", "!=", "&&", "||",  "=", "+=", "-=", ")", ";", "*","/","%"));
		    else 
			System.out.println(errorMessage + " unary expression has incorrect beginning");
		}
		else if (top.name == "UNARYOP") {
		    if (contains (curr.name, "+", "-", "!", "&", "*") )
			addTerminalChildrenAndPush(top, curr);
		    else
			System.out.println(errorMessage + " need a unary op");
		}
		else if (top.name == "POSTFIX-EXPR") {
		    // curr in first(primary-expr) ?
		    if (contains (curr.name, "(")||idNumRealCheck(curr.tokenType))
			addChildrenAndPush(top, "POSTFIXROE", "PRIMARY-EXPR");
		    else 
			System.out.println(errorMessage + " postfix expr must start with a primary expression");
		}
		else if (top.name == "POSTFIXROE") {
		    if (contains(curr.name, "[")) {
			addTerminalChildrenAndPush(top, new Punctuation("]"));
			addChildrenAndPush(top, "EXPR");
			addTerminalChildrenAndPush(top, curr);
		    }
		    else if (contains(curr.name, "++", "--"))
			    addTerminalChildrenAndPush(top, curr);
		    // curr in follow(postfixROE) ?
		    else if (contains(curr.name, "<=", ">=", "<", ">", "==", "!=", "=", "+=", "-=", ")", ";", "||", "&&", "+", "-", "*", "/", "%"));
		    else 
			System.out.println(errorMessage + " incorrect postfix expression");
			
		}
		else if (top.name == "PRIMARY-EXPR") {
		    if (contains(curr.name, "(")) {
			addTerminalChildrenAndPush(top, new Punctuation(")"));
			addChildrenAndPush(top, "EXPR");
			addTerminalChildrenAndPush(top, curr);
		    }
		    else if (curr.tokenType.equals( "int") || curr.tokenType.equals( "hex"))
			addNumChildAndPush(top, curr);
		    else if (curr.tokenType.equals( "real"))
			addIdRealChildAndPush(top, curr);
		    else if (curr.tokenType.equals( "id" )) {
			addChildrenAndPush(top, "PRIMROE");
			addIdRealChildAndPush(top, curr);
		    }
		    else 
			System.out.println(errorMessage + " primary expr must be an expression, id, call, num, or real");
		}
		else if (top.name == "PRIMROE") {
			if(curr.name.equals( "(")) {
			    addTerminalChildrenAndPush(top, new Punctuation(")"));
			    addChildrenAndPush(top, "ARGS");
			    addTerminalChildrenAndPush(top, curr);
			}
			// curr in follow(PRIMROE) ?
			else if (contains(curr.name, "[", "++", "--", "<=", ">=", "<", ">", "==", "!=", "=", "+=", "-=", ")", ";", "||", "&&", "+", "-", "*", "/", "%")) {
			    
			}
			else 
			    System.out.println(errorMessage + " id must be followed by (args) or epsilon");
		}
		else if (top.name == "ARGS") {
		    // curr is in first(arg-list)
		    if (contains(curr.name, "+", "-", "!", "&", "*", "(")|| idNumRealCheck(curr.tokenType))
			addChildrenAndPush(top, "ARGLIST");		    
		    // curr is in follow(args) --> EPSILON-- put nothing on stack
		    else if  (contains(curr.name, ")"));
		    else 
			System.out.println(errorMessage + " args must have an arglist or be epsilon");
		}
		else if (top.name == "ARGLIST") {
		    //curr is in first(expr)
		    if (contains(curr.name, "+", "-", "!", "&", "*", "(") || idNumRealCheck(curr.tokenType))
			addChildrenAndPush(top, "ARGLISTROE", "EXPR");
		    else 
			System.out.println(errorMessage + " arglist must begin with an expression");
		}
		else if (top.name == "ARGLISTROE") {
		    if (contains(curr.name, ",")) {
			addChildrenAndPush(top, "ARGLIST");
			addTerminalChildrenAndPush(top, curr);
		    }
		    // curr is in follow(arglistROE) --> EPSILON: put nothing on stack
		    else if (contains(curr.name, ")" ));
		    else
			System.out.println(errorMessage + " arglist must have list of args");
		}
		    
		}
		    
		
	
    }
    
    public static void addChildrenAndPush(ParseNode parentNode, String... args) {
	for(String name: args) {
	    ParseNode childNode = new ParseNode(name);
	    parentNode.addChildren(childNode);
	    parseStack.push(childNode);
	}
    }
    
    public static void addTerminalChildrenAndPush(ParseNode parentNode, Token... args) {
	for (Token terminal: args) {
	    // add id child to the parent
	    ParseNode childNode = new ParseNode(terminal.tokenType, true);
	    parentNode.addChildren(childNode);
	    // add 9 child to the id node
	    ParseNode grandChildNode = new ParseNode(terminal.name, true);
	    childNode.addChildren(grandChildNode);
	    // push the 9 on stack
	    parseStack.push(grandChildNode);
	}
    }
    
    public static void addIdRealChildAndPush(ParseNode parentNode, Token child) {
	ParseNode childNode = new ParseNode(child.tokenType, true);
	childNode.addChildren(new ParseNode(child.name, true));
	parentNode.addChildren(childNode);
	parseStack.push(childNode);
    }
    
    public static void addNumChildAndPush(ParseNode parentNode, Token child) {
	ParseNode childNode = new ParseNode("num", true);
	childNode.addChildren(new ParseNode(child.name, true));
	parentNode.addChildren(childNode);
	parseStack.push(childNode);
    }
    
    public static boolean contains(String x, String... possibilities) {
	for (String i: possibilities) {
	    if (x.equals(i))
		return true;
	}
	return false;
    }
    
    public static boolean idNumRealCheck(String tokenType) {
	if (tokenType=="id"||tokenType=="real"||tokenType=="int"||tokenType=="hex") return true;
	return false;
    }
    
    public static void updateInputPointers() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
	if (lex.hasNextToken()) {
	    curr = lex.getNextToken();
	    if (lex.hasNextToken())
		next = lex.peekNextToken();
	    else
		next = null;
	}
	else {
	    curr = null;
	    next = null;
	}
    }

    // Method to print out a tree in Scheme-expression-style notation.
    public static void printTree(ParseNode root, int numTabs) {
	// check for a leaf node
	if(root.children.size()==0) {
	    printTabs(numTabs);
	    System.out.println(root.name);
	}
	// check for a terminal that has the extra info
	else if (root.isTerminal) {
	    printTabs(numTabs);
	    System.out.println(root.toString()+ "("+root.children.get(0).name+")");
	}
	else {
	    printTabs(numTabs);
	    System.out.println("("+ root.name);
	    if(root.children.size() > 0 )
	    for (int i = (root.children.size() - 1); i >= 0; i --)
		printTree(root.children.get(i), numTabs + 1);
	    printTabs(numTabs);
	    System.out.println(")");
	}
    }
    
    public static void printTabs(int numTabs) {
	for (int i = 0 ; i <numTabs ; i ++ )
	    System.out.print("   ");
    }
    
    // testing method to print out the stack
    public static void printStack() {
	System.out.print("STACK: ");
	Iterator<ParseNode> it = parseStack.iterator();
	while(it.hasNext())
	    System.out.print(it.next().toString());
	System.out.println();
    }

}

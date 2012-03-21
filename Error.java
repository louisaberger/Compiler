
public class Error extends Token {
    // passed in by the constructor-- keeps track of what line you're on.
    int currline;
    
    public Error(String name, int currline) {
	super(name);
	this.currline = currline;
    }

    public Error(String name) {
	super(name);
	// TODO Auto-generated constructor stub
    }
    
    public Error() {
	
    }
    
    public void setCurrline(int currline) {
	this.currline = currline;
    }
    
    public String toString() {
	if(name != null) {
	    return "\n ERROR on line " + currline + ": "+ name+ " is not a valid token";
	}
	else {
	    return "ERROR";
	}
    }

}
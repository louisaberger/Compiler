
public abstract class Token {
    
    public String name;
    public String tokenType = "";
    public int currline = 0;
    
    public Token(String name) {
	this.name = name;
    }
    
    public Token(String name, String tokenType) {
	this.name = name;
	this.tokenType = tokenType;
    }
    
    
    public Token() {
    }
    
    public void setCurrline(int currline) {
	this.currline = currline;
    }
    // rewrite this method for type, keyword, punctuation   
    public String toString() {
	return tokenType+"("+name+")";
    }


    
}

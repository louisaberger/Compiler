
public class Int extends Token{
    public Int(String name) {
        this.name= name;
	this.tokenType = "int";
    }
    public String toString(){
	return tokenType+"("+name+")";
    }

}

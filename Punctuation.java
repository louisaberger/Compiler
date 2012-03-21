
public class Punctuation extends Type{

    public Punctuation(String name) {
	super(name);
    }
    
    public String toString() {
	if (name.indexOf("(") > -1) return "leftp";
	if (name.indexOf(")") > -1) return "rightp";
	if (name.indexOf("[") > -1) return "lbrace";
	if (name.indexOf("]") > -1)return "rbrace";
	if (name.indexOf("{") > -1) return "lcurlbrace";
	if (name.indexOf("}") > -1)return "rcurlbrace";	
	if (name.indexOf(";") > -1) return "semic";
	if (name.indexOf(",") > -1) return "comma";
	return name;
    }

}

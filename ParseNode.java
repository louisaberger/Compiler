
import java.util.ArrayList;


public class ParseNode {
    
    public String name = "";
    ArrayList <ParseNode> children = new ArrayList <ParseNode> (); 
    public boolean isTerminal;
    
    public ParseNode(String name) {
      	this.name = name;
	this.isTerminal = false;
    }
    
    public String toString() {
	return name;
    }
    
    public ParseNode(String name, boolean isTerminal) {
	this.name = name;
	this.isTerminal = isTerminal;
    }
    
    public int numChildren () {
	return children.size();
    }
    
    public ArrayList <ParseNode> getChildren() {
	return children;
    }
    
    public ArrayList <ParseNode> addChildren(ArrayList <ParseNode> children) {
	for(int i= 0 ; i < children.size(); i++) {
		this.children.add(children.get(i));
	}
	return children;
    }
    
    public ArrayList <ParseNode> addChildren(ParseNode child ) {
	this.children.add(child);
	return children;
    }

}

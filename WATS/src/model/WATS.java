package model;

import java.util.ArrayList;

import skf.coder.HLAunicodeStringCoder;
import skf.model.object.annotations.Attribute;
import skf.model.object.annotations.ObjectClass;

@ObjectClass(name = "PhysicalEntity.WATS")
public class WATS {
	
	@Attribute(name = "name", coder = HLAunicodeStringCoder.class)
	private String entity_name = null;
	
	@Attribute(name = "parent_reference_frame", coder = HLAunicodeStringCoder.class)
	private String parent_reference_frame = null;
	
	@Attribute(name = "type", coder = HLAunicodeStringCoder.class)
	private String entity_type = null;
	
//	@Attribute(name = "position", coder = HLAPositonCoder.class)
	private Position position = null;
	
	private ArrayList<Recipes> recipes = new ArrayList<Recipes>();
	
	public WATS() {}
	
	public WATS(String entity_name, String parent_reference_frame, String entity_type, Position position){
		this.entity_name = entity_name;
		this.parent_reference_frame = parent_reference_frame;
		this.entity_type = entity_type;
		this.position = position;
		this.recipes=readRecipes();
	}

	
	public ArrayList<Recipes> getRecipes() {
		return recipes;
	}

	public void setRecipes(ArrayList<Recipes> recipes) {
		this.recipes = recipes;
	}

	public ArrayList<Recipes> readRecipes() {
		ArrayList<Recipes> n = new ArrayList<Recipes>();
		n.add(new Recipes("Bottle","Plastic, 10"));
		n.add(new Recipes("Screw","Iron, 2; Plastic, 1"));
		return n;
	}
	
	public String getEntity_name() {
		return entity_name;
	}

	public void setEntity_name(String entity_name) {
		this.entity_name = entity_name;
	}

	public String getParent_reference_frame() {
		return parent_reference_frame;
	}

	public void setParent_reference_frame(String parent_reference_frame) {
		this.parent_reference_frame = parent_reference_frame;
	}

	public String getEntity_type() {
		return entity_type;
	}

	public void setEntity_type(String entity_type) {
		this.entity_type = entity_type;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
	
	public String RecipestoString() {
		String s="";
		for(int i=0; i<recipes.size();i++) {
			int a=i+1;
			if(i!=recipes.size()-1) {
				s+=a+". "+recipes.get(i).toString()+"\n";
			}else {
				s+=a+". "+recipes.get(i).toString();
			}
		}
		return s;
	}
	
	public int search(String n){
		int i=-99;
		for(int j=0;j<recipes.size();j++) {
			if(recipes.get(j).getName().equals(n)) {
				i=j;
				break;
			}
		}
		return i;
	}
	
	public String reMats(String n) {
		int ind=search(n);
		return recipes.get(ind).toString();
	}
	
	
}

package model;


public class Recipes {
	
	private String name;
	private String materials;
	
	public Recipes(String name, String materials) {
		this.setName(name);
		this.setMaterials(materials);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMaterials() {
		return materials;
	}

	public void setMaterials(String materials) {
		this.materials = materials;
	}
	
	public String toString() {
		return "Recipe for: "+name+", Materials Needed: "+materials;
	}
	
}

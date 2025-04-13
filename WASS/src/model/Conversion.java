package model;

import java.util.ArrayList;

public class Conversion {
	private String part;
	private ArrayList<String> materials=new ArrayList<String>();
	private ArrayList<Integer> weight=new ArrayList<Integer>();
	
	public Conversion(String p, ArrayList<String> m, ArrayList<Integer> w) {
		part=p;
		materials=m;
		weight=w;
	}

	public String getPart() {
		return part;
	}

	public void setPart(String part) {
		this.part = part;
	}

	public ArrayList<String> getMaterials() {
		return materials;
	}

	public void setMaterials(ArrayList<String> materials) {
		this.materials = materials;
	}

	public ArrayList<Integer> getWeight() {
		return weight;
	}

	public void setWeight(ArrayList<Integer> weight) {
		this.weight = weight;
	}
	
	public String toString() {
		return "Trash's Name: "+part+" | Trash's Components: "+materials.toString()+ " | Trash's Components Weight per KG: "+weight.toString();
	}
	
}

package model;

public class Trash {
	
	private String name;
	private String components;
	private long weight;
	private long id;
	private static long idc=0;
	public Trash(String name, String components, long weight) {
		this.name = name;
		this.components = components;
		this.weight=weight;
		this.setId(idc);
		idc++;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getComponents() {
		return components;
	}
	public void setComponents(String components) {
		this.components = components;
	}
	public long getWeight() {
		return weight;
	}
	public void setWeight(long weight) {
		this.weight = weight;
	}
	
	public String toString() {
		return "Trash's Name: "+name+" | Trash's Components: "+components+" | Trash's Weight: "+weight;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	
}

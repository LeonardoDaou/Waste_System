package model;

public class Trash {
	
	private String name;
	private String components;
	private String pyroWaste;
	private long weight;
	private long id;
	private static long idc=0;
	public Trash(String name, String components, long weight, String pyroWaste) {
		this.name = name;
		this.components = components;
		this.weight=weight;
		this.pyroWaste=pyroWaste;
		id=idc;
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
		return "Trash's Name: "+name+" | Trash's Components: "+components+" | Trash's Pyrolysis Waste: "+ pyroWaste +" | Trash's Weight: "+weight;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getPyroWaste() {
		return pyroWaste;
	}
	public void setPyroWaste(String pyroWaste) {
		this.pyroWaste = pyroWaste;
	}
	
	
}

package model;

public class Material {
	
	private String name;
	private long count;
	
	public Material(String n, long l) {
		this.name=n;
		this.count=l;
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		this.name = n;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long c) {
		this.count = c;
	}
	
	public String toString() {
		return "Material's Name: "+name+", Material's Count: "+count;
	}
}

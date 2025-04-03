package model;

public class TParts {
	
	private String name;
	private long id;
	private static long idc=0;
	
	public TParts(String name) {
		this.setName(name);
		setId(idc);
		idc++;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
}

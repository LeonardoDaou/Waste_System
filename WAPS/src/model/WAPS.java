package model;

import java.util.ArrayList;

import skf.coder.HLAunicodeStringCoder;
import skf.model.object.annotations.Attribute;
import skf.model.object.annotations.ObjectClass;

@ObjectClass(name = "PhysicalEntity.WAPS")
public class WAPS {
	
	@Attribute(name = "name", coder = HLAunicodeStringCoder.class)
	private String entity_name = null;
	
	@Attribute(name = "parent_reference_frame", coder = HLAunicodeStringCoder.class)
	private String parent_reference_frame = null;
	
	@Attribute(name = "type", coder = HLAunicodeStringCoder.class)
	private String entity_type = null;
	
	//@Attribute(name = "position", coder = HLAPositonCoder.class)
	private Position position = null;
	
	private String mirrors=null;
	
	public WAPS() {}
		
	public WAPS(String entity_name, String parent_reference_frame, String entity_type, Position position){
		this.entity_name = entity_name;
		this.parent_reference_frame = parent_reference_frame;
		this.entity_type = entity_type;
		this.position = position;
		this.mirrors="closed";
	}
	
	public String getMirros() {
		return mirrors;
	}
	
	public void openMirrors() {
		mirrors="opened";
		System.out.println("Opening reflective surfaces and getting ready for pyrolysis.");
	}
	
	public void closeMirrors() {
		mirrors="closed";
		System.out.println("Closing refelctive surfaces and shutting pyrolysis conduit.");
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
	
	public ArrayList<Material> pyro(String s) {
		if(mirrors.equals("closed")) {
			openMirrors();
		}
		ArrayList<Material> mat=new ArrayList<Material>();
		String two=s.split("\\|")[2].trim();
		two=two.split(":")[1].trim();
		String[] m=two.split(";");
		for(int i=0; i<m.length;i++) {
			String[] materials=m[i].split(",");
			System.out.println(materials[0]);
			mat.add(new Material(materials[0].trim(),Integer.parseInt(materials[1].trim())));
		}
		closeMirrors();
		return mat;
	}
	
	

}

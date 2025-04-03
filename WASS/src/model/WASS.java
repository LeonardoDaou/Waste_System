package model;

import java.util.ArrayList;

import interactions.ConsoleColors;
import skf.coder.HLAunicodeStringCoder;
import skf.model.object.annotations.Attribute;
import skf.model.object.annotations.ObjectClass;

@ObjectClass(name = "PhysicalEntity.WASS")
public class WASS {
	
	@Attribute(name = "name", coder = HLAunicodeStringCoder.class)
	private String entity_name = null;
	
	@Attribute(name = "parent_reference_frame", coder = HLAunicodeStringCoder.class)
	private String parent_reference_frame = null;
	
	@Attribute(name = "type", coder = HLAunicodeStringCoder.class)
	private String entity_type = null;
	
	//@Attribute(name = "materials", coder = HLAMaterialCoder.class)
	private ArrayList<Material> materials = new ArrayList<Material>();
	
	//@Attribute(name = "trash", coder = HLATrashCoder.class)
	private ArrayList<Trash> trash = new ArrayList<Trash>();
	
	//@Attribute(name = "trash", coder = HLATPartsCoder.class)
	private ArrayList<TParts> parts = new ArrayList<TParts>();
	
	//private ArrayList<Transformed> transformed = new ArrayList<tranformed>();
	
	@Attribute(name = "position", coder = HLAPositionCoder.class)
	private Position position = null;
	
	
//	@Attribute(name = "Status", coder = HLAunicodeStringCoder.class)
//	private String status = null;
	
	
	public WASS() {}
	
	public WASS(String entity_name, String parent_reference_frame, String entity_type, Position position){
		this.entity_name = entity_name;
		this.parent_reference_frame = parent_reference_frame;
		this.entity_type = entity_type;
		this.position = position;
		this.materials= readMaterials();
		this.trash=readTrash();
		this.setParts(readParts());
	}
	
	public ArrayList<Material> readMaterials() {
		ArrayList<Material> n = new ArrayList<Material>();
		n.add(new Material("Steel",100));
		n.add(new Material("Titanium",20));
		n.add(new Material("Copper",50));
		n.add(new Material("Carbon Dioxide",10));
		n.add(new Material("Water",10));
		n.add(new Material("Plastic",0));
		return n;
	}
	
	public ArrayList<TParts> readParts() {
		ArrayList<TParts> n = new ArrayList<TParts>();
		n.add(new TParts("Motherboard Chip"));
		n.add(new TParts("Wheel"));
		n.add(new TParts("Window"));
		n.add(new TParts("Bottle"));
		n.add(new TParts("Screw"));
		return n;
	}

	public ArrayList<Trash> readTrash() {//redo in a way that reads the trash from one file and the returns from another while calculating things based on the weight
		ArrayList<Trash> n = new ArrayList<Trash>();
		n.add(new Trash("Rover","Steel, 5; Copper, 10", 20, "Carbon Dioxide, 20; Water, 40"));
		n.add(new Trash("Rover","Steel, 10; Copper, 20", 40, "Carbon Dioxide, 40; Water, 80"));
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

	public ArrayList<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(ArrayList<Material> materials) {
		this.materials = materials;
	}

	public ArrayList<Trash> getTrash() {
		return trash;
	}

	public void settrash(ArrayList<Trash> trash) {
		this.trash = trash;
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
	
	public String addMat(String n, int a) {
		String s="";
		boolean found=false;
		for(int i=0; i<materials.size(); i++) {
			if(materials.get(i).getName().equalsIgnoreCase(n)) {
				found=true;
				Material c=materials.get(i);
				c.setCount(c.getCount()+a);
				materials.set(i, c);
				s=n+"'s amount increased";
				ConsoleColors.logInfo(s);
				break;
			}
		}
		if(found==false) {
			materials.add(new Material(n,a));
			s="New material"+n+"added to storage";
			ConsoleColors.logInfo(s);
		}
		return s;
	}
	
	public String redMat(String n, int a) {
		String s="";
		boolean found=false;
		for(int i=0; i<materials.size(); i++) {
			if(materials.get(i).getName().equalsIgnoreCase(n)) {
				found=true;
				Material c=materials.get(i);
				long l=c.getCount()-a;
				if(l>0) {
					c.setCount(c.getCount()-a);
					materials.set(i, c);
					s="Material amount decreased. New amount for "+n+" is "+l;
					ConsoleColors.logInfo(s);
				}else {
					s="Not enough materials";
					ConsoleColors.logWarning("[WARNING] Not enough materials");
				}
				break;
			}
		}
		if(found==false) {
			s="Material not found";
			ConsoleColors.logWarning("[WARNING] Material not found");
		}
		return s;
	}
	

    public int searchMaterials(String n) {
    	int index=-99;
		boolean found=false;
		for(int i=0; i<materials.size(); i++) {
			if(materials.get(i).getName().equalsIgnoreCase(n)) {
				found=true;
				index=i;
				System.out.println("Material Found!");
				break;
			}
		}
		if(found==false) {
			ConsoleColors.logWarning(n+" not found");
		}
		return index;
    }
	
	public String MaterialstoString() {
		String s="";
		if(materials.size()==0) {
			s="No materials in storage";
		}else {
		for(int i=0; i<materials.size();i++) {
			int a=i+1;
			if(i!=materials.size()-1) {
				s+=a+". "+materials.get(i).toString()+"\n";
			}else {
				s+=a+". "+materials.get(i).toString();
			}
		}
		}
		return s;
	}
	
	public String TrashtoString() {
		String s="";
		if(trash.size()==0) {
			s="No materials in storage";
		}else {
			for(int i=0; i<trash.size();i++) {
				int a=i+1;
				if(i!=trash.size()-1) {
					s+=a+". "+trash.get(i).toString()+"\n";
				}else {
					s+=a+". "+trash.get(i).toString();
				}
			}
		}
		return s;
	}
	
	public String TPartstoString() {
		String s="";
		if(parts.size()==0) {
			s="No materials in storage";
		}else {
			for(int i=0; i<parts.size();i++) {
				int a=i+1;
				if(i!=parts.size()-1) {
					s+=a+". "+parts.get(i).toString()+"\n";
				}else {
					s+=a+". "+parts.get(i).toString();
				}
			}
		}
		return s;
	}

	public ArrayList<TParts> getParts() {
		return parts;
	}

	public void setParts(ArrayList<TParts> parts) {
		this.parts = parts;
	}
	

}

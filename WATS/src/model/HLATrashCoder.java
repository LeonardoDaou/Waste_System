package model;

import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.HLAinteger64LE;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIinternalError;
import skf.coder.Coder;

public class HLATrashCoder implements Coder<Trash>{
	private EncoderFactory factory = null;
	private HLAfixedRecord coder = null;
	
	private HLAunicodeString components;
	private HLAunicodeString name;
	private HLAunicodeString pyroWaste;
	private HLAinteger64LE weight;
	
	public HLATrashCoder() throws RTIinternalError {
		this.factory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		this.coder = factory.createHLAfixedRecord();
		
		this.weight = factory.createHLAinteger64LE();
		this.components = factory.createHLAunicodeString();
		this.name = factory.createHLAunicodeString();
		this.pyroWaste = factory.createHLAunicodeString();
		
		coder.add(name);
		coder.add(components);
		coder.add(weight);
		coder.add(pyroWaste);
	}

	public Trash decode(byte[] arg0) throws DecoderException {
		coder.decode(arg0);
		return new Trash(name.getValue(), components.getValue(), weight.getValue(), pyroWaste.getValue());
	}
	@Override
	public byte[] encode(Trash arg0) {
		this.name.setValue(arg0.getName());
		this.components.setValue(arg0.getComponents());
		this.weight.setValue(arg0.getWeight());
		this.pyroWaste.setValue(arg0.getPyroWaste());
		return coder.toByteArray();
	}

	public Class<Trash> getAllowedType() {
		return Trash.class;
	}
}

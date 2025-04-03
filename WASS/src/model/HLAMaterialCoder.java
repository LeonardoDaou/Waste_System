package model;

import hla.rti1516e.encoding.HLAinteger64LE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.exceptions.RTIinternalError;
import skf.coder.Coder;

public class HLAMaterialCoder implements Coder<Material>{
	private EncoderFactory factory = null;
	private HLAfixedRecord coder = null;
	
	private HLAinteger64LE count;
	private HLAunicodeString name;
	
	public HLAMaterialCoder() throws RTIinternalError {
		this.factory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		this.coder = factory.createHLAfixedRecord();
		
		this.count = factory.createHLAinteger64LE();
		this.name = factory.createHLAunicodeString();
		
		coder.add(name);
		coder.add(count);
		
	}

	public Material decode(byte[] arg0) throws DecoderException {
		coder.decode(arg0);
		return new Material(name.getValue(), count.getValue());
	}
	@Override
	public byte[] encode(Material arg0) {
		this.name.setValue(arg0.getName());
		this.count.setValue(arg0.getCount());
		return coder.toByteArray();
	}

	public Class<Material> getAllowedType() {
		return Material.class;
	}

}

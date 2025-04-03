package model;

import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAinteger64LE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIinternalError;
import skf.coder.Coder;

public class HLATPartsCoder implements Coder<TParts>{
	private EncoderFactory factory = null;
	private HLAfixedRecord coder = null;
	
	private HLAunicodeString name;
	
	public HLATPartsCoder() throws RTIinternalError {
		this.factory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		this.coder = factory.createHLAfixedRecord();
		
		this.name = factory.createHLAunicodeString();
		
		coder.add(name);
	}

	public TParts decode(byte[] arg0) throws DecoderException {
		coder.decode(arg0);
		return new TParts(name.getValue());
	}
	@Override
	public byte[] encode(TParts arg0) {
		this.name.setValue(arg0.getName());
		return coder.toByteArray();
	}

	public Class<TParts> getAllowedType() {
		return TParts.class;
	}

}

package core;

import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateIsExecutionMember;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.IllegalName;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
import hla.rti1516e.exceptions.ObjectInstanceNameNotReserved;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;

import java.net.MalformedURLException;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import model.LandingBeacon;
import model.Position;
import siso.smackdown.frame.FrameType;
import skf.config.Configuration;
import skf.core.SEEAbstractFederate;
import skf.core.SEEAbstractFederateAmbassador;
import skf.exception.PublishException;
import skf.exception.UpdateException;

public class LandingBeaconFederate extends SEEAbstractFederate implements Observer {

	private LandingBeacon landingBeacon = null;
	private String local_settings_designator = null;

	public LandingBeaconFederate(SEEAbstractFederateAmbassador seefedamb, LandingBeacon landingBeacon) {
		super(seefedamb);
		this.landingBeacon  = landingBeacon;
	}

	public void configureAndStart(Configuration config) throws ConnectionFailed, InvalidLocalSettingsDesignator, UnsupportedCallbackModel, CallNotAllowedFromWithinCallback, RTIinternalError, CouldNotCreateLogicalTimeFactory, FederationExecutionDoesNotExist, InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, SaveInProgress, RestoreInProgress, NotConnected, MalformedURLException, FederateNotExecutionMember, NameNotFound, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, InstantiationException, IllegalAccessException, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, AttributeNotOwned, ObjectInstanceNotKnown, PublishException, UpdateException,
	skf.exception.SubscribeException, skf.exception.UnsubscribeException, skf.exception.UpdateException, 
    InterruptedException	{
		// 1. configure the SKF framework
		super.configure(config);

		// 2. Connect to RTI
    /*
    *For MAK local_settings_designator = "";
    *For PITCH local_settings_designator = "crcHost=" + <crc_host> + "\ncrcPort=" + <crc_port>;
    */
		local_settings_designator = "crcHost="+config.getCrcHost()+"\ncrcPort="+config.getCrcPort();
		super.connectToRTI(local_settings_designator);
		// NEW above
		// 3. The Federate joins the Federation execution
		super.joinFederationExecution();

		// 4. Subscribe the Subject
		super.subscribeSubject(this);

		// 5. publish landingBeacon object to RTI
		super.publishElement(landingBeacon);
		super.subscribeReferenceFrame(FrameType.MoonCentricFixed);
		
		// NEW: Subscribe to ExecutionConfiguration to receive updates
	    super.subscribeElement((Class<? extends skf.model.object.annotations.ObjectClass>) skf.model.object.executionConfiguration.ExecutionConfiguration.class);
	    super.waitForElementDiscovery((Class<? extends skf.model.object.annotations.ObjectClass>) skf.model.object.executionConfiguration.ExecutionConfiguration.class, 10000);
		
		// NEW: Request ExecutionConfiguration updates
	    while (super.getExecutionConfiguration() == null) {
	        super.requestAttributeValueUpdate((Class<? extends skf.model.object.annotations.ObjectClass>) skf.model.object.executionConfiguration.ExecutionConfiguration.class);
	        Thread.sleep(10);
	    }
		
		// 6. Start Execution
		super.startExecution();
	}

	private void stopExecution() throws InvalidResignAction, OwnershipAcquisitionPending, FederateOwnsAttributes, FederateNotExecutionMember, NotConnected, RTIinternalError, FederateIsExecutionMember, CallNotAllowedFromWithinCallback, SaveInProgress, RestoreInProgress {
		super.unsubscribeSubject(this);
//		super.disconnectFromRTI();
//		super.shudownExecution();
	}

	@Override
	protected void doAction() {
		Position curr_pos = this.landingBeacon.getPosition();
		curr_pos.setX(curr_pos.getX()+10); // update the x coordinate
		
		try {
			
			super.updateElement(this.landingBeacon);
			
		} catch (FederateNotExecutionMember | NotConnected | AttributeNotOwned
				| AttributeNotDefined | ObjectInstanceNotKnown | SaveInProgress
				| RestoreInProgress | RTIinternalError | IllegalName
				| ObjectInstanceNameInUse | ObjectInstanceNameNotReserved
				| ObjectClassNotPublished | ObjectClassNotDefined
				| UpdateException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void update(Observable arg0, Object arg1) {
		
		System.out.println("The landingBeacon has received an update");
		System.out.println(arg1);
		
		if (arg1 instanceof skf.model.object.executionConfiguration.ExecutionConfiguration) {
	        super.setExecutionConfiguration((skf.model.object.executionConfiguration.ExecutionConfiguration) arg1);

	        if ((super.getExecutionConfiguration().getCurrent_execution_mode() == skf.model.object.executionConfiguration.ExecutionMode.EXEC_MODE_FREEZE || 
	             super.getExecutionConfiguration().getCurrent_execution_mode() == skf.model.object.executionConfiguration.ExecutionMode.EXEC_MODE_RUNNING) &&
	            super.getExecutionConfiguration().getNext_execution_mode() == skf.model.object.executionConfiguration.ExecutionMode.EXEC_MODE_SHUTDOWN) {
	            
	            super.shudownExecution(); // Correctly shut down when RTI tells us to
	        }
	    }
		
	}

}

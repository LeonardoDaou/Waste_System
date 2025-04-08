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
import hla.rti1516e.exceptions.FederateServiceInvocationsAreBeingReportedViaMOM;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.IllegalName;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hla.rti1516e.exceptions.InteractionClassNotPublished;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
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
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import model.WAPS;
import model.Material;
import model.Position;
import interactions.ConsoleColors;
import interactions.FederateMessage;
import interactions.WASSWAPSMessage;
import siso.smackdown.frame.FrameType;
import siso.smackdown.frame.ReferenceFrame;
import skf.config.Configuration;
import skf.core.SEEAbstractFederate;
import skf.core.SEEAbstractFederateAmbassador;
import skf.exception.PublishException;
import skf.exception.UpdateException;
import skf.model.interaction.modeTransitionRequest.ModeTransitionRequest;
import skf.model.object.annotations.ObjectClass;
import skf.model.object.executionConfiguration.ExecutionConfiguration;
import skf.model.object.executionConfiguration.ExecutionMode;
import skf.synchronizationPoint.SynchronizationPoint;
import skf.model.interaction.annotations.InteractionClass;
public class WAPSFederate extends SEEAbstractFederate implements Observer {

private static final int MAX_WAIT_TIME = 10000;
	
	private WAPS waps=null;
	private String local_settings_designator = null;
	private ReferenceFrame currentReferenceFrame;
    private ModeTransitionRequest mtr = new ModeTransitionRequest();
    private WASSWAPSMessage message = new WASSWAPSMessage();
    private boolean messageSent = false;
    private ArrayList<FederateMessage> messages=new ArrayList<FederateMessage>();

	public WAPSFederate(SEEAbstractFederateAmbassador seefedamb, WAPS waps) {
		super(seefedamb);
		this.waps  = waps;
	}

	public void configureAndStart(Configuration config) throws ConnectionFailed, InvalidLocalSettingsDesignator, UnsupportedCallbackModel, CallNotAllowedFromWithinCallback, RTIinternalError, CouldNotCreateLogicalTimeFactory, FederationExecutionDoesNotExist, InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, SaveInProgress, RestoreInProgress, NotConnected, MalformedURLException, FederateNotExecutionMember, NameNotFound, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, InstantiationException, IllegalAccessException, IllegalName, ObjectInstanceNameInUse, ObjectInstanceNameNotReserved, ObjectClassNotPublished, AttributeNotOwned, ObjectInstanceNotKnown, PublishException, UpdateException,
	skf.exception.SubscribeException, skf.exception.UnsubscribeException, skf.exception.UpdateException, 
    InterruptedException, InvalidInteractionClassHandle, InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM	{
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
		if (!SynchronizationPoint.INITIALIZATION_STARTED.isAnnounced()) {
            super.waitingForAnnouncement(SynchronizationPoint.INITIALIZATION_COMPLETED, MAX_WAIT_TIME);

            super.subscribeElement((Class<? extends ObjectClass>) ExecutionConfiguration.class);
            super.waitForElementDiscovery((Class<? extends ObjectClass>) ExecutionConfiguration.class, MAX_WAIT_TIME);

            while (super.getExecutionConfiguration() == null) {
                super.requestAttributeValueUpdate((Class<? extends ObjectClass>) ExecutionConfiguration.class);
                Thread.sleep(10);
            }

            super.publishInteraction(mtr);
            super.subscribeReferenceFrame(FrameType.AitkenBasinLocalFixed);

            super.publishElement(waps, "WAPS");

            super.publishInteraction(message);
            super.subscribeInteraction((Class<? extends InteractionClass>) WASSWAPSMessage.class);

            super.setupHLATimeManagement();
        } else {
            throw new RuntimeException("WAPS is not a Late Joiner Federate");
        }

//		// 5. publish waps object to RTI
//		super.publishElement(waps);
//		super.subscribeReferenceFrame(FrameType.AitkenBasinLocalFixed);
//		
//		// NEW: Subscribe to ExecutionConfiguration to receive updates
//	    super.subscribeElement((Class<? extends skf.model.object.annotations.ObjectClass>) skf.model.object.executionConfiguration.ExecutionConfiguration.class);
//	    super.waitForElementDiscovery((Class<? extends skf.model.object.annotations.ObjectClass>) skf.model.object.executionConfiguration.ExecutionConfiguration.class, 10000);
//		
//		// NEW: Request ExecutionConfiguration updates
//	    while (super.getExecutionConfiguration() == null) {
//	        super.requestAttributeValueUpdate((Class<? extends skf.model.object.annotations.ObjectClass>) skf.model.object.executionConfiguration.ExecutionConfiguration.class);
//	        Thread.sleep(10);
//	    }
//		System.out.println("Heyyy");
		// 6. Start Execution
		super.startExecution();
	}

	private void stopExecution() throws InvalidResignAction, OwnershipAcquisitionPending, FederateOwnsAttributes, FederateNotExecutionMember, NotConnected, RTIinternalError, FederateIsExecutionMember, CallNotAllowedFromWithinCallback, SaveInProgress, RestoreInProgress {
		super.unsubscribeSubject(this);
//		super.disconnectFromRTI();
//		super.shudownExecution();
	}
//	
	public void menu() {
		System.out.println("Pick an option:");
		System.out.println("1. Open Reflective Surfaces");
		System.out.println("2. Close Reflective Surfaces");
		System.out.println("3. Exit");
		System.out.println("---------------------------------");
	}
	@Override
	public void doAction() {//add message received
		Scanner scan=new Scanner(System.in);
		int choice=0;
		do{
				menu();
				choice=readInt();
				switch(choice){
				case 1:
					System.out.println();
					waps.openMirrors();
					System.out.println();
					break;
				case 2:
					System.out.println();
					waps.closeMirrors();
					System.out.println();
					break;
				case 3:
					System.out.println("Shutting down");
					System.exit(1);
					break;
				default:
					System.out.println("Wrong Input");
				}
    	}while(choice!=2);
	}
	
	@Override
    public void update(Observable o, Object arg) {
        if (arg instanceof ExecutionConfiguration) {
            super.setExecutionConfiguration((ExecutionConfiguration) arg);
        } else if (arg instanceof WASSWAPSMessage) {
        	WASSWAPSMessage msg = (WASSWAPSMessage) arg;
         	System.out.println("[WAPS] Received message: " + msg.getMessageType() + " :\n" + msg.getContent());
        	handleResponses((WASSWAPSMessage) arg);
        } else if (arg instanceof ReferenceFrame) {
            this.currentReferenceFrame = (ReferenceFrame) arg;
            //System.out.println("[DEBUG] WAPS received Reference Frame update.");
        } else {
            System.out.println("[WAPS] Unknown update: " + arg.getClass().getSimpleName());
        }
    }
	
	private void sendMessage(String sender, String receiver, String type, String content) {
        this.message.setSender(sender);
        this.message.setReceiver(receiver);
        this.message.setMessageType(type);
        this.message.setContent(content);

        try {
            super.updateInteraction(this.message);
            ConsoleColors.logInfo("[WAPS] Sent message to " + receiver+":\n" + content);
        } catch (Exception e) {
            ConsoleColors.logError("[WAPS] Failed to send message: " + e.getMessage());
        }
    }

    private void handleResponses(WASSWAPSMessage message) {
        switch (message.getMessageType()) {
            case "PYROLYSIS_REQUEST":
            	ConsoleColors.logInfo("[WAPS] Received a pyrolysis request from " + message.getSender() + " for the following trash: " + message.getContent());
            	ArrayList<Material> mat=waps.pyro(message.getContent());
            	String s=MaterialstoString(mat);
            	sendMessage("WAPS", "WASS", "RESIDUALS_FROM_PYROLYSIS", s);
            	ConsoleColors.logInfo("[WAPS] Pyrolysis Done.");
            	ConsoleColors.logInfo("[WAPS] Sending the residuals back to WASS. Materials: "+s);
            	break;
            case "PYROLYSIS_REQUEST_LARS":
            	ConsoleColors.logInfo("[WAPS] Received a pyrolysis request from " + message.getSender() + " for the following trash: " + message.getContent());
            	ArrayList<Material> mat1=waps.pyro(message.getContent());
            	String s1=MaterialstoString(mat1);
            	sendMessage("WAPS", "WASS", "RESIDUALS_FROM_PYROLYSIS_LARS", s1);
            	ConsoleColors.logInfo("[WAPS] Pyrolysis Done.");
            	ConsoleColors.logInfo("[WAPS] Sending the residuals back to WASS. Materials: "+s1);
            	break;
            default:
                ConsoleColors.logInfo("[WAPS] Unknown message type: " + message.getMessageType());
                break;
        }
    }
    
    public String MaterialstoString(ArrayList<Material> materials) {
		String s="";
		for(int i=0; i<materials.size();i++) {
			if(i!=materials.size()-1) {
				s+=materials.get(i).toString()+" | ";
			}else {
				s+=materials.get(i).toString();
			}
		}
		return s;
	}
    
    public int readInt() {
    	Scanner scan=new Scanner(System.in);
    	boolean fine=true;
    	int n=0;
    	do {
        	try {
        		n=scan.nextInt();
        		fine=true;
        	}catch(InputMismatchException e) {
        		fine=false;
        		System.out.print("You need to enter a number! Enter again: ");
        		scan.next();
        	}
    	}while(fine!=true);
    	return n;
    }
    
//
//	@Override
//	public void update(Observable arg0, Object arg1) {
//		
//		System.out.println("The waps has received an update");
//		System.out.println(arg1);
//		
//		if (arg1 instanceof skf.model.object.executionConfiguration.ExecutionConfiguration) {
//			System.out.println("Hey");
//	        super.setExecutionConfiguration((skf.model.object.executionConfiguration.ExecutionConfiguration) arg1);
//	        System.out.println(skf.model.object.executionConfiguration.ExecutionMode.EXEC_MODE_SHUTDOWN);
//	        if ((super.getExecutionConfiguration().getCurrent_execution_mode() == skf.model.object.executionConfiguration.ExecutionMode.EXEC_MODE_FREEZE || 
//	             super.getExecutionConfiguration().getCurrent_execution_mode() == skf.model.object.executionConfiguration.ExecutionMode.EXEC_MODE_RUNNING) &&
//	            super.getExecutionConfiguration().getNext_execution_mode() == skf.model.object.executionConfiguration.ExecutionMode.EXEC_MODE_SHUTDOWN) {
//	            
//	            super.shudownExecution(); // Correctly shut down when RTI tells us to
//	        }
//	    }
//		
//	}

}

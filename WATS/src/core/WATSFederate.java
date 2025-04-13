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

import model.WATS;
import model.Position;
import interactions.ConsoleColors;
import interactions.FederateMessage;
import interactions.WASSWATSMessage;
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

public class WATSFederate extends SEEAbstractFederate implements Observer {

	private static final int MAX_WAIT_TIME = 10000;
	
	private WATS wats=null;
	private String local_settings_designator = null;
	private ReferenceFrame currentReferenceFrame;
    private ModeTransitionRequest mtr = new ModeTransitionRequest();
    private WASSWATSMessage message = new WASSWATSMessage();

	public WATSFederate(SEEAbstractFederateAmbassador seefedamb, WATS wats) {
		super(seefedamb);
		this.wats  = wats;
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

            super.publishElement(wats, "WATS");
            
            super.publishInteraction(message);
            super.subscribeInteraction((Class<? extends InteractionClass>) WASSWATSMessage.class);

            super.setupHLATimeManagement();
        } else {
            throw new RuntimeException("WATS is not a Late Joiner Federate");
        }
		
		// 6. Start Execution
		super.startExecution();
	}

	private void stopExecution() throws InvalidResignAction, OwnershipAcquisitionPending, FederateOwnsAttributes, FederateNotExecutionMember, NotConnected, RTIinternalError, FederateIsExecutionMember, CallNotAllowedFromWithinCallback, SaveInProgress, RestoreInProgress {
		super.unsubscribeSubject(this);
//		super.disconnectFromRTI();
//		super.shudownExecution();
	}

	public void menu() {
		System.out.println("Pick an option:");
		System.out.println("1. View Recipes");
		System.out.println("2. Exit");
		System.out.println("---------------------------------");
	}
	
	@Override
	protected void doAction() {
		Scanner scan=new Scanner(System.in);
		int choice=0;
		do{
				menu();
				choice=readInt();
				System.out.println();
				switch(choice){
				case 1:
					System.out.println(wats.RecipestoString());
					System.out.println();
					break;
				case 2:
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
        } else if (arg instanceof WASSWATSMessage) {
        	WASSWATSMessage msg = (WASSWATSMessage) arg;
        	System.out.println("[WATS] Received message: " + msg.getMessageType() + " :\n" + msg.getContent());
        	handleResponses((WASSWATSMessage) arg);
        } else if (arg instanceof ReferenceFrame) {
            this.currentReferenceFrame = (ReferenceFrame) arg;
            //System.out.println("[DEBUG] WATS received Reference Frame update.");
        } else {
            System.out.println("[WATS] Unknown update: " + arg.getClass().getSimpleName());
        }
    }

    private void sendMessage(String sender, String receiver, String type, String content) {
        this.message.setSender(sender);
        this.message.setReceiver(receiver);
        this.message.setMessageType(type);
        this.message.setContent(content);

        try {
            super.updateInteraction(this.message);
            ConsoleColors.logInfo("[WATS] Sent message to " + receiver+":\n" + content);
        } catch (Exception e) {
            ConsoleColors.logError("[WATS] Failed to send message: " + e.getMessage());
        }
    }

    public String[] decomp(WASSWATSMessage message) {
    	String trash=message.getContent();
    	String[] dividents=trash.split("\\|");
    	String name=dividents[0].split(": ")[1].trim();
    	String weight=dividents[3].split(": ")[1].trim();
    	String components=dividents[1].split(": ")[1].trim();
    	String[] indi=components.split("; ");
    	String[] numbers=new String[indi.length];
    	String[] materials=new String[indi.length];
    	for(int i=0;i<indi.length;i++) {
    		String[] split=indi[i].split(", ");
    		materials[i]=split[0];
    		numbers[i]=split[1];
    	}
    	String compos="";
    	for(int i=0;i<materials.length;i++) {
    		if(i==0) {
    			compos+=numbers[i]+" parts of "+materials[i];
    		}
    		else if(i==materials.length-1 && i>1) {
    			compos+=", and "+numbers[i]+" parts of "+materials[i];
    		}else if(i==materials.length-1 && i==1) {
    			compos+=" and " +numbers[i]+" parts of "+materials[i];
    		}else {
    			compos+=", "+numbers[i]+" parts of "+materials[i];
    		}
    	}
    	String[] returnee= {components,weight,name,compos};
    	return returnee;
    }
    
    private void handleResponses(WASSWATSMessage message) {
    	switch (message.getMessageType()) {
            case "TRANSFORMATION_INFORMATION":
            	ConsoleColors.logInfo("[WATS] Got a request for recipe information, sending the list!");
            	sendMessage("WATS", "WASS", "RECIPES", wats.RecipestoString());
    			break;
            case "RECIPE_LIST_WASS":
            	ConsoleColors.logInfo("[WATS] Got a request for recipe information, sending the list!");
            	sendMessage("WATS", "WASS", "RECIPE_LIST_WATS", wats.RecipestoString());
    			break;
            case "CHOOSE_RECIPE_WASS":
            	ConsoleColors.logInfo("[WATS] Got a request for recipe information, sending the list!");
            	sendMessage("WATS", "WASS", "CHOOSE_RECIPE_WATS", wats.RecipestoString());
    			break;
            case "FABRICATION_REQUEST":
            	ConsoleColors.logInfo("[WATS] Got a transformation request. Starting Fabricator!");
            	String re=wats.getRecipes().get(Integer.parseInt(message.getContent().trim())).getName();
            	ConsoleColors.logInfo("[WATS] Fabrication of "+re+" in progress ...");
            	ConsoleColors.logInfo("[WATS] Fabrication Done");
            	sendMessage("WATS", "WASS", "FABRICATION_COMPLETE", re);
            	break;
            case "DECOMPOSITION_REQUEST":
            	String [] returned=decomp(message);
            	ConsoleColors.logInfo("[WATS] Decomposition is finished. The "+returned[1]+" KG "+returned[2]+" yeilded "+returned[3]+".");
            	sendMessage("WATS","WASS","DECOMPOSITION_COMPLETE", returned[0]);
            	break;
            case "DECOMPOSITION_REQUEST_LARS":
            	String [] returned_lars=decomp(message);
            	ConsoleColors.logInfo("[WATS] Decomposition is finished. The "+returned_lars[1]+" KG "+returned_lars[2]+" yeilded "+returned_lars[3]+".");
            	sendMessage("WATS","WASS","DECOMPOSITION_COMPLETE_LARS", returned_lars[0]);
            	break;
            case "RECYCLING_REQUEST_LARS":
            	int ind=Integer.parseInt(message.getContent());
            	if(ind>=wats.getRecipes().size()) {
            		sendMessage("WATS","WASS","RECIPE_CHOICE_ERROR","Recipe does not exist!");
            	}else {
	            	String mats=wats.reMats(ind);
	            	sendMessage("WATS","WASS","MATERIAL_CHECK",mats);
	            }
            	break;
            case "RECYCLING_EXECUTION_LARS":
            	ConsoleColors.logInfo("[WATS] Got a transformation request. Starting Fabricator!");
            	String re1=wats.getRecipes().get(wats.search(message.getContent())).getName();
            	ConsoleColors.logInfo("[WATS] Fabrication of "+re1+" in progress ...");
            	ConsoleColors.logInfo("[WATS] Fabrication Done");
            	sendMessage("WATS", "WASS", "FABRICATION_COMPLETE_LARS", re1);
            	break;
            default:
                ConsoleColors.logInfo("[WATS] Unknown message type: " + message.getMessageType());
        }
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
    
	

}

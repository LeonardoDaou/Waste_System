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

import model.WASS;
import model.WAPS;
import model.WATS;
import model.Conversion;
import model.Material;
import model.Position;
import model.TParts;
import model.Trash;
import interactions.ConsoleColors;
import interactions.FederateMessage;
import interactions.WASSERRVMessage;
import interactions.WASSLARSMessage;
import interactions.WASSWAPSMessage;
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

public class WASSFederate extends SEEAbstractFederate implements Observer {

	
	private static final int MAX_WAIT_TIME = 10000;
	private SharedData shared=new SharedData();
	
	private String local_settings_designator = null;
	private ReferenceFrame currentReferenceFrame;
    private ModeTransitionRequest mtr = new ModeTransitionRequest();
    private FederateMessage message = new FederateMessage();
    private WASSERRVMessage wemessage = new WASSERRVMessage();
    private WASSLARSMessage wlmessage = new WASSLARSMessage();
    private WASSWATSMessage wwtmessage = new WASSWATSMessage();
    private WASSWAPSMessage wwpmessage = new WASSWAPSMessage();
    private boolean messageSent = false;
    private ArrayList<FederateMessage> messages=new ArrayList<FederateMessage>();
    private String lastPrint="";
	public WASSFederate(SEEAbstractFederateAmbassador seefedamb, WASS wass) {
		super(seefedamb);
		this.shared.setWASS(wass);
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

            super.publishElement(shared.getWASS(), "WASS");
            //super.subscribeElement((Class<? extends ObjectClass>) WAPS.class);
            //super.subscribeElement((Class<? extends ObjectClass>) WATS.class);

            super.publishInteraction(wlmessage);
            super.publishInteraction(wwpmessage);
            super.publishInteraction(wwtmessage);
            super.publishInteraction(wemessage);
            super.subscribeInteraction((Class<? extends InteractionClass>) WASSLARSMessage.class);
            super.subscribeInteraction((Class<? extends InteractionClass>) WASSERRVMessage.class);
            super.subscribeInteraction((Class<? extends InteractionClass>) WASSWAPSMessage.class);
            super.subscribeInteraction((Class<? extends InteractionClass>) WASSWATSMessage.class);

            super.setupHLATimeManagement();
        } else {
            throw new RuntimeException("WASS is not a Late Joiner Federate");
        }
		
		// 5. publish wass object to RTI
//		super.publishElement(wass);
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
		
		// 6. Start Execution
		super.startExecution();
	}

	private void stopExecution() throws InvalidResignAction, OwnershipAcquisitionPending, FederateOwnsAttributes, FederateNotExecutionMember, NotConnected, RTIinternalError, FederateIsExecutionMember, CallNotAllowedFromWithinCallback, SaveInProgress, RestoreInProgress {
		super.unsubscribeSubject(this);
//		super.disconnectFromRTI();
//		super.shudownExecution();
	}
	public void menu() {
		String s="What system do you want to interact with?\n"
							+"1. WASS\n"
							+"2. WAPS\n"
							+"3. WATS\n"
							+"4. LARS\n"
							+"5. Exit\n"
							+"---------------------------------\n"
							+"Enter your choice:";
		System.out.print(s);
		lastPrint=s;
	}
	
	public void wapsProcess() {
		if(shared.getWASS().getTrash().size()==0) {
			ConsoleColors.logStatus("No trash in storage!");
		}else {
			Scanner scan=new Scanner(System.in);
			String s="";
			s+="Which trash do you want to send for pyrolysis?\n";
			for(int i=0;i<shared.getWASS().getTrash().size();i++) {
				int a=i+1;
				s+=a+" "+shared.getWASS().getTrash().get(i).toString()+"\n";
			}
			s+="---------------------------------\n";
			s+="Enter your choice: ";
			System.out.print(s);
			lastPrint=s;
			int tind=readInt();
			tind--;
			if(tind<shared.getWASS().getTrash().size()) {
				sendWWPMessage("WASS", "WAPS", "PYROLYSIS_REQUEST", shared.getWASS().getTrash().get(tind).toString());
				shared.setTaskStarted(true);
			}else {
				ConsoleColors.logError("Choice does not exist!");
			}
		}
	}
	
	public void wassProcess() {
		Scanner scan=new Scanner(System.in);
		String s="";
		s+="Which options do you want?\n";
		s+="1. Print Materials\n";
		s+="2. Print Trash\n";
		s+="3. Print Parts\n";
		s+="---------------------------------\n";
		s+="Enter your choice: ";
		System.out.print(s);
		lastPrint=s;
		int cind=readInt();
		switch(cind) {
		case 1:
			System.out.println();
			System.out.println(shared.getWASS().MaterialstoString());
			System.out.println();
			break;
		case 2:
			System.out.println();
			System.out.println(shared.getWASS().TrashtoString());
			System.out.println();
			break;
		case 3:
			System.out.println();
			System.out.println(shared.getWASS().TPartstoString());
			System.out.println();
			break;
		default:
			ConsoleColors.logWarning("[WASS] Choice does not exist!");
		}
	}
	
	public void watsProcess() {
		Scanner scan=new Scanner(System.in);
		String s="";
		s+="Which operation do you want?\n";
		s+="1. Decomposition\n";
		s+="2. Recycling\n";
		s+="---------------------------------\n";
		s+="Enter your choice: ";
		System.out.println(s);
		lastPrint=s;
		int pid=readInt();
		switch(pid) {
		case 1:
			if(shared.getWASS().getTrash().size()==0) {
				ConsoleColors.logStatus("No trash in storage!");
			}else {
				String s2="";
				s2="Choose which trash you want to decompose:\n";
				for(int i=0;i<shared.getWASS().getTrash().size();i++) {
	    			int a=i+1;
					s2+=a+" "+shared.getWASS().getTrash().get(i).toString()+"\n";
	    		}
	    		s2+="---------------------------------\n";
	    		s2+="Enter your choice: ";
	    		System.out.println(s2);
	    		lastPrint=s2;
	    		int tindex=readInt();
	    		tindex--;
	    		if(tindex<shared.getWASS().getTrash().size()) {
	    			sendWWTMessage("WASS", "WATS", "DECOMPOSITION_REQUEST", shared.getWASS().getTrash().get(tindex).toString());
	    			WASS wass=shared.getWASS();
	    			ArrayList<Trash> t=wass.getTrash();
	    			t.remove(tindex);
	    			wass.settrash(t);
	    			shared.setWASS(wass);
	    			shared.setTaskStarted(true);
	    		}else {
	    			ConsoleColors.logError("[ERROR] Choice does not exist!");
	    			shared.setTaskDone(true);
	    		}
			}
			break;
		case 2:
			sendWWTMessage("WASS", "WATS", "TRANSFORMATION_INFORMATION", "Send all of your recipes and their information");
			shared.setTaskStarted(true);
			break;
		default:
			ConsoleColors.logWarning("Wrong Choice!");
			shared.setTaskDone(true);
		}
	}
	
	public void larsProcess() {
		Scanner scan=new Scanner(System.in);
		String s="";
		s+="Which operation do you want?\n";
		s+="1. Send materials list\n";
		s+="2. Send recipe list\n";
		s+="3. Send waste list\n";
		s+="4. Send parts list\n";
		s+="---------------------------------\n";
		s+="Enter your choice: ";
		System.out.println(s);
		lastPrint=s;
		int pid=readInt();
		switch(pid) {
		case 1:
        	sendWLMessage("WASS","LARS","MATERIALS_LIST_WASS",shared.getWASS().MaterialstoString());
        	break;
        case 2:
        	sendWWTMessage("WASS","WATS","RECIPE_LIST_WASS", "Send all of your recipe information");
        	break;
        case 3:
        	sendWLMessage("WASS","LARS","WASTE_LIST_WASS",shared.getWASS().TrashtoString());
        	break;
        case 4:
        	sendWLMessage("WASS","LARS","PARTS_LIST_WASS",shared.getWASS().TPartstoString());
        	break;
		default:
			ConsoleColors.logWarning("Wrong Choice!");
			shared.setTaskDone(true);
		}
	}
	
	@Override
	protected void doAction() {//change the class name of the interactions and give UCF that. (Might need to do that for each one).
        // sendMessage("LARS", "LandingBeacon", "REPORT_ENV_CONDITIONS", "Dust levels nominal. Terrain stable.");
		Scanner scan=new Scanner(System.in);
		int choice=0;
		do{
			if(shared.isTaskStarted()==false) {menu();
				choice=readInt();
				System.out.println();
				switch(choice){
				case 1:
					wassProcess();
					break;
				case 2:
					wapsProcess();
					break;
				case 3:
					watsProcess();
					break;
				case 4:
					larsProcess();
					break;
				case 5:
					System.out.println("Shutting down");
					System.exit(1);
					break;
				default:
					System.out.println("Wrong Input");
				}
			}
			if(shared.isTaskDone()==true) {
				shared.setTaskStarted(false);
				shared.setTaskDone(false);
			}
    	}while(choice!=5);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof ExecutionConfiguration) {
            super.setExecutionConfiguration((ExecutionConfiguration) arg);
        }else if(arg instanceof WASSLARSMessage) {
        	WASSLARSMessage msg = (WASSLARSMessage) arg;
            System.out.println();
        	ConsoleColors.logInfo("[WASS] Received message: " + msg.getMessageType() + " :\n" + msg.getContent());
        	handleResponses((WASSLARSMessage) arg);
    	}else if(arg instanceof WASSWAPSMessage) {
    		WASSWAPSMessage msg = (WASSWAPSMessage) arg;
            System.out.println();
        	ConsoleColors.logInfo("[WASS] Received message: " + msg.getMessageType() + " :\n" + msg.getContent());
        	handleResponses((WASSWAPSMessage) arg);
    	}else if(arg instanceof WASSWATSMessage) {
    		WASSWATSMessage msg = (WASSWATSMessage) arg;
            System.out.println();
        	ConsoleColors.logInfo("[WASS] Received message: " + msg.getMessageType() + " :\n" + msg.getContent());
        	handleResponses((WASSWATSMessage) arg);
    	}else if(arg instanceof WASSERRVMessage) {
    		WASSERRVMessage msg = (WASSERRVMessage) arg;
            System.out.println();
        	ConsoleColors.logInfo("[WASS] Received message: " + msg.getMessageType() + " :\n" + msg.getContent());
        	handleResponses((WASSERRVMessage) arg);
    	}else if (arg instanceof ReferenceFrame) {
            this.currentReferenceFrame = (ReferenceFrame) arg;
            //ConsoleColors.logStatus("[DEBUG] WASS received Reference Frame update.");
        }else {
        	ConsoleColors.logInfo("[WASS] Unknown update: " + arg.getClass().getSimpleName());
        }
    }

    
    private void sendWLMessage(String sender, String receiver, String type, String content) {
        this.wlmessage.setSender(sender);
        this.wlmessage.setReceiver(receiver);
        this.wlmessage.setMessageType(type);
        this.wlmessage.setContent(content);

        try {
            super.updateInteraction(this.wlmessage);
            ConsoleColors.logInfo("[WASS] Sent message to " + receiver+":\n" + content);
        } catch (Exception e) {
            ConsoleColors.logError("[WASS] Failed to send message: " + e.getMessage());
            shared.setTaskDone(true);
        }
    }
    private void sendWWTMessage(String sender, String receiver, String type, String content) {
        this.wwtmessage.setSender(sender);
        this.wwtmessage.setReceiver(receiver);
        this.wwtmessage.setMessageType(type);
        this.wwtmessage.setContent(content);

        try {
            super.updateInteraction(this.wwtmessage);
            ConsoleColors.logInfo("[WASS] Sent message to " + receiver+":\n" + content);
        } catch (Exception e) {
            ConsoleColors.logError("[WASS] Failed to send message: " + e.getMessage());
            shared.setTaskDone(true);
        }
    }
    private void sendWWPMessage(String sender, String receiver, String type, String content) {
        this.wwpmessage.setSender(sender);
        this.wwpmessage.setReceiver(receiver);
        this.wwpmessage.setMessageType(type);
        this.wwpmessage.setContent(content);

        try {
            super.updateInteraction(this.wwpmessage);
            ConsoleColors.logInfo("[WASS] Sent message to " + receiver+":\n" + content);
        } catch (Exception e) {
            ConsoleColors.logError("[WASS] Failed to send message: " + e.getMessage());
            shared.setTaskDone(true);
        }
    }
    
    private void sendWEMessage(String sender, String receiver, String type, String content) {
        this.wemessage.setSender(sender);
        this.wemessage.setReceiver(receiver);
        this.wemessage.setMessageType(type);
        this.wemessage.setContent(content);

        try {
            super.updateInteraction(this.wemessage);
            ConsoleColors.logInfo("[WASS] Sent message to " + receiver+":\n" + content);
        } catch (Exception e) {
            ConsoleColors.logError("[WASS] Failed to send message: " + e.getMessage());
            shared.setTaskDone(true);
        }
    }
    
    private void handleResponses(WASSERRVMessage message) {
    	switch(message.getMessageType()){
    	case "PART":
    		String[] con=message.getContent().trim().split(",");
    		Conversion request=shared.getWASS().searchConversions(con[0]);
    		WASS wass=shared.getWASS();
    		ArrayList<Trash> t=wass.getTrash();
    		String mats="";
    		int co=Integer.parseInt(con[1])*2;
    		int ho=Integer.parseInt(con[1])*2;
    		String waste="Carbon Dioxide, "+co+"; Water, "+ho;
    		for(int i=0;i<request.getMaterials().size();i++) {
    			if(i==request.getMaterials().size()-1) {
    				int w=request.getWeight().get(i)*Integer.parseInt(con[1]);
	    			mats+=request.getMaterials().get(i)+", "+w;
    			}else {
	    			int w=request.getWeight().get(i)*Integer.parseInt(con[1]);
	    			mats+=request.getMaterials().get(i)+", "+w+";";
    			}
    		}
    		t.add(new Trash(con[0],mats,Integer.parseInt(con[1]),waste));
    		sendWEMessage("WASS","ERRV","PART_RECEIVED",con[0]+ " part received and stored.");
    		break;
    	default:
    		ConsoleColors.logInfo("[WASS] Unknown message type: " + message.getMessageType());
    	}
    }
    private void handleResponses(WASSLARSMessage message) {
    	Scanner scan=new Scanner(System.in);
    	switch(message.getMessageType()){
    	case "MATERIALS_LIST_LARS":
        	sendWLMessage("WASS","LARS","MATERIALS_LIST_WASS",shared.getWASS().MaterialstoString());
        	break;
        case "RECIPE_LIST_LARS":
        	sendWWTMessage("WASS","WATS","RECIPE_LIST_WASS", "Send all of your recipe information");
        	break;
        case "POSSIBLE_WASTE_LIST_LARS":
        	sendWLMessage("WASS","LARS","WASTE_LIST_WASS",shared.getWASS().TrashtoString());
        	break;
        case "PARTS_LIST_LARS":
        	sendWLMessage("WASS","LARS","PARTS_LIST_WASS",shared.getWASS().TPartstoString());
        	break;
        case "PYRO_REQUEST_LARS":
        	String[] li=message.getContent().split(" ");
        	int t=Integer.parseInt(li[li.length-1].trim())-1;
        	if(shared.getWASS().getTrash().size()==0) {
    			sendWLMessage("WASS","LARS","PYROLOSIS_CHOICE_ERROR", "No trash in storage!");
    			shared.setTaskDone(true);
    		}else if(t<shared.getWASS().getTrash().size()) {
        		sendWWPMessage("WASS", "WAPS", "PYROLYSIS_REQUEST_LARS", shared.getWASS().getTrash().get(t).toString());
        		shared.remove(t);
    		}else {
    			sendWLMessage("WASS","LARS","PYROLOSIS_CHOICE_ERROR", "Choice does not exist!");
    			shared.setTaskDone(true);
    		}
        	break;
        case "DECOMPOSITION_REQUEST_LIST_LARS":
        	String[] li2=message.getContent().split(" ");
        	int num=Integer.parseInt(li2[li2.length-1].trim())-1;
        	if(shared.getWASS().getTrash().size()==0) {
        		sendWLMessage("WASS","LARS","DECOMPOSITION_CHOICE_ERROR","No trash in storage!");
        		shared.setTaskDone(true);
        	}else if(num>shared.getWASS().getTrash().size()-1){
        		sendWLMessage("WASS","LARS","DECOMPOSITION_CHOICE_ERROR","No such choice exists!");
        		shared.setTaskDone(true);
        	}else {
	        	sendWWTMessage("WASS", "WATS", "DECOMPOSITION_REQUEST_LARS", shared.getWASS().getTrash().get(num).toString());
	        	shared.remove(num);
	        }
        	break;
        case "RECYCLING_REQUEST_LIST_LARS":
        	String[] li3=message.getContent().split(" ");
        	int num2=Integer.parseInt(li3[li3.length-1].trim())-1;
        	sendWWTMessage("WASS","WATS","RECYCLING_REQUEST_LARS",""+num2);
        	sendWLMessage("WASS","LARS","RECYCLING_REQUEST_MATERIAL_CHECK", "Checking if materials are sufficient and if the recipe requested exists");
        	break;
        case "PYRO_LIST_REQUEST_LARS":
        	ConsoleColors.logInfo("[WASS] Recieved "+message.getContent()+" from " +message.getSender());
        	sendWLMessage("WASS","LARS","CHOOSE_WASTE_PYROLYSIS",shared.getWASS().TrashtoString());
        	break;
		case "DECOMPOSITION_LIST_REQUEST_LIST_LARS":
        	ConsoleColors.logInfo("[WASS] Recieved "+message.getContent()+" from " +message.getSender());
			sendWLMessage("WASS","LARS","CHOOSE_WASTE_DECOMPOSITION",shared.getWASS().TrashtoString());
			break;
		case "RECYCLING_LIST_REQUEST_LIST_LARS":
        	ConsoleColors.logInfo("[WASS] Recieved "+message.getContent()+" from " +message.getSender());
			sendWWTMessage("WASS","WATS","CHOOSE_RECIPE_WASS",shared.getWASS().TrashtoString());
			break;
        case "ENVIRONMENTAL_STATUS":
        	ConsoleColors.logInfo("[WASS] Recieved "+message.getContent()+" from " +message.getSender());
        	break;
        default:
        	ConsoleColors.logInfo("[WASS] Unknown message type: " + message.getMessageType());
            shared.setTaskDone(true);
    	}
    }
        
    private void handleResponses(WASSWATSMessage message) {
        Scanner scan=new Scanner(System.in);
    	switch (message.getMessageType()) {
	    	case "RECIPE_CHOICE_ERROR":
	    		sendWLMessage("WASS","LARS","RECIPE_LIST_WASSL", message.getContent());
	    		break;
    		case "RECIPE_LIST_WATS":
	        	sendWLMessage("WASS","LARS","RECIPE_LIST_WASSL", message.getContent());
	        	break;	
    		case "CHOOSE_RECIPE_WATS":
	        	sendWLMessage("WASS","LARS","CHOOSE_RECIPE", message.getContent());
	        	break;	
	    	case "RECIPES":
            	System.out.print("Choose the recipe you need: ");
            	int c=readInt();
            	String[] arr=message.getContent().split("\n");
            	int maxnum=Integer.parseInt(arr[arr.length-1].split("\\.")[0].trim());
            	ArrayList<Integer> indices=new ArrayList<Integer>();
            	ArrayList<Integer> amounts=new ArrayList<Integer>();
            	c--;
            	if(c>maxnum) {
            		ConsoleColors.logWarning("[WARNING] Choice does not exist!");
            		shared.setTaskDone(true);
            	}else {
            		String[] mats=arr[c].split("Materials Needed: ")[1].trim().split(";");
            		boolean plausible=true;
            		for(int i=0;i<mats.length;i++) {
            			String mat=mats[i].split(", ")[0].trim();
            			int amount=Integer.parseInt(mats[i].split(", ")[1].trim());
            			int index=shared.getWASS().searchMaterials(mat);
            			indices.add(index);
            			amounts.add(amount);
            			if(index==-99) {
            				System.out.println("Please wait until we have "+mat);
            				plausible=false;
            			}else if(amount>shared.getWASS().getMaterials().get(index).getCount()) {
            				long needed=amount-shared.getWASS().getMaterials().get(index).getCount();
            				System.out.println("Insufficient "+mat+"! Need an additional "+needed+" KG.");
            				plausible=false;
            			}
            		}
        			String recipe=arr[c].split(", Materials Needed:")[0].trim().split(": ")[1].trim();
            		if(plausible==true) {
            			ConsoleColors.logStatus("[STATUS] Sufficient materials. Sending Request to WATS for "+recipe+".");
        				ArrayList<Material> materials=shared.getWASS().getMaterials();
        				WASS wass3=shared.getWASS();
        				for(int i=0;i<indices.size();i++) {
        					Material m=materials.get(indices.get(i));
        					m.setCount(m.getCount()-amounts.get(i));
        					materials.set(i, m);
        					wass3.setMaterials(materials);
            			}
        				shared.setWASS(wass3);
            			sendWWTMessage("WASS", "WATS", "FABRICATION_REQUEST", ""+c);
            		}else {
            			ConsoleColors.logStatus("[STATUS] Insufficient materials for "+recipe+".");
            			shared.setTaskDone(true);
            		}
            	}
            	break;
            case "FABRICATION_COMPLETE":
            	WASS wass=shared.getWASS();
            	ArrayList<TParts> part=wass.getParts();
            	part.add(new TParts(message.getContent()));
            	wass.setParts(part);
            	shared.setWASS(wass);
            	ConsoleColors.logStatus("[WASS] Part "+part.get(part.size()-1).getName() +" with ID "+ part.get(part.size()-1).getId()+" has been recieved and stored.");
            	shared.setTaskDone(true);
            	break;
            case "DECOMPOSITION_COMPLETE":
            	decompoComplete(message);
            	shared.setTaskDone(true);
            	break;
            case "DECOMPOSITION_COMPLETE_LARS":
            	decompoComplete(message);
            	sendWLMessage("WASS" ,"LARS", "DECOMPOSITION_COMPLETE_WASS", "Materials obtained from decomposition: "+message.getContent()+".");
            	shared.setTaskDone(true);
            	break;
            case "MATERIAL_CHECK":
            	ArrayList<Integer> indices1=new ArrayList<Integer>();
            	ArrayList<Integer> amounts1=new ArrayList<Integer>();
            	String[] mats=message.getContent().split("Materials Needed: ")[1].trim().split(";");
            	boolean plausible=true;
            	String msg1="";
            	String msg2="";
            	for(int i=0;i<mats.length;i++) {
            		String mat=mats[i].split(", ")[0].trim();
            		int amount=Integer.parseInt(mats[i].split(", ")[1].trim());
            		int index=shared.getWASS().searchMaterials(mat);
            		indices1.add(index);
            		amounts1.add(amount);
            		if(index==-99) {
            			msg1+=mat+" not found in storage database.\n";
            			plausible=false;
            		}else if(amount>shared.getWASS().getMaterials().get(index).getCount()) {
            			long needed=amount-shared.getWASS().getMaterials().get(index).getCount();
            			msg2+="Insufficient "+mat+"! Need an additional "+needed+" KG.\n";
            			plausible=false;
            		}
            	}
        		String recipe=message.getContent().split(", Materials Needed:")[0].trim().split(": ")[1].trim();
            	if(plausible==true) {
            		ConsoleColors.logStatus("[STATUS] Sufficient materials. Sending Request to WATS for "+recipe+".");
        			ArrayList<Material> materials=shared.getWASS().getMaterials();
        			WASS wass3=shared.getWASS();
        			for(int i=0;i<indices1.size();i++) {
        				Material m=materials.get(indices1.get(i));
        				m.setCount(m.getCount()-amounts1.get(i));
        				materials.set(i, m);
        				wass3.setMaterials(materials);
            		}
        			shared.setWASS(wass3);
            		sendWWTMessage("WASS", "WATS", "RECYCLING_EXECUTION_LARS", recipe);
            	}else {
            		ConsoleColors.logStatus("[STATUS] Failed to fabricate "+recipe+":\n"+msg1+msg2);
        			sendWLMessage("WASS","LARS","INSUFFICIENT_MATERIALS","Failed to fabricate "+recipe+":\n"+msg1+msg2);
            		shared.setTaskDone(true);
            	}
            	break;
            case "FABRICATION_COMPLETE_LARS":
            	WASS wass1=shared.getWASS();
            	ArrayList<TParts> part1=wass1.getParts();
            	part1.add(new TParts(message.getContent()));
            	wass1.setParts(part1);
            	shared.setWASS(wass1);
            	ConsoleColors.logStatus("[WASS] Part "+part1.get(part1.size()-1).getName() +" with ID "+ part1.get(part1.size()-1).getId()+" has been recieved and stored.");
            	sendWLMessage("WASS", "LARS", "FABRICATION_COMPLETE_LARS", "Part "+part1.get(part1.size()-1).getName() +" with ID "+ part1.get(part1.size()-1).getId()+" has been recieved and stored.");
            	shared.setTaskDone(true);
            	break;
            default:
                ConsoleColors.logInfo("[WASS] Unknown message type: " + message.getMessageType());
                shared.setTaskDone(true);
        }
    }
    
    public void decompoComplete(WASSWATSMessage message) {
    	ConsoleColors.logInfo("[WASS] Materials from decomposition have arrived! Integrating them into storage!");
    	WASS wass2=shared.getWASS();
    	String components=message.getContent();
    	String[] indi=components.split("; ");
    	for(int i=0;i<indi.length;i++) {
    		String[] split=indi[i].split(", ");
    		wass2.addMat(split[0], Integer.parseInt(split[1]));
    	}
    	shared.setWASS(wass2);
    	ConsoleColors.logStatus("[WASS] Material integration finished!");
    }
    
    private void handleResponses(WASSWAPSMessage message) {
        Scanner scan=new Scanner(System.in);
    	switch (message.getMessageType()) {
	    	case "RESIDUALS_FROM_PYROLYSIS_LARS":
	        	ConsoleColors.logInfo("[WASS] Got some stuff, saving them in storage!");
				String s1=message.getContent().trim();
				String[] multi1=s1.split("\\|");
				for(int i=0;i<multi1.length;i++) {
	    			String[] splitter=multi1[i].trim().split(",");
	    			String name="";
	    			int amount=0;
	    			name=splitter[0].split(":")[1].trim();
	    			amount=Integer.parseInt(splitter[1].split(":")[1].trim());
	    			WASS wasses=shared.getWASS();
	    			wasses.addMat(name, amount);
	    			shared.setWASS(wasses);
				}
				sendWLMessage("WASS","LARS","PYROLYSIS_RESULTS",message.getContent());
				break;   
	    	case "RESIDUALS_FROM_PYROLYSIS":
	            	ConsoleColors.logInfo("[WASS] Got some stuff, saving them in storage!");
	    			String s=message.getContent().trim();
	    			String[] multi=s.split("\\|");
	    			for(int i=0;i<multi.length;i++) {
		    			String[] splitter=multi[i].trim().split(",");
		    			String name="";
		    			int amount=0;
		    			name=splitter[0].split(":")[1].trim();
		    			amount=Integer.parseInt(splitter[1].split(":")[1].trim());
		    			WASS wass=shared.getWASS();
		    			wass.addMat(name, amount);
		    			shared.setWASS(wass);
	    			}
	    			shared.setTaskDone(true);
	    			break;
            default:
                ConsoleColors.logInfo("[WASS] Unknown message type: " + message.getMessageType());
                shared.setTaskDone(true);
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




class SharedData {
	private WASS wass = null;

    private boolean taskStarted=false;
    private boolean taskDone=false;

    public synchronized void setWASS(WASS wass) {
        this.wass=wass;
    }

    public synchronized WASS getWASS() {
        return wass;
    }

	public synchronized boolean isTaskStarted() {
		return taskStarted;
	}

	public synchronized void setTaskStarted(boolean taskStarted) {
		this.taskStarted = taskStarted;
	}

	public synchronized boolean isTaskDone() {
		return taskDone;
	}

	public synchronized void setTaskDone(boolean taskDone) {
		this.taskDone = taskDone;
	}
	
	public synchronized void remove(int t) {
		wass.remove(t);
	}
}

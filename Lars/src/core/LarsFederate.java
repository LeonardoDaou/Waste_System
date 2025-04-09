package core;

import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import model.Lars;
import model.LandingBeacon;
import interactions.ConsoleColors;
import interactions.FederateMessage;
import interactions.WASSLARSMessage;
import siso.smackdown.frame.FrameType;
import siso.smackdown.frame.ReferenceFrame;
import skf.config.Configuration;
import skf.core.SEEAbstractFederate;
import skf.core.SEEAbstractFederateAmbassador;
import skf.model.interaction.modeTransitionRequest.ModeTransitionRequest;
import skf.model.object.annotations.ObjectClass;
import skf.model.object.executionConfiguration.ExecutionConfiguration;
import skf.model.object.executionConfiguration.ExecutionMode;
import skf.synchronizationPoint.SynchronizationPoint;
import skf.model.interaction.annotations.InteractionClass;

public class LarsFederate extends SEEAbstractFederate implements Observer {

    private static final int MAX_WAIT_TIME = 10000;

    private Lars lars;
    private LandingBeacon beacon;

    private String local_settings_designator;
    private ReferenceFrame currentReferenceFrame;
    private ModeTransitionRequest mtr = new ModeTransitionRequest();
    private FederateMessage message = new FederateMessage();
    //Added by Leonardo
    private WASSLARSMessage wlmessage = new WASSLARSMessage();
    //
    // NEW
    private boolean messageSent = false;
    //Added by Leonardo
    private boolean recipeMsgSent=false;
	private boolean materialsMsgSent=false;
	private boolean decompositionMsgSent=false;
	private boolean recyclingMsgSent=false;
	private boolean wasteMsgSent=false;
	private boolean pyroMsgSent=false;
	private boolean partsMsgSent=false;
	//

    public LarsFederate(SEEAbstractFederateAmbassador fedAmb, Lars lars) {
        super(fedAmb);
        this.lars = lars;
    }

    @SuppressWarnings("unchecked")
    public void configureAndStart(Configuration config) throws Exception {
        super.configure(config);

        local_settings_designator = "crcHost=" + config.getCrcHost() + "\ncrcPort=" + config.getCrcPort();
        super.connectToRTI(local_settings_designator);
        super.joinFederationExecution();
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

            super.publishElement(lars, "LARS");
            super.subscribeElement((Class<? extends ObjectClass>) LandingBeacon.class);

            super.publishInteraction(message);
            //Added by Leonardo
            super.publishInteraction(wlmessage);
            super.subscribeInteraction((Class<? extends InteractionClass>) WASSLARSMessage.class);
            //
            super.subscribeInteraction((Class<? extends InteractionClass>) FederateMessage.class);

            super.setupHLATimeManagement();
        } else {
            throw new RuntimeException("LARS is not a Late Joiner Federate");
        }

        super.startExecution();
    }

    @Override
    protected void doAction() {
        // sendMessage("LARS", "LandingBeacon", "REPORT_ENV_CONDITIONS", "Dust levels nominal. Terrain stable.");
//    	if (!messageSent) {
//            sendMessage("LARS", "LandingBeacon", "LARS_STATUS", "Surface scan completed. No hazards detected.");
//            messageSent = true;
//        }
    	 //Added by Leonardo
    	if(!materialsMsgSent) {
    		sendWLMessage("LARS", "WASS", "MATERIALS_LIST_LARS", "Request for the material list of WASS.");
    		materialsMsgSent=true;
    	}
    	else if(!wasteMsgSent) {
    		sendWLMessage("LARS", "WASS", "POSSIBLE_WASTE_LIST_LARS", "Request for the wastes list of WASS.");
    		wasteMsgSent=true;
    	}
    	else if(!partsMsgSent) {
    		sendWLMessage("LARS", "WASS", "PARTS_LIST_LARS", "Request for the parts list of WASS.");
    		partsMsgSent=true;
    	}
    	else if(!recipeMsgSent) {
    		sendWLMessage("LARS", "WASS", "RECIPE_LIST_LARS", "Request for the recipe list of WATS.");
    		recipeMsgSent=true;
    	}
    	else if(!pyroMsgSent) {
    		sendWLMessage("LARS", "WASS", "PYRO_LIST_REQUEST_LARS", "Requesting waste list");
    		pyroMsgSent=true;
    	}
    	else if(!decompositionMsgSent) {
    		sendWLMessage("LARS", "WASS", "DECOMPOSITION_LIST_REQUEST_LIST_LARS", "Requesting waste list");
    		decompositionMsgSent=true;
    	}
    	else if(!recyclingMsgSent) {
    		sendWLMessage("LARS", "WASS", "RECYCLING_LIST_REQUEST_LIST_LARS", "Requesting recipe list");
    		recyclingMsgSent=true;
    	}
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof ExecutionConfiguration) {
            super.setExecutionConfiguration((ExecutionConfiguration) arg);
        } else if (arg instanceof FederateMessage) {
            handleResponses((FederateMessage) arg);
            FederateMessage msg = (FederateMessage) arg;
            System.out.println("[LARS] Received message: " + msg.getMessageType() + " - " + msg.getContent());
        //Added by Leonardo
        } else if (arg instanceof WASSLARSMessage) {
            handleResponses((WASSLARSMessage) arg);
            WASSLARSMessage msg = (WASSLARSMessage) arg;
            System.out.println("[LARS] Received message: " + msg.getMessageType() + " - " + msg.getContent());
        //
        } else if (arg instanceof ReferenceFrame) {
            this.currentReferenceFrame = (ReferenceFrame) arg;
            System.out.println("[DEBUG] LARS received Reference Frame update.");
        } else if (arg instanceof LandingBeacon) {
            this.beacon = (LandingBeacon) arg;
            System.out.println("[DEBUG] LARS received LandingBeacon update.");
        } else {
            System.out.println("[LARS] Unknown update: " + arg.getClass().getSimpleName());
        }
    }

    private void sendMessage(String sender, String receiver, String type, String content) {
        this.message.setSender(sender);
        this.message.setReceiver(receiver);
        this.message.setMessageType(type);
        this.message.setContent(content);
        System.out.println("[DEBUG] LARS sending message: " + type + " to " + receiver);

        try {
            super.updateInteraction(this.message);
            ConsoleColors.logInfo("[LARS] Sent message: " + content + " to " + receiver);
        } catch (Exception e) {
            ConsoleColors.logError("[LARS] Failed to send message: " + e.getMessage());
        }
    }
    //Added by Leonardo
    private void sendWLMessage(String sender, String receiver, String type, String content) {
        this.wlmessage.setSender(sender);
        this.wlmessage.setReceiver(receiver);
        this.wlmessage.setMessageType(type);
        this.wlmessage.setContent(content);

        try {
            super.updateInteraction(this.wlmessage);
            ConsoleColors.logInfo("[LARS] Sent message to " + receiver+":\n" + content);
        } catch (Exception e) {
            ConsoleColors.logError("[LARS] Failed to send message: " + e.getMessage());
        }
    }
    
    private void handleResponses(WASSLARSMessage message) {
    	Scanner scan=new Scanner(System.in);
    	switch(message.getMessageType()) {
	    	case "DECOMPOSITION_COMPLETE_WASS":
	    		ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
	    		break;
            case "PYROLYSIS_RESULTS":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	break;
            case "RECIPE_LIST_WASSL":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	break;
            case "MATERIALS_LIST_WASS":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	break;
            case "WASTE_LIST_WASS":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	break;
            case "PARTS_LIST_WASS":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	break;
            case "FABRICATION_COMPLETE_LARS":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	break;
            case "RECYCLING_REQUEST_MATERIAL_CHECK":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	break;
            case "INSUFFICIENT_MATERIALS":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	break;
            case "PYROLOSIS_CHOICE_ERROR":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	break;
            case "DECOMPOSITION_CHOICE_ERROR":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	break;
            case "CHOOSE_RECIPE":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	System.out.print("Enter the item that you want fabricated: ");
            	int num=scan.nextInt();
        		sendWLMessage("LARS", "WASS", "RECYCLING_REQUEST_LIST_LARS", ""+num);
            	break;
            case "CHOOSE_WASTE_PYROLYSIS":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	System.out.print("Enter the waste that you want pyrolyzed: ");
            	int num2=scan.nextInt();
            	sendWLMessage("LARS", "WASS", "PYRO_REQUEST_LARS", ""+num2);
            	break;
            case "CHOOSE_WASTE_DECOMPOSITION":
            	ConsoleColors.logInfo("[LARS] Recieved "+message.getContent()+" from " +message.getSender());
            	System.out.print("Enter the item that you want decomposed: ");
            	int num3=scan.nextInt();
        		sendWLMessage("LARS", "WASS", "DECOMPOSITION_REQUEST_LIST_LARS", ""+num3);
            	break;
            default:
                ConsoleColors.logInfo("[LARS] Unknown message type: " + message.getMessageType());
                break;
        }
    }
    //
    private void handleResponses(FederateMessage message) {
        switch (message.getMessageType()) {
            case "ACK_REPORT_RECEIVED":
                ConsoleColors.logInfo("[LARS] Received ACK from " + message.getSender() + ": " + message.getContent());
                break;
            default:
                ConsoleColors.logInfo("[LARS] Unknown message type: " + message.getMessageType());
                break;
        }
    }
}

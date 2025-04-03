package model;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.siso.spacefom.frame.ReferenceFrameTranslation;
import org.siso.spacefom.frame.SpaceTimeCoordinateState;

import referenceFrame.coder.*;
import siso.smackdown.frame.ReferenceFrame;
import skf.coder.HLAunicodeStringCoder;
import skf.model.object.annotations.Attribute;
import skf.model.object.annotations.ObjectClass;

@ObjectClass(name = "PhysicalEntity.LandingBeacon")
public class LandingBeacon {
    
    @Attribute(name = "name", coder = HLAunicodeStringCoder.class)
    private String name = null;
    
    @Attribute(name = "parent_reference_frame", coder = HLAunicodeStringCoder.class)
    private String parentReferenceFrame = null;
    
    @Attribute(name = "state", coder = SpaceTimeCoordinateStateCoder.class)
    private SpaceTimeCoordinateState state = null;
    
    @Attribute(name = "type", coder = HLAunicodeStringCoder.class)
    private String type = null;
    
    @Attribute(name = "position2", coder = HLAPositionCoder.class)
    private Position position = null;
    
    private Quaternion quaternion = null;
    
    public LandingBeacon() {}

    public LandingBeacon(String name, String type, String parentReferenceFrame, Position position, Quaternion quaternion) {
        this.name = name;
        this.type = type;
        this.parentReferenceFrame = parentReferenceFrame;
        this.state = new SpaceTimeCoordinateState();
        this.position = position;
        this.quaternion = quaternion;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public SpaceTimeCoordinateState getState() {
        return state;
    }

    public void setState(SpaceTimeCoordinateState state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentReferenceFrame() {
        return parentReferenceFrame;
    }

    public void setParentReferenceFrame(String parentReferenceFrame) {
        this.parentReferenceFrame = parentReferenceFrame;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Quaternion getQuaternion() {
        return quaternion;
    }

    public void setQuaternion(Quaternion quaternion) {
        this.quaternion = quaternion;
    }
    
    /**
     * Updates the LandingBeacon state based on received data
     */
    public void updateState(double x, double y, double z, double w, double qx, double qy, double qz) {
        if (this.state == null) {
            this.state = new SpaceTimeCoordinateState();
        }

        ReferenceFrameTranslation translationalState = this.state.getTranslationalState();
        Vector3D newPosition = new Vector3D(x, y, z);
        translationalState.setPosition(newPosition);

        updateCurrentPosition(x, y, z);
        updateCurrentQuaternion(w, qx, qy, qz);
    }

    /**
     * Updates the current position of the LandingBeacon
     */
    private void updateCurrentPosition(double x, double y, double z) {
        if (this.position != null) {
            this.position.setX(x);
            this.position.setY(y);
            this.position.setZ(z);
        } else {
            this.position = new Position(x, y, z);
        }
    }

    /**
     * Updates the current quaternion orientation
     */
    private void updateCurrentQuaternion(double w, double qx, double qy, double qz) {
        if (this.quaternion != null) {
            this.quaternion.setW(w);
            this.quaternion.setX(qx);
            this.quaternion.setY(qy);
            this.quaternion.setZ(qz);
        } else {
            this.quaternion = new Quaternion(w, qx, qy, qz);
        }
    }

    /**
     * Notifies when a Lander is approaching the Spaceport
     */
//    public boolean isLanderApproaching(Lander lander) {
//        if (lander == null || lander.getPosition() == null || this.position == null) {
//            return false;
//        }
//
//        double distance = calculateDistance(lander.getPosition(), this.position);
//        return distance < 1000.0; // Threshold for proximity
//    }

    /**
     * Sends a status request to the Spaceport
     */
//    public void requestLandingClearance(Spaceport spaceport) {
//        if (spaceport != null) {
//            System.out.println("[LandingBeacon] Requesting landing clearance from Spaceport...");
//            // Logic to send message interaction here
//        }
//    }

    /**
     * Calculates the distance between the Lander and the LandingBeacon
     */
    private double calculateDistance(Position pos1, Position pos2) {
        double dx = pos1.getX() - pos2.getX();
        double dy = pos1.getY() - pos2.getY();
        double dz = pos1.getZ() - pos2.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}

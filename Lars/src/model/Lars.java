package model;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.siso.spacefom.frame.ReferenceFrameTranslation;
import org.siso.spacefom.frame.SpaceTimeCoordinateState;

import referenceFrame.*;
import skf.coder.HLAunicodeStringCoder;
import skf.model.object.annotations.Attribute;
import skf.model.object.annotations.ObjectClass;

@ObjectClass(name = "PhysicalEntity.Lars")
public class Lars {

    @Attribute(name = "name", coder = HLAunicodeStringCoder.class)
    private String name;

    @Attribute(name = "type", coder = HLAunicodeStringCoder.class)
    private String type;

    @Attribute(name = "parent_reference_frame", coder = HLAunicodeStringCoder.class)
    private String parentReferenceFrame;

    @Attribute(name = "position", coder = HLAPositionCoder.class)
    private Position position;

    private Quaternion quaternion;

    private SpaceTimeCoordinateState state;

    public Lars() {}

    public Lars(String name, String type, String parentReferenceFrame, Position position, Quaternion quaternion) {
        this.name = name;
        this.type = type;
        this.parentReferenceFrame = parentReferenceFrame;
        this.position = position;
        this.quaternion = quaternion;
        this.state = new SpaceTimeCoordinateState();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getParentReferenceFrame() {
        return parentReferenceFrame;
    }

    public Position getPosition() {
        return position;
    }

    public Quaternion getQuaternion() {
        return quaternion;
    }

    public SpaceTimeCoordinateState getState() {
        return state;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setQuaternion(Quaternion quaternion) {
        this.quaternion = quaternion;
    }

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

    private void updateCurrentPosition(double x, double y, double z) {
        if (this.position != null) {
            this.position.setX(x);
            this.position.setY(y);
            this.position.setZ(z);
        } else {
            this.position = new Position(x, y, z);
        }
    }

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
}

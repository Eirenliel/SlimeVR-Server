// automatically generated by the FlatBuffers compiler, do not modify

package overlay_protocol;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

public class BonePoseT {
  private int id;
  private overlay_protocol.QuatT quat;
  private overlay_protocol.Vec3fT pos;

  public int getId() { return id; }

  public void setId(int id) { this.id = id; }

  public overlay_protocol.QuatT getQuat() { return quat; }

  public void setQuat(overlay_protocol.QuatT quat) { this.quat = quat; }

  public overlay_protocol.Vec3fT getPos() { return pos; }

  public void setPos(overlay_protocol.Vec3fT pos) { this.pos = pos; }


  public BonePoseT() {
    this.id = 0;
    this.quat = new overlay_protocol.QuatT();
    this.pos = new overlay_protocol.Vec3fT();
  }
}


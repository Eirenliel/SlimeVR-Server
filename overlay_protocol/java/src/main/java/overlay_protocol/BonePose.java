// automatically generated by the FlatBuffers compiler, do not modify

package overlay_protocol;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class BonePose extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_2_0_0(); }
  public static BonePose getRootAsBonePose(ByteBuffer _bb) { return getRootAsBonePose(_bb, new BonePose()); }
  public static BonePose getRootAsBonePose(ByteBuffer _bb, BonePose obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public BonePose __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int id() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0; }
  public overlay_protocol.Quat quat() { return quat(new overlay_protocol.Quat()); }
  public overlay_protocol.Quat quat(overlay_protocol.Quat obj) { int o = __offset(6); return o != 0 ? obj.__assign(o + bb_pos, bb) : null; }
  public overlay_protocol.Vec3f pos() { return pos(new overlay_protocol.Vec3f()); }
  public overlay_protocol.Vec3f pos(overlay_protocol.Vec3f obj) { int o = __offset(8); return o != 0 ? obj.__assign(o + bb_pos, bb) : null; }

  public static void startBonePose(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addId(FlatBufferBuilder builder, int id) { builder.addByte(0, (byte) id, (byte) 0); }
  public static void addQuat(FlatBufferBuilder builder, int quatOffset) { builder.addStruct(1, quatOffset, 0); }
  public static void addPos(FlatBufferBuilder builder, int posOffset) { builder.addStruct(2, posOffset, 0); }
  public static int endBonePose(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public BonePose get(int j) { return get(new BonePose(), j); }
    public BonePose get(BonePose obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public BonePoseT unpack() {
    BonePoseT _o = new BonePoseT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(BonePoseT _o) {
    int _oId = id();
    _o.setId(_oId);
    if (quat() != null) quat().unpackTo(_o.getQuat());
    else _o.setQuat(null);
    if (pos() != null) pos().unpackTo(_o.getPos());
    else _o.setPos(null);
  }
  public static int pack(FlatBufferBuilder builder, BonePoseT _o) {
    if (_o == null) return 0;
    startBonePose(builder);
    addId(builder, _o.getId());
    addQuat(builder, overlay_protocol.Quat.pack(builder, _o.getQuat()));
    addPos(builder, overlay_protocol.Vec3f.pack(builder, _o.getPos()));
    return endBonePose(builder);
  }
}


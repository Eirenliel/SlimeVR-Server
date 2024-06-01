import { Bone, Color, Matrix4, Object3D, Vector2, Vector3 } from 'three';
import { LineSegments2 } from 'three/examples/jsm/lines/LineSegments2';
import { LineMaterial } from 'three/examples/jsm/lines/LineMaterial.js';
import { LineSegmentsGeometry } from 'three/examples/jsm/lines/LineSegmentsGeometry.js';
import { BodyPart, BoneT } from 'solarxr-protocol';
import { Vector3FromVec3fT } from '@/maths/vector3';
import { QuaternionFromQuatT } from '@/maths/quaternion';

const _vector = new Vector3();
const _boneMatrix = new Matrix4();
const _matrixWorldInv = new Matrix4();

export class BasedSkeletonHelper extends LineSegments2 {
  isSkeletonHelper: boolean;
  root: Object3D;
  bones: Bone[];
  readonly type = 'SkeletonHelper';

  get resolution() {
    return this.material.resolution;
  }
  set resolution(v: Vector2) {
    this.material.resolution = v;
  }

  constructor(object: Object3D) {
    const bones = getBoneList(object);

    const geometry = new LineSegmentsGeometry();

    const vertices = [];
    const colors = [];

    const color1 = new Color(0, 0, 1);
    const color2 = new Color(0, 1, 0);

    for (let i = 0; i < bones.length; i++) {
      const bone = bones[i];

      if (bone.parent && (bone.parent as Bone).isBone) {
        vertices.push(0, 0, 0);
        vertices.push(0, 0, 0);
        if (bone.parent instanceof BoneKind) {
          const color = bone.parent.boneColor;
          colors.push(color.r, color.g, color.b);
          colors.push(color.r, color.g, color.b);
        } else {
          colors.push(color1.r, color1.g, color1.b);
          colors.push(color2.r, color2.g, color2.b);
        }
      }
    }

    geometry.setPositions(vertices);
    geometry.setColors(colors);

    const material = new LineMaterial({
      vertexColors: true,
      // depthTest: false,
      // depthWrite: false,
      toneMapped: false,
      transparent: true,
      linewidth: 4,
    });

    super(geometry, material);

    this.isSkeletonHelper = true;

    this.root = object;
    this.bones = bones;

    this.matrix = object.matrixWorld;
    this.matrixAutoUpdate = false;
  }

  updateMatrixWorld(force: boolean) {
    const bones = this.bones;

    const geometry = this.geometry;
    const vertices = [];

    _matrixWorldInv.copy(this.root.matrixWorld).invert();

    for (let i = 0; i < bones.length; i++) {
      const bone = bones[i];

      if (bone.parent && (bone.parent as Bone).isBone) {
        _boneMatrix.multiplyMatrices(_matrixWorldInv, bone.parent.matrixWorld);
        _vector.setFromMatrixPosition(_boneMatrix);
        vertices.push(_vector.x, _vector.y, _vector.z);

        _boneMatrix.multiplyMatrices(_matrixWorldInv, bone.matrixWorld);
        _vector.setFromMatrixPosition(_boneMatrix);
        vertices.push(_vector.x, _vector.y, _vector.z);
      }
    }

    geometry.setPositions(vertices);

    super.updateMatrixWorld(force);
  }

  dispose() {
    this.geometry.dispose();
    if (Array.isArray(this.material)) {
      this.material.forEach((x) => x.dispose());
    } else {
      this.material.dispose();
    }
  }
}

function getBoneList(object: Object3D): Bone[] {
  const boneList: Bone[] = [];

  if ((object as Bone).isBone) {
    boneList.push(object as Bone);
  }

  for (let i = 0; i < object.children.length; i++) {
    boneList.push(...getBoneList(object.children[i]));
  }

  return boneList;
}

export class BoneKind extends Bone {
  boneT: BoneT;
  tail: boolean;

  constructor(bones: Map<BodyPart, BoneT>, bodyPart: BodyPart, tail: boolean) {
    super();
    const bone = bones.get(bodyPart);
    if (!bone) {
      throw 'Couldnt find bone ' + BodyPart[bodyPart];
    }
    this.boneT = bone;
    this.name = BodyPart[bodyPart];
    this.tail = tail;
    this.updateData(bones);
  }

  updateData(bones: Map<BodyPart, BoneT>) {
    this.boneT = bones.get(this.boneT.bodyPart) ?? this.boneT;
    const parent = BoneKind.parent(this.boneT.bodyPart);
    const parentBone = parent === null ? undefined : bones.get(parent);
    if (this.boneT.bodyPart === BoneKind.root) {
      this.position.set(0, this.boneT.headPositionG?.y ?? 0, 0);
      return;
    }

    if (!this.tail) {
      const localPosition = Vector3FromVec3fT(this.boneT.headPositionG).sub(
        Vector3FromVec3fT(
          parentBone === undefined
            ? new Vector3(0, 0, 0)
            : Vector3FromVec3fT(parentBone.headPositionG)
        )
      );
      this.position.copy(localPosition);
      return;
    }

    const quat = QuaternionFromQuatT(this.boneT.rotationG)
      // .normalize()
      // .multiply(
      //   parentBone === undefined
      //     ? new Quaternion().identity()
      //     : QuaternionFromQuatT(parentBone.rotationG).normalize().invert().normalize()
      // )
      .normalize();

    // console.log(this.quaternion);
    // console.log(
    //   parentBone === undefined
    //     ? new Vector3(0, 0, 0)
    //     : Vector3FromVec3fT(parentBone.headPositionG),
    //   Vector3FromVec3fT(this.boneT.headPositionG)
    // );
    this.position.set(0, -this.boneT.boneLength, 0);
    this.position.applyQuaternion(quat);
    // console.log(this.position);
  }

  get boneColor(): Color {
    switch (this.boneT.bodyPart) {
      case BodyPart.PLAYSPACE:
      case BodyPart.NONE:
        throw 'Unexpected body part';
      case BodyPart.HEAD:
        return new Color('black');
      case BodyPart.NECK:
        return new Color('silver');
      case BodyPart.UPPER_CHEST:
        return new Color('blue');
      case BodyPart.CHEST:
        return new Color('purple');
      case BodyPart.WAIST:
        return new Color('red');
      case BodyPart.HIP:
        return new Color('orange');
      case BodyPart.LEFT_UPPER_LEG:
      case BodyPart.RIGHT_UPPER_LEG:
        return new Color('blue');
      case BodyPart.LEFT_LOWER_LEG:
      case BodyPart.RIGHT_LOWER_LEG:
        return new Color('teal');
      case BodyPart.LEFT_FOOT:
      case BodyPart.RIGHT_FOOT:
        return new Color('#00ffcc');
      case BodyPart.LEFT_LOWER_ARM:
      case BodyPart.RIGHT_LOWER_ARM:
        return new Color('red');
      case BodyPart.LEFT_UPPER_ARM:
      case BodyPart.RIGHT_UPPER_ARM:
        return new Color('indianred');
      case BodyPart.LEFT_HAND:
      case BodyPart.RIGHT_HAND:
        return new Color('fuchsia');
      case BodyPart.LEFT_SHOULDER:
      case BodyPart.RIGHT_SHOULDER:
        return new Color('#00ffff');
      case BodyPart.LEFT_HIP:
      case BodyPart.RIGHT_HIP:
        return new Color('pink');
    }
  }

  static root: BodyPart = BodyPart.HEAD;

  static children(part: BodyPart): BodyPart[] {
    switch (part) {
      case BodyPart.PLAYSPACE:
      case BodyPart.NONE:
        throw 'Unexpected body part';
      case BodyPart.HEAD:
        return [BodyPart.NECK];
      case BodyPart.NECK:
        return [BodyPart.UPPER_CHEST, BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER];
      case BodyPart.UPPER_CHEST:
        return [BodyPart.CHEST];
      case BodyPart.CHEST:
        return [BodyPart.WAIST];
      case BodyPart.WAIST:
        return [BodyPart.HIP];
      case BodyPart.HIP:
        return [BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP];

      case BodyPart.LEFT_HIP:
        return [BodyPart.LEFT_UPPER_LEG];
      case BodyPart.RIGHT_HIP:
        return [BodyPart.RIGHT_UPPER_LEG];
      case BodyPart.LEFT_UPPER_LEG:
        return [BodyPart.LEFT_LOWER_LEG];
      case BodyPart.RIGHT_UPPER_LEG:
        return [BodyPart.RIGHT_LOWER_LEG];
      case BodyPart.LEFT_LOWER_LEG:
        return [BodyPart.LEFT_FOOT];
      case BodyPart.RIGHT_LOWER_LEG:
        return [BodyPart.RIGHT_FOOT];
      case BodyPart.LEFT_FOOT:
        return [];
      case BodyPart.RIGHT_FOOT:
        return [];

      case BodyPart.LEFT_SHOULDER:
        return [BodyPart.LEFT_UPPER_ARM];
      case BodyPart.RIGHT_SHOULDER:
        return [BodyPart.RIGHT_UPPER_ARM];
      case BodyPart.LEFT_UPPER_ARM:
        return [BodyPart.LEFT_LOWER_ARM];
      case BodyPart.RIGHT_UPPER_ARM:
        return [BodyPart.RIGHT_LOWER_ARM];
      case BodyPart.LEFT_LOWER_ARM:
        return [BodyPart.LEFT_HAND];
      case BodyPart.RIGHT_LOWER_ARM:
        return [BodyPart.RIGHT_HAND];
      case BodyPart.LEFT_HAND:
        return [];
      case BodyPart.RIGHT_HAND:
        return [];
    }
  }

  static parent(part: BodyPart): BodyPart | null {
    switch (part) {
      case BodyPart.PLAYSPACE:
      case BodyPart.NONE:
        throw 'Unexpected body part';
      case BodyPart.HEAD:
        return null;
      case BodyPart.NECK:
        return BodyPart.HEAD;
      case BodyPart.UPPER_CHEST:
        return BodyPart.NECK;
      case BodyPart.CHEST:
        return BodyPart.UPPER_CHEST;
      case BodyPart.WAIST:
        return BodyPart.CHEST;
      case BodyPart.HIP:
        return BodyPart.WAIST;

      case BodyPart.LEFT_HIP:
      case BodyPart.RIGHT_HIP:
        return BodyPart.HIP;
      case BodyPart.LEFT_UPPER_LEG:
        return BodyPart.LEFT_HIP;
      case BodyPart.RIGHT_UPPER_LEG:
        return BodyPart.RIGHT_HIP;
      case BodyPart.LEFT_LOWER_LEG:
        return BodyPart.LEFT_UPPER_LEG;
      case BodyPart.RIGHT_LOWER_LEG:
        return BodyPart.RIGHT_UPPER_LEG;
      case BodyPart.LEFT_FOOT:
        return BodyPart.LEFT_LOWER_LEG;
      case BodyPart.RIGHT_FOOT:
        return BodyPart.RIGHT_LOWER_LEG;

      case BodyPart.LEFT_SHOULDER:
      case BodyPart.RIGHT_SHOULDER:
        return BodyPart.NECK;
      case BodyPart.LEFT_UPPER_ARM:
        return BodyPart.LEFT_SHOULDER;
      case BodyPart.RIGHT_UPPER_ARM:
        return BodyPart.RIGHT_SHOULDER;
      case BodyPart.LEFT_LOWER_ARM:
        return BodyPart.LEFT_UPPER_ARM;
      case BodyPart.RIGHT_LOWER_ARM:
        return BodyPart.RIGHT_UPPER_ARM;
      case BodyPart.LEFT_HAND:
        return BodyPart.LEFT_LOWER_ARM;
      case BodyPart.RIGHT_HAND:
        return BodyPart.RIGHT_LOWER_ARM;
    }
  }
}

export function createChildren(
  bones: Map<BodyPart, BoneT>,
  body: BodyPart,
  parentBone?: BoneKind
): (BoneKind | Bone)[] {
  if (bones.size === 0) return [new Bone()];
  const childrenBodies = BoneKind.children(body);
  const parent = new BoneKind(bones, body, false);
  parentBone?.add(parent);
  if (childrenBodies.length === 0) {
    const tail = new BoneKind(bones, body, true);
    parent.add(tail);
    return [parent, tail];
  }

  const children = childrenBodies.flatMap((bodyPart) =>
    createChildren(bones, bodyPart, parent)
  );
  return [parent, ...children];
}

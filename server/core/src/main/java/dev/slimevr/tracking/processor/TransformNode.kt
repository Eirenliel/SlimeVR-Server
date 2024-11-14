package dev.slimevr.tracking.processor

import io.eiren.util.ann.ThreadSafe
import io.github.axisangles.ktmath.Transform
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

/**
 * Represents a joint
 */
class TransformNode(val localRotation: Boolean) {
	val localTransform = Transform()
	val worldTransform = Transform()
	var parent: TransformNode? = null
		private set
	val children: MutableList<TransformNode> = CopyOnWriteArrayList()

	fun attachChild(node: TransformNode) {
		require(node.parent == null) { "The child node must not already have a parent." }
		children.add(node)
		node.parent = this
	}

	@ThreadSafe
	fun update() {
		// Update transform
		updateWorldTransforms()

		// Update children
		for (node in children) {
			node.update()
		}
	}

	@ThreadSafe
	fun updateThisNode() {
		updateWorldTransforms()
	}

	@Synchronized
	private fun updateWorldTransforms() {
		worldTransform.set(localTransform)
		parent?.let {
			if (localRotation) {
				worldTransform.combineWithParent(it.worldTransform)
			} else {
				combineWithParentGlobalRotation(it.worldTransform)
			}
		}
	}

	fun depthFirstTraversal(visitor: Consumer<TransformNode?>) {
		for (node in children) {
			node.depthFirstTraversal(visitor)
		}
		visitor.accept(this)
	}

	private fun combineWithParentGlobalRotation(parent: Transform) {
		worldTransform.scale = worldTransform.scale hadamard parent.scale
		val scaledTranslation = worldTransform.translation hadamard parent.scale
		worldTransform.translation = parent.rotation.sandwich(scaledTranslation) + parent.translation
	}

	fun detachWithChildren() {
		for (child in children) {
			child.parent = null
		}
		children.clear()
		parent?.children?.remove(this)
		parent = null
	}
}

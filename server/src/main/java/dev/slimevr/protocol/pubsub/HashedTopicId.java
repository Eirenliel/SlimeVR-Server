package dev.slimevr.protocol.pubsub;

import solarxr_protocol.pub_sub.TopicId;

import java.util.Objects;


// This class is so the HashMap referencing the TopicId as key works
// it needs a unique hashcode based on the topicId and also an equals function
// because equals hashcode does not mean equals strings
public class HashedTopicId {

	private final TopicId inner;
	private final int hashcode;

	public HashedTopicId(TopicId topicIdT) {
		this.inner = topicIdT;
		this.hashcode = (inner.getAppName()
			+ "."
			+ inner.getOrganization()
			+ "."
			+ inner.getTopic()).hashCode();
	}

	public TopicId getInner() {
		return inner;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		HashedTopicId that = (HashedTopicId) o;
		return Objects.equals(inner.getOrganization(), that.getInner().getOrganization())
			&& Objects.equals(inner.getAppName(), that.getInner().getAppName())
			&& Objects.equals(inner.getTopic(), that.getInner().getTopic());
	}
}

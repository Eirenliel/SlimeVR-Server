package dev.slimevr.protocol.pubsub;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import dev.slimevr.protocol.GenericConnection;
import dev.slimevr.protocol.ProtocolAPI;
import dev.slimevr.protocol.ProtocolHandler;
import io.eiren.util.logging.LogManager;
import solarxr_protocol.MessageBundle;
import solarxr_protocol.datatypes.Bytes;
import solarxr_protocol.datatypes.StringTable;
import solarxr_protocol.pub_sub.*;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;


public class PubSubHandler extends ProtocolHandler<PubSubHeader> {

	private final ProtocolAPI api;

	// Two ways maps for faster reading when handling lots of packets
	public HashMap<HashedTopicId, Short> topicsHandle = new HashMap<>();
	public HashMap<Short, HashedTopicId> handleTopics = new HashMap<>();

	public AtomicInteger nextLocalHandle = new AtomicInteger();

	public PubSubHandler(ProtocolAPI api) {
		super();
		this.api = api;

		registerPacketListener(PubSubUnion.SubscriptionRequest, this::onSubscriptionRequest);
		registerPacketListener(PubSubUnion.TopicHandleRequest, this::onTopicHandleRequest);
		registerPacketListener(PubSubUnion.Message, this::onTopicMessage);
	}

	/**
	 * @return Returns an unsigned short!
	 */
	private short getTopicHandle(TopicId topicId) {
		HashedTopicId hashedTopicId = new HashedTopicId(topicId);
		Short handleT = topicsHandle.get(hashedTopicId);
		// if no handle exists for this topic id we create one and return it
		// anyway
		if (handleT == null) {
			handleT = (short) nextLocalHandle.incrementAndGet();
			topicsHandle.put(hashedTopicId, handleT);
			handleTopics.put(handleT, hashedTopicId);
		}

		return handleT;
	}

	private int createTopicIdOffset(FlatBufferBuilder fbb, TopicId topicId) {
		return TopicId.createTopicId(
				fbb,
				fbb.createString(topicId.getOrganizationAsByteBuffer()),
				fbb.createString(topicId.getAppNameAsByteBuffer()),
				fbb.createString(topicId.getTopicAsByteBuffer())
		);
	}

	public void onSubscriptionRequest(GenericConnection conn, PubSubHeader messageHeader) {
		SubscriptionRequest req = (SubscriptionRequest) messageHeader.u(new SubscriptionRequest());

		if (req == null)
			return;

		// Unsigned short!
		short subHandle = -1;
		if (req.getTopicType() == Topic.TopicHandle) {
			TopicHandle handle = (TopicHandle) req.topic(new TopicHandle());
			if (handle != null && handleTopics.containsKey(handle.getId()))
				subHandle = handle.getId();
		} else if (req.getTopicType() == Topic.TopicId) {
			TopicId topicId = (TopicId) req.topic(new TopicId());
			if (topicId != null)
				subHandle = getTopicHandle(topicId);
		}

		assert subHandle != -1;

		final short finalSubHandle = subHandle;
		Optional<Integer> first = conn
			.getContext()
			.getSubscribedTopics()
			.stream()
			.filter((handle) -> handle == finalSubHandle)
			.findFirst();
		if (first.isEmpty()) {
			conn.getContext().getSubscribedTopics().add((int) finalSubHandle);
		}


		FlatBufferBuilder fbb = new FlatBufferBuilder(32);
		int topicIdOffset = createTopicIdOffset(fbb, handleTopics.get(finalSubHandle).getInner());
		int topicHandleOffset = TopicHandle.createTopicHandle(fbb, finalSubHandle);

		int outbound = createMessage(
			fbb,
			PubSubUnion.TopicMapping,
			TopicMapping.createTopicMapping(fbb, topicIdOffset, topicHandleOffset)
		);
		fbb.finish(outbound);
		conn.send(fbb.dataBuffer());
	}

	public void onTopicHandleRequest(GenericConnection conn, PubSubHeader messageHeader) {

		TopicHandleRequest topicRequest = (TopicHandleRequest) messageHeader.u(new TopicHandleRequest());

		if (topicRequest == null)
			return;

		short handle = getTopicHandle(topicRequest.getId());

		FlatBufferBuilder fbb = new FlatBufferBuilder(32);
		int topicIdOffset = createTopicIdOffset(fbb, topicRequest.getId());
		int topicHandleOffset = TopicHandle.createTopicHandle(fbb, handle);

		int outbound = createMessage(
			fbb,
			PubSubUnion.TopicMapping,
			TopicMapping.createTopicMapping(fbb, topicIdOffset, topicHandleOffset)
		);
		fbb.finish(outbound);
		conn.send(fbb.dataBuffer());
	}


	public void onTopicMessage(GenericConnection c, PubSubHeader messageHeader) {
		Message message = (Message) messageHeader.u(new Message());

		if (message == null)
			return;

		short subHandle = 1;
		if (message.getTopicType() == Topic.TopicHandle) {
			subHandle = ((TopicHandle) message.topic(new TopicHandle())).getId();
		} else if (message.getTopicType() == Topic.TopicId) {
			subHandle = getTopicHandle((TopicId) message.topic(new TopicId()));
		}


		assert subHandle != -1;

		int finalSubHandle = subHandle;

		this.api.getAPIServers().forEach((server) -> {
			server.getAPIConnections().forEach((conn) -> {
				// Make sure that we are not sending a message to ourselves
				// And check that the receiver has subscribed to the topic
				if (
					!conn.getConnectionId().equals(c.getConnectionId())
						&& conn.getContext().getSubscribedTopics().contains(finalSubHandle)
				) {
					FlatBufferBuilder fbb = new FlatBufferBuilder(32);
					int outbound = createMessage(
						fbb,
						PubSubUnion.Message,
						createMessageMessageOffset(fbb, message)
					);
					fbb.finish(outbound);
					conn.send(fbb.dataBuffer());
				}
			});
		});
	}

	private int createMessageMessageOffset(FlatBufferBuilder fbb, Message msg) {
		int topicOffset = switch(msg.getTopicType()) {
			case Topic.TopicHandle -> TopicHandle.createTopicHandle(
				fbb, ((TopicHandle) msg.topic(new TopicHandle())).getId()
			);
			case Topic.TopicId -> createTopicIdOffset(
				fbb, (TopicId) msg.topic(new TopicId())
			);
			default -> throw new RuntimeException("Unknown message type");
		};
		int payloadOffset = switch(msg.getPayloadType()) {
			case Payload.solarxrProtocolDatatypesStringTable ->
				StringTable.createStringTable(
					fbb,
					fbb.createString(((StringTable) msg.payload(new StringTable())).getSAsByteBuffer())
				);
			case Payload.solarxrProtocolDatatypesBytes ->
				Bytes.createBytes(
					fbb,
					fbb.createByteVector(((Bytes) msg.payload(new Bytes())).getBAsByteBuffer())
				);
			case Payload.KeyValues -> {
				KeyValues keyValues = (KeyValues) msg.payload(new KeyValues());
				keyValues.getByteBuffer()
			}
		};
		return Message.createMessage(
				fbb,
				msg.getTopicType(),
				topicOffset,
				msg.getPayloadType(),
				payloadOffset
		);
	}

	@Override
	public void onMessage(GenericConnection conn, PubSubHeader message) {
		BiConsumer<GenericConnection, PubSubHeader> consumer = this.handlers[message.uType()];
		if (consumer != null)
			consumer.accept(conn, message);
		else
			LogManager
				.info("[ProtocolAPI] Unhandled PubSub packet received id: " + message.uType());
	}

	@Override
	public int messagesCount() {
		return PubSubUnion.names.length;
	}

	public int createMessage(FlatBufferBuilder fbb, byte messageType, int messageOffset) {
		int[] data = new int[1];

		data[0] = PubSubHeader.createPubSubHeader(fbb, messageType, messageOffset);

		int messages = MessageBundle.createPubSubMsgsVector(fbb, data);

		MessageBundle.startMessageBundle(fbb);
		MessageBundle.addPubSubMsgs(fbb, messages);
		return MessageBundle.endMessageBundle(fbb);
	}
}

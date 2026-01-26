import java.io.Serializable;

public class Message implements Serializable {
	private MessageType type;
	private String serviceId;
	private ServiceInstance instance;
	private Object payload;
	
	public Message(MessageType type, String serviceId, ServiceInstance instance, Object payload) {
		this.type = type;
		this.instance = instance;
		this.serviceId = serviceId;
		this.payload = payload;
	}

	public Message(MessageType type, ServiceInstance instance) {
		this(type, null, instance, null);
	}

	public Message(MessageType type, String serviceId) {
		this(type, serviceId, null, null);
	}

	public Message(MessageType type, Object payload) {
		this(type, null, null, payload);
	}

	public MessageType getType() {
		return type;
	}

	public ServiceInstance getInstance() {
		return instance;
	}

	public String getServiceId() {
		return serviceId;
	}

	public Object getPayload() {
		return payload;
	}
}
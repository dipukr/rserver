import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DiscoveryClient {
	private ServiceInstance serviceInstance;
	private String serverHost;
	private int serverPort;
	private ScheduledExecutorService heartbeatExecutor;
	private Map<ServiceInstance, ScheduledFuture<?>> heartbeats;

	public DiscoveryClient(ServiceInstance instance, String serverHost, int serverPort) {
		this.serviceInstance = instance;
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.heartbeats = new ConcurrentHashMap<>();
		this.heartbeatExecutor = Executors.newScheduledThreadPool(2);
		this.register();
	}

	public void register() {
		sendMessage(new Message(MessageType.REGISTER, serviceInstance));
		ScheduledFuture<?> heartbeatTask = 
				heartbeatExecutor.scheduleAtFixedRate(() -> sendHeartbeat(serviceInstance),
				5000, 5000, TimeUnit.MILLISECONDS);
		heartbeats.put(serviceInstance, heartbeatTask);
		System.out.println("Registered service: " + serviceInstance);
	}

	public void deregister() {
		ScheduledFuture<?> task = heartbeats.remove(serviceInstance);
		if (task != null) task.cancel(false);
		sendMessage(new Message(MessageType.DEREGISTER, serviceInstance));
		System.out.println("Deregistered service: " + serviceInstance);
	}

	public void sendHeartbeat(ServiceInstance instance) {
		try {
			sendMessage(new Message(MessageType.HEARTBEAT, instance));
		} catch (Exception e) {
			System.err.println("Heartbeat failed for " + instance + ": " + e.getMessage());
		}
	}

	public List<ServiceInstance> discover(String serviceId) {
		Message response = sendMessage(new Message(MessageType.DISCOVER, serviceId));
		if (response != null && response.getPayload() instanceof List)
			return (List<ServiceInstance>) response.getPayload();
		return Collections.emptyList();
	}

	public List<String> listServices() {
		Message response = sendMessage(new Message(MessageType.LIST_SERVICES, (Object) null));
		if (response != null && response.getPayload() instanceof List)
			return (List<String>) response.getPayload();
		return Collections.emptyList();
	}

	public Message sendMessage(Message message) {
		try (Socket socket = new Socket(serverHost, serverPort);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
			out.writeObject(message);
			out.flush();
			return (Message) in.readObject();
		} catch (Exception e) {
			System.err.println("Failed to send message: " + e.getMessage());
			return null;
		}
	}

	public void shutdown() {
		heartbeats.values().forEach(task -> task.cancel(false));
		heartbeatExecutor.shutdown();
	}
}

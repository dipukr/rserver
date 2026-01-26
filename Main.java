import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
	public static void main(String[] args) throws Exception {
		Thread serverThread = new Thread(new ServerThread());
		serverThread.start();

		Thread.sleep(1000);

		Map<String, String> metadata1 = new HashMap<>();
		metadata1.put("version", "1.0");
		metadata1.put("region", "us-east");
		
		Map<String, String> metadata2 = new HashMap<>();
		metadata2.put("version", "1.0");
		metadata2.put("region", "us-west");

		ServiceInstance userService1 = new ServiceInstance(
				"user-service", "192.168.1.10", 8080, metadata1);

		ServiceInstance userService2 = new ServiceInstance(
				"user-service", "192.168.1.11", 8080, metadata2);

		ServiceInstance orderService = new ServiceInstance(
				"order-service", "192.168.1.20", 8081, new HashMap<>());
		
		DiscoveryClient client1 = new DiscoveryClient(userService1, "localhost", 9090);
		DiscoveryClient client2 = new DiscoveryClient(userService2, "localhost", 9090);
		DiscoveryClient client3 = new DiscoveryClient(orderService, "localhost", 9090);
		
		LoadBalancer lbUser = new LoadBalancer();
		LoadBalancer lbOrder = new LoadBalancer();

		Thread.sleep(1000);

		System.out.println("\n=== Service Discovery ===");
		List<String> services = client1.listServices();
		System.out.println("Available services: " + services);

		System.out.println("\n=== Load Balancing (Round Robin) ===");
		List<ServiceInstance> userInstances = client1.discover("user-service");
		for (int i = 0; i < 5; i++) {
			ServiceInstance instance = lbUser.roundRobin(userInstances);
			System.out.println("Request " + (i + 1) + " -> " + instance);
		}

		System.out.println("\n=== All Instances ===");
		userInstances.forEach(System.out::println);

		// Deregister one instance
		System.out.println("\n=== Deregistering user-service-1 ===");
		client1.deregister();

		Thread.sleep(1000);

		System.out.println("\n=== After Deregistration ===");
		userInstances = client1.discover("user-service");
		userInstances.forEach(System.out::println);

		client1.shutdown();
		client2.shutdown();
	}
}

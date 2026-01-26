import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {
	private AtomicInteger counter = new AtomicInteger(0);
	private Random random = new Random();

	public ServiceInstance roundRobin(List<ServiceInstance> instances) {
		if (instances.isEmpty()) return null;
		int index = Math.abs(counter.getAndIncrement() % instances.size());
		return instances.get(index);
	}

	public ServiceInstance random(List<ServiceInstance> instances) {
		if (instances.isEmpty()) return null;
		return instances.get(random.nextInt(instances.size()));
	}
}

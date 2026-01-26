public class ServerThread implements Runnable {

	@Override
	public void run() {
		var server = new DiscoveryServer(9090, 15000);
		server.start();
	}
}

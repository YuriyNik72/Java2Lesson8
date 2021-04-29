package server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerStart {
	public static void main(String[] args) {
		//new ConsoleServer();

		ExecutorService service=Executors.newFixedThreadPool(50);
		service.execute((Runnable) new ConsoleServer());
		service.shutdown();
	}
}

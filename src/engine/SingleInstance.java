package engine;

import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class SingleInstance {
	private Collection<ActionListener> notifiers;
	private final static int MIN_PORT = 45000;
	private final static int MAX_PORT = 65000;
	private final int appInstancePort;
	private final String appInstanceName;

	public SingleInstance(String instanceName) {
		notifiers = Collections.synchronizedList(new ArrayList<ActionListener>());
		appInstanceName = instanceName;
		appInstancePort = MIN_PORT + (appInstanceName.hashCode() % (MAX_PORT-MIN_PORT)); 
		registerInstance();
	}
	
	public void addAnotherStartNotifier(ActionListener notifier) {
		notifiers.add(notifier);
	}

	private void registerInstance() throws RuntimeException {
		Socket outSock = null;
		byte[] data = new byte[appInstanceName.length()];
		try {
			outSock = new Socket(InetAddress.getLocalHost(), appInstancePort);
			InputStream is = outSock.getInputStream();
			for (int i = 0; i < data.length; i++) {
				// Allow this to throw an exception
				data[i] = (byte) is.read();
			}
			is.close();
			outSock.close();
		} catch (Exception e) {
			// Good!
		}
		
		if (Arrays.equals(data, appInstanceName.getBytes())) {
			throw new RuntimeException("Already Running");
		}

		Thread t = new Thread(new InstanceServer(), "SingleInstanceServer");
		t.setDaemon(true);
		t.start();
	}

	public class InstanceServer implements Runnable {
		@Override
		public void run() {
			try {
				ServerSocket sock = new ServerSocket(appInstancePort, 10, InetAddress.getLocalHost());

				for(;;) {
					Socket client = sock.accept();
					client.getOutputStream().write(appInstanceName.getBytes());
					client.getOutputStream().flush();
					client.close();
					
					for(ActionListener l : notifiers)
						l.actionPerformed(null);
				}
			} catch (Exception e) {
				
			}
		}
	}
}

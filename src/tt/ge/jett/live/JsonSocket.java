package tt.ge.jett.live;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.google.gson.Gson;

public class JsonSocket {
	private static final Logger LOGGER = Logger.getLogger(JsonSocket.class.getName());
	
	private static String escape(String message) {
		return message.replaceAll("\\n", "\\\\n").replaceAll("\\r", "\\\\r");
	}
	
	private Gson gson = new Gson();
	
	private SocketFactory factory;
	private Socket socket;
	private OutputStream out;
	private InputStream in;
	
	private String host;
	private int port;
	
	public JsonSocket(String host, int port) {
		this(host, port, true);
	}
	
	public JsonSocket(String host, int port, boolean ssl) {
		this.host = host;
		this.port = port;
		
		if(ssl) {
			factory = SSLSocketFactory.getDefault();
		} else {
			factory = SocketFactory.getDefault();
		}
	}
	
	public void connect() throws IOException {
		socket = factory.createSocket(host, port);
		out = socket.getOutputStream();
		in = socket.getInputStream();
		
		write("GET / HTTP/1.1\r\nUpgrade: jsonsocket\r\n\r\n");
		read("\r\n\r\n");
	}
	
	public void close() throws IOException {
		socket.close();
	}
	
	public void send(Object obj) throws IOException {
		send(gson.toJson(obj));
	}
	
	public void send(String message) throws IOException {
		write(message + "\n");
	}
	
	public String receive() throws IOException {
		String response = read("\n");
		
		while(response.equals("ping")) {
			send("pong");
			response = read("\n");
		}
		
		return response;
	}
	
	public <T> T receiveJson(Class<T> klass) throws IOException {
		return gson.fromJson(receive(), klass);
	}
	
	public void setGson(Gson gson) {
		this.gson = gson;
	}
	
	private void write(String message) throws IOException {
		out.write(message.getBytes());
		out.flush();
		
		LOGGER.finer("Write -> " + escape(message));
	}
	
	private String read(String terminator) throws IOException {
		ByteArrayOutputStream message = new ByteArrayOutputStream();
		int read = 0;
		byte[] buffer = new byte[1024];
		byte[] term = terminator.getBytes("UTF-8");
		
		while((read = in.read(buffer)) > 0) {
			message.write(buffer, 0, read);
			
			LOGGER.finest("Read from socket: read = " + read + ", total_size = " + message.size());
			
			if(terminated(buffer, read, term)) {
				break;
			}
		}
		
		if(read <= 0 || message.size() < term.length) {
			LOGGER.severe("Unexpected EOF: read = " + read + ", total_size = " + message.size());
			
			throw new IOException("Unexpected EOF");
		}
		
		String result = new String(message.toByteArray(), 0, message.size() - term.length, "UTF-8");
		
		LOGGER.finer("Read -> " + escape(result));
		
		return result;
	}
	
	private boolean terminated(byte[] buffer, int read, byte[] term) {
		int length = read - term.length;
		
		if(length < 0) {
			return false;
		}
		
		for(int i = 0; i < term.length; i++) {
			if(buffer[length + i] != term[i]) {
				return false;
			}
		}
		
		return true;
	}
}

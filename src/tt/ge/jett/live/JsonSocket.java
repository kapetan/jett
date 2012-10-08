package tt.ge.jett.live;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.google.gson.Gson;

public class JsonSocket {
	private Gson gson = new Gson();
	
	private SSLSocket socket;
	private OutputStream out;
	private InputStream in;
	
	private String host;
	private int port;
	
	public JsonSocket(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void connect() throws IOException {
		socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
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
		
		System.out.println("####\n" + response);
		
		while(response.equals("ping")) {
			send("pong");
			response = read("\n");
			
			System.out.println("####\n" + response);
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
		
		System.out.println("----\n" + message);
	}
	
	private String read(String terminator) throws IOException {
		ByteArrayOutputStream message = new ByteArrayOutputStream();
		int read = 0;
		byte[] buffer = new byte[1024];
		byte[] term = terminator.getBytes("UTF-8");
		
		while((read = in.read(buffer)) > 0) {
			message.write(buffer, 0, read);
			
			System.out.println("-> read = " + read + ", message.size() = " + message.size());
			
			if(terminated(buffer, read, term)) {
				break;
			}
		}
		
		System.out.println("-> " + new String(message.toByteArray(), "UTF-8"));
		
		return new String(message.toByteArray(), 0, message.size() - term.length, "UTF-8");
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

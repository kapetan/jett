package tt.ge.jett.rest.url;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Client {
	private static final int BUFFER_LENGTH = 65535;
	private static final Map<String, String> defaultHeaders;
	private static final int REDIRECT_DEPTH = 3;
	private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
	
	static {
		defaultHeaders = new HashMap<String, String>();
		defaultHeaders.put("User-Agent", "gett-jett");
	}
	
	public InputStream request(String method, String url) throws IOException {
		return request(method, url, new HashMap<String, String>());
	}
	
	public InputStream request(String method, String url, Map<String, String> query) throws IOException {
		return request(method, url, query, null, new HashMap<String, String>());
	}
	
	public InputStream request(String method, String url, Map<String, String> query, 
			InputStream body) throws IOException {
		
		return request(method, url, query, body, new HashMap<String, String>());
	}
	
	public InputStream request(String method, String url, Map<String, String> query, 
			InputStream body, Map<String, String> headers) throws IOException {
		
		return request(method, url, query, body, headers, REDIRECT_DEPTH);
	}
	
	public InputStream request(String method, String url, Map<String, String> query, 
			InputStream body, Map<String, String> headers, int followed) throws IOException {
		StringBuilder encodedQuery = new StringBuilder();
		
		for(String key : query.keySet()) {
			encodedQuery.append(URLEncoder.encode(key, "UTF-8"));
			encodedQuery.append("=");
			encodedQuery.append(URLEncoder.encode(query.get(key), "UTF-8"));
		}
		
		if(encodedQuery.length() != 0) {
			encodedQuery.insert(0, "?");
			url += encodedQuery.toString();
		}
		
		headers = this.headers(headers);
		
		LOGGER.fine(method + " " + url + " " + headers);
		
		URLConnection conn = new URL(url).openConnection();
		HttpURLConnection http = (HttpURLConnection) conn;
		
		String contentLength = headers.get("Content-Length");
		
		if(contentLength != null) {
			http.setFixedLengthStreamingMode(Integer.parseInt(contentLength));
		} else if(body != null) {
			http.setChunkedStreamingMode(BUFFER_LENGTH);
		}
		
		for(String key : headers.keySet()) {
			http.setRequestProperty(key, headers.get(key));
		}
		
		http.setDoInput(true);
		http.setRequestMethod(method);
		
		if(body != null) {
			http.setDoOutput(true);
			
			byte[] buffer = new byte[BUFFER_LENGTH];
			int read = 0;
			OutputStream out = http.getOutputStream();
			
			while((read = body.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
			
			out.flush();
			out.close();
		}
		
		int status = http.getResponseCode();
		
		LOGGER.fine(status + " " + http.getResponseMessage());
		
		if(300 <= status && status <= 303 || status == 307) {
			if(followed > 0) {
				Map<String, String> empty = new HashMap<String, String>();
				return request("GET", http.getHeaderField("Location"), empty, null, empty, followed - 1);
			}
			else {
				throw new IOException("Too many redirects");
			}
		}
		else if(status < 200 || status > 299) {
			throw new IOException("Unexpected status code returned " + status + 
					" " + http.getResponseMessage());
		}
		
		return http.getInputStream();
	}
	
	public String readRequest(String method, String url, Map<String, String> query, 
			String body, Map<String, String> headers) throws IOException {
		
		InputStream in = null;
		
		if(body != null) {
			byte[] raw = body.getBytes();
			in = new ByteArrayInputStream(raw);
			
			headers.put("Content-Length", String.valueOf(raw.length));
			
			in = request(method, url, query, in, headers);
			
			LOGGER.finer("Body -> " + body);
		}
		else {
			in = request(method, url, query, null, headers);
		}
		
		byte[] buffer = new byte[BUFFER_LENGTH];
		int read = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		while((read = in.read(buffer)) > 0) {
			out.write(buffer, 0, read);
		}
		
		in.close();
		
		return new String(out.toByteArray(), "UTF-8");
	}
	
	private Map<String, String> headers(Map<String, String> headers) {
		Map<String, String> result = new HashMap<String, String>();
		result.putAll(defaultHeaders);
		result.putAll(headers);
		
		return result;
	}
}

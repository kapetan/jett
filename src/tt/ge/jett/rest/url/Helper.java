package tt.ge.jett.rest.url;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import tt.ge.jett.rest.ReadyState;
import tt.ge.jett.rest.Token;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class Helper {
	public static final Client URL_CLIENT = new Client();
	public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Date.class, 
		new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type arg1,
					JsonDeserializationContext arg2)  throws JsonParseException {
				
				return new Date(json.getAsJsonPrimitive().getAsLong() * 1000);
			}
	}).registerTypeAdapter(ReadyState.class, 
		new JsonDeserializer<ReadyState>() {
			@Override
			public ReadyState deserialize(JsonElement json, Type arg1,
					JsonDeserializationContext arg2)  throws JsonParseException {
				
				String readystate = json.getAsJsonPrimitive().getAsString();
				return ReadyState.valueOf(readystate.toUpperCase());
			}
	}).create();
	
	public static <T> T get(String url, Token token, Class<T> klass) throws IOException {
		String response = request("GET", url, token, null);
		
		return GSON.fromJson(response, klass);
	}
	
	public static <T,V> T post(String url, Token token, V body, Class<T> klass) throws IOException {
		String requestBody = "";
		
		if(body != null) {
			requestBody = GSON.toJson(body);
		}
		
		String response = request("POST", url, token, requestBody);
		
		if(klass != null) {
			return GSON.fromJson(response, klass);
		}
		
		return null;
	}
	
	public static String request(String method, String url, Token token, String body) throws IOException {
		Map<String, String> query = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		
		if(token != null) {
			query.put("accesstoken", token.getAccesstoken());
		}
		
		if(body != null && body.length() != 0) {
			headers.put("Content-Type", "application/json");
		}
		
		return URL_CLIENT.readRequest(method, "https://open.ge.tt/1/" + url, query, body, headers);
	}
	
	public static String apiUrl(String url, String... args) {
		return "https://open.ge.tt/1/" + String.format(url, (Object[]) args);
	}
}

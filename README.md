# Jett

A Java wrapper for the Ge.tt API. Developed and tested using Java 6. This library exposes all documented API actions. You can find the documentation here [https://open.ge.tt/1/doc][api].

# Usage

The REST API can be accessed using the `tt.ge.jett.rest.User` class. With an instance of this class, shares and files can be fetched, created and destroyed.

```Java
// Use the static login method on the User class to get an user instance
User user = User.login("my@email.com", "password", "apikey");

Share share = user.createShare("Share title");
File file = share.createFile("filename.txt");

file.upload("Upload a simple text file containing this string.");
```

It is also possible to download an upload files.

```Java
// The login method is overloaded. Login using a refreshtoken.
User user = User.login("refreshtoken");

Share share = user.getShare("sharename");
File file = share.getFile("fileid");

file.download(new java.io.File("path/to/dir/" + file.getFilename()));
```

The `tt.ge.jett.rest.File` class has multiple convenience methods for uploading and downloading a string or file. It is also possible to get the raw input stream or pass an output stream to be used for uploading or downlading a file.

### Live API

The class `tt.ge.jett.live.Api` is able to listen for live events and notifier listeners when a message has been received.

```Java
User user = User.login("refreshtoken");
Api api = new Api();

api.addMessageListener(new MessageListener.Adapter() {
	@Override
	public void download(String sharename, String fileid, String filename) {
		System.out.println("File downloaded " + filename);
	}
});

api.connect(user);

// The run method blocks this thread. Api implements the Runnable interface, so it can be started in a new Thread.
// This also means that the calls to a message listener also will be executed in that thread.
api.run();
```

[api]:https://open.ge.tt/1/doc "Ge.tt API documentation"

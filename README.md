# Jett

A Java wrapper for the Ge.tt API. Developed and tested using Java 6. This library exposes all documented API actions. You can find the documentation here [https://open.ge.tt/1/doc][api].

# Dependencies

The library uses Googles JSON library [Gson][gson] for JSON parsing and serialization. It needs to be added to the classpath.

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

### SDK

This library also contains a higher level API, which is a combination of the live and REST API. By calling `User#connect()` a live API thread is started, together with multiple worker threads. The worker threads handle all the file uploads, which means calling `File#upload()` will return immediately and the upload will be queued, and uploaded later when a worker thread is available.

The file will still be available to others (e.g. on http://ge.tt), since we are using the live API. When a download event is received for a queued file, that file is moved to the top of the upload queue.

It is possible to add multiple `FileListener` instances to a file and listen for file events. When using the live API, all the events are executed in the same event thread, for this reason no heavy work should be done in the individual listener methods.

```Java
User user = User.login("refreshtoken");

// Start the live API
user.connect();

Share share = user.createShare();
final File file = share.createFile("video.avi");

file.addListener(new FileListener.Adapter() {
	@Override
	public void download() {
		System.out.println(String.format("%s downloaded %s times", file.getFilename(), file.getDownloads()));
	}

	@Override
	public void uploadProgress(long progress, int percent) {
		System.out.println(String.format("%s upload progress %s", file.getFilename(), percent));
	}
});

// Queues upload
file.upload(new java.io.File("/path/to/file"));
```

It is also possible to add file listeners to an user or a share instance, using the `tt.ge.jett.rest.FileProxyListener` interface, which mirrors the `FileListener` interface but every method has an extra file argument, referencing the file which was the target of the event.

# License 

**This software is licensed under "MIT"**

> Copyright (c) 2012 Mirza Kapetanovic
> 
> Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
> 
> The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
> 
> THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

[api]:https://open.ge.tt/1/doc "Ge.tt API documentation"
[gson]:http://code.google.com/p/google-gson/ "GSON"

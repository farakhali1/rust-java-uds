import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

public class server {
  public static void main(String[] args) throws IOException {
    Path socketPath =
        Path.of(System.getProperty("user.home")).resolve("baeldung.socket");
    var socketAddress = UnixDomainSocketAddress.of(socketPath);

    try (ServerSocketChannel serverChannel =
             ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
      serverChannel.bind(socketAddress);

      System.out.println("Server is waiting for client messages...");
      SocketChannel clientChannel = serverChannel.accept();
      while (true) {
        if (clientChannel != null) {
          ByteBuffer buf = ByteBuffer.allocate(64);
          int bytesRead = clientChannel.read(buf);
          if (bytesRead > 0) {
            buf.flip();
            byte[] receivedData = new byte[bytesRead];
            buf.get(receivedData);
            String message = new String(receivedData);

            System.out.println("Received message from client: " + message);
            String responseMessage = "Server received your message: " + message;
            ByteBuffer responseBuffer =
                ByteBuffer.wrap(responseMessage.getBytes());

            clientChannel.write(responseBuffer);
          }
        }
      }
    } finally {
      Files.deleteIfExists(socketAddress.getPath());
    }
  }
}

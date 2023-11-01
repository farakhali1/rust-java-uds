import static java.net.StandardProtocolFamily.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class client {
  public static void main(String[] args) throws Exception {
    Path socketPath =
        Path.of(System.getProperty("user.home")).resolve("baeldung.socket");
    var socketAddress = UnixDomainSocketAddress.of(socketPath);

    try (SocketChannel clientChannel = SocketChannel.open(socketAddress)) {
      String message = "Hello world";

      while (true) {
        ByteBuffer sendBuffer = ByteBuffer.wrap(message.getBytes());
        clientChannel.write(sendBuffer);

        ByteBuffer receiveBuffer = ByteBuffer.allocate(64);
        int bytesRead = clientChannel.read(receiveBuffer);
        if (bytesRead > 0) {
          receiveBuffer.flip();
          byte[] receivedData = new byte[bytesRead];
          receiveBuffer.get(receivedData);
          String responseMessage = new String(receivedData);

          System.out.println("Received response from server: " +
                             responseMessage);

          TimeUnit.SECONDS.sleep(1);
        } else {
          System.out.println("No response from server.");
          break;
        }
      }
    }
  }
}
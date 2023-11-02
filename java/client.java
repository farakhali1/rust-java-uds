import static java.net.StandardProtocolFamily.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import temp.tutorial.input;
import temp.tutorial.result;

public class client {
  public static void main(String[] args) throws Exception {
    Path socketPath =
        Path.of(System.getProperty("user.home")).resolve("baeldung.socket");
    var socketAddress = UnixDomainSocketAddress.of(socketPath);

    try (SocketChannel clientChannel = SocketChannel.open(socketAddress)) {
      input message = input.newBuilder().setX(2).setY(4).build();
      byte[] messageBytes = message.toByteArray();

      while (true) {
        ByteBuffer sendBuffer = ByteBuffer.wrap(messageBytes);
        clientChannel.write(sendBuffer);

        ByteBuffer receiveBuffer = ByteBuffer.allocate(64);
        int bytesRead = clientChannel.read(receiveBuffer);
        if (bytesRead > 0) {
          receiveBuffer.flip();
          byte[] receivedData = new byte[bytesRead];
          receiveBuffer.get(receivedData);

          result response = result.parseFrom(receivedData);

          int value1 = response.getRes();
          System.out.println("Received message from server: " + value1);

          TimeUnit.SECONDS.sleep(1);
        } else {
          System.out.println("No response from server.");
          break;
        }
      }
    }
  }
}
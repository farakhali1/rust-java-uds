import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import temp.tutorial.calculator;
import temp.tutorial.result;

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

            byte[] receivedData = new byte[bytesRead];
            buf.flip();
            calculator message = calculator.parseFrom(buf.get(receivedData));
            int value1 = message.getX();
            int value2 = message.getY();
            System.out.println("Received message from client: number: " +
                               value1 + " number : " + value2);

            int resultValue = value1 + value2 + 1;

            result.Builder messageBuilder =
                result.newBuilder().setRes(resultValue);

            result responseMessage = messageBuilder.build();

            ByteBuffer responseBuffer =
                ByteBuffer.wrap(responseMessage.toString().getBytes());

            // responseBuffer.position(0);
            clientChannel.write(responseBuffer);
            // responseBuffer.flip();
          }
        }
      }
    } finally {
      Files.deleteIfExists(socketAddress.getPath());
    }
  }
}

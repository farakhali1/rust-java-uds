import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import temp.tutorial.input;
import temp.tutorial.result;

public class server {
  public static void main(String[] args) throws IOException {
    Path socketPath =
        Path.of(System.getProperty("user.home")).resolve("my.socket");
    var socketAddress = UnixDomainSocketAddress.of(socketPath);

    try (ServerSocketChannel serverChannel =
             ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
      serverChannel.bind(socketAddress);

      System.out.println("Server is waiting for client messages...");
      SocketChannel clientChannel = serverChannel.accept();
      while (true) {
        if (clientChannel != null) {
          ByteBuffer buf = ByteBuffer.allocate(2048);
          int bytesRead = clientChannel.read(buf);
          if (bytesRead > 0) {
            buf.flip();
            byte[] receivedData = new byte[bytesRead];
            buf.get(receivedData);
            input message = input.parseFrom(receivedData);
            int int_value = message.getIntValue();
            long uint_value = message.getUintValue();
            float float_value1 = message.getFloatValue1();
            float float_value2 = message.getFloatValue2();
            float float_value3 = message.getFloatValue3();
            float float_value4 = message.getFloatValue4();
            float float_value5 = message.getFloatValue5();
            String pubkey = message.getPubkey();
            String signature = message.getSignature();
            String uid = message.getUid();
            Boolean flag = message.getFlag();
            // System.out.printf(
            //     "\n\nRequest from client: int_value: %d, uint_value: %d,
            //     float_value1: %.9f, float_value2: %.9f, float_value3: %.9f,
            //     float_value4: %.9f, float_value5: %.9f, pubkey: %s,
            //     signature: %s, uid: %s, flag: %b", int_value, uint_value,
            //     float_value1, float_value2, float_value3, float_value4,
            //     float_value5, pubkey, signature, uid, flag);

            int resultValue = int_value + (int)uint_value;

            result responseMessage = result.newBuilder()
                                         .setRes(resultValue)
                                         .setUintValue(uint_value)
                                         .setPubkey(pubkey)
                                         .setUid(uid)
                                         .setFlag(false)
                                         .setFloatValue1(float_value1)
                                         .setFloatValue2(float_value2)
                                         .build();
            byte[] responseBytes = responseMessage.toByteArray();

            ByteBuffer responseBuffer = ByteBuffer.wrap(responseBytes);
            clientChannel.write(responseBuffer);
          }
        }
      }
    } finally {
      Files.deleteIfExists(socketAddress.getPath());
    }
  }
}

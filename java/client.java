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
        Path.of(System.getProperty("user.home")).resolve("my.socket");
    var socketAddress = UnixDomainSocketAddress.of(socketPath);

    try (SocketChannel clientChannel = SocketChannel.open(socketAddress)) {
      input message =
          input.newBuilder()
              .setIntValue(2)
              .setUintValue(34)
              .setFloatValue1(1.11111111f)
              .setFloatValue2(2.00000001f)
              .setFloatValue3(3.99999991f)
              .setFloatValue4(4.10000009f)
              .setFloatValue5(5.11111119f)
              .setPubkey("dfaASJN675hgkGKH6085blkhkjbgiyhkjg67nbhjfgyuGL669BDj")
              .setSignature(
                  "dfaASJN675@gkGKH6085!lkhkjbgiyh#jg67nbhjf&yuGL6$9BDj")
              .setUid("my-random-uuid")
              .setFlag(true)
              .build();
      byte[] messageBytes = message.toByteArray();

      while (true) {
        ByteBuffer sendBuffer = ByteBuffer.wrap(messageBytes);
        clientChannel.write(sendBuffer);

        ByteBuffer receiveBuffer = ByteBuffer.allocate(2048);
        int bytesRead = clientChannel.read(receiveBuffer);
        if (bytesRead > 0) {
          receiveBuffer.flip();
          byte[] receivedData = new byte[bytesRead];
          receiveBuffer.get(receivedData);

          result response = result.parseFrom(receivedData);
          int res = response.getRes();
          long uint_value = response.getUintValue();
          float float_value1 = response.getFloatValue1();
          float float_value2 = response.getFloatValue2();
          String pubkey = response.getPubkey();
          String uid = response.getUid();
          Boolean flag = response.getFlag();
          System.out.printf(
              "\nResponse from server: res: %d, uint_value: %d, float_value1: %.9f, float_value2: %.9f, pubkey: %s, uid: %s, flag: %b",
              res, uint_value, float_value1, float_value2, pubkey, uid, flag);

          TimeUnit.SECONDS.sleep(1);
        } else {
          System.out.println("No response from server.");
          break;
        }
      }
    }
  }
}
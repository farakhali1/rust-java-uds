import static java.net.StandardProtocolFamily.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.Vector;
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

      int n = 1005000;
      int i = 0;
      Vector<Long> latencies = new Vector<Long>(n);
      Boolean run_first_test = false;
      if (args.length > 0 && args[0].equals("1")) {
        System.out.println("Starting Test Number 1");
        while (i < n) {
          i = i + 1;
          long start_time_1 = System.nanoTime(); // Test 1 (start Timer)
          byte[] messageBytes = message.toByteArray();
          ByteBuffer sendBuffer = ByteBuffer.wrap(messageBytes);
          clientChannel.write(sendBuffer);

          ByteBuffer receiveBuffer = ByteBuffer.allocate(2048);
          int bytesRead = clientChannel.read(receiveBuffer);
          if (bytesRead > 0) {
            receiveBuffer.flip();
            byte[] receivedData = new byte[bytesRead];
            receiveBuffer.get(receivedData);

            result response = result.parseFrom(receivedData);

            long end_time_1 = System.nanoTime(); // Test 1 (stop Timer)
            long latency_1 = end_time_1 - start_time_1;
            latencies.add(latency_1);
            int res = response.getRes();
            long uint_value = response.getUintValue();
            float float_value1 = response.getFloatValue1();
            float float_value2 = response.getFloatValue2();
            String pubkey = response.getPubkey();
            String uid = response.getUid();
            Boolean flag = response.getFlag();
            // System.out.printf(
            // "\nResponse from server: res: %d, uint_value: %d,
            // float_value1:
            // %.9f, float_value2: %.9f, pubkey: %s, uid: %s, flag: %b\n",
            // res, uint_value, float_value1, float_value2, pubkey, uid,
            // flag);
          } else {
            System.out.println("No response from server.");
            break;
          }
        }
      } else if (args.length > 0 && args[0].equals("2")) {
        System.out.println("Starting Test Number 2");
        while (i < n) {
          i = i + 1;
          byte[] messageBytes = message.toByteArray();
          ByteBuffer sendBuffer = ByteBuffer.wrap(messageBytes);
          long start_time_2 = System.nanoTime(); // Test 2 (start Timer)
          clientChannel.write(sendBuffer);

          ByteBuffer receiveBuffer = ByteBuffer.allocate(2048);
          int bytesRead = clientChannel.read(receiveBuffer);
          long end_time_2 = System.nanoTime(); // Test 2 (stop Timer)
          if (bytesRead > 0) {
            receiveBuffer.flip();
            byte[] receivedData = new byte[bytesRead];
            receiveBuffer.get(receivedData);

            result response = result.parseFrom(receivedData);

            long latency_2 = end_time_2 - start_time_2;
            latencies.add(latency_2);
            int res = response.getRes();
            long uint_value = response.getUintValue();
            float float_value1 = response.getFloatValue1();
            float float_value2 = response.getFloatValue2();
            String pubkey = response.getPubkey();
            String uid = response.getUid();
            Boolean flag = response.getFlag();
            // System.out.printf(
            // "\nResponse from server: res: %d, uint_value: %d,
            // float_value1:
            // %.9f, float_value2: %.9f, pubkey: %s, uid: %s, flag: %b\n",
            // res, uint_value, float_value1, float_value2, pubkey, uid,
            // flag);

            // TimeUnit.SECONDS.sleep(1);
          } else {
            System.out.println("No response from server.");
            break;
          }
        }
      } else {
        if (args.length > 0) {
          System.out.println(
              "Client: Invalid Argument Provided possibile options are {1,2}");
        } else {
          System.out.println(
              "Client: No Input Argument Provided possibile options are {1,2}");
        }
      }
      for (int j = 0; j < latencies.size(); j++) {
        System.out.print(latencies.get(j) + "\n");
      }
    }
  }
}
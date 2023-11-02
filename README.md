# rust-java-uds

This repository contains IPC communication example between `rust` and `java` over unix domain socket (uds) using `protobuf`.

# How to build and Run
```bash
cd ./rust-java-uds

# build java clinet/server
cd ./java
protoc -I=../proto --java_out=. ../proto/data.proto
javac {client/server}.java temp/tutorial/dataProtos.java -cp .:protobuf-java-3.24.0-RC1.jar
# you need to link "protobuf-java-3.24.0-RC1.jar" file (https://jar-download.com/artifacts/com.google.protobuf/protobuf-java/3.24.0/source-code)

# run java client/server
java -cp .:protobuf-java-3.24.0-RC1.jar {client/server}


# build rust clinet/server
cd ./rust/{client/server}
cargo build

# run rust client/server
cargo run

```
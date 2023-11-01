# rust-java-uds


# How to build and Run
```bash

# build and run java clinet/server
cd ./java
javac client.java
java clinet

javac server.java
java server

# build and run rust clinet/server
cd ./rust/clinet
cargo build
cargo run

cd ./rust/server
cargo build
cargo run
```

protoc -I=. --java_out=. calculator.proto
javac client.java temp/tutorial/calculatorProtos.java -cp .:protobuf-java-3.24.0-RC1.jar
java -cp .:protobuf-java-3.24.0-RC1.jar client
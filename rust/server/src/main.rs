extern crate nix;

use nix::unistd::write;
use std::fs;
use std::io::Read;
use std::os::fd::AsRawFd;
use std::os::unix::net::UnixListener;

include!(concat!(env!("OUT_DIR"), "/proto/mod.rs"));
use data::{Input, Result};
use protobuf::Message;

#[allow(unused_variables)]

fn handle_client(mut stream: std::os::unix::net::UnixStream) {
    let mut buffer = [0; 2048];
    loop {
        match stream.read(&mut buffer) {
            Ok(n) => {
                if n > 0 {
                    let request_message = Input::parse_from_bytes(&buffer[..n]).unwrap();
                    println!("New Request fom client");

                    let int_value = request_message.int_value;
                    let uint_value = request_message.uint_value;
                    let float_value1 = request_message.float_value1;
                    let float_value2 = request_message.float_value2;
                    let float_value3 = request_message.float_value3;
                    let float_value4 = request_message.float_value4;
                    let float_value5 = request_message.float_value5;
                    let pubkey = request_message.pubkey;
                    let signature = request_message.signature;
                    let uid = request_message.uid;
                    let flag = request_message.flag;
                    println!(
                        "\nRequest from client: int_value: {}, uint_value: {}, float_value1: {}, float_value2: {}, float_value3: {}, float_value4: {}, float_value5: {}, pubkey: {},  signature: {}, uid: {}, flag: {}",
                        int_value, uint_value, float_value1, float_value2, float_value3,
                        float_value4, float_value5, pubkey, signature, uid, flag);

                    let result = int_value + uint_value as i32;

                    let mut response_message = Result::new();
                    response_message.res = result;
                    response_message.uint_value = uint_value;
                    response_message.float_value1 = float_value1;
                    response_message.float_value2 = float_value2;
                    response_message.pubkey = pubkey;
                    response_message.uid = uid;
                    response_message.flag = flag;

                    // println!("Message response: {:#?}", response_message);
                    let out_bytes: Vec<u8> = response_message.write_to_bytes().unwrap();
                    // println!("Sending Response -> {:?}", out_bytes);

                    if let Err(err) = write(stream.as_raw_fd(), &out_bytes) {
                        println!("Error sending response: {:?}", err);
                        break;
                    }
                } else {
                    println!("Client disconnected");
                    break;
                }
            }
            Err(err) => {
                println!("Error reading data from client: {:?}", err);
                break;
            }
        }
    }
}

fn main() {
    let socket_path = std::env::var("HOME").unwrap() + "/my.socket";

    let _ = fs::remove_file(&socket_path);

    let listener = UnixListener::bind(&socket_path).expect("Failed to bind Unix socket");

    println!("Server is waiting for client messages...");

    match listener.accept() {
        Ok((stream, _)) => {
            println!("Client connected!");
            handle_client(stream);
        }
        Err(err) => {
            println!("Error accepting connection: {:?}", err);
        }
    }
}

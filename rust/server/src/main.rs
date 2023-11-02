extern crate nix;

use nix::unistd::write;
use std::fs;
use std::io::Read;
use std::os::fd::AsRawFd;
use std::os::unix::net::UnixListener;

include!(concat!(env!("OUT_DIR"), "/proto/mod.rs"));
use data::{Input, Result};
use protobuf::Message;

fn handle_client(mut stream: std::os::unix::net::UnixStream) {
    let mut buffer = [0; 64];
    loop {
        match stream.read(&mut buffer) {
            Ok(n) => {
                if n > 0 {
                    let in_msg = Input::parse_from_bytes(&buffer[..n]).unwrap();
                    println!("New Request fom client");

                    let result = in_msg.x + in_msg.y;

                    let mut out_msg = Result::new();
                    out_msg.res = result;
                    // println!("Message response: {:#?}", out_msg);
                    let out_bytes: Vec<u8> = out_msg.write_to_bytes().unwrap();
                    println!("Sending Response -> {:?}", out_bytes);

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
    let socket_path = std::env::var("HOME").unwrap() + "/baeldung.socket";

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

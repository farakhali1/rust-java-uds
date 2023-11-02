extern crate nix;

use std::io::Read;
use std::io::Write;
use std::os::unix::net::UnixStream;

include!(concat!(env!("OUT_DIR"), "/proto/mod.rs"));
use data::{Input, Result};
use protobuf::Message;

fn main() {
    let socket_path = std::env::var("HOME").unwrap() + "/my.socket";

    match UnixStream::connect(&socket_path) {
        Ok(mut stream) => {
            println!("Connected to the server");

            loop {
                let mut out_msg = Input::new();
                out_msg.x = 2;
                out_msg.y = 34;
                // println!("Message request:\nout_msg {:#?}", out_msg);
                let out_bytes: Vec<u8> = out_msg.write_to_bytes().unwrap();
                println!("Resquest Message -> {:?}", out_bytes);
                if let Err(err) = stream.write_all(&out_bytes) {
                    println!("Error sending message: {:?}", err);
                    break;
                }

                let mut response = [0; 64];
                match stream.read(&mut response) {
                    Ok(n) if n > 0 => {
                        let response_message = Result::parse_from_bytes(&response[..n]).unwrap();
                        println!("Response from server: {}", response_message.res);
                    }
                    Ok(_) => {
                        println!("Received an empty response from the server.");
                    }
                    Err(err) => {
                        println!("Error reading response: {:?}", err);
                        break;
                    }
                }
            }
        }
        Err(err) => {
            println!("Failed to connect to the server: {:?}", err);
            println!("Retrying in 2 seconds...");
            std::thread::sleep(std::time::Duration::from_secs(2));
        }
    }
}

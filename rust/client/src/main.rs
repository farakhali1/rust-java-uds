extern crate nix;

use std::io::Read;
use std::io::Write;
use std::os::unix::net::UnixStream;

fn main() {
    let socket_path = std::env::var("HOME").unwrap() + "/baeldung.socket";

    match UnixStream::connect(&socket_path) {
        Ok(mut stream) => {
            println!("Connected to the server");

            loop {
                let message = String::from("Hello world");

                if let Err(err) = stream.write_all(message.as_bytes()) {
                    println!("Error sending message: {:?}", err);
                    break;
                }

                let mut response = [0; 64];
                match stream.read(&mut response) {
                    Ok(n) if n > 0 => {
                        let response_message = String::from_utf8_lossy(&response[..n]);
                        println!("Received response from server: {}", response_message);
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

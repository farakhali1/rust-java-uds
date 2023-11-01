extern crate nix;

use nix::unistd::write;
use std::fs;
use std::io::Read;
use std::os::fd::AsRawFd;
use std::os::unix::net::UnixListener;

fn handle_client(mut stream: std::os::unix::net::UnixStream) {
    let mut buffer = [0; 64];
    loop {
        match stream.read(&mut buffer) {
            Ok(n) => {
                if n > 0 {
                    let message = String::from_utf8_lossy(&buffer[..n]);
                    println!("Received message from client: {}", message);

                    let response = format!("Server received your message: {}", message);
                    if let Err(err) = write(stream.as_raw_fd(), response.as_bytes()) {
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

    for stream in listener.incoming() {
        match stream {
            Ok(stream) => {
                std::thread::spawn(move || {
                    handle_client(stream);
                });
            }
            Err(err) => {
                println!("Error accepting connection: {:?}", err);
            }
        }
    }
}

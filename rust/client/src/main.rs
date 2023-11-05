extern crate nix;

use std::io::Read;
use std::io::Write;
use std::os::unix::net::UnixStream;
use std::thread;
use std::time::{Duration, SystemTime};

include!(concat!(env!("OUT_DIR"), "/proto/mod.rs"));
use data::{Input, Result};
use protobuf::Message;

fn main() {
    let socket_path = std::env::var("HOME").unwrap() + "/my.socket";

    match UnixStream::connect(&socket_path) {
        Ok(mut stream) => {
            println!("Connected to the server");

            loop {
                let mut request = Input::new();
                request.int_value = 2;
                request.uint_value = 34;
                request.float_value1 = 1.11111111;
                request.float_value2 = 2.00000001;
                request.float_value3 = 3.99999991;
                request.float_value4 = 4.10000009;
                request.float_value5 = 5.11111119;
                request.pubkey =
                    String::from("dfaASJN675hgkGKH6085blkhkjbgiyhkjg67nbhjfgyuGL669BDj");
                request.signature =
                    String::from("dfaASJN675@gkGKH6085!lkhkjbgiyh#jg67nbhjf&yuGL6$9BDj");
                request.uid = String::from("my-random-uuid");
                request.flag = true;
                // println!("Message request:\nrequest {:#?}", request);
                let start_time_1 = SystemTime::now()
                    .duration_since(SystemTime::UNIX_EPOCH)
                    .expect("REASON")
                    .as_nanos(); // Test 1 (start Timer)
                let out_bytes: Vec<u8> = request.write_to_bytes().unwrap();
                // println!("Resquest Message -> {:?}", out_bytes);
                let start_time_2 = SystemTime::now()
                    .duration_since(SystemTime::UNIX_EPOCH)
                    .expect("REASON")
                    .as_nanos(); // Test 2 (start Timer)
                if let Err(err) = stream.write_all(&out_bytes) {
                    println!("Error sending message: {:?}", err);
                    break;
                }

                let mut response = [0; 2048];
                match stream.read(&mut response) {
                    //end 2
                    Ok(n) if n > 0 => {
                        let end_time_2 = SystemTime::now()
                            .duration_since(SystemTime::UNIX_EPOCH)
                            .expect("REASON")
                            .as_nanos(); // Test 2 (stop Timer)
                        let response_message = Result::parse_from_bytes(&response[..n]).unwrap();
                        let end_time_1 = SystemTime::now()
                            .duration_since(SystemTime::UNIX_EPOCH)
                            .expect("REASON")
                            .as_nanos(); // Test 1 (stop Timer)

                        let latency_1 = end_time_1 - start_time_1;
                        let latency_2 = end_time_2 - start_time_2;
                        println!("latency 1: {}, latency 1: {}", latency_1, latency_2);
                        let res: i32 = response_message.res;
                        let uint_value: u64 = response_message.uint_value;
                        let float_value1: f32 = response_message.float_value1;
                        let float_value2: f32 = response_message.float_value2;
                        let pubkey: String = response_message.pubkey;
                        let uid: String = response_message.uid;
                        let flag: bool = response_message.flag;
                        // println!("\nResponse from server: res: {}, uint_value: {}, float_value1: {}, float_value2: {}, pubkey: {}, uid: {}, flag: {}", res, uint_value, float_value1, float_value2, pubkey, uid, flag);
                    }
                    Ok(_) => {
                        println!("Received an empty response from the server.");
                    }
                    Err(err) => {
                        println!("Error reading response: {:?}", err);
                        break;
                    }
                }
                // thread::sleep(Duration::from_secs(2));
            }
        }
        Err(err) => {
            println!("Failed to connect to the server: {:?}", err);
            println!("Retrying in 2 seconds...");
            std::thread::sleep(Duration::from_secs(2));
        }
    }
}

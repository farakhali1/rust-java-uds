use std::path::Path;

fn main() {
    let proto_path = "../../proto/data.proto";
    let absolute_path = Path::new(proto_path)
        .canonicalize()
        .expect("Failed to get absolute path");

    protobuf_codegen::Codegen::new()
        .cargo_out_dir("proto")
        .include(
            absolute_path
                .parent()
                .expect("Failed to get parent directory")
                .to_str()
                .unwrap(),
        )
        .input(
            absolute_path
                .to_str()
                .expect("Failed to convert path to string"),
        )
        .run_from_script();
}

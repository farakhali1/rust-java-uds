#!/bin/bash

is_rust_server=0
is_java_server=0

parse_args() {
    IFS=':' read -ra first_lang <<<"$1"
    language=${first_lang[0]}
    type=${first_lang[1]}
    if [ "$language" == "rust" ]; then
        if [ "$type" == "client" ]; then
            is_rust_server=0
        elif [ "$type" == "server" ]; then
            is_rust_server=1
        else
            echo "Invalid client/server argument."
            exit 1
        fi
    elif [ "$language" == "java" ]; then
        if [ "$type" == "client" ]; then
            is_java_server=0
        elif [ "$type" == "server" ]; then
            is_java_server=1
        else
            echo "Invalid client/server argument."
            exit 1
        fi
    else
        echo "Invalid language argument."
        exit 1
    fi
}

first_arg=$1
second_arg=$2
third_arg=$3
IFS=':' read -ra test <<<"$third_arg"
test_name=${test[0]}
test_number=${test[1]}

if [ "$test_number" != "1" ] && [ "$test_number" != "2" ]; then
    echo "Invalid test number possibile test numbers are {1,2}. Exiting."
    exit 1
fi

parse_args $first_arg
parse_args $second_arg

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ "$is_rust_server" -eq 1 ] && [ "$is_java_server" -eq 0 ]; then
    echo "Running test number "$test_number" with rust server and java client"
    cd $script_dir/../rust/server
    cargo run &
    sleep 5
    cd $script_dir/../java
    java -cp .:protobuf-java-3.24.0-RC1.jar client $test_number >$script_dir/output.txt
    $script_dir/calculate_performance_stats.sh
elif [ "$is_rust_server" -eq 0 ] && [ "$is_java_server" -eq 1 ]; then
    echo "Running test number "$test_number" with rust client and java server"
    cd $script_dir/../java
    java -cp .:protobuf-java-3.24.0-RC1.jar server &
    sleep 5
    cd $script_dir/../rust/client
    cargo run $test_number  >$script_dir/output.txt
    $script_dir/calculate_performance_stats.sh
else
    echo "Invalid args: Usage example"
    echo "./run.sh {rust/java}:{client/server} {rust/java}:{client/server} test:{1,2}"
fi

#!/bin/bash

pid1=
pid2=
logistic_regression_args=""

if [[ -z "$1" || "$1" == "--help" || "$1" == "-h" ]]; then
    java -jar target/logistic-regression-jar-with-dependencies.jar --help
    echo
    echo "Run script usage:"
    echo
    echo "    $0 <dataset> <options>"
    echo
    echo "where <options> are the options described above."
    echo
    echo "Example: $0 mtcars --privacy-budget 1"
    echo
    echo "Available datasets:"
    echo "  mtcars"
    echo "  breast_cancer"
    exit 1
fi

test -f target/logistic-regression-jar-with-dependencies.jar || mvn package

echo "Started at $(date)"

ctrl_c() {
    echo "CTRL-C: cleanup $pid1 and $pid2"
    kill -9 $pid1
    kill -9 $pid2
    exit 1
}

main() {
    trap ctrl_c INT

    dataset=$1
    shift

    out1File=$(mktemp)
    out2File=$(mktemp)

    java \
        -jar target/logistic-regression-jar-with-dependencies.jar \
        -p1:localhost:8871 \
        -p2:localhost:8872 \
        $* \
        -i1 < "target/classes/${dataset}_party1.txt" > ${out1File} 2> party1.log &
    pid1=$!
    java \
        -jar target/logistic-regression-jar-with-dependencies.jar \
        -p1:localhost:8871 \
        -p2:localhost:8872 \
        $* \
        -i2 < "target/classes/${dataset}_party2.txt" > ${out2File} 2> party2.log &
    pid2=$!
    wait

    out1=$(cat ${out1File})
    out2=$(cat ${out2File})

    if [[ "${out1}" != "${out2}" ]]; then
        echo "Output is not the same!"
        echo "Party 1: ${out1}"
        echo "Party 2: ${out2}"
    else
        echo "Output: ${out1}"
    fi
}

time main $*

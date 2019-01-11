#!/bin/sh

test -f target/logistic-regression-jar-with-dependencies.jar || mvn package

echo "Started at $(date)"

run () {
    java \
        -jar target/logistic-regression-jar-with-dependencies.jar \
        -p1:localhost:8871 \
        -p2:localhost:8872 \
        $@
}

main() {
    out1File=$(mktemp)
    out2File=$(mktemp)

    run -i1 < target/classes/mtcars_party1.txt > ${out1File} 2> party1.log &
    run -i2 < target/classes/mtcars_party2.txt > ${out2File} 2> party2.log &
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

time main

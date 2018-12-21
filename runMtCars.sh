#!/bin/sh

mvn package -DskipTests

echo "Started at $(date)"

main() {
    out1File=$(mktemp)
    out2File=$(mktemp)
    java -jar target/logistic-regression-jar-with-dependencies.jar -i1 -p1:localhost:8871 -p2:localhost:8872 < target/classes/mtcars_party1.txt > ${out1File} 2> party1.log &
    java -jar target/logistic-regression-jar-with-dependencies.jar -i2 -p1:localhost:8871 -p2:localhost:8872 < target/classes/mtcars_party2.txt > ${out2File} 2> party2.log &
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

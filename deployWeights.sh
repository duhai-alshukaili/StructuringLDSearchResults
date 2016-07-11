#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 COLLECTION_DIR" >&2
    exit 1
fi

if ! [ -f $1/output/Model.txt ]; then
    echo "$1/output/Model.txt not found." >&2
    exit 1
fi

python extract_weights.py $1/output/Model.txt

if [ -f p.txt ] && [ -f w.txt ] && [ o.txt ]; then
    mv p.txt w.txt o.txt $1/../weights/
else
    echo "Some/All weight files were not generated" >&2
    exit 1
fi


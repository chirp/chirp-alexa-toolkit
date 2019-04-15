#!/bin/bash

if [ "$1" = "python" ]
then
    echo "Building python..."
    rm -rf alexa_python.zip .build
    mkdir .build
    mkdir .build/python
    cd python
    cp lambda_function.py requirements.txt ../.build/python/
    cd ../.build/python
    pip install -r requirements.txt -t ./
    zip -r ../../alexa_python.zip ./*
    echo "done"
elif [ "$1" = "nodejs" ]
then
    echo "Building nodejs..."
    rm -rf alexa_nodejs.zip .build
    mkdir .build
    mkdir .build/nodejs
    cd nodejs
    cp index.js package.json package-lock.json ../.build/nodejs/
    cd ../.build/nodejs
    npm install
    zip -r ../../alexa_nodejs.zip ./node_modules ./index.js
    echo "done"
else
    echo "No platform specified. Available platforms: 'nodejs', 'python'"
fi

#!/bin/bash

docker run --name one-frame -p 8080:8080 -d paidyinc/one-frame
docker run --name forex-cache -d redis

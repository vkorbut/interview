#!/bin/bash

docker run -p 8080:8080 -d paidyinc/one-frame
docker run --name forex-cache -d redis

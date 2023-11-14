#!/bin/bash
if ! lsof -i :80 > /dev/null; then
    echo "Port 80 is not in use. Starting web server..."
else
    echo "Port 80 is in use."
fi
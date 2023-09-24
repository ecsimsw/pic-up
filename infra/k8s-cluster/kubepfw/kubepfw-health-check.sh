if ! lsof -i :8080 > /dev/null; then
    /usr/local/bin/kubectl port-forward service/kong-1691514203-kong-proxy -n kong 8080:80 --address 0.0.0.0
    echo "Port 8080 is not in use. Starting web server..."
else
    echo "Port 8080 is in use."
fi

if ! lsof -i :8443 > /dev/null; then
    /usr/local/bin/kubectl port-forward service/kong-1691514203-kong-proxy -n kong 8443:443 --address 0.0.0.0
    echo "Port 8443 is not in use. Starting web server..."
else
    echo "Port 8443 is in use."
fi

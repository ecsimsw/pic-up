# File server

### Build docker container image
```
docker build -f ./Dockerfile -t mymarket-web:latest .
docker run -d --name mymarket-web -p 3000:80 mymarket-web
```

### Request index.html
```
curl localhost:3000/static/products/html/index.html
```

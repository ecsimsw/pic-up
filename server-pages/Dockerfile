FROM nginx:latest

# config
COPY ./config/nginx.conf                /etc/nginx/nginx.conf
COPY ./config/mymarket.conf             /etc/nginx/conf.d/mymarket.conf

# products
COPY ./files/favicon.ico                /usr/share/nginx/mymarket/static/
COPY ./files/css                        /usr/share/nginx/mymarket/static/css
COPY ./files/html                       /usr/share/nginx/mymarket/static/html
COPY ./files/js                         /usr/share/nginx/mymarket/static/js

# expose
CMD ["/usr/sbin/nginx", "-g", "daemon off;"]
EXPOSE 80

[![GitHub license](https://img.shields.io/github/license/yamagl/stw)](https://github.com/yamagl/stw/blob/master/License) 
# A Simple Socks 5 proxy masquerading as HTTP service

## As HTTP Service 
```
λ curl -v http://localhost:9503
* Rebuilt URL to: http://localhost:9503/
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 9503 (#0)
> GET / HTTP/1.1
> Host: localhost:9503
> User-Agent: curl/7.55.1
> Accept: */*
>
< HTTP/1.1 200 OK
< content-type: text/plain
< content-length: 11
<
Hello World* Connection #0 to host localhost left intact
```


## As Socks5 Proxy

```
$ export http_proxy=socks5://127.0.0.1:9503
$ curl -v http://example.org/
* Uses proxy env variable http_proxy == 'socks5://127.0.0.1:9503'
*   Trying 127.0.0.1:9503...
* TCP_NODELAY set
* SOCKS5 communication to example.org:80
* SOCKS5 connect to IPv4 93.184.216.34:80 (locally resolved)
* SOCKS5 request granted.
* Connected to 127.0.0.1 (127.0.0.1) port 9503 (#0)
> GET / HTTP/1.1
> Host: example.org
> User-Agent: curl/7.68.0
> Accept: */*
>
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Age: 219832
< Cache-Control: max-age=604800
< Content-Type: text/html; charset=UTF-8
< Date: Sun, 02 May 2021 11:51:04 GMT
< Etag: "3147526947+ident"
< Expires: Sun, 09 May 2021 11:51:04 GMT
< Last-Modified: Thu, 17 Oct 2019 07:18:26 GMT
< Server: ECS (oxr/8325)
< Vary: Accept-Encoding
< X-Cache: HIT
< Content-Length: 1256
```

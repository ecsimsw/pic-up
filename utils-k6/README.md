## Stress test with k6
`https://k6.io/docs/`

### Run with docker
```
docker run --rm -i grafana/k6 run - <script.js
```

### vu
k6 runs multiple iterations in parallel with virtual users (VUs).
In general terms, more virtual users means more simulated traffic.

### metrics
- http_req_duration : the end-to-end time of all requests (that is, the total latency)
- http_req_failed : the total number of failed requests
- iterations : the total number of iterations

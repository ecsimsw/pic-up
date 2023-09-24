## Secret for docker registry

### Reason
```
*Docker Desktop is free to use, as part of the Docker Personal subscription, for individuals, non-commercial open source developers, students and educators, and small businesses of less than 250 employees AND less than $10 million in revenue. Commercial use of Docker Desktop at a company of more than 250 employees OR more than $10 million in annual revenue requires a paid subscription (Pro, Team, or Business) to use Docker Desktop. While the effective date of these terms is August 31, 2021, there is a grace period until January 31, 2022 for those that require a paid subscription to use Docker Desktop.
```

### Create secret
```
kubectl create secret docker-registry <secret-name> --docker-username=<your-name> --docker-password=<your-pword>

ex, kubectl create secret docker-registry docker-registry-secret --docker-username=ecsimsw --docker-password=password --namespace=global-keys
```

### How to use 1
```shell
spec:
  containers:
  imagePullSecrets:
    - name: <secret-name>
```

### How to use 2

usually the default serviceAccount is responsible for pulling the images.
```shell
kubectl patch serviceaccount default -p '{"imagePullSecrets": [{"name": "<secret-name>"}]}'
```

## install github actions runner controller

ref,
https://github.com/actions/actions-runner-controller/blob/master/docs/quickstart.md

### Install cert-manager
```
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.8.2/cert-manager.yaml
```

### Install arc with helm
```
helm repo add actions-runner-controller https://actions-runner-controller.github.io/actions-runner-controller
```

```
helm upgrade --install --namespace actions-runner-system --create-namespace\
  --set=authSecret.create=true\
  --set=authSecret.github_token=${GITHUB_PAT_TOKEN}\
  --wait actions-runner-controller actions-runner-controller/actions-runner-controller
```

### Define runnerDeployment for specific repo

``` yaml
apiVersion: actions.summerwind.dev/v1alpha1
kind: RunnerDeployment
metadata:
  name: example-runnerdeploy
spec:
  replicas: 1
  template:
    spec:
      repository: ${USER_NAME}/${REPO_NAME}
      labels:
        - ${MY_LABEL}
```

### HPA

ref, `https://github.com/actions/actions-runner-controller/blob/master/docs/automatically-scaling-runners.md`

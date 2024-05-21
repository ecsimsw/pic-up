## Infra-vault

### Vault kubernetes injector (Side car)

1. Pod 가 생성될 때 Init container에서 Service account(token)로 Vault k8s auth 에 로그인을 요청한다.
2. Vault agent container는 발급 받은 로그인 토큰과 함께 필요한 Secret 값을 요청한다.
3. Vault에선 로그인 토큰의 유효함, 사용자의 권한을 확인한다.
4. 요청한 Secret 값을 전달하고, Vault agent 는 이를 임시 저장 공간에 저장한다.
5. Application container에서 저장된 Secret 값을 참조하여 환경 변수를 구성한다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/f311d849-e2a2-4ac7-b007-16c4f447b38b" width="800px">

### Configuration

#### 1. Helm values.yaml
``` yaml
global:
  externalVaultAddr: "${KUBERNETES_IP:PORT}
injector:
  enabled: "true"
  port: ${PORT}
```

#### 2. Install Vault agent injector
```
helm repo add hashicorp https://helm.releases.hashicorp.com
helm install vault -f values.yaml hashicorp/vault
```

#### 3. Vault kubernetes 인증에 사용할 Secret 생성 
``` yaml
apiVersion: v1
kind: Secret
metadata:
  name: vault-auth-secret
  annotations:
    kubernetes.io/service-account.name: vault
type: kubernetes.io/service-account-token
```
#### 4. Initialize vault kubernetes auth
```
TOKEN_REVIEWER_JWT=$(kubectl get secret vault-auth-secret --output='go-template={{ .data.token }}' | base64 --decode)
KUBE_CA_CERT=$(kubectl config view --raw --minify --flatten --output 'jsonpath={.clusters[].cluster.certificate-authority-data}' | base64 --decode)
KUBE_HOST=$(kubectl config view --raw --minify --flatten -o jsonpath='{.clusters[].cluster.server}')
```
```
vault auth enable kubernetes

vault write auth/kubernetes/config  \
   token_reviewer_jwt="$TOKEN_REVIEWER_JWT" \
   kubernetes_ca_cert="$KUBE_CA_CERT" \
   kubernetes_host="$KUBE_HOST"
```
#### 5. Vault Secret
```
vault secrets enable -path=picup kv

vault kv put picup/common version=1 created=20240516
```

#### 6. Vault Policy
```
path "picup/*" {
  capabilities = ["read", "list"]
}
```
```
vault policy write read-picup /vault/policy/read-picup.hcl
```
#### 7. Vault Auth role
```
vault write auth/kubernetes/role/internal-app \
    bound_service_account_names=default \
    bound_service_account_namespaces=picup \
    policies=read-picup \
    ttl=24h
```
#### 8. Test deployment 
``` yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: vault-injector-test
  name: vault-injector-test
  namespace: picup
spec:
  replicas: 1
  selector:
    matchLabels:
      app: vault-injector-test
  template:
    metadata:
      annotations:
        vault.hashicorp.com/role: internal-app
        vault.hashicorp.com/agent-inject: "true"
        # KEY : agent-inject-secret-FILEPATH prefixes the path of the file, /vault/secrets directory.
        # VALUE : vault secret path
        vault.hashicorp.com/agent-inject-secret-picup: picup/common
        # DESC : template that what to write in
        # SECRET : vault secret path
        # .Data : map[created:20240516 version:1]
        vault.hashicorp.com/agent-inject-template-picup: |
            {{- with secret "picup/common" -}}
            {{- range $k, $v := .Data }}
            {{- $k }}={{ $v }}
            {{end}}{{end}}
      labels:
        app: vault-injector-test
    spec:
      containers:
        - image: alpine
          args:
            - "sh"
            - "-c"
            - "source /vault/secrets/picup && sleep 10000"
          imagePullPolicy: IfNotPresent
          name: vault-injector-test
          resources: {}
      terminationGracePeriodSeconds: 30
```
- cat /vault/secrets/picup
- echo $version
- echo $created

#### 9. Ref
- [Vault helm](https://github.com/hashicorp/vault-helm)
- [Hashicorp/k8s-injector](https://developer.hashicorp.com/vault/docs/platform/k8s/injector)

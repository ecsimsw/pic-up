## Vault cheat sheet

https://developer.hashicorp.com/vault/tutorials/getting-started/getting-started-first-secret

### Init and unseal
- init : Vault 저장소를 초기화한다. Unseal에 사용할 키와 최소 입력 개수를 입력한다.
- unseal : init 후 부여된 unseal 키를 최소 입력 개수만큼 입력하여 unseal 한다.
- login : init 후 부여된 Root key를 이용하여 로그인한다.
```
export VAULT_ADDR="http://127.0.0.1:8200"
vault status

vault operator init -key-shares=3 -key-threshold=2
vault operator unseal

vault login ${ROOT_KEY}
```

### KV
- Key-Value 행태의 Secret engine 을 {PATH}에 생성한다.
- v1는 정적인 key:value secret, v2는 버전닝, TTL, checkAndSet 등의 기능을 갖는다.
```
vault secrets enable -path={PATH} kv
ex) vault secrets enable -path=picup kv
```
- secret 목록을 조회하여 정상 생성을 확인한다.
```
vault secrets list
```
- 생성한 secret engine 아래 {SUB_PATH}를 key로 k-v 값들을 생성한다.
```
vault kv put {PATH}/{SUB_PATH} {k1}={v1} {k2}={v2}
ex) vault kv put picup/common version=1 created=20240516
```
- k-v 값을 조회한다.
```
vault kv get {PATH}/{SUB_PATH}
ex) vault kv get picup/common
```
- {PATH}의 KV secret engine에 생성된 key(SUB_PATH) 목록을 조회한다.
```
vault kv list {PATH}
ex) vault kv list picup
```

### Policy
- 매번 Root 유저를 사용하는 것이 아닌, Picup에서 사용할 User를 따로 생성하여 시크릿을 다루려고 한다.
- User를 생성하기 전, 앞서 생성한 경로의 secrets 에 접근할 수 있는 Policy 를 생성한다.
- 우선 `/vault/policy/admin-picup.hcl` 경로에 secret 의 모든 권한을 부여하는 policy 를 파일로 정의해두었다.
- 정책을 정의한 파일 내용은 다음과 같다.
```
path "picup/*" {
  capabilities = ["create", "read", "update", "patch", "delete", "list"]
}
```
- 해당 파일에 해당하는 정책을 생성한다.
```
vault policy write {POLICY_NAME} {POLICY_FILE_PATH}
ex) vault policy write admin-picup /vault/policy/admin-picup.hcl
```
- policy 목록을 조회하여 정상 생성을 확인한다.
```
vault policy list
```

### Authorization
- Policy 를 갖는 인증 방법 (유저) 를 생성할 수 있다.
- Vault는 token, aws secret, github 등 다양한 인증 방법을 지원한다.
- 예시에선 token 으로 생성한다.
```
vault token create
ex) vault token create -policy=admin-picup 
```
- 생성하는 토큰의 유효 기간을 지정할 수 있다.
```
ex) vault token create -period=30m
```
- 인증 방식 목록을 조회하여 정상 생성을 확인한다.
```
vault auth list
```
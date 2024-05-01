## Infra AWS

### Module : S3
- BE server 에서 직접 업로드 할 수 있다.
- 생성한 Pre-signed url 으로 객체를 업로드 할 수 있다.
- Pre-signed url 으로 업로드시 헤더의 컨텐츠 사이즈를 넘어선 파일은 업로드 할 수 없다.

### Module : Cloudfront
- Origin S3를 캐싱한다.
- 응답 헤더에 Content-Cache 를 추가한다.
- Public key 로 암호화된 개인 CDN URL 을 복호화한다.

### Module : CDN-OAI
- CDN 을 제외한 외부에서 S3 객체에 직접 액세스할 수 없다. (OAI)

### Module : Thumbnail lambda
- S3에 업로드 이벤트를 트리거로 썸네일 파일을 생성, 저장한다.
- 썸네일 생성 결과를 WAS 에 알린다.
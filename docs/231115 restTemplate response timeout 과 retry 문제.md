## RestTemplate response timeout 과 retry 문제

재밌는 이슈가 있었다.
Album 에서 Storage 로 사진을 전달하고 업로드 하는 요청에 10~15초가 걸리거나 Socket timeout 문제가 발생하는 것이었다.
처리하고 나니 생각해볼 수 있었고, 더 꼼꼼했다면 미리 인지 할 수 있었던 문제였다.
한번 경험했으니 다음엔 인지하는걸로.

Storage 서버에서 사진을 업로드하고 응답하는데 시간이 꽤 걸린다. 이 업로드 시간 문제는 다시 정리하기로 하고, 이번엔 다른 문제다.
Storage 서버에서 FS에만 저장하면 응답하는데 평균 0.5초, cloud 만 올리고 응답하면 3초 정도 걸렸다고 확인했다.
그리고 restTemplate 의 response timeout 을 5초로 했다.

이렇게만 생각하고 인수 테스트를 했을 때 파일이 제대로 저장되길래 문제 없을 줄 알았는데 완전 바보였다.

지금까지 파일 크기가 다른 못하고 있다가 파일 크기가 30MB 짜리 사진으로 테스트하는 중에 랜덤하게 Response time 이 갑자기 15초가 걸리는 문제가 있어 완전 헤매었었다.
게다가 일반적일 때는 5초 안으로 문제 없다가 가끔 나오는 문제였고, 특히 response timeout 을 5초로 해두었으니 더 이상하다고 여기고 gateway 의 문제를 보면서 엉뚱한 곳을 뒤지고 있었다.
파일 사이즈 문제로 Kong 에서 넘어갈 때 문제가 되는 경우가 있나, 가끔씩 응답 시간이 뻥튀기 되는 일이 종종 있는건가 하고 말이다.

정답은 간헐적으로 응답이 5초를 벗어날 때가 있는데 그 때마다 restTemplate 은 timeout 정책으로 예외로 처리하고,
내 재시도 로직은 예외를 잡고는 좋다고 두, 세번씩 재시도 하다가 최종적으로 사용자에겐 10 ~ 15초의 응답으로 처리된 것이었다.

```
2023-11-15T11:08:42,287 INFO  [http-nio-8083-exec-2] e.p.c.StorageUploadController: Upload response by user 1, it took 4.19sec
2023-11-15T11:08:50,659 INFO  [http-nio-8083-exec-3] e.p.c.StorageUploadController: Upload response by user 1, it took 4.181sec
2023-11-15T11:08:58,556 INFO  [http-nio-8083-exec-4] e.p.c.StorageUploadController: Upload response by user 1, it took 5.946sec
```

요즘 배포한다고 너무 인프라만 잡았다. 문제도 내가 더 모른다고 생각했던 인프라 문제일 줄 알았다.
특히 connection time out, response time out 설정했음을 생각하고 있었어서, 설정을 애매하게 알고는 자만해서, 더 이상한 땅을 팠지 않았나 싶다.

해결은 우선 response time out 을 늘렸다. 다음 스텝으로 파일 크기에 따라 느려지는 응답을 해결할 방법도 찾아야겠다.
object storage 를 바꿔보고, 백업 업로드를 비동기로 변경할 생각을 해보고, 다른 스토리지 서비스들은 파일 크기별 업로드 시간이 어떻게 되는지 확인해봐야겠다.
아쉬움 리포트.

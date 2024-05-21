## Test utils

### 테스트 목표
- 더미데이터로 유저 3백개, 앨범 3천개, 사진 3천만개를 생성한다.
- 리소스 사용량을 확인하고 파드 수, 리소스 사이즈를 결정한다.
- Container, Heap memory 사용률이 최대 80%를 넘지 않도록 한다.

### 목표 수용치 (vUser : 300, Duration : 10m)
1. My info : 서로 다른 유저가 로그인, 사용자 정보 조회
2. Upload image : 서로 다른 유저가 이미지 업로드 Url 요청
3. Upload image : 한 유저가 동시에 100개의 이미지 업로드 Url 요청
4. Commit image : 서로 다른 유저가 이미지 업로드 Commit
5. Commit image : 한 유저가 동시에 100개의 이미지 업로드 Commit
6. Get pictures : 서로 다른 유저가 무작위 페이지 Picture 조회 반복

### 메모리 조정
- 실행에 필요한 최소 Container 메모리 : 242Mi
- 테스트 통과에 필요한 최소 Container 메모리 : 450Mi

#### 1 pod, 500Mi
![image](https://github.com/ecsimsw/pic-up/assets/46060746/eab4c571-f672-42b8-8301-00aacc70fee6)
- 테스트는 통과하나 Heap 최대 사용량 : 93% -> Java OOM 위험

#### 3 pod, 400Mi
![image](https://github.com/ecsimsw/pic-up/assets/46060746/9ec16999-1cb8-4e7d-a940-83ddaee5f9ca)
- Heap 최대 사용량은 83%, 메모리 사용량은 만족
- 컨테이너가 사용하는 메모리량이 평균 340Mi, limit 에 최대 88% 사용 -> Pod OOM killed 위험

#### 2 pod, 500Mi
![image](https://github.com/ecsimsw/pic-up/assets/46060746/3700b519-f348-495f-b7ff-829ceb043fbf)
- Heap 최대 사용량 85%, 메모리 사용량 만족
- 컨테이너가 사용하는 메모리량이 평균 340Mi, limit 에 최대 67% 사용

![image](https://github.com/ecsimsw/pic-up/assets/46060746/5906001e-972f-4a6d-9be8-5f2d4db99096)

- 요청이 몰리는 상황을 가정하여, 2분동안 500명의 가상 유저, 약 9만개의 요청을 전달

![image](https://github.com/ecsimsw/pic-up/assets/46060746/3f9b9292-b5b9-43b9-a3c1-c50672fa2cdd)

- 결정!

### 인프라
- vCpu 2, vMem 2GB -> vCpu 2, vMem 4GB
- cpu 사용량이 널널해서 코어를 1로 줄이려고 했는데, master node cpu 최소 사양이 cpu 2 이상.
- 억지로 에러 무시하고 kubeadm init 해도 coreDns cpu request가 2이어서 번거로움
- cpu 는 그대로 2 코어를 두는 것으로 😅
- 아래는 결정한 Picup Pod (mem 500Mi)를 2개 띄웠을 때의 htop, 메모리 42% 사용

![image](https://github.com/ecsimsw/pic-up/assets/46060746/266f2998-32f9-4002-9cf5-656d444aaa82)

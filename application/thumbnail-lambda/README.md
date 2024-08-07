## 썸네일 처리 AWS Lambda

### 썸네일 생성을 BE에서 직접할 때의 문제점
- 기존에는 파일을 BE에서 받아 썸네일을 생성하고 S3에 업로드했다.
- 이제 파일을 BE에 업로드하지 않고, FE에서 직접 S3로 업로드하기에 썸네일 처리를 BE에서 할 수 없다.
- BE에서 썸네일을 제작한다고 하더라도 리사이징 또는 프레임 캡쳐에 시간이 오래걸렸고,
- 메모리가 사용되어 처리할 때마다 사용 메모리가 급등했다가 GC로 바로 제거되는 등, 메모리 급변 문제가 있었다.

### S3 triggered lambda
- S3 업로드 이벤트를 트리거하여 썸네일 파일을 저장하는 람다를 제작한다.
- 지정 크기 이상 사진 파일을 리사이징하여 썸네일을 생성한다.
- 동영상의 경우 프레임을 캡쳐하여 썸네일 사진을 생성한다.
- 생성한 썸네일을 S3에 저장한다.
- 썸네일 처리 완료를 BE 서버에 알린다.

### 썸네일이 제작되고 S3에 저장되는 시간 간격 처리 고민
1. 썸네일이 생성되기 전까지는 썸네일로 원본 사진을 사용
2. 동영상은 썸네일 제작 때까지 임시 파일 사용
3. 썸네일 제작이 완료되면 Lambda -> WAS으로 썸네일 파일 생성과 저장이 완료됨을 알림. 

### 이미지 리사이징
- awt.Graphics2D 를 사용한 이미지 리사이징에선 간혹 사진이 회전되는 문제가 있다.
- Image file의 exif 메타데이터를 사용하여 회전 값을 고칠 수 있지만, 해당 메타데이터를 읽기 위한 라이브러리가 추가로 필요하고 코드 추가도 많다.
- Thumbnailator는 exif 를 사용하여 리사이징하기에 회전 문제가 발생하지 않는다. 
- 단, BufferedImage를 원본 파일의 타입으로 사용하면 그대로 회전 문제가 발생한다.
- https://github.com/coobird/thumbnailator/issues/159

### 동영상 캡처 
- JCodec으로 동영상의 첫번째 프레임을 캡쳐한다.
- 프레임 캡쳐시 해당 파일을 파일 시스템에 저장해야 하는데 AWS 람다에선 512 MB 를 기본으로 제공한다.
- Picup의 파일 최대 크기는 300MB로 제한하여 당장은 람다의 임시 공간을 사용해도 문제가 되지 않았다.
- 만약 영구 저장이 필요하거나 더 큰 공간이 필요하다면 EFS를 우선으로 알아볼거 같다.
- https://docs.aws.amazon.com/lambda/latest/dg/gettingstarted-limits.html

### How to build
```
./gradlew buildZip
```

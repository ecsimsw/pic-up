## Utils thumbnail

S3 업로드 이벤트를 트리거하여 썸네일 파일을 저장하는 람다

1. Java17, Thumbnailator, Jcodec, Httpclient
2. 지정 크기 이상 사진 파일을 리사이징하여 썸네일을 생성한다. (Exif)
3. 동영상의 프레임을 캡쳐하여 썸네일 사진을 생성한다.
4. 생성한 썸네일을 S3에 저장한다.
5. 썸네일 처리 완료를 BE 서버에 알린다.

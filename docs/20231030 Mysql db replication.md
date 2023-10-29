# 20231030 Mysql db replication

DB를 백업하기 위해서 replication 을 사용한다. 당장은 '백업'을 위함이 가장 컸지만 replication 도입으로 '부하분산', '지역화'도 얻을 수 있을 것이다.

## Replication 종류 

Mysql 의 복제 방식은 '동기', '비동기', '반동기'가 대표적이다.           
    
동기의 경우에는 한 노드에 요청된 쿼리를 트랜잭션 내에서 다른 노드들에 전달하고 모든 노드들이 동기화되면 트랜잭션의 결과를 반환한다. 
즉 마스터(또는 첫 요청을 받은 노드)가 다른 노드들에 쿼리를 전달하는 식으로 진행된다.    
모든 노드에서 동일한 요청을 수행한 후 결과를 반환하기에 모든 노드에 데이터 정합성이 보장된다는 장점이 있으나, 그렇기 때문에 애플리케이션 쪽에선 DB 처리 속도가 느려지고 트랜잭션의 생명주기가 길어진다는 단점이 있다.    
또 노드 중 하나라도 에러나 다른 상태를 갖게된다면 모든 노드가 결국 요청을 못 다루는 상황이 생기게 된다.    

비동기의 경우에는 한 노드에 요청된 쿼리를 다른 노드에서 읽어서 처리한다.    
Mysql 의 경우 수행된 요청을 Binary log 에 저장하고 slave 에서 이 log 를 읽어 본인의 data 에도 이를 반영하는 식으로 진행된다.    
애플리케이션 입장에선 Master 노드의 처리만 기다리면 되기 때문에 속도에서 이점이 있으나, slave 에서 sync 처리를 반영하는 사이 시간 동안 데이터 정합성이 깨지는 단점이 있다.    

반동기의 경우에는 이 둘을 섞는다.    
Master 가 요청된 요청을 Binary log 에 저장하고 slave 는 이 log 를 읽고 처리가 완료되면 master node 에 ACK 로 알린다. Master 는 한개 이상의 ACK 를 확인하면 그제서야 트랜잭션을 커밋한다.    
이렇게하면 모든 Node 를 기다리지 않되 한 개의 slave 는 데이터 정합성을 유지한다는 것이 보장되게 된다. 그렇지만 여전히 비동기 방식보다 느리고, slave 의 상태나 네트워크에 영향을 받는다는 단점이 있다.         

## 비동기식 Replication

이 문서는 Album server db replication 을 정리하기 위해 작성되었는데, Album server 의 경우 데이터 정합성이 생기는 경우 사진을 삭제했는데 여전히 보인다거나 생성했는데 사진이 안보이는 상황이 가장 대표적일텐데 다시 새로고침을 하지 그렇게 심각한 문제가 아니라고 생각했다.
그보다 동기식으로 처리했을 때의 속도 문제, 노드 예외 문제 등이 더 심각한 문제로 생각했다.

그리고 WAS 가 많거나, 요청이 많은 상황이 아니라 데이터 백업의 목적이 당장은 제일 컸기 때문에 Master, Slave 를 각각 하나씩 두려고 한다. 그렇기에 반동기식은 동기와 같고 의미를 갖지 못한다.
그래서 비동기식 replicate 방식으로 결정하게 되었다.

### 비동기식 Replication 의 정합성 문제

비동기식의 결정 근거에서 데이터 정합성이 가끔 생겨도 심각한 상황이 안나올 것 같다고 했는데, 구체적으로 어떤 상황에서 비동기식 replication 이 데이터 정합성 문제가 생기는지 고민해보았다.     
결국 master db 의 처리와 그 내용이 slave db 에서 sync 완료되는 시간 사이에 slave db 에서 읽기가 되는 경우일 것 같다. 그리고 그 경우들은 아래와 같을 것이다.     

1. 읽기 요청 과다 : slave db에 읽기 요청이 워낙 많아 처리 성능 자체가 떨어지고 sync 도 밀리는 경우이다. 
-> 이런 경우 slave db 를 scale up 하거나, slave db 자체를 증설하여 요청을 분산 할 수 있겠다.

2. Sync 보다 많은 쓰기 : mater db 의 쓰기량에 비해 slave db sync 속도가 낮은 경우이다.  
-> Mysql 의 기본 옵션은 replication 을 위해 싱글 스레드만을 사용했다. master db 는 여러 세션에서 처리 결과를 만들고 쿼리 내용 또는 결과를 log 에 기록하는데 복제를 위한 thread 는 단 한개 뿐이라 쓰기가 많아진다면, slave db 가 많아진다면 정합성 문제는 불가피했다.
-> Mysql 5.6 부터 추가된 Multi-Threaded Replication Slaves 을 사용해서 복제를 위한 스레드 (SQL thread, worker)를 증설하는 것이 도움이 된다.  
-> [mysql-mysql-5-6-ga-replication-enhancements](https://dev.mysql.com/blog-archive/mysql-5-6-ga-replication-enhancements/)

3. 너무 느린 Sync : slave db 에서 Sync 를 처리하는 시간 자체가 느린 경우를 말한다. 그 시간 동안 정합성 문제가 생긴다.
-> Mysql 의 binary log 을 이용한 동기화엔 여러 방식이 있다. (Binary Format) 
-> 그 중 Statement Based Replication 는 master 에서 처리한 쿼리를 그래도 slave 에서 실행하는 방식이고 그 경우 쿼리 시간 이상은 반드시 정합성 문제가 생긴다.
-> 쿼리 속도 자체를 낮추거나 Row Based Replication 으로 바꿔 master 에서 쿼리 실행으로 변경된 row 를 sync 하는 것으로 쿼리 시간 대신 변경 반영 시간으로 정합성 문제의 시간을 바꿀 수 있다.

#### Multi source replication 
반대로 여러 master 에서 한 slave 에 복제가 요구되는 경우도 있을 것이다. 예를 들어 DB 를 분리해둬서 master 가 여러 개인데 모니터링이나 로그를 이유로 한번에 여러 master db 의 처리를 모으고 싶은 경우이다.        
그럴 땐 Multi source replication 을 키워드로 적용할 수 있을 것 같다.   


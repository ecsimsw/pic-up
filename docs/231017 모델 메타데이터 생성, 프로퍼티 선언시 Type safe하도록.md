## Hibernate jpa model metadata

Criteria api 나 Sort로 엔티티의 프로퍼트를 사용해야할 때 아래처럼 프로퍼티명을 직접 선언해야하는 경우가 많았다.
이렇게 선언하는 경우 type safe 하지 않아 관리에 어려움이 보였지만, 그렇다고 리플렉션을 사용하고 싶지는 않았다.   
``` java
PageRequest.of(0, 1, Sort.by(Direction.DESC, "orderNumber", "id"));
```

model metadata 를 컴파일 시점에서 생성해주는 라이브러리를 사용하여 type safe 문제를 해결할 수 있었다.

hibernate-jpamodelgen 를 빌드 의존성에 추가해주고,  
```
dependencies {
    implementation 'org.hibernate:hibernate-jpamodelgen'
    annotationProcessor 'org.hibernate:hibernate-jpamodelgen'
}
```

gradle 빌드를 하게되면 'Entity명' + "_"으로 metadata 파일이 생성된다.

이제 자동 생성된 metadata 파일을 이용하여 위 코드는 아래처럼 바꿀 수 있다.
``` java
PageRequest.of(0, 1, Sort.by(Direction.DESC, Picture_.ORDER_NUMBER, Picture_.ID));
```


package ecsimsw.picup;

import ecsimsw.picup.domain.Member;
import ecsimsw.picup.domain.MemberRepository;
import ecsimsw.picup.domain.Password;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class PicUpMemberApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpMemberApplication.class);
        app.setAdditionalProfiles("dev");
        ConfigurableApplicationContext run = app.run(args);
        TestDummy testDummy = run.getBean(TestDummy.class);

        testDummy.doSave();
        testDummy.doSaveAll();
    }
}

@Component
class TestDummy {

    @Autowired
    private MemberRepository memberRepository;

    public void doSave() {
        long start = System.currentTimeMillis();
        for(int i =0; i< 3; i++) {
            memberRepository.save(new Member("hi", new Password("hi", "asdfadsfsdf")));
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    public void doSaveAll() {
        long start = System.currentTimeMillis();
        LinkedList<Member> list = new LinkedList<>();
        for(int i =0; i< 3; i++) {
            list.add(new Member("hi", new Password("hi", "asdfadsfsdf")));
        }
        memberRepository.saveAll(list);
        System.out.println(System.currentTimeMillis() - start);
    }

    // TODO :: saveAll 은 트랜젝션이 하나, save은 개당 -> 그래서 느리다
    // TODO :: 우리는 어차피 @Transactional로 묶을텐데? -> 어차피 하나로 묶이지 않나?
    // TODO :: @Transactional로 묶어서 이미 있는 트렌젝션에 참여하는 애들도 그래도 느리다.
    // TODO :: -> 그럼 트랜젝션 자체의 생성과는 거리가 있네.


    // TODO :: 일단 오늘 밤 결과
    // TODO :: 1. 쿼리 개수는 같다.
    // TODO :: 2. @Transcationl을 안붙이면 save는 매번 트랜젝션, saveall 하나
    // TODO :: 3. @Transcationl을 붙이면 save는 이미 있는 트랜젝션에 참여, 대신 엔티티 매니저 세션 같은 선처리 로직은 필요함,
    //                                 saveall 하나만, 다른 트랜젝션 처리 없음
    // TODO :: 4. save, saveall을 확인했다. saveall은 별개 없다 단순히 내부 클래스에서 save를 반복호출한다.
    // TODO ::                          단,

}


package hello.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BatchApplicationTests {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    //	@Test
//    void repositoryTest() {
//        for (Person person : personRepository.findAll()) {
//            System.out.println(person.getName());
//        }
//    }
//
//    @BeforeEach
//    void setUp() {
//        personRepository.deleteAll();
//        for (int i = 0; i < 20; i++) {
//            Person person = new Person();
//            person.setName("name" + i);
//            personRepository.save(person);
//        }
//    }
//
//    @Test
//    void prettyTest() throws Exception {
//        jobLauncher.run(job, new JobParametersBuilder().addDate("date", new Date()).toJobParameters());
//    }
}

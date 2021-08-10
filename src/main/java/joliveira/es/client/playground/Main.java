package joliveira.es.client.playground;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);

        EsHealthChecker esHealthChecker = context.getBean(EsHealthChecker.class);
        System.out.println(esHealthChecker.isHealthy());

        EsDao esDao = context.getBean(EsDao.class);

        List<Integer> companies = Arrays.asList(1, 2, 3);
        List<Integer> ages =  Arrays.asList(20, 30, 40, 50, 70, 80, 90, 100);


        Random rand = new Random();

        for (int i = 0; i < 100000; i++) {

            int companyId = companies.get(rand.nextInt(companies.size()));
            int age = rand.nextInt(ages.size());
            Person person = new Person();
            person.setId(UUID.randomUUID().toString());
            person.setName(String.format("Person_%s_%s", companyId, age));
            person.setEmail(String.format("some-email-%s-%s@github.com", companyId, age));
            person.setAge(age);
            person.setCompanyId(companyId);

            esDao.save(person);
        }

    }
}

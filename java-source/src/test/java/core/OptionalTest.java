package core;

import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Optional特性、实例分析
 */
public class OptionalTest {

    @Test
    public void test() {

        Zoo zoo = new Zoo();
        if (zoo != null) {
            Dog dog = zoo.getDog();
            if (dog != null) {
                int age = dog.getAge();
                System.out.println(age);

            }
        }

        String name = Optional.ofNullable(zoo).map(o -> o.getDog()).map(d -> d.getName()).orElse("");



        ZooFlat zooFlat = new ZooFlat();

        Optional.ofNullable(zooFlat).map(o -> o.getDog()).flatMap(d -> d.getAge()).ifPresent(age ->
                System.out.println(age)
        );

        Optional.empty().filter(e->equals(1));

    }

    class Zoo {
        private Dog dog = new Dog();

        public Dog getDog() {
            return dog;
        }
    }

    class Dog {
        private int age = 1;

        private String name;

        public int getAge() {
            return age;
        }

        public String getName() {
            return name;
        }
    }


    class ZooFlat {
        private DogFlat dog = new DogFlat();

        public DogFlat getDog() {
            return dog;
        }
    }

    class DogFlat {
        private int age = 1;

        public Optional<Integer> getAge() {
            return Optional.ofNullable(age);
        }
    }

}

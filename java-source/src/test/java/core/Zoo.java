package core;

public class Zoo {

    private Tiger tiger;

    public static void main(String[] args) {

        Zoo zoo = new Zoo();

        zoo.setTiger(new Tiger("公老虎"));

        zoo.setTiger(new Tiger("母老虎"));


    }

    public Tiger getTiger() {
        return tiger;
    }

    public void setTiger(Tiger tiger) {
        this.tiger = tiger;
    }

    static class Tiger{
        public Tiger() {
        }

        public Tiger(String name){

        }
    }
}


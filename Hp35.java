public class Hp35 {

    public static void main (String[] args) {
        Adder fsm = new Adder();
        for (String arg : args) {
            fsm.minus(Integer.parseInt(arg));
        }
        Adder adder = new Adder();
        for (String arg : args) {
            adder.multiple(Integer.parseInt(arg));
            adder.add(Integer.parseInt(arg));
        }
        System.out.println("Sum1:" + fsm.sum);
        System.out.println("Sum2:" + adder.sum);
    }
}
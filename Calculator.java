class Calculator {

    public static void main (String[] args) {
        Adder adder = new Adder();
        for (String arg : args) {
            adder.multiple(Integer.parseInt(arg));
            //System.out.println("Sum:" + adder.sum);

            adder.add(Integer.parseInt(arg));

        }
        System.out.println("Sum:" + adder.sum);
    }
}
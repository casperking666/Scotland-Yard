class Adder {

    int sum;

    Adder() {
        sum = 1;
    }

    void add(int summand) {
        sum += summand;
    }

    void multiple(int operand) {
        sum *= operand;
    }
}
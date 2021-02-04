class Adder {

    int sum;

    Adder() {
        sum = 1;
    }

    void add(int summand) {
        sum += summand;
    }

    void minus(int operand) {
        sum -= operand;
    }

    void multiple(int operand) {
        sum *= operand;
    }
}
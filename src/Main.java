import java.util.*;

public class Main {

    public static void main(String[] args) {

        Calculator calc = new Calculator();

        Scanner s = new Scanner(System.in);
        while (calc.input(s.nextLine())) {}

    }
}
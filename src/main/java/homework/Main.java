package homework;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {


        System.out.println("Введите первое число:");
        int firstNumber = Integer.parseInt(new Scanner(System.in).nextLine());

        System.out.println("Введите второе число:");
        int secondNumber = Integer.parseInt(new Scanner(System.in).nextLine());

        int sum = (int) secondNumber + (int) firstNumber;
        System.out.println("Cумма чисел:" + sum);

        int difference = (int) secondNumber - (int) firstNumber;
        System.out.println("Разность чисел:" + difference);

        int product = (int) secondNumber * (int) firstNumber;
        System.out.println("Произведение чисел:" + product);

        double quotient = (double) secondNumber / firstNumber;
        System.out.println("Частное чисел:" + quotient);
    }
}

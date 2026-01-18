import ru.stepup.utils.Analyze;

import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Создаём Scanner один раз

        while (true) {
            System.out.println("Введите путь к файлу:");
            String path = scanner.nextLine(); // Используем существующий scanner
            File file = new File(path);

            if (!file.exists()) {
                System.out.println("Файл не существует");
                continue;
            }
            if (file.isDirectory()) {
                System.out.println("Это папка, а не файл");
                continue;
            }

            System.out.println("Путь указан верно");

            // Анализируем файл и выводим результаты
            Analyze.analyzeFile(path);

            System.out.println("\n" + "=".repeat(60) + "\n");
        }
    }
}
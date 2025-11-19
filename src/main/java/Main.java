import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        int pathFileCount = 0;
        while (true) {
            System.out.println("Введите путь к файлу:");
            String path = new Scanner(System.in).nextLine();
            File file = new File(path);
            boolean fileExists = file.exists(); // проверка существования файла
            boolean isDirectory = file.isDirectory(); // проверка того что это файл, а не папка
            if (!fileExists) {
                System.out.println("Файл не существует");
                continue;
            }
            if (isDirectory) {
                System.out.println("Это папка, а не файл");
                continue;
            }
            pathFileCount++;
            System.out.println("Путь указан верно");
            System.out.println("Это файл номер " + pathFileCount);
        }
    }
}

import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int pathFileCount = 0;
        while (true) {
            System.out.println("Введите путь к файлу:");
            String path = new Scanner(System.in).nextLine();
            File file = new File(path);
            boolean fileExists = file.exists(); // проверка существования файла
            boolean isFile = file.isFile(); // проверка того что это файл
            boolean isDirectory = file.isDirectory(); // проверка того что это файл, а не папка
            if (!fileExists || isDirectory) {
                if (!fileExists) {
                    System.out.println("Файл не существует");
                } else if (isDirectory) {
                    System.out.println("Это папка, а не файл");
                }
                continue;
            }
            pathFileCount++;
            System.out.println("Путь указан верно");
            System.out.println("Это файл номер " + pathFileCount);
        }
    }
}

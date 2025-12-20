import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

            try {
                FileReader fileReader = new FileReader(path);
                BufferedReader reader =
                        new BufferedReader(fileReader);
                String line;
                int lineCount = 0;
                String longestLine = "";
                String shortestLine = "";
                int longestLength = 0;
                int shortestLength = Integer.MAX_VALUE;
                while ((line = reader.readLine()) != null) {
                    int length = line.length();
                    lineCount++;

                    if (length > longestLength) {
                        longestLength = length;
                        longestLine = line;
                    }

                    if (length < shortestLength) {
                        shortestLength = length;
                        shortestLine = line;
                    }

                    if (length > 1024) {
                        throw new IllegalArgumentException("Длина строки превышает 1024 символа");
                    }
                }
                System.out.println("Общее количество строк: " + lineCount);
                System.out.println("Длина самой длинной строки в файле: " + longestLine);
                System.out.println("Длина самой короткой строки в файле: " + shortestLine);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                System.err.println("Обработка файла прекращена");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }
}

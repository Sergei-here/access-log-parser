import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

class VeryLongLine extends RuntimeException {
    public VeryLongLine(String message) {
        super(message);
    }
}

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
            analyzeFile(path);

            System.out.println("\n" + "=".repeat(60) + "\n");
        }
    }

    // Анализируем файл и выводим результаты
    private static void analyzeFile(String filePath) {
        int totalLines = 0;
        int yandexBotCount = 0;
        int googleBotCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                totalLines++;

                // Проверка на слишком длинную строку
                if (line.length() > 1024) {
                    throw new VeryLongLine("Длина строки превышает 1024 символа");
                }

                // Анализ строки лога
                String userAgent = extractUserAgentFromLogLine(line);
                if (userAgent != null) {
                    String program = extractProgramFromUserAgent(userAgent);

                    if (program != null) {
                        if (program.equalsIgnoreCase("YandexBot")) {
                            yandexBotCount++;
                        } else if (program.equalsIgnoreCase("Googlebot")) {
                            googleBotCount++;
                        }
                    }
                }
            }

            // Вывод результатов после анализа всего файла
            printResults(totalLines, yandexBotCount, googleBotCount);

        } catch (VeryLongLine e) {
            System.err.println(e.getMessage());
            System.err.println("Обработка файла прекращена");
        } catch (Exception ex) {
            System.err.println("Ошибка при чтении файла: " + ex.getMessage());
        }
    }

     //Выводим результаты анализа
    private static void printResults(int totalLines, int yandexBotCount, int googleBotCount) {
        System.out.println("══════════════ РЕЗУЛЬТАТЫ АНАЛИЗА ══════════════");
        System.out.println("Всего запросов в файле: " + totalLines);
        System.out.println("Запросов от YandexBot: " + yandexBotCount);
        System.out.println("Запросов от Googlebot: " + googleBotCount);

        if (totalLines > 0) {
            double yandexShare = (double) yandexBotCount / totalLines * 100;
            double googleShare = (double) googleBotCount / totalLines * 100;
            double totalBotShare = (double) (yandexBotCount + googleBotCount) / totalLines * 100;

            System.out.println("\nДоли запросов:");
            System.out.printf("YandexBot: %.2f%%\n", yandexShare);
            System.out.printf("Googlebot: %.2f%%\n", googleShare);
            System.out.printf("Всего от поисковых ботов: %.2f%%\n", totalBotShare);
            System.out.printf("Прочие запросы: %.2f%%\n", 100 - totalBotShare);
        } else {
            System.out.println("Файл пуст!");
        }

        System.out.println("════════════════════════════════════════════════");
    }

    //Извлекаем User-Agent из строки лога
    private static String extractUserAgentFromLogLine(String logLine) {
        int lastQuoteIndex = logLine.lastIndexOf('"');
        if (lastQuoteIndex == -1) return null;

        int secondLastQuoteIndex = logLine.lastIndexOf('"', lastQuoteIndex - 1);
        if (secondLastQuoteIndex == -1) return null;

        String userAgent = logLine.substring(secondLastQuoteIndex + 1, lastQuoteIndex).trim();

        if (userAgent.isEmpty() || userAgent.equals("-")) {
            return null;
        }
        return userAgent;
    }

    // Извлекаем название программы из User-Agent
    private static String extractProgramFromUserAgent(String userAgent) {
        try {
            int openBracketIndex = userAgent.indexOf('(');
            int closeBracketIndex = userAgent.indexOf(')', openBracketIndex);

            if (openBracketIndex == -1 || closeBracketIndex == -1) {
                return null;
            }

            String firstBrackets = userAgent.substring(openBracketIndex + 1, closeBracketIndex);
            String[] parts = firstBrackets.split(";");

            if (parts.length >= 2) {
                String fragment = parts[1].trim();
                int slashIndex = fragment.indexOf('/');
                if (slashIndex != -1) {
                    return fragment.substring(0, slashIndex).trim();
                } else {
                    return fragment.trim();
                }
            }

        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
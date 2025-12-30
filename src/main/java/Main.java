import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.temporal.ChronoUnit;
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
            analyzeFile(path);

            System.out.println("\n" + "=".repeat(60) + "\n");
        }
    }

    // Анализируем файл и выводим результаты
    private static void analyzeFile(String filePath) {
        int totalLines = 0;
        int yandexBotCount = 0;
        int googleBotCount = 0;
        Statistics statistics = new Statistics();
        int parseErrors = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                totalLines++;

                // Проверка на слишком длинную строку
                if (line.length() > 1024) {
                    throw new VeryLongLine("Длина строки превышает 1024 символа");
                }

                try {
                    // Создаем объект LogEntry используя новые классы
                    LogEntry entry = new LogEntry(line);

                    // Добавляем запись в статистику
                    statistics.addEntry(entry);

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

                } catch (IllegalArgumentException e) {
                    // Пропускаем строки с неверным форматом
                    parseErrors++;
                    System.err.println("Ошибка парсинга строки " + totalLines + ": " + e.getMessage());
                }
            }

            // Вывод результатов после анализа всего файла
            printResults(totalLines, yandexBotCount, googleBotCount, statistics, parseErrors);

        } catch (VeryLongLine e) {
            System.err.println(e.getMessage());
            System.err.println("Обработка файла прекращена");
        } catch (Exception ex) {
            System.err.println("Ошибка при чтении файла: " + ex.getMessage());
        }
    }

    //Выводим результаты анализа
    private static void printResults(int totalLines, int yandexBotCount, int googleBotCount,
                                     Statistics statistics, int parseErrors) {
        System.out.println("══════════════ РЕЗУЛЬТАТЫ АНАЛИЗА ══════════════");
        System.out.println("Всего строк в файле: " + totalLines);
        System.out.println("Успешно разобрано строк: " + statistics.getEntryCount());
        System.out.println("Ошибок парсинга: " + parseErrors);
        System.out.println("Запросов от YandexBot: " + yandexBotCount);
        System.out.println("Запросов от Googlebot: " + googleBotCount);

        System.out.println("\n══════════════ СТАТИСТИКА ТРАФИКА ══════════════");
        System.out.println("Общий объем трафика: " + statistics.getTotalTraffic() + " байт");
        if (statistics.getMinTime() != null && statistics.getMaxTime() != null) {
            long hoursBetween = ChronoUnit.HOURS.between(statistics.getMinTime(), statistics.getMaxTime());
            System.out.println("Период анализа: " + statistics.getMinTime() + " - " + statistics.getMaxTime());
            System.out.println("Всего часов в периоде: " + (hoursBetween == 0 ? 1 : hoursBetween));
        }
        System.out.printf("Средний трафик в час: %.2f байт/час\n", statistics.getTrafficRate());

        if (statistics.getEntryCount() > 0) {
            double yandexShare = (double) yandexBotCount / statistics.getEntryCount() * 100;
            double googleShare = (double) googleBotCount / statistics.getEntryCount() * 100;
            double totalBotShare = (double) (yandexBotCount + googleBotCount) / statistics.getEntryCount() * 100;

            System.out.println("\n══════════════ ДОЛИ ЗАПРОСОВ ═══════════════");
            System.out.printf("YandexBot: %.2f%%\n", yandexShare);
            System.out.printf("Googlebot: %.2f%%\n", googleShare);
            System.out.printf("Всего от поисковых ботов: %.2f%%\n", totalBotShare);
            System.out.printf("Прочие запросы: %.2f%%\n", 100 - totalBotShare);
        } else {
            System.out.println("\nНет успешно разобранных записей для анализа долей!");
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Statistics {

    private long totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private int entryCount;
    private Set<String> existingPages; // Множество существующих страниц (код ответа 200)
    private Set<String> nonExistingPages; // Множество существующих страниц (код ответа 404)
    private Map<String, Integer> osCounts; // Счетчики операционных систем
    private Map<String, Integer> browserCounts; // Счетчики браузеров
    private int humanVisitsCount; // Количество посещений реальными пользователями (не ботами)
    private int errorRequestsCount; // Количество ошибочных запросов (4xx или 5xx)
    private Set<String> uniqueHumanIPs; // Уникальные IP-адреса реальных пользователей

    // Конструктор без параметров
    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.entryCount = 0;
        this.existingPages = new HashSet<>();
        this.osCounts = new HashMap<>();
        this.nonExistingPages = new HashSet<>();
        this.browserCounts = new HashMap<>();
        this.humanVisitsCount = 0;
        this.errorRequestsCount = 0;
        this.uniqueHumanIPs = new HashSet<>();
    }

    // Метод для добавления записи лога
    public void addEntry(LogEntry entry) {
        // Добавляем трафик
        this.totalTraffic += entry.getResponseSize();

        // Обновляем minTime и maxTime
        LocalDateTime entryTime = entry.getTime();

        if (this.minTime == null || entryTime.isBefore(this.minTime)) {
            this.minTime = entryTime;
        }

        if (this.maxTime == null || entryTime.isAfter(this.maxTime)) {
            this.maxTime = entryTime;
        }

        // Добавляем страницу в список существующих, если код ответа 200
        if (entry.getResponseCode() == 200) {
            existingPages.add(entry.getPath());
        }

        // Добавляем страницу в список несуществующих, если код ответа 404
        if (entry.getResponseCode() == 404) {
            nonExistingPages.add(entry.getPath());
        }

        // Подсчет ошибочных запросов (4xx или 5xx)
        int responseCode = entry.getResponseCode();
        if (responseCode >= 400 && responseCode < 600) {
            errorRequestsCount++;
        }

        // Проверка, является ли запрос от реального пользователя (не бота)
        UserAgent agent = entry.getAgent();
        boolean isHuman = !agent.isBot();

        if (isHuman) {
            // Увеличиваем счетчик посещений реальными пользователями
            humanVisitsCount++;

            // Добавляем IP-адрес в множество уникальных IP-адресов реальных пользователей
            uniqueHumanIPs.add(entry.getIpAddr());
        }

        // Подсчитываем операционные системы
        String osType = entry.getAgent().getOsType();
        osCounts.put(osType, osCounts.getOrDefault(osType, 0) + 1);

        // Подсчитываем браузеры
        String browserType = entry.getAgent().getBrowserType();
        browserCounts.put(browserType, browserCounts.getOrDefault(browserType, 0) + 1);

        this.entryCount++;
    }

    // Метод подсчёта среднего количества посещений сайта за час (только реальные пользователи)
    public double getAverageVisitsPerHour() {
        if (minTime == null || maxTime == null || humanVisitsCount == 0) {
            return 0.0;
        }

        // Вычисляем разницу во времени в часах
        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);

        // Если временной интервал меньше часа, считаем его как 1 час для избежания деления на 0
        if (hoursBetween == 0) {
            hoursBetween = 1;
        }

        // Разделяем количество посещений реальными пользователями на период времени в часах
        return (double) humanVisitsCount / hoursBetween;
    }

    // Метод подсчёта среднего количества ошибочных запросов в час
    public double getAverageErrorRequestsPerHour() {
        if (minTime == null || maxTime == null || errorRequestsCount == 0) {
            return 0.0;
        }

        // Вычисляем разницу во времени в часах
        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);

        // Если временной интервал меньше часа, считаем его как 1 час для избежания деления на 0
        if (hoursBetween == 0) {
            hoursBetween = 1;
        }

        // Разделяем количество ошибочных запросов на период времени в часах
        return (double) errorRequestsCount / hoursBetween;
    }

    // Метод расчёта средней посещаемости одним пользователем
    public double getAverageVisitsPerUser() {
        if (humanVisitsCount == 0 || uniqueHumanIPs.isEmpty()) {
            return 0.0;
        }

        // Делим общее количество посещений реальными пользователями на число уникальных IP-адресов
        return (double) humanVisitsCount / uniqueHumanIPs.size();
    }

    // Метод для расчета среднего объема трафика за час
    public double getTrafficRate() {
        if (minTime == null || maxTime == null || totalTraffic == 0) {
            return 0.0;
        }

        // Вычисляем разницу во времени в часах
        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);

        // Если временной интервал меньше часа, считаем его как 1 час для избежания деления на 0
        if (hoursBetween == 0) {
            hoursBetween = 1;
        }

        // Возвращаем средний трафик в час
        return (double) totalTraffic / hoursBetween;
    }

    // Метод для возвращения списка всех существующих страниц сайта (код ответа 200)
    public Set<String> getExistingPages() {
        // Возвращаем копию множества, чтобы защитить исходные данные
        return new HashSet<>(existingPages);
    }

    // Метод для возвращения списка всех несуществующих страниц сайта (код ответа 404)
    public Set<String> getNonExistingPages() {
        // Возвращаем копию множества, чтобы защитить исходные данные
        return new HashSet<>(nonExistingPages);
    }

    // Метод для возвращения статистики операционных систем (доли от 0 до 1)
    public Map<String, Double> getOsStatistics() {
        Map<String, Double> osStatistics = new HashMap<>();
        if (entryCount == 0) {
            return osStatistics; // Возвращаем пустую карту, если нет записей
        }
        // Рассчитываем долю для каждой операционной системы
        for (Map.Entry<String, Integer> entry : osCounts.entrySet()) {
            double share = (double) entry.getValue() / entryCount;
            osStatistics.put(entry.getKey(), share);
        }
        return osStatistics;
    }

    // Метод для возвращения статистики браузеров (доли от 0 до 1)
    public Map<String, Double> getBrowserStatistics() {
        Map<String, Double> browserStatistics = new HashMap<>();

        if (entryCount == 0) {
            return browserStatistics; // Возвращаем пустую карту, если нет записей
        }

        // Рассчитываем долю для каждого браузера
        for (Map.Entry<String, Integer> entry : browserCounts.entrySet()) {
            double share = (double) entry.getValue() / entryCount;
            browserStatistics.put(entry.getKey(), share);
        }
        return browserStatistics;
    }

    public int getExistingPagesCount() { // возвращаем количество существующих страниц
        return existingPages.size();
    }

    public int getNonExistingPagesCount() { // возвращаем количество существующих страниц
        return nonExistingPages.size();
    }

    // Дополнительные геттеры для статистики
    public long getTotalTraffic() {
        return totalTraffic;
    }

    public LocalDateTime getMinTime() {
        return minTime;
    }

    public LocalDateTime getMaxTime() {
        return maxTime;
    }

    public int getEntryCount() {
        return entryCount;
    }
}
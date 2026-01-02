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
    private Map<String, Integer> osCounts; // Счетчики операционных систем

    // Конструктор без параметров
    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.entryCount = 0;
        this.existingPages = new HashSet<>();
        this.osCounts = new HashMap<>();
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

        // Подсчитываем операционные системы
        String osType = entry.getAgent().getOsType();
        osCounts.put(osType, osCounts.getOrDefault(osType, 0) + 1);

        this.entryCount++;
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

    public int getExistingPagesCount() { // возвращаем количество существующих страниц
        return existingPages.size();
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

class Statistics {
    private long totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private int entryCount;

    // Конструктор без параметров
    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.entryCount = 0;
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
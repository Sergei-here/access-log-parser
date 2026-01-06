import java.net.URI;
import java.net.URISyntaxException;
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
    private Map<Long, Integer> visitsPerSecond; // Посещения по секундам (только реальные пользователи)
    private Set<String> refererDomains; // Сайты ссылающиеся на текущий
    private Map<String, Integer> visitsPerHumanUser; // Посещения по каждому пользователю (IP)

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
        this.visitsPerSecond=new HashMap<>();
        this.refererDomains=new HashSet<>();
        this.visitsPerHumanUser=new HashMap<>();
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

        // Обработка referer для получения домена
        processRefererDomain(entry.getReferer());

        if (isHuman) {
            // Увеличиваем счетчик посещений реальными пользователями
            humanVisitsCount++;

            // Добавляем IP-адрес в множество уникальных IP-адресов реальных пользователей
            uniqueHumanIPs.add(entry.getIpAddr());

            // Подсчет посещений по секундам (только для реальных пользователей)
            updateVisitsPerSecond(entryTime);

            // Подсчет посещений по каждому пользователю (IP)
            updateVisitsPerUser(entry.getIpAddr());
        }

        // Подсчитываем операционные системы
        String osType = entry.getAgent().getOsType();
        osCounts.put(osType, osCounts.getOrDefault(osType, 0) + 1);

        // Подсчитываем браузеры
        String browserType = entry.getAgent().getBrowserType();
        browserCounts.put(browserType, browserCounts.getOrDefault(browserType, 0) + 1);

        this.entryCount++;
    }

    // Вспомогательный метод для обновления посещений по секундам
    private void updateVisitsPerSecond(LocalDateTime entryTime) {
        // Преобразуем время в секунды с эпохи (Unix timestamp)
        long seconds = entryTime.toEpochSecond(java.time.ZoneOffset.UTC);

        // Увеличиваем счетчик для этой секунды
        visitsPerSecond.put(seconds, visitsPerSecond.getOrDefault(seconds, 0) + 1);
    }

    // Вспомогательный метод для обновления посещений по пользователю
    private void updateVisitsPerUser(String ipAddress) {
        visitsPerHumanUser.put(ipAddress, visitsPerHumanUser.getOrDefault(ipAddress, 0) + 1);
    }

    // Вспомогательный метод для обработки referer и извлечения домена
    private void processRefererDomain(String referer) {
        if (referer == null || referer.isEmpty() || referer.equals("-")) {
            return;
        }

        try {
            // Пытаемся создать URI из referer
            URI uri = new URI(referer);
            String host = uri.getHost();

            if (host != null && !host.isEmpty()) {
                // Убираем www. если есть
                if (host.startsWith("www.")) {
                    host = host.substring(4);
                }
                refererDomains.add(host);
            }
        } catch (URISyntaxException e) {
            // Если не удалось распарсить как URI, попробуем извлечь домен вручную
            extractDomainManually(referer);
        }
    }

    // Вспомогательный метод для ручного извлечения домена
    private void extractDomainManually(String referer) {
        try {
            // Убираем протокол если есть
            String url = referer.toLowerCase();
            if (url.startsWith("http://")) {
                url = url.substring(7);
            } else if (url.startsWith("https://")) {
                url = url.substring(8);
            }

            // Убираем www. если есть
            if (url.startsWith("www.")) {
                url = url.substring(4);
            }

            // Берем часть до первого / или конца строки
            int slashIndex = url.indexOf('/');
            if (slashIndex > 0) {
                url = url.substring(0, slashIndex);
            }

            // Убираем порт если есть
            int colonIndex = url.indexOf(':');
            if (colonIndex > 0) {
                url = url.substring(0, colonIndex);
            }

            if (!url.isEmpty()) {
                refererDomains.add(url);
            }
        } catch (Exception e) {
            // Игнорируем некорректные referer
        }
    }

    // Метод расчёта пиковой посещаемости сайта (в секунду)
    public int getPeakVisitsPerSecond() {
        if (visitsPerSecond.isEmpty()) {
            return 0;
        }

        // Находим максимальное значение в мапе
        return visitsPerSecond.values().stream()
                .max(Integer::compareTo)
                .orElse(0);
    }

    // Метод, возвращающий список сайтов, со страниц которых есть ссылки на текущий сайт
    public Set<String> getRefererDomains() {
        // Возвращаем копию для защиты данных
        return new HashSet<>(refererDomains);
    }

    // Метод расчёта максимальной посещаемости одним пользователем
    public int getMaxVisitsBySingleUser() {
        if (visitsPerHumanUser.isEmpty()) {
            return 0;
        }

        // Находим максимальное значение в мапе
        return visitsPerHumanUser.values().stream()
                .max(Integer::compareTo)
                .orElse(0);
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
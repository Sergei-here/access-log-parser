package ru.stepup.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Класс для представления записи лога
public class LogEntry {
    private final String ipAddr;
    private final LocalDateTime time;
    private final HttpMethod method;
    private final String path;
    private final int responseCode;
    private final int responseSize;
    private final String referer;
    private final UserAgent agent;

    // Конструктор, принимающий строку лога
    public LogEntry(String logLine) {
        this.ipAddr = extractIpAddress(logLine);
        this.time = extractDateTime(logLine);
        this.method = extractHttpMethod(logLine);
        this.path = extractPath(logLine);
        this.responseCode = extractResponseCode(logLine);
        this.responseSize = (int) extractDataSize(logLine);
        this.referer = extractReferer(logLine);
        this.agent = new UserAgent(extractUserAgentString(logLine));
    }

    // Методы для извлечения компонентов из строки лога
    private String extractIpAddress(String logLine) {
        // IP-адрес находится в начале строки до первого пробела
        int spaceIndex = logLine.indexOf(' ');
        if (spaceIndex != -1) {
            return logLine.substring(0, spaceIndex);
        }
        throw new IllegalArgumentException("Неверный формат строки лога: не найден IP-адрес");
    }

    private LocalDateTime extractDateTime(String logLine) {
        // Дата находится между квадратными скобками
        int openBracket = logLine.indexOf('[');
        int closeBracket = logLine.indexOf(']', openBracket);

        if (openBracket != -1 && closeBracket != -1) {
            String dateTimeStr = logLine.substring(openBracket + 1, closeBracket);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            return LocalDateTime.parse(dateTimeStr, formatter);
        }
        throw new IllegalArgumentException("Неверный формат строки лога: не найдена дата");
    }

    private HttpMethod extractHttpMethod(String logLine) {
        // Метод находится после даты, между кавычками
        int afterDate = logLine.indexOf(']') + 2; // +2 чтобы пропустить "] "
        int firstQuote = logLine.indexOf('"', afterDate);
        int firstSpace = logLine.indexOf(' ', firstQuote + 1);

        if (firstQuote != -1 && firstSpace != -1) {
            String methodStr = logLine.substring(firstQuote + 1, firstSpace);
            try {
                return HttpMethod.valueOf(methodStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Неизвестный метод HTTP: " + methodStr);
            }
        }
        throw new IllegalArgumentException("Неверный формат строки лога: не найден метод HTTP");
    }

    private String extractPath(String logLine) {
        // Путь находится после метода, до " HTTP/"
        int afterDate = logLine.indexOf(']') + 2;
        int firstQuote = logLine.indexOf('"', afterDate);
        int firstSpace = logLine.indexOf(' ', firstQuote + 1);
        int httpIndex = logLine.indexOf(" HTTP/", firstSpace + 1);

        if (firstSpace != -1 && httpIndex != -1) {
            return logLine.substring(firstSpace + 1, httpIndex);
        }
        throw new IllegalArgumentException("Неверный формат строки лога: не найден путь");
    }

    private int extractResponseCode(String logLine) {
        // Код ответа находится после пути, перед размером данных
        int afterHttp = logLine.indexOf("HTTP/");
        if (afterHttp == -1) {
            throw new IllegalArgumentException("Неверный формат строки лога: не найден HTTP");
        }

        // Ищем следующий пробел после HTTP/
        int spaceAfterHttp = logLine.indexOf(' ', afterHttp);
        int nextSpace = logLine.indexOf(' ', spaceAfterHttp + 1);

        if (spaceAfterHttp != -1 && nextSpace != -1) {
            String codeStr = logLine.substring(spaceAfterHttp + 1, nextSpace);
            try {
                return Integer.parseInt(codeStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Неверный формат кода ответа: " + codeStr);
            }
        }
        throw new IllegalArgumentException("Неверный формат строки лога: не найден код ответа");
    }

    private long extractDataSize(String logLine) {
        // Размер данных находится после кода ответа
        int afterHttp = logLine.indexOf("HTTP/");
        int spaceAfterHttp = logLine.indexOf(' ', afterHttp);
        int firstSpace = logLine.indexOf(' ', spaceAfterHttp + 1);
        int secondSpace = logLine.indexOf(' ', firstSpace + 1);

        if (firstSpace != -1 && secondSpace != -1) {
            String sizeStr = logLine.substring(firstSpace + 1, secondSpace);
            try {
                return Long.parseLong(sizeStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Неверный формат размера данных: " + sizeStr);
            }
        }
        throw new IllegalArgumentException("Неверный формат строки лога: не найден размер данных");
    }

    private String extractReferer(String logLine) {
        // Referer находится в кавычках после размера данных
        int sizeEnd = logLine.indexOf(' ', logLine.indexOf("HTTP/") + 1);
        sizeEnd = logLine.indexOf(' ', sizeEnd + 1); // Переходим к позиции после размера данных

        int firstQuote = logLine.indexOf('"', sizeEnd + 1);
        int secondQuote = logLine.indexOf('"', firstQuote + 1);

        if (firstQuote != -1 && secondQuote != -1) {
            String refererStr = logLine.substring(firstQuote + 1, secondQuote);
            return refererStr.equals("-") ? "" : refererStr;
        }
        throw new IllegalArgumentException("Неверный формат строки лога: не найден referer");
    }

    private String extractUserAgentString(String logLine) {
        // User-Agent находится в последних кавычках
        int lastQuote = logLine.lastIndexOf('"');
        int secondLastQuote = logLine.lastIndexOf('"', lastQuote - 1);

        if (lastQuote != -1 && secondLastQuote != -1) {
            String userAgent = logLine.substring(secondLastQuote + 1, lastQuote);
            return userAgent.equals("-") ? "" : userAgent;
        }
        throw new IllegalArgumentException("Неверный формат строки лога: не найден User-Agent");
    }

    // Геттеры
    public String getIpAddr() {
        return ipAddr;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public long getResponseSize() {
        return responseSize;
    }

    public String getReferer() {
        return referer;
    }

    public UserAgent getAgent() {
        return agent;
    }
}
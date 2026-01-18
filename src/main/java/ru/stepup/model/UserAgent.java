package ru.stepup.model;

public class UserAgent {
    private final String osType;
    private final String browserType;
    private final String agent;

    public UserAgent(String userAgentString) {
        this.agent = userAgentString;
        this.osType = parseOsType(userAgentString);
        this.browserType = parseBrowserType(userAgentString);
    }

    private String parseOsType(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return "Unknown";
        }

        userAgentString = userAgentString.toLowerCase();

        if (userAgentString.contains("windows")) {
            return "Windows";
        } else if (userAgentString.contains("mac os") || userAgentString.contains("macos")) {
            return "macOS";
        } else if (userAgentString.contains("linux")) {
            return "Linux";
        } else if (userAgentString.contains("android")) {
            return "Android";
        } else if (userAgentString.contains("ios")) {
            return "iOS";
        } else {
            return "Other";
        }
    }

    private String parseBrowserType(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return "Unknown";
        }

        userAgentString = userAgentString.toLowerCase();

        if (userAgentString.contains("edg/") || userAgentString.contains("edge/")) {
            return "Edge";
        } else if (userAgentString.contains("firefox") || userAgentString.contains("fxios")) {
            return "Firefox";
        } else if (userAgentString.contains("chrome") && !userAgentString.contains("chromium")) {
            return "Chrome";
        } else if (userAgentString.contains("chromium")) {
            return "Chromium";
        } else if (userAgentString.contains("safari") && !userAgentString.contains("chrome")) {
            return "Safari";
        } else if (userAgentString.contains("opera") || userAgentString.contains("opr/")) {
            return "Opera";
        } else {
            return "Other";
        }
    }

    public String getOsType() {
        return osType;
    }

    public String getBrowserType() {
        return browserType;
    }

    // Метод для определения, является ли User-Agent ботом
    public boolean isBot() {
        if (agent == null || agent.isEmpty()) {
            return false;
        }
        // Ищем слово "bot" в любом регистре
        return agent.toLowerCase().contains("bot");
    }
}
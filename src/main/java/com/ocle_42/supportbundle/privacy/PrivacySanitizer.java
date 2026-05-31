package com.ocle_42.supportbundle.privacy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class PrivacySanitizer {
    private final List<Replacement> replacements;

    private PrivacySanitizer(List<Replacement> replacements) {
        this.replacements = List.copyOf(replacements);
    }

    public static PrivacySanitizer fromConfig(String usernameToMask, String serverAddressToMask, boolean maskServerAddresses, List<String> extraPatterns) {
        List<Replacement> replacements = new ArrayList<>();
        replacements.add(new Replacement(Pattern.compile("(?i)(bearer\\s+)[A-Za-z0-9._\\-]+"), "$1[redacted]"));
        replacements.add(new Replacement(Pattern.compile("(?i)((?:access[_-]?token|client[_-]?token|session[_-]?id|sessionid)\\s*[=:]\\s*)[^\\s\"']+"), "$1[redacted]"));
        replacements.add(new Replacement(Pattern.compile("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}"), "[redacted-email]"));
        replacements.add(new Replacement(Pattern.compile("(?i)[A-Z]:\\\\Users\\\\[^\\\\\\s]+"), "C:\\\\Users\\\\[user]"));
        replacements.add(new Replacement(Pattern.compile("/(?:home|Users)/[^/\\s]+"), "/home/[user]"));

        if (usernameToMask != null && !usernameToMask.isBlank()) {
            replacements.add(new Replacement(Pattern.compile(Pattern.quote(usernameToMask), Pattern.CASE_INSENSITIVE), "[username]"));
        }

        if (serverAddressToMask != null && !serverAddressToMask.isBlank()) {
            replacements.add(new Replacement(Pattern.compile(Pattern.quote(serverAddressToMask), Pattern.CASE_INSENSITIVE), "[server]"));
        }

        if (maskServerAddresses) {
            replacements.add(new Replacement(Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d{1,5})?\\b"), "[ip-address]"));
        }

        for (String pattern : extraPatterns) {
            if (!pattern.isBlank()) {
                replacements.add(new Replacement(Pattern.compile(pattern), "[redacted]"));
            }
        }

        return new PrivacySanitizer(replacements);
    }

    public String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        String sanitized = value;
        for (Replacement replacement : replacements) {
            sanitized = replacement.pattern().matcher(sanitized).replaceAll(replacement.replacement());
        }
        return sanitized;
    }

    private record Replacement(Pattern pattern, String replacement) {
    }
}

package com.ocle_42.supportbundle.config;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class SupportBundleConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> GUI_TITLE = BUILDER
            .comment("Title shown at the top of the report screen.")
            .define("gui.title", "SupportBundle");

    public static final ModConfigSpec.ConfigValue<String> GUI_INSTRUCTION = BUILDER
            .comment("Short instruction text shown on the report screen.")
            .define("gui.instruction", "Create a local report zip and attach it to your support request.");

    public static final ModConfigSpec.ConfigValue<String> MESSAGE_TEMPLATE = BUILDER
            .comment("Text copied by the Discord/GitHub message button.",
                    "Available variables: {minecraft_version}, {neoforge_version}, {modpack}, {report_file}.")
            .define("message.template",
                    "I created a SupportBundle report.\n\nMinecraft: {minecraft_version}\nNeoForge: {neoforge_version}\nModpack: {modpack}\nReport file: {report_file}\nProblem description:\n<write here what happened>");

    public static final ModConfigSpec.ConfigValue<String> REPORTS_FOLDER = BUILDER
            .comment("Folder under the game directory where report zip files are written.")
            .define("reports.folder", "supportbundle/reports");

    public static final ModConfigSpec.ConfigValue<String> ZIP_PREFIX = BUILDER
            .comment("Filename prefix used for report zip files.")
            .define("reports.zipPrefix", "supportbundle");

    public static final ModConfigSpec.BooleanValue SHOW_PAUSE_MENU_BUTTON = BUILDER
            .comment("Show a SupportBundle button in the pause screen.")
            .define("gui.showPauseMenuButton", true);

    public static final ModConfigSpec.BooleanValue ENABLE_KEYBIND = BUILDER
            .comment("Allow the SupportBundle key mapping to open the report screen.")
            .define("controls.enableKeybind", true);

    public static final ModConfigSpec.BooleanValue COLLECT_LATEST_LOG = BUILDER
            .comment("Include a sanitized tail of logs/latest.log when it exists.")
            .define("collection.latestLog", true);

    public static final ModConfigSpec.IntValue LATEST_LOG_MAX_KB = BUILDER
            .comment("Maximum latest.log tail size in KiB.")
            .defineInRange("collection.latestLogMaxKiB", 256, 16, 4096);

    public static final ModConfigSpec.BooleanValue COLLECT_CRASH_REPORTS = BUILDER
            .comment("Include the newest sanitized crash report when one exists.")
            .define("collection.crashReports", true);

    public static final ModConfigSpec.BooleanValue COLLECT_RESOURCE_PACKS = BUILDER
            .comment("Include active resource pack information.")
            .define("collection.resourcePacks", true);

    public static final ModConfigSpec.BooleanValue COLLECT_KEYBIND_CONFLICTS = BUILDER
            .comment("Include key mappings that share the same assigned key.")
            .define("collection.keybindConflicts", true);

    public static final ModConfigSpec.BooleanValue INCLUDE_SERVER_ADDRESS = BUILDER
            .comment("Include the current multiplayer server address. Disabled by default for privacy.")
            .define("privacy.includeServerAddress", false);

    public static final ModConfigSpec.BooleanValue INCLUDE_USERNAME = BUILDER
            .comment("Include the current account name. Disabled by default for privacy.")
            .define("privacy.includeUsername", false);

    public static final ModConfigSpec.BooleanValue INCLUDE_OS_JVM_INFO = BUILDER
            .comment("Include operating system and Java runtime information.")
            .define("privacy.includeOsJvmInfo", true);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_FILES = BUILDER
            .comment("Extra text files to include from the game directory.",
                    "Each entry must be a relative path without '..'. Files are sanitized before they are added.")
            .defineListAllowEmpty("collection.additionalFiles", List.of(), () -> "", value -> value instanceof String path && isSafeRelativePath(path));

    public static final ModConfigSpec.ConfigValue<List<? extends String>> EXTRA_SANITIZER_PATTERNS = BUILDER
            .comment("Additional regular expressions replaced with [redacted] in collected text.")
            .defineListAllowEmpty("privacy.extraSanitizerPatterns", List.of(), () -> "", SupportBundleConfig::isValidRegex);

    public static final ModConfigSpec.ConfigValue<String> SUPPORT_URL = BUILDER
            .comment("Optional support page URL shown in the report screen.")
            .define("links.supportUrl", "");

    public static final ModConfigSpec.ConfigValue<String> ISSUE_TRACKER_URL = BUILDER
            .comment("Optional issue tracker URL shown in the report screen.")
            .define("links.issueTrackerUrl", "");

    public static final ModConfigSpec.ConfigValue<String> DISCORD_URL = BUILDER
            .comment("Optional Discord URL shown in the report screen.")
            .define("links.discordUrl", "");

    public static final ModConfigSpec.BooleanValue SHOW_LINKS_IN_GUI = BUILDER
            .comment("Show configured support links in the report screen.")
            .define("links.showInGui", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private SupportBundleConfig() {
    }

    private static boolean isSafeRelativePath(String path) {
        String normalized = path.replace('\\', '/');
        return !normalized.isBlank()
                && !normalized.startsWith("/")
                && !normalized.matches("^[A-Za-z]:/.*")
                && normalized.chars().noneMatch(ch -> ch == 0)
                && !List.of(normalized.split("/")).contains("..");
    }

    private static boolean isValidRegex(Object value) {
        if (!(value instanceof String pattern) || pattern.isBlank()) {
            return value instanceof String;
        }

        try {
            Pattern.compile(pattern);
            return true;
        } catch (PatternSyntaxException ignored) {
            return false;
        }
    }
}

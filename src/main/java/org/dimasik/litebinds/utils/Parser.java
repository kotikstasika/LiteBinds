package org.dimasik.litebinds.utils;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Parser {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final boolean SUPPORTS_RGB = true;

    public static String color(String message) {
        if (message == null) return "";

        if (SUPPORTS_RGB) {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuilder buffer = new StringBuilder();

            while (matcher.find()) {
                String hex = matcher.group(1);
                try {
                    String replacement = ChatColor.of("#" + hex).toString();
                    matcher.appendReplacement(buffer, replacement);
                } catch (Exception e) {
                    matcher.appendReplacement(buffer, "");
                }
            }

            matcher.appendTail(buffer);
            message = buffer.toString();
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
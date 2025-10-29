package com.youtube.captionssubtitlesservice.domain.valueobjects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Language code value object supporting ISO 639-1 two-letter codes
 */
@Getter
@RequiredArgsConstructor
public enum LanguageCode {
    ENGLISH("en", "English"),
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    ITALIAN("it", "Italian"),
    PORTUGUESE("pt", "Portuguese"),
    RUSSIAN("ru", "Russian"),
    JAPANESE("ja", "Japanese"),
    KOREAN("ko", "Korean"),
    CHINESE_SIMPLIFIED("zh", "Chinese (Simplified)"),
    CHINESE_TRADITIONAL("zh-TW", "Chinese (Traditional)"),
    ARABIC("ar", "Arabic"),
    HINDI("hi", "Hindi"),
    DUTCH("nl", "Dutch"),
    POLISH("pl", "Polish"),
    TURKISH("tr", "Turkish"),
    THAI("th", "Thai"),
    VIETNAMESE("vi", "Vietnamese"),
    INDONESIAN("id", "Indonesian"),
    SWEDISH("sv", "Swedish"),
    NORWEGIAN("no", "Norwegian"),
    DANISH("da", "Danish"),
    FINNISH("fi", "Finnish"),
    CZECH("cs", "Czech"),
    HUNGARIAN("hu", "Hungarian"),
    ROMANIAN("ro", "Romanian"),
    GREEK("el", "Greek"),
    BULGARIAN("bg", "Bulgarian"),
    UKRAINIAN("uk", "Ukrainian"),
    HEBREW("he", "Hebrew"),
    MALAY("ms", "Malay"),
    FILIPINO("tl", "Filipino");
    
    private final String code;
    private final String displayName;
    
    public static LanguageCode fromCode(String code) {
        for (LanguageCode lang : values()) {
            if (lang.code.equals(code)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Unsupported language code: " + code);
    }
}

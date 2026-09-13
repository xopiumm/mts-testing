package ru.mts.itmo.aqa.junit5;

import io.qameta.allure.Attachment;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.BLACKLISTED;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.CONTAINS_LOGIN;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.HAS_WHITESPACE;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_DIGIT;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_LOWER;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_SPECIAL;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.NO_UPPER;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.REPEATED_RUN;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.TOO_LONG;
import static ru.mts.itmo.aqa.junit5.PasswordPolicy.Violation.TOO_SHORT;

@Epic("Password policy")
@Feature("Проверка пароля по спецификации")
@DisplayName("PasswordPolicy: проверка правил пароля")
class PasswordPolicyTest {

    private static final String VALID_PASSWORD = "Aa1!bcde";

    private final PasswordPolicy policy = new PasswordPolicy();

    @Nested
    @Story("Валидность результата")
    @DisplayName("Валидность результата")
    class ResultValidity {

        @Test
        @DisplayName("Пароль без нарушений валиден и имеет пустой список нарушений")
        void validPasswordHasNoViolations() {
            PasswordPolicy.Result result = checkPassword(VALID_PASSWORD);

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(result.isValid()).isTrue();
            softly.assertThat(result.violations()).isEmpty();
            softly.assertAll();
        }

        @Test
        @DisplayName("Пароль с нарушением невалиден")
        void passwordWithViolationIsInvalid() {
            PasswordPolicy.Result result = checkPassword("Aa1!bcd");

            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @Story("Обработка null")
    @DisplayName("Обработка null")
    class NullPassword {

        @Test
        @DisplayName("Для null-пароля выбрасывается IllegalArgumentException")
        void nullPasswordThrowsIllegalArgumentException() {
            assertThatThrownBy(() -> policy.check(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @Story("Ограничения длины")
    @DisplayName("Ограничения длины")
    class LengthRules {

        static Stream<Arguments> lengthBoundaryCases() {
            return Stream.of(
                    Arguments.of(7, TOO_SHORT, true),
                    Arguments.of(8, TOO_SHORT, false),
                    Arguments.of(64, TOO_LONG, false),
                    Arguments.of(65, TOO_LONG, true)
            );
        }

        @ParameterizedTest(name = "длина {0}: нарушение {1} присутствует - {2}")
        @MethodSource("lengthBoundaryCases")
        @DisplayName("Границы длины 7/8 и 64/65")
        // Регрессионная проверка BUG-01: длина 64 должна оставаться допустимой
        void checksLengthBoundaries(
                int length,
                PasswordPolicy.Violation violation,
                boolean expectedToContain
        ) {
            PasswordPolicy.Result result = checkPassword(validPasswordOfLength(length));

            if (expectedToContain) {
                assertThat(result.violations()).contains(violation);
            } else {
                assertThat(result.violations()).doesNotContain(violation);
            }
        }
    }

    @Nested
    @Story("Наличие цифры")
    @DisplayName("Наличие цифры")
    class DigitRule {

        @Test
        @DisplayName("Пароль без цифры получает NO_DIGIT")
        void passwordWithoutDigitIsRejected() {
            assertThat(checkPassword("Aa!bcdef").violations()).contains(NO_DIGIT);
        }

        @Test
        @DisplayName("Пароль с цифрой не получает NO_DIGIT")
        void passwordWithDigitIsAccepted() {
            assertThat(checkPassword(VALID_PASSWORD).violations()).doesNotContain(NO_DIGIT);
        }
    }

    @Nested
    @Story("Наличие заглавной латинской буквы")
    @DisplayName("Наличие заглавной латинской буквы")
    class UppercaseRule {

        @ParameterizedTest(name = "пароль {0} не содержит заглавной латинской буквы")
        @ValueSource(strings = {"aa1!bcde", "Яa1!bcde"})
        @DisplayName("Пароль без заглавной латинской буквы получает NO_UPPER")
        void passwordWithoutLatinUppercaseIsRejected(String password) {
            assertThat(checkPassword(password).violations()).contains(NO_UPPER);
        }

        @Test
        @DisplayName("Пароль с заглавной латинской буквой не получает NO_UPPER")
        void passwordWithLatinUppercaseIsAccepted() {
            assertThat(checkPassword(VALID_PASSWORD).violations()).doesNotContain(NO_UPPER);
        }
    }

    @Nested
    @Story("Наличие строчной латинской буквы")
    @DisplayName("Наличие строчной латинской буквы")
    class LowercaseRule {

        @ParameterizedTest(name = "пароль {0} не содержит строчной латинской буквы")
        @ValueSource(strings = {"AA1!BCDE", "Aя1!БВГД"})
        @DisplayName("Пароль без строчной латинской буквы получает NO_LOWER")
        void passwordWithoutLatinLowercaseIsRejected(String password) {
            assertThat(checkPassword(password).violations()).contains(NO_LOWER);
        }

        @Test
        @DisplayName("Пароль со строчной латинской буквой не получает NO_LOWER")
        void passwordWithLatinLowercaseIsAccepted() {
            assertThat(checkPassword(VALID_PASSWORD).violations()).doesNotContain(NO_LOWER);
        }
    }

    @Nested
    @Story("Наличие разрешённого специального символа")
    @DisplayName("Наличие разрешённого специального символа")
    class SpecialCharacterRule {

        @ParameterizedTest(name = "специальный символ {0} разрешён")
        @ValueSource(strings = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_", "=", "+"})
        @DisplayName("Пароль с разрешённым специальным символом не получает NO_SPECIAL")
        void allowedSpecialCharacterIsAccepted(String specialCharacter) {
            String password = "Aa1" + specialCharacter + "bcde";

            assertThat(checkPassword(password).violations()).doesNotContain(NO_SPECIAL);
        }

        @ParameterizedTest(name = "пароль {0} не содержит разрешённого специального символа")
        @ValueSource(strings = {"Aa1bcdef", "Aa1.bcde"})
        @DisplayName("Пароль без разрешённого специального символа получает NO_SPECIAL")
        void passwordWithoutAllowedSpecialCharacterIsRejected(String password) {
            assertThat(checkPassword(password).violations()).contains(NO_SPECIAL);
        }
    }

    @Nested
    @Story("Запрет пробельных символов")
    @DisplayName("Запрет пробельных символов")
    class WhitespaceRule {

        @ParameterizedTest(name = "пробельный символ с кодом {0}")
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Пароль с пробелом, табуляцией и переводом строки получает HAS_WHITESPACE")
        void whitespaceCharacterIsRejected(String whitespace) {
            String password = "Aa1!" + whitespace + "bcd";

            assertThat(checkPassword(password).violations()).contains(HAS_WHITESPACE);
        }

        @Test
        @DisplayName("Пароль без пробельных символов не получает HAS_WHITESPACE")
        void passwordWithoutWhitespaceIsAccepted() {
            assertThat(checkPassword(VALID_PASSWORD).violations()).doesNotContain(HAS_WHITESPACE);
        }
    }

    @Nested
    @Story("Серия одинаковых символов")
    @DisplayName("Серия одинаковых символов")
    class RepeatedRunRule {

        static Stream<Arguments> repeatedRunBoundaryCases() {
            return Stream.of(
                    Arguments.of("Aa1!bbcd", false),
                    Arguments.of("Aa1!bbbc", true)
            );
        }

        @ParameterizedTest(name = "пароль {0}: REPEATED_RUN присутствует - {1}")
        @MethodSource("repeatedRunBoundaryCases")
        @DisplayName("Граница серии одинаковых символов 2/3")
        // Регрессионная проверка BUG-02: серия ровно из трёх символов должна блокироваться
        void checksRepeatedRunBoundary(String password, boolean expectedToContain) {
            PasswordPolicy.Result result = checkPassword(password);

            if (expectedToContain) {
                assertThat(result.violations()).contains(REPEATED_RUN);
            } else {
                assertThat(result.violations()).doesNotContain(REPEATED_RUN);
            }
        }

        @Test
        @DisplayName("Серия более трёх одинаковых символов приводит к REPEATED_RUN")
        void longerRepeatedRunIsRejected() {
            assertThat(checkPassword("Aa1!bbbb").violations()).contains(REPEATED_RUN);
        }

        @Test
        @DisplayName("Одинаковые символы не подряд не приводят к REPEATED_RUN")
        void separatedRepeatedCharactersAreAccepted() {
            assertThat(checkPassword("Aa1!bcbc").violations()).doesNotContain(REPEATED_RUN);
        }
    }

    @Nested
    @Story("Пароль содержит логин")
    @DisplayName("Пароль содержит логин")
    class ContainsLoginRule {

        @ParameterizedTest(name = "пароль {0} содержит логин {1}")
        @MethodSource("passwordsContainingLogin")
        @DisplayName("Логин обнаруживается без учёта регистра")
        void containedLoginIsRejected(String password, String login) {
            assertThat(checkPassword(password, login).violations()).contains(CONTAINS_LOGIN);
        }

        static Stream<Arguments> passwordsContainingLogin() {
            return Stream.of(
                    Arguments.of("Aa1!ivan", "ivan"),
                    Arguments.of("Aa1!IvAn", "iVaN")
            );
        }

        @Test
        @DisplayName("Логин, которого нет в пароле, не приводит к CONTAINS_LOGIN")
        void absentLoginIsAccepted() {
            assertThat(checkPassword(VALID_PASSWORD, "ivan").violations())
                    .doesNotContain(CONTAINS_LOGIN);
        }

        @Test
        @DisplayName("Пустой логин отключает правило CONTAINS_LOGIN")
        void emptyLoginDisablesRule() {
            assertThat(checkPassword(VALID_PASSWORD, "").violations())
                    .doesNotContain(CONTAINS_LOGIN);
        }

        @Test
        @DisplayName("null-логин отключает правило CONTAINS_LOGIN")
        void nullLoginDisablesRule() {
            assertThat(checkPassword(VALID_PASSWORD, null).violations())
                    .doesNotContain(CONTAINS_LOGIN);
        }

        @Test
        @DisplayName("Вызов без логина не применяет правило CONTAINS_LOGIN")
        void overloadWithoutLoginDisablesRule() {
            assertThat(checkPassword(VALID_PASSWORD).violations())
                    .doesNotContain(CONTAINS_LOGIN);
        }
    }

    @Nested
    @Story("Чёрный список паролей")
    @DisplayName("Чёрный список паролей")
    class BlacklistRule {

        @ParameterizedTest(name = "пароль {0} входит в чёрный список")
        @ValueSource(strings = {"password", "qwerty", "123456", "admin", "welcome", "PaSsWoRd"})
        @DisplayName("Простые пароли блокируются без учёта регистра")
        // Регрессионная проверка BUG-03: чёрный список должен работать без учёта регистра
        void blacklistedPasswordIsRejected(String password) {
            assertThat(checkPassword(password).violations()).contains(BLACKLISTED);
        }

        @ParameterizedTest(name = "пароль {0} не совпадает с элементом чёрного списка")
        @ValueSource(strings = {"Aa1!bcde", "Aa1!passwordX"})
        @DisplayName("Пароль вне списка и частичное совпадение не приводят к BLACKLISTED")
        void passwordNotEqualToBlacklistEntryIsAccepted(String password) {
            assertThat(checkPassword(password).violations()).doesNotContain(BLACKLISTED);
        }
    }

    @Nested
    @Story("Накопление нарушений")
    @DisplayName("Накопление нарушений")
    class ViolationAccumulation {

        @Test
        @DisplayName("Все независимые нарушения возвращаются одновременно")
        void returnsAllViolationsAtOnce() {
            PasswordPolicy.Result result = checkPassword("aaa");

            assertThat(result.violations()).containsExactlyInAnyOrder(
                    TOO_SHORT,
                    NO_DIGIT,
                    NO_UPPER,
                    NO_SPECIAL,
                    REPEATED_RUN
            );
        }
    }

    @Step("Проверить пароль: {password}")
    private PasswordPolicy.Result checkPassword(String password) {
        PasswordPolicy.Result result = policy.check(password);
        attachResult(password, null, result);
        return result;
    }

    @Step("Проверить пароль: {password}, логин: {login}")
    private PasswordPolicy.Result checkPassword(String password, String login) {
        PasswordPolicy.Result result = policy.check(password, login);
        attachResult(password, login, result);
        return result;
    }

    @Attachment(value = "Результат проверки PasswordPolicy", type = "text/plain")
    private String attachResult(String password, String login, PasswordPolicy.Result result) {
        return "password=" + printable(password)
                + System.lineSeparator()
                + "login=" + printable(login)
                + System.lineSeparator()
                + "isValid=" + result.isValid()
                + System.lineSeparator()
                + "violations=" + result.violations();
    }

    private String printable(String value) {
        if (value == null) {
            return "<null>";
        }
        return value.replace("\t", "\\t").replace("\n", "\\n");
    }

    private static String validPasswordOfLength(int length) {
        StringBuilder password = new StringBuilder("Aa1!");
        for (int i = password.length(); i < length; i++) {
            password.append(i % 2 == 0 ? 'b' : 'c');
        }
        return password.toString();
    }
}

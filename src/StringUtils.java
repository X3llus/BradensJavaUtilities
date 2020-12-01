import java.util.Arrays;
import java.util.List;

public class StringUtils {
    /**
     * formats a string to be camel case
     *
     * @param unformatted unformatted string
     * @return camel cased string
     */
    static public String camelFormat(String unformatted) {
        StringBuilder formatBuilder = new StringBuilder();
        List<String> words = Arrays.asList(unformatted.split(" "));
        words.replaceAll(String::toLowerCase);
        words.replaceAll(word -> word.substring(0,1).toUpperCase() + word.substring(1));
        words.forEach(formatBuilder::append);
        return formatBuilder.substring(0,1).toLowerCase() + formatBuilder.substring(1);
    }
}

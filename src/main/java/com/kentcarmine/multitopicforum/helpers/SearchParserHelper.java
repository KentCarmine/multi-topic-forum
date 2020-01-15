package com.kentcarmine.multitopicforum.helpers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class that handles parsing user-provided search text
 */
public class SearchParserHelper {

    /**
     * Helper method that parses text entered into a search field. The string is split on spaces and double quotes, with
     * substrings inside double quotes still containing spaces. Returns a list of tokens.
     *
     * @param searchText the text to be parsed.
     * @return the list of tokens
     * @throws UnsupportedEncodingException
     */
    public static List<String> parseSearchText(String searchText) throws UnsupportedEncodingException {
        List<String> searchTerms = new ArrayList<>();

        searchText = URLEncoderDecoderHelper.decode(searchText).trim();

        String regex = "\"([^\"]*)\"|(\\S+)";

        Matcher matcher = Pattern.compile(regex).matcher(searchText);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
//                System.out.println(matcher.group(1));
                searchTerms.add(matcher.group(1));
            } else {
//                System.out.println(matcher.group(2));
                searchTerms.add(matcher.group(2));
            }
        }

        return searchTerms.stream().filter(st -> st.length() > 0).distinct().collect(Collectors.toList());
    }
}

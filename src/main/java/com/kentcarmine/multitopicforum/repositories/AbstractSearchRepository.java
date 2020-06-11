package com.kentcarmine.multitopicforum.repositories;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class containing helper methods for searching for text in a SQL database.
 */
public class AbstractSearchRepository {

    /**
     * Helper method that splits a single string of space-delimited search terms into a set of strings and escapes SQL
     * LIKE query special characters
     *
     * @param searchTerms a string of space-delimited search terms
     * @return a set of those search terms with special characters escaped
     */
    protected Set<String> splitAndEscapeSearchTerms(String searchTerms) {
        return escapeWildcardsInSearchTerms(splitSearchTerms(searchTerms));
    }

    /**
     * Helper method that splits a single string of space-delimited search terms into a set of strings
     *
     * @param searchTerms a string of space-delimited search terms
     * @return a set of those search terms
     */
    protected Set<String> splitSearchTerms(String searchTerms) {
        String[] stArr = searchTerms.split(" ");
        return Set.of(stArr);
    }

    /**
     * Helper method that escapes special characters in SQL LIKE queries.
     *
     * @param searchTerms the set of search terms to escape
     * @return the set of search terms with special characters escaped
     */
    protected Set<String> escapeWildcardsInSearchTerms(Set<String> searchTerms) {
        Set<String> results = new HashSet<>();

        for (String st : searchTerms) {
            String escapedStr = st.replace("_", "\\_")
                    .replace("^", "\\^")
                    .replace("[", "\\[")
                    .replace("]", "\\]")
                    .replace("%", "\\%");
            results.add(escapedStr);
        }

        return results;
    }
}

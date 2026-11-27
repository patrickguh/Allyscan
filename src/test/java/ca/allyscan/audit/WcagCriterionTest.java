package ca.allyscan.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class WcagCriterionTest {

    private static List<String> numbers(String tagsCsv) {
        return WcagCriterion.fromTagsCsv(tagsCsv).stream().map(WcagCriterion::number).toList();
    }

    @Test
    void parsesCriterionTagsAndIgnoresLevelAndCategoryTags() {
        assertEquals(List.of("1.4.3"), numbers("cat.color,wcag2aa,wcag143,TTv5,EN-301-549"));
    }

    @Test
    void handlesTwoDigitCriterionNumbersAndSortsNumerically() {
        assertEquals(List.of("1.4.4", "1.4.10"), numbers("wcag1410,wcag21aa,wcag144"));
    }

    @Test
    void lookUpGivesNameAndLevel() {
        WcagCriterion c = WcagCriterion.fromTagsCsv("cat.name-role-value,wcag2a,wcag412").get(0);
        assertEquals("Name, Role, Value", c.name());
        assertEquals("A", c.level());
    }

    @Test
    void deduplicatesRepeatedTags() {
        assertEquals(List.of("1.1.1"), numbers("wcag111,wcag111"));
    }

    @Test
    void bestPracticeRulesHaveNoCriteria() {
        assertTrue(WcagCriterion.fromTagsCsv("cat.keyboard,best-practice").isEmpty());
        assertTrue(WcagCriterion.fromTagsCsv(null).isEmpty());
        assertTrue(WcagCriterion.fromTagsCsv("").isEmpty());
    }

    @Test
    void unknownCriterionStillComesBackWithEmptyName() {
        WcagCriterion c = WcagCriterion.fromTagsCsv("wcag259").get(0);
        assertEquals("2.5.9", c.number());
        assertEquals("", c.name());
    }
}

package ca.allyscan.audit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A WCAG success criterion, e.g. 1.4.3 "Contrast (Minimum)", Level AA.
 *
 * axe-core tags each rule with the criteria it tests, as compact tags like
 * {@code wcag143} (1.4.3) or {@code wcag1410} (1.4.10). Rules that only carry
 * {@code best-practice} map to no criterion at all.
 */
public record WcagCriterion(String number, String name, String level) {

    /** Matches axe-core's per-criterion tags: wcag + principle + guideline + criterion. */
    private static final Pattern TAG = Pattern.compile("wcag(\\d)(\\d)(\\d{1,2})");

    /** WCAG 2.0 and 2.1 Level A and AA criteria, in spec order. */
    private static final Map<String, WcagCriterion> KNOWN = new LinkedHashMap<>();

    static {
        add("1.1.1", "Non-text Content", "A");
        add("1.2.1", "Audio-only and Video-only (Prerecorded)", "A");
        add("1.2.2", "Captions (Prerecorded)", "A");
        add("1.2.3", "Audio Description or Media Alternative (Prerecorded)", "A");
        add("1.2.4", "Captions (Live)", "AA");
        add("1.2.5", "Audio Description (Prerecorded)", "AA");
        add("1.3.1", "Info and Relationships", "A");
        add("1.3.2", "Meaningful Sequence", "A");
        add("1.3.3", "Sensory Characteristics", "A");
        add("1.3.4", "Orientation", "AA");
        add("1.3.5", "Identify Input Purpose", "AA");
        add("1.4.1", "Use of Color", "A");
        add("1.4.2", "Audio Control", "A");
        add("1.4.3", "Contrast (Minimum)", "AA");
        add("1.4.4", "Resize Text", "AA");
        add("1.4.5", "Images of Text", "AA");
        add("1.4.10", "Reflow", "AA");
        add("1.4.11", "Non-text Contrast", "AA");
        add("1.4.12", "Text Spacing", "AA");
        add("1.4.13", "Content on Hover or Focus", "AA");
        add("2.1.1", "Keyboard", "A");
        add("2.1.2", "No Keyboard Trap", "A");
        add("2.1.4", "Character Key Shortcuts", "A");
        add("2.2.1", "Timing Adjustable", "A");
        add("2.2.2", "Pause, Stop, Hide", "A");
        add("2.3.1", "Three Flashes or Below Threshold", "A");
        add("2.4.1", "Bypass Blocks", "A");
        add("2.4.2", "Page Titled", "A");
        add("2.4.3", "Focus Order", "A");
        add("2.4.4", "Link Purpose (In Context)", "A");
        add("2.4.5", "Multiple Ways", "AA");
        add("2.4.6", "Headings and Labels", "AA");
        add("2.4.7", "Focus Visible", "AA");
        add("2.5.1", "Pointer Gestures", "A");
        add("2.5.2", "Pointer Cancellation", "A");
        add("2.5.3", "Label in Name", "A");
        add("2.5.4", "Motion Actuation", "A");
        add("3.1.1", "Language of Page", "A");
        add("3.1.2", "Language of Parts", "AA");
        add("3.2.1", "On Focus", "A");
        add("3.2.2", "On Input", "A");
        add("3.2.3", "Consistent Navigation", "AA");
        add("3.2.4", "Consistent Identification", "AA");
        add("3.3.1", "Error Identification", "A");
        add("3.3.2", "Labels or Instructions", "A");
        add("3.3.3", "Error Suggestion", "AA");
        add("3.3.4", "Error Prevention (Legal, Financial, Data)", "AA");
        add("4.1.1", "Parsing", "A");
        add("4.1.2", "Name, Role, Value", "A");
        add("4.1.3", "Status Messages", "AA");
    }

    /** Orders criteria numerically, so 1.4.10 sorts after 1.4.4 rather than before it. */
    public static final Comparator<WcagCriterion> SPEC_ORDER = (a, b) -> {
        String[] x = a.number.split("\\.");
        String[] y = b.number.split("\\.");
        for (int i = 0; i < Math.min(x.length, y.length); i++) {
            int c = Integer.compare(Integer.parseInt(x[i]), Integer.parseInt(y[i]));
            if (c != 0) {
                return c;
            }
        }
        return Integer.compare(x.length, y.length);
    };

    private static void add(String number, String name, String level) {
        KNOWN.put(number, new WcagCriterion(number, name, level));
    }

    /**
     * Parses an axe-core tag list (comma-separated, as stored on {@link Violation})
     * into the criteria it references, in spec order and without duplicates.
     * A criterion number the table above doesn't know still comes back, with an
     * empty name, so nothing axe reports is silently dropped.
     */
    public static List<WcagCriterion> fromTagsCsv(String tagsCsv) {
        List<WcagCriterion> result = new ArrayList<>();
        if (tagsCsv == null || tagsCsv.isBlank()) {
            return result;
        }
        for (String raw : tagsCsv.split(",")) {
            Matcher m = TAG.matcher(raw.trim());
            if (!m.matches()) {
                continue;
            }
            String number = m.group(1) + "." + m.group(2) + "." + Integer.parseInt(m.group(3));
            WcagCriterion criterion = KNOWN.getOrDefault(number, new WcagCriterion(number, "", ""));
            if (!result.contains(criterion)) {
                result.add(criterion);
            }
        }
        result.sort(SPEC_ORDER);
        return result;
    }
}

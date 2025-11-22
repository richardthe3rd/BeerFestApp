package ralcock.cbf.view;

import android.content.Context;
import ralcock.cbf.R;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class for displaying allergen information using UK FSA standard 14 allergens.
 * Provides abbreviations and display names for allergens commonly found in beer/beverages.
 *
 * @see <a href="https://www.food.gov.uk/business-guidance/allergen-guidance-for-food-businesses">FSA Allergen Guidance</a>
 */
public final class AllergenHelper {

    // UK FSA 14 regulated allergens with abbreviations
    private static final Map<String, String> ALLERGEN_ABBREVIATIONS = new LinkedHashMap<>();

    static {
        // Common beer/beverage allergens first
        ALLERGEN_ABBREVIATIONS.put("gluten", "G");
        ALLERGEN_ABBREVIATIONS.put("sulphites", "Su");
        ALLERGEN_ABBREVIATIONS.put("sulphur dioxide", "Su");
        ALLERGEN_ABBREVIATIONS.put("barley", "G");  // Contains gluten
        ALLERGEN_ABBREVIATIONS.put("wheat", "G");   // Contains gluten
        ALLERGEN_ABBREVIATIONS.put("oats", "G");    // Contains gluten
        ALLERGEN_ABBREVIATIONS.put("rye", "G");     // Contains gluten

        // Other UK regulated allergens
        ALLERGEN_ABBREVIATIONS.put("celery", "Ce");
        ALLERGEN_ABBREVIATIONS.put("crustaceans", "Cr");
        ALLERGEN_ABBREVIATIONS.put("eggs", "E");
        ALLERGEN_ABBREVIATIONS.put("fish", "F");
        ALLERGEN_ABBREVIATIONS.put("lupin", "L");
        ALLERGEN_ABBREVIATIONS.put("milk", "M");
        ALLERGEN_ABBREVIATIONS.put("molluscs", "Mo");
        ALLERGEN_ABBREVIATIONS.put("mustard", "Mu");
        ALLERGEN_ABBREVIATIONS.put("peanuts", "P");
        ALLERGEN_ABBREVIATIONS.put("sesame", "Se");
        ALLERGEN_ABBREVIATIONS.put("soybeans", "So");
        ALLERGEN_ABBREVIATIONS.put("soya", "So");
        ALLERGEN_ABBREVIATIONS.put("tree nuts", "N");
        ALLERGEN_ABBREVIATIONS.put("nuts", "N");
    }

    // Display names for allergen abbreviations (for legend/tooltip)
    private static final Map<String, String> ABBREVIATION_NAMES = new LinkedHashMap<>();

    static {
        ABBREVIATION_NAMES.put("G", "Gluten");
        ABBREVIATION_NAMES.put("Su", "Sulphites");
        ABBREVIATION_NAMES.put("Ce", "Celery");
        ABBREVIATION_NAMES.put("Cr", "Crustaceans");
        ABBREVIATION_NAMES.put("E", "Eggs");
        ABBREVIATION_NAMES.put("F", "Fish");
        ABBREVIATION_NAMES.put("L", "Lupin");
        ABBREVIATION_NAMES.put("M", "Milk");
        ABBREVIATION_NAMES.put("Mo", "Molluscs");
        ABBREVIATION_NAMES.put("Mu", "Mustard");
        ABBREVIATION_NAMES.put("P", "Peanuts");
        ABBREVIATION_NAMES.put("Se", "Sesame");
        ABBREVIATION_NAMES.put("So", "Soybeans");
        ABBREVIATION_NAMES.put("N", "Tree Nuts");
    }

    private AllergenHelper() {
        // Utility class
    }

    /**
     * Converts a comma-separated allergen string to abbreviations.
     * E.g., "gluten, sulphites" becomes "G, Su"
     */
    public static String toAbbreviations(final String allergens) {
        if (allergens == null || allergens.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] parts = allergens.split(",");

        for (String part : parts) {
            String allergen = part.trim().toLowerCase();
            String abbrev = ALLERGEN_ABBREVIATIONS.get(allergen);

            if (abbrev != null) {
                // Avoid duplicates (e.g., "gluten" and "barley" both map to "G")
                if (result.indexOf(abbrev) < 0) {
                    if (result.length() > 0) {
                        result.append(" ");
                    }
                    result.append(abbrev);
                }
            } else if (!allergen.isEmpty()) {
                // Unknown allergen - use first 2 chars uppercase
                String unknown = allergen.length() >= 2
                        ? allergen.substring(0, 1).toUpperCase() + allergen.substring(1, 2)
                        : allergen.toUpperCase();
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(unknown);
            }
        }
        return result.toString();
    }

    /**
     * Gets the full display text for allergens (for details view).
     * Preserves the original allergen names from JSON as source of truth.
     * E.g., "gluten, sulphites" becomes "Contains: Gluten, Sulphites"
     */
    public static String toDisplayText(final String allergens) {
        if (allergens == null || allergens.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] parts = allergens.split(",");

        for (String part : parts) {
            String allergen = part.trim();
            if (!allergen.isEmpty()) {
                if (result.length() > 0) {
                    result.append(", ");
                }
                // Capitalize first letter, preserve rest as-is from JSON
                result.append(allergen.substring(0, 1).toUpperCase())
                      .append(allergen.substring(1));
            }
        }

        if (result.length() > 0) {
            return "Contains: " + result.toString();
        }
        return "";
    }

    /**
     * Gets a legend explaining all abbreviations.
     */
    public static String getLegend() {
        StringBuilder legend = new StringBuilder();
        for (Map.Entry<String, String> entry : ABBREVIATION_NAMES.entrySet()) {
            if (legend.length() > 0) {
                legend.append("\n");
            }
            legend.append(entry.getKey()).append(" = ").append(entry.getValue());
        }
        return legend.toString();
    }

    /**
     * Checks if the given allergens string contains a specific allergen.
     */
    public static boolean containsAllergen(final String allergens, final String allergenToCheck) {
        if (allergens == null || allergens.isEmpty()) {
            return false;
        }
        String[] parts = allergens.split(",");
        String checkLower = allergenToCheck.toLowerCase();

        for (String part : parts) {
            String allergen = part.trim().toLowerCase();
            if (allergen.equals(checkLower)) {
                return true;
            }
            // Also check if it maps to the same abbreviation (e.g., "barley" -> gluten)
            String abbrev = ALLERGEN_ABBREVIATIONS.get(allergen);
            String checkAbbrev = ALLERGEN_ABBREVIATIONS.get(checkLower);
            if (abbrev != null && abbrev.equals(checkAbbrev)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all known allergen types for filtering purposes.
     */
    public static String[] getFilterableAllergens() {
        return new String[]{
                "Gluten",
                "Sulphites",
                "Milk",
                "Eggs",
                "Fish",
                "Peanuts",
                "Tree Nuts",
                "Soybeans",
                "Celery",
                "Mustard",
                "Sesame",
                "Crustaceans",
                "Molluscs",
                "Lupin"
        };
    }
}

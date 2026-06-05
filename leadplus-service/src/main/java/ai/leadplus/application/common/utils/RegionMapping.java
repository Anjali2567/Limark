package ai.leadplus.application.common.utils;

import java.util.List;
import java.util.Map;

public class RegionMapping {
    private RegionMapping() {}

    public final static Map<String, List<String>> SALES_REGIONS = Map.ofEntries(
            Map.entry("WestCoast", List.of(
                    "California", "Oregon", "Washington", "Alaska", "Hawaii",
                    "Nevada", "Utah", "Arizona", "Los Angeles"
            )),
            Map.entry("EastCoast", List.of(
                    "New York", "New Jersey", "Massachusetts", "Pennsylvania", "Connecticut",
                    "Maryland", "Virginia", "North Carolina", "South Carolina", "Georgia", "Florida",
                    "Delaware", "District of Columbia", "New Hampshire"
            )),
            Map.entry("Midwest", List.of(
                    "Illinois", "Ohio", "Michigan", "Minnesota", "Wisconsin", "Missouri", "Indiana",
                    "Iowa", "Kansas", "Nebraska", "North Dakota", "South Dakota", "Colorado"
            )),
            Map.entry("South", List.of(
                    "Texas", "Oklahoma", "Arkansas", "Louisiana", "Tennessee", "Kentucky",
                    "Alabama", "Mississippi", "New Braunfels"
            )),
            Map.entry("Canada", List.of(
                    "Ontario", "Quebec", "British Columbia", "Alberta", "Manitoba", "Saskatchewan",
                    "Nova Scotia", "New Brunswick", "Newfoundland And Labrador", "Prince Edward Island",
                    "Northwest Territories", "Yukon", "Nunavut", "Canada"
            )),
            Map.entry("Mexico", List.of(
                    "Aguascalientes", "Baja California", "Baja California Sur", "Campeche", "Chiapas",
                    "Chihuahua", "Coahuila", "Colima", "Durango", "Guanajuato", "Guerrero", "Hidalgo",
                    "Jalisco", "Mexico State", "Michoacán", "Morelos", "Nayarit", "Nuevo León", "Oaxaca",
                    "Puebla", "Querétaro", "Quintana Roo", "San Luis Potosí", "Sinaloa", "Sonora", "Tabasco",
                    "Tamaulipas", "Tlaxcala", "Veracruz", "Yucatán", "Zacatecas"
            )),
            Map.entry("APAC", List.of(
                    "Sri Lanka", "Singapore", "Japan", "Malaysia", "Australia", "Hong Kong", "Taiwan", "South Korea", "New Zealand"
            )),
            Map.entry("Europe", List.of(
                    "Portugal", "Germany", "Spain", "France", "Denmark", "Netherlands",
                    "Sweden", "Belgium", "Switzerland", "United Kingdom", "Ireland", "Italy", "Austria", "Croatia", "Norway", "Israel"
            )),
            Map.entry("MiddleEast", List.of(
                    "Saudi Arabia", "United Arab Emirates", "Israel"
            )),
            Map.entry("LATAM", List.of("Brazil")),
            Map.entry("US", List.of("United States"))
    );
}
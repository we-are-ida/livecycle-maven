package be.idamediafoundry.sofa.livecycle.example;

import org.apache.commons.lang.StringUtils;

/**
 * Example LiveCycle custom component that will be called by the sample of the livecycle-maven-plugin project.
 *
 * @DSC
 * @smallIcon icon_small.png
 * @largeIcon icon_large.png
 */
public class ExampleCustomComponent {

    /**
     * Capitalizes a String.
     *
     * @param input The String to capitalize, may be null
     * @return The capitalized String, null is input String is null
     *
     * @outputParamName capitalizedString
     */
    public String capitalize(final String input) {
        return StringUtils.capitalize(input);
    }
}

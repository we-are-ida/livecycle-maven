package be.idamediafoundry.sofa.livecycle.example;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExampleCustomComponentTest {

    //SUT
    private ExampleCustomComponent exampleCustomComponent;

    @Before
    public void setup() {
        exampleCustomComponent = new ExampleCustomComponent();
    }

    @Test
    public void testCapitalize() {
        String expected = "Test";

        String actual = exampleCustomComponent.capitalize("test");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCapitalizeNullness() {
        Assert.assertNull(exampleCustomComponent.capitalize(null));
    }
}

package komu.blunt.types;

import org.junit.Test;

import static komu.blunt.types.Type.genericType;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypeApplicationTest {

    @Test
    public void printingArrowTypes() {
        Type type = genericType("->", var("a"), var("b"));

        assertThat(type.toString(), is("a -> b"));
    }

    private static TypeVariable var(String name) {
        return new TypeVariable(name, Kind.STAR);
    }
}

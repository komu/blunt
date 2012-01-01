package komu.blunt.types;

import org.junit.Test;

import static komu.blunt.types.Type.functionType;
import static komu.blunt.types.Type.typeVariable;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypeApplicationTest {

    @Test
    public void printingArrowTypes() {
        assertThat(functionType(typeVariable("a"), typeVariable("b")).toString(), is("a -> b"));
    }
}

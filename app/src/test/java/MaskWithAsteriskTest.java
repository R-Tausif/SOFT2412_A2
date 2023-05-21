import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;


import static org.junit.jupiter.api.Assertions.*;

public class MaskWithAsteriskTest {
    private InputStream sysInBackup;
    private PrintStream sysOutBackup;

    @BeforeEach
    public void setup(){
        this.sysInBackup = System.in;
        this.sysOutBackup = System.out;
    }

    @AfterEach
    public void cleanUp(){
        System.setIn(sysInBackup);
        System.setOut(sysOutBackup);
    }

    public ByteArrayOutputStream writeInputOutput(String input) {
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        System.setIn(bais);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        System.setOut(printStream);
        return baos;
    }

    @Test
    public void readPasswordTest(){
        ByteArrayOutputStream out = this.writeInputOutput("");

    }
}

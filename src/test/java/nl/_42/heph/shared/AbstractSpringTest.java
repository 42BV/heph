package nl._42.heph.shared;

import nl._42.database.truncator.DatabaseTruncator;
import nl._42.heph.Application;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Application.class)
public abstract class AbstractSpringTest {

    @Autowired
    private DatabaseTruncator databaseTruncator;

    @BeforeEach
    public void cleanUp() throws Exception {
        databaseTruncator.truncate();
    }

}

package nl._42.heph.shared;

import nl._42.database.truncator.DatabaseTruncator;
import nl._42.heph.ApplicationConfig;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
public abstract class AbstractSpringTest {

    @Autowired
    private DatabaseTruncator databaseTruncator;

    @Before
    public void cleanUp() throws Exception {
        databaseTruncator.truncate();
    }

}

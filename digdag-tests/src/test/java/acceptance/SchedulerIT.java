package acceptance;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utils.CommandStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static utils.TestUtils.copyResource;
import static utils.TestUtils.main;

public class SchedulerIT
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path root()
    {
        return folder.getRoot().toPath().toAbsolutePath();
    }

    @Test
    public void testScheduler()
            throws Exception
    {
        copyResource("acceptance/scheduler/simple.dig", root().resolve("test.dig"));
        CommandStatus status = main("scheduler", "--project", root().toString());
        assertThat(status.errUtf8(), status.code(), is(0));

        for (int i = 0; i < 90; i++) {
            if (Files.exists(root().resolve("foo.out"))) {
                assertTrue(true);
                return;
            }
            TimeUnit.SECONDS.sleep(2);
        }
        assertTrue(false);
    }

    @Test
    public void testInvalidScheduler()
            throws Exception
    {
        try {
            copyResource("acceptance/scheduler/invalid.dig", root().resolve("test.dig"));
            main("scheduler", "--project", root().toString());
            fail();
        } catch (Throwable ie) {
            assertThat(ie.getCause().getCause().toString(), Matchers.containsString("Parameter 'skip_on_over_time' is not used at schedule. > Did you mean '[skip_on_overtime]'"));
        }
    }

    @Test
    public void testInvalidOpScheduler()
            throws Exception
    {
        try {
            copyResource("acceptance/scheduler/invalid_op.dig", root().resolve("test.dig"));
            main("scheduler", "--project", root().toString());
            fail();
        } catch (Throwable ie) {
            assertThat(ie.getCause().getCause().toString(), Matchers.containsString("Parameter 'wrong>' is not used at schedule."));
        }
    }
}

package utils;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

import play.Logger;
import play.Logger.ALogger;

public class ExternalExecute {

	private static final long DEFAULT_TIMEOUT = 10000;

	private static final ALogger log = Logger.of(ExternalExecute.class);

	public static void execExternal(CommandLine cl) throws IOException {
		execExternal(cl, DEFAULT_TIMEOUT);
	}

	public static void execExternal(CommandLine cl, long timeout)
			throws IOException {
		Executor exec = new DefaultExecutor();
		PumpStreamHandler sh = new PumpStreamHandler();
		sh.setStopTimeout(timeout);
		exec.setStreamHandler(sh);
		exec.setWatchdog(new ExecuteWatchdog(timeout));
		log.info("are going to execute {}", cl);
		int exitvalue = exec.execute(cl);
		log.info("exit status: {}", exitvalue);
	}

}

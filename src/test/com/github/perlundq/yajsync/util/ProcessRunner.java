package com.github.perlundq.yajsync.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class ProcessRunner {

	public class Result {
		public final String STDOUT;
		public final String STDERR;
		public final int EXITCODE;
		private final String command;

		public Result(String out, String err, int exitcode, String command) {
			this.STDOUT = out;
			this.STDERR = err;
			this.EXITCODE = exitcode;
			this.command = command;
		}

		public String getCommand() {
			return command;
		}

		public ProcessException getProcessException() {
			return new ProcessException("CMD: "+this.command+"; EXITCODE: "+this.EXITCODE+"; STDOUT: "+this.STDOUT+"; STDERR: "+this.STDERR);
		}
	}

	public class ProcessException extends IOException {

		private static final long serialVersionUID = 1L;
		String msg;

		ProcessException(String msg) {
			this.msg = msg;
		}

		@Override
		public String getMessage() {
			return this.msg;
		}
	}

	public static Result exec(List<String> commandList) throws IOException {

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();

		ProcessRunner pr = new ProcessRunner();

		StringBuilder buf = new StringBuilder();
		for (String cmd : commandList) {
			buf.append(cmd).append(" ");
		}
		String command = buf.toString().trim();

		Process process = Runtime.getRuntime().exec(command);
		InputStreamTransformer stdoutReader = pr.new InputStreamTransformer(process.getInputStream(), stdout);
		InputStreamTransformer stderrReader = pr.new InputStreamTransformer(process.getErrorStream(), stderr);

		try {
			process.waitFor();
			stdoutReader.join();
			stderrReader.join();
		} catch (InterruptedException ie) {
			throw new IOException(ie);
		}

		return pr.new Result(stdout.toString(), stderr.toString(), process.exitValue(), command);
	}

	class InputStreamTransformer extends Thread {

		private final InputStream m_in;

		private final OutputStream m_out;

		public InputStreamTransformer(InputStream in, OutputStream out)
		{
			m_in = in;
			m_out = out;
			start();
		}

		@Override
		public void run()
		{
			try
			{
				int c;
				while ((c = m_in.read()) != -1)
				{
					m_out.write((char) c);
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}

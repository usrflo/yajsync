package com.github.perlundq.yajsync.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class ProcessRunner {

	public class Result {
		public String STDOUT;
		public String STDERR;
		public int EXITCODE;
		private final List<String> commandList;

		public Result(String out, String err, int exitcode, List<String> commandList) {
			this.STDOUT = out;
			this.STDERR = err;
			this.EXITCODE = exitcode;
			this.commandList = commandList;
		}

		public String getCommand() {
			StringBuilder buf = new StringBuilder();
			for (String commandPart : commandList) {
				buf.append(commandPart).append(" ");
			}
			return buf.toString().trim();
		}

		public ProcessException getProcessException() {
			return new ProcessException("CMD: "+getCommand()+"; EXITCODE: "+this.EXITCODE+"; STDOUT: "+this.STDOUT+"; STDERR: "+this.STDERR);
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

		Process process = Runtime.getRuntime().exec(buf.toString());
		InputStreamTransformer stdoutReader = pr.new InputStreamTransformer(process.getInputStream(), stdout);
		InputStreamTransformer stderrReader = pr.new InputStreamTransformer(process.getErrorStream(), stderr);

		try {
			process.waitFor();
			stdoutReader.join();
			stderrReader.join();
		} catch (InterruptedException ie) {
			throw new IOException(ie);
		}

		return pr.new Result(stdout.toString(), stderr.toString(), process.exitValue(), commandList);
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

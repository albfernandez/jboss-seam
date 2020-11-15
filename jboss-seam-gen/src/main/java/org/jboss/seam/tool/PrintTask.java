package org.jboss.seam.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class PrintTask extends Task {
	private String file;

	public PrintTask() {
		super();
	}
	
	public void setFile(String file) {
		this.file = file;
	}

	@Override
	public void execute() throws BuildException {
		BufferedReader reader = null;
		try {
			reader = Files.newBufferedReader(new File(file).toPath(), StandardCharsets.UTF_8);
			while (reader.ready()) {
				System.out.println(reader.readLine());
			}
			reader.close();
		} catch (Exception e) {
			throw new BuildException(e);
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {
					//
				}
			}
		}
	}
}

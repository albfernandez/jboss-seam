package org.jboss.seam.document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.jboss.seam.document.DocumentData.DocumentType;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

public final class DocumentDataFactory {

	private static final LogProvider log = Logging.getLogProvider(DocumentDataFactory.class);

	private DocumentDataFactory() {
		throw new AssertionError("No instances allowed");
	}

	public static DocumentData getDocumentData(String baseName, DocumentType documentType, byte[] data) {
		File tempFile = getTempFile(data);
		if (tempFile != null) {
			return new TempFileDocumentData(baseName, documentType, tempFile);
		}
		return new ByteArrayDocumentData(baseName, documentType, data);
	}

	private static File getTempFile(byte[] data) {

		File tempDir = getTempDir();
		if (tempDir == null) {
			return null;
		}
		File tempFile = new File(tempDir, "seam-docstore-" + UUID.randomUUID().toString());
		
		try {
			Files.write(tempFile.toPath(), data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException ioe) {
			log.warn("Error creating temp file", ioe);
			tempFile = null;
		}
		return tempFile;
	}

	private static File getTempDir() {
		Object ctx = FacesContext.getCurrentInstance().getExternalContext().getContext();
		if (ctx instanceof ServletContext) {
			ServletContext servletContext = (ServletContext) ctx;
			boolean useTempFiles = Boolean.parseBoolean(servletContext.getInitParameter("org.jboss.seam.document.useTempFiles"));
			if (!useTempFiles) {
				return null;
			}
			String result = servletContext.getInitParameter("org.jboss.seam.document.tempFilesDirectory");
			if (!Strings.isEmpty(result)) {
				return new File(result);
			}
			File servletTempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
			if (servletTempDir != null) {
				return servletTempDir;
			}
			return new File(System.getProperty("java.io.tmpdir"));
		}
		return null;

	}
}

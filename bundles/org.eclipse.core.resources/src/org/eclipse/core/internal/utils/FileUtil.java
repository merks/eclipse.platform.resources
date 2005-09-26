/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.utils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.filesystem.IFileStoreConstants;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * Static utility methods for manipulating Files and URIs.
 */
public class FileUtil {
	/**
	 * Singleton buffer created to prevent buffer creations in the
	 * transferStreams method.  Used as an optimization, based on the assumption
	 * that multiple writes won't happen in a given instance of FileStore.
	 */
	private static final byte[] buffer = new byte[8192];

	/**
	 * Closes a stream and ignores any resulting exception. This is useful
	 * when doing stream cleanup in a finally block where secondary exceptions
	 * are not worth logging.
	 */
	public static void safeClose(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			//ignore
		}
	}

	/**
	 * Closes a stream and ignores any resulting exception. This is useful
	 * when doing stream cleanup in a finally block where secondary exceptions
	 * are not worth logging.
	 */
	public static void safeClose(OutputStream out) {
		try {
			if (out != null)
				out.close();
		} catch (IOException e) {
			//ignore
		}
	}

	/**
	 * Converts a path to a URI
	 */
	public static URI toURI(IPath path) {
		if (path.isAbsolute()) {
			String filePath = path.toFile().getAbsolutePath();
			StringBuffer pathBuf = new StringBuffer(filePath.length() + 1);
			//double-slash is the URI host separator
			pathBuf.append('/');
			//additional double-slash for UNC paths
			if (path.isUNC())
				pathBuf.append('/').append('/');
			pathBuf.append(filePath);
			try {
				return new URI(IFileStoreConstants.SCHEME_FILE, null, pathBuf.toString(), null);
			} catch (URISyntaxException e) {
				IllegalArgumentException iae = new IllegalArgumentException();
				iae.initCause(e);
				throw iae;
			}
		}
		return URI.create(path.toString());
	}

	public static final void transferStreams(InputStream source, OutputStream destination, String path, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			/*
			 * Note: although synchronizing on the buffer is thread-safe,
			 * it may result in slower performance in the future if we want 
			 * to allow concurrent writes.
			 */
			synchronized (buffer) {
				while (true) {
					int bytesRead = -1;
					try {
						bytesRead = source.read(buffer);
					} catch (IOException e) {
						String msg = NLS.bind(Messages.localstore_failedReadDuringWrite, path);
						throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, new Path(path), msg, e);
					}
					if (bytesRead == -1)
						break;
					try {
						destination.write(buffer, 0, bytesRead);
					} catch (IOException e) {
						String msg = NLS.bind(Messages.localstore_couldNotWrite, path);
						throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, new Path(path), msg, e);
					}
					monitor.worked(1);
				}
			}
		} finally {
			safeClose(source);
			safeClose(destination);
		}
	}

	/**
	 * Not intended for instantiation.
	 */
	private FileUtil() {
		super();
	}
}
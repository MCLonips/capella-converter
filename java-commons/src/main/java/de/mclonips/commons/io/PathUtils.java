package de.mclonips.commons.io;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Tim Franken (FRTI)
 */
@UtilityClass
public class PathUtils {

    /**
     * Writes the content of the given {@link InputStream} to the given target file.
     *
     * @param inputStream {@link InputStream} containing all data
     * @param targetFile  {@link Path} to the target file
     *
     * @return {@link Path} to the written file
     */
    public Path write(final InputStream inputStream, final Path targetFile) throws IOException {
        Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        return targetFile;
    }

    /**
     * Creates directory inside systems tmp path.
     *
     * @return {@link Path} to the created temporary directory
     *
     * @throws IOException if directory can not be created
     */
    public Path createTempDir() throws IOException {
        return Files.createTempDirectory(UUID.randomUUID().toString());
    }

    /***
     * Extracts the file in question into the specified destination
     * @param fileToExtract Zip to be extracted
     * @param destinationDirectory Destination directory of zip's contents
     *
     * @return {@link Collection} containing the {@link Path paths} to all extracted files
     */
    public Collection<Path> simpleExtract(final Path fileToExtract, final Path destinationDirectory) throws IOException {
        final List<Path> paths = Lists.newArrayList();

        try (final ZipFile zipFile = new ZipFile(fileToExtract.toFile())) {
            final Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
            while (zipFileEntries.hasMoreElements()) {
                final ZipEntry entry = zipFileEntries.nextElement();
                final Path destinationFile = destinationDirectory.resolve(entry.getName());

                if (!entry.isDirectory()) {

                    final Path parent = destinationFile.getParent();
                    if (!(PathUtils.exists(parent))) {
                        PathUtils.createDirectories(parent);
                    }

                    Files.copy(zipFile.getInputStream(entry), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                    paths.add(destinationFile);
                }//if
            }//while
        }//try

        return paths;
    }

    /**
     * Tests whether a file exists.
     *
     * @param path the path to the file to test
     *
     * @return {@code true} if the file exists; {@code false} if the file does not exist or its existence cannot be determined.
     */
    public boolean exists(final Path path) {
        return path != null && Files.exists(path);
    }

    /**
     * Method tries to create given directory
     *
     * @param path directory which has to be created
     *
     * @return path to the created directory
     *
     * @throws IOException if directory can not be created
     */
    public Path createDirectories(final Path path) throws IOException {
        return Files.createDirectories(path);
    }

    /**
     * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
     *
     * @param path file or directory to delete, can be {@code null}
     *
     * @return {@code true} if the file or directory was deleted, otherwise {@code false}
     */
    public boolean deleteQuietly(final Path path) {
        return FileUtils.deleteQuietly(path.toFile());
    }

    /**
     * Creates an empty file in the default temporary-file directory, using
     * the given filename. The resulting {@code Path} is associated with the default {@code FileSystem}.
     *
     * @param filename name of the temporary file
     *
     * @return the path to the newly created file that did not exist before
     * this method was invoked
     *
     * @throws IOException if an I/O error occurs or the temporary-file directory does not
     *                     exist
     */
    public Path createTempFile(final String filename) throws IOException {
        return PathUtils.createTempFile(filename, "");
    }

    public Path createTempFile(final String prefix, final String suffix) throws IOException {
        return Files.createTempFile(prefix, suffix);
    }

    /**
     * Extract the filename from the given Java resource path,
     * e.g. {@code "mypath/myfile.txt" -> "myfile.txt"}.
     *
     * @param path the file path (may be {@code null})
     *
     * @return the extracted filename, or {@code null} if none
     */
    public String getFilename(final String path) {
        if (path == null) {
            return null;
        }

        final int separatorIndex = path.lastIndexOf(FileSystems.getDefault().getSeparator());
        return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
    }

    /**
     * Deletes the given path from local filesystem. If the path represants an file, the file will be deleted.
     * If the path represents an directory, the directory will be cleaned before it is deleted.
     *
     * @param path {@link Path} to the path to be deleted
     *
     * @return {@code true}, if path exists and has been removed successfully, {@code false} otherwise
     *
     * @throws IOException if the path could not be deleted
     */
    public boolean delete(final Path path) throws IOException {
        if (!PathUtils.exists(path)) {
            return false;
        }

        if (Files.isDirectory(path)) {
            PathUtils.cleanDirectory(path);
        }

        Files.delete(path);

        return true;
    }

    /**
     * Method to clean all Files inside given directory
     *
     * @param path directory to be cleaned
     *
     * @return {@code true} if all files has been removed from directory
     *
     * @throws IOException if one of the files could not be deleted
     */
    public boolean cleanDirectory(final Path path) throws IOException {
        if (!PathUtils.exists(path)) {
            return false;
        }
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                if (file.equals(path)) {
                    return FileVisitResult.CONTINUE;
                }


                if (Files.isDirectory(file)) {
                    PathUtils.cleanDirectory(file);
                }

                Files.delete(file);

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                if (!dir.equals(path)) {
                    PathUtils.cleanDirectory(dir);
                    Files.delete(dir);
                }

                return FileVisitResult.CONTINUE;
            }
        });

        return true;
    }

    /**
     * Deletes the given list with {@link Path paths}.
     *
     * @param paths {@link Collection} with {@link Path paths}
     *
     * @throws IOException if one of the paths could not be deleted
     */
    public void delete(final Collection<Path> paths) throws IOException {
        for (final Path path : paths) {
            PathUtils.delete(path);
        }
    }

    /**
     * Writes bytes to a file. All bytes in the byte array are written to the file.
     * The method ensures that the file is closed when all bytes have been
     * written (or an I/O error or other runtime exception is thrown). If an I/O
     * error occurs then it may do so after the file has created or truncated,
     * or after some bytes have been written to the file.
     *
     * @param path  the path to the file
     * @param bytes the byte array with the bytes to write
     *
     * @return the path to the written file
     *
     * @throws IOException if an I/O error occurs writing to or creating the file
     */
    public Path write(final Path path, final byte[] bytes) throws IOException {
        return Files.write(path, bytes);
    }

    /**
     * Returns the size of a file (in bytes). The size may differ from the
     * actual size on the file system due to compression, support for sparse
     * files, or other reasons.
     *
     * @param path the path to the file
     *
     * @return the file size, in bytes
     *
     * @throws IOException if an I/O error occurs
     */
    public long size(final Path path) throws IOException {
        return Files.size(path);
    }

    /**
     * Reads all the bytes from a file. The method ensures that the file is
     * closed when all bytes have been read or an I/O error, or other runtime
     * exception, is thrown.
     *
     * <p> Note that this method is intended for simple cases where it is
     * convenient to read all bytes into a byte array. It is not intended for
     * reading in large files.
     *
     * @param   path the path to the file
     *
     * @return  a byte array containing the bytes read from the file
     *
     * @throws  IOException if an I/O error occurs reading from the stream
     */
    public byte[] readAllBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }
}

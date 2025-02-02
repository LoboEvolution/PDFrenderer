/*
 * MIT License
 *
 * Copyright (c) 2014 - 2023 LoboEvolution
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Contact info: ivan.difrancesco@yahoo.it
 */

package org.loboevolution.pdfview.action;

import org.loboevolution.pdfview.PDFObject;
import org.loboevolution.pdfview.PDFParseException;

import java.io.IOException;

/**
 * **************************************************************************
 * Action for launching an application, mostly used to open a file.
 * <p>
 * Author  Katja Sondermann
 *
 * @since 08.07.2009
 * **************************************************************************
 */
public class LaunchAction extends PDFAction {
    // file separator according to PDF spec
    /**
     * Constant <code>SOLIDUS="/"</code>
     */
    public static final String SOLIDUS = "/";

    /**
     * the file/application to be opened (optional)
     */
    private final FileSpec file;
    private final PDFObject unixParam;
    private final PDFObject macParam;
    private final WinLaunchParam winParam;
    /**
     * should a new window be opened (optional)
     */
    private boolean newWindow = false;

    /**
     * Creates a new instance of LaunchAction from an object
     *
     * @param obj  - the PDFObject with the action information
     * @param root - the root object
     * @throws IOException if any.
     */
    public LaunchAction(final PDFObject obj, final PDFObject root) throws IOException {
        super("Launch");
        // find the file/application and parse it
        final PDFObject fileObj = obj.getDictRef("F");
        this.file = parseFileSpecification(fileObj);

        // find the new window flag and parse it
        final PDFObject newWinObj = obj.getDictRef("NewWindow");
        if (newWinObj != null) {
            this.newWindow = newWinObj.getBooleanValue();
        }
        // parse the OS specific launch parameters:
        this.winParam = parseWinDict(obj.getDictRef("Win"));
        // unix and mac dictionaries are not further specified, so can not be parsed yet.
        this.unixParam = obj.getDictRef("Unix");
        this.macParam = obj.getDictRef("Mac");

        // check if at least the file or one of the OS specific launch parameters is set:
        if ((this.file == null)
                && (this.winParam == null)
                && (this.unixParam == null)
                && (this.macParam == null)) {
            throw new PDFParseException("Could not parse launch action (file or OS " +
                    "specific launch parameters are missing): " + obj.toString());
        }
    }

    /**
     * **********************************************************************
     * Is the file name absolute (if not, it is relative to the path of the
     * currently opened PDF file).
     * If the file name starts with a "/", it is considered to be absolute.
     *
     * @param fileName a {@link String} object.
     * @return boolean
     * **********************************************************************
     */
    public static boolean isAbsolute(final String fileName) {
        return fileName.startsWith(SOLIDUS);
    }

    /*************************************************************************
     * Parse the file specification object
     * @param fileObj a {@link org.loboevolution.pdfview.PDFObject} object.
     * @return FileSpec - might be <code>null</code> in case the passed object is null
     * @throws IOException a {@link IOException} object.
     * @throws PDFParseException a {@link org.loboevolution.pdfview.PDFParseException} object.
     ************************************************************************/
    private FileSpec parseFileSpecification(final PDFObject fileObj) throws PDFParseException, IOException {
        FileSpec file = null;
        if (fileObj != null) {
            file = new FileSpec();
            if (fileObj.getType() == PDFObject.DICTIONARY) {
                file.setFileSystem(PdfObjectParseUtil.parseStringFromDict("FS", fileObj, false));
                file.setFileName(PdfObjectParseUtil.parseStringFromDict("F", fileObj, false));
                file.setUnicode(PdfObjectParseUtil.parseStringFromDict("UF", fileObj, false));
                file.setDosFileName(PdfObjectParseUtil.parseStringFromDict("DOS", fileObj, false));
                file.setMacFileName(PdfObjectParseUtil.parseStringFromDict("Mac", fileObj, false));
                file.setUnixFileName(PdfObjectParseUtil.parseStringFromDict("Unix", fileObj, false));
                file.setVolatileFile(PdfObjectParseUtil.parseBooleanFromDict("V", fileObj, false));
                file.setDescription(PdfObjectParseUtil.parseStringFromDict("Desc", fileObj, false));
                file.setId(fileObj.getDictRef("ID"));
                file.setEmbeddedFile(fileObj.getDictRef("EF"));
                file.setRelatedFile(fileObj.getDictRef("RF"));
                file.setCollectionItem(fileObj.getDictRef("CI"));
            } else if (fileObj.getType() == PDFObject.STRING) {
                file.setFileName(fileObj.getStringValue());
            } else {
                throw new PDFParseException("File specification could not be parsed " +
                        "(should be of type 'Dictionary' or 'String'): " + fileObj.toString());
            }
        }
        return file;
    }


    /*************************************************************************
     * Parse the windows specific launch parameters
     * @param winDict a {@link org.loboevolution.pdfview.PDFObject} object.
     * @throws IOException - in case of a problem during parsing content
     ************************************************************************/
    private WinLaunchParam parseWinDict(final PDFObject winDict) throws IOException {
        if (winDict == null) {
            return null;
        }
        final WinLaunchParam param = new WinLaunchParam();

        // find and parse the file/application name
        param.setFileName(PdfObjectParseUtil.parseStringFromDict("F", winDict, true));

        // find and parse the directory
        param.setDirectory(PdfObjectParseUtil.parseStringFromDict("D", winDict, false));

        // find and parse the operation to be performed
        param.setOperation(PdfObjectParseUtil.parseStringFromDict("O", winDict, false));

        // find and parse the parameter to be passed to the application
        param.setParameter(PdfObjectParseUtil.parseStringFromDict("P", winDict, false));

        return param;
    }

    /**
     * **********************************************************************
     * The file / application to be opened
     *
     * @return FileSpec
     * **********************************************************************
     */
    public FileSpec getFileSpecification() {
        return this.file;
    }

    /**
     * **********************************************************************
     * Should a new window be opened for the file/application?
     *
     * @return boolean
     * **********************************************************************
     */
    public boolean isNewWindow() {
        return this.newWindow;
    }

    /**
     * **********************************************************************
     * Get the unix specific launch parameters.
     * Note: The dictionary is not specified yet in the PDF spec., so the PdfObject
     * which is returned here is not parsed.
     *
     * @return PDFObject
     * **********************************************************************
     */
    public PDFObject getUnixParam() {
        return this.unixParam;
    }

    /**
     * **********************************************************************
     * Get the mac specific launch parameters.
     * Note: The dictionary is not specified yet in the PDF spec., so the PdfObject
     * which is returned here is not parsed.
     *
     * @return PDFObject
     * **********************************************************************
     */
    public PDFObject getMacParam() {
        return this.macParam;
    }

    /**
     * **********************************************************************
     * Get the windows specific launch parameters.
     *
     * @return WinLaunchParam
     * **********************************************************************
     */
    public WinLaunchParam getWinParam() {
        return this.winParam;
    }

    /*****************************************************************************
     * Internal class for the windows specific launch parameters
     *
     * @version $Id: LaunchAction.java,v 1.1 2009-07-10 12:47:31 xond Exp $
     * Author  xond
     * @since 08.07.2009
     ****************************************************************************/
    public static class WinLaunchParam {
        private String fileName;
        private String directory;
        private String operation = "open";
        private String parameter;

        /*************************************************************************
         * The file/application name to be opened
         * @return a {@link String} object.
         ************************************************************************/
        public String getFileName() {
            return this.fileName;
        }

        /*************************************************************************
         * The file/application name to be opened
         * @param fileName a {@link String} object.
         ************************************************************************/
        public void setFileName(final String fileName) {
            this.fileName = fileName;
        }

        /*************************************************************************
         * The directory in standard DOS syntax
         * @return a {@link String} object.
         ************************************************************************/
        public String getDirectory() {
            return this.directory;
        }

        /*************************************************************************
         * The directory in standard DOS syntax
         * @param directory a {@link String} object.
         ************************************************************************/
        public void setDirectory(final String directory) {
            this.directory = directory;
        }

        /*************************************************************************
         * The operation to be performed (open or print). Ignored
         * in case the "F" parameter describes a file to be opened.
         * Default is "open".
         * @return a {@link String} object. a {@link String} object.
         ************************************************************************/
        public String getOperation() {
            return this.operation;
        }

        /*************************************************************************
         * The operation to be performed ("open" or "print").Ignored
         * in case the "F" parameter describes a file to be opened.
         * Default is "open".
         * @param operation a {@link String} object.
         ************************************************************************/
        public void setOperation(final String operation) {
            this.operation = operation;
        }

        /*************************************************************************
         * A parameter which shall be passed to the application. Ignored
         * in case the "F" parameter describes a file to be opened.
         * @return a {@link String} object.
         ************************************************************************/
        public String getParameter() {
            return this.parameter;
        }

        /*************************************************************************
         * A parameter which shall be passed to the application. Ignored
         * in case the "F" parameter describes a file to be opened.
         * @param parameter a {@link String} object.
         ************************************************************************/
        public void setParameter(final String parameter) {
            this.parameter = parameter;
        }
    }

    /*****************************************************************************
     * Inner class for storing a file specification
     *
     * @version $Id: LaunchAction.java,v 1.1 2009-07-10 12:47:31 xond Exp $
     * Author  xond
     * @since 08.07.2009
     ****************************************************************************/
    public static class FileSpec {
        private String fileSystem;
        private String fileName;
        private String dosFileName;
        private String unixFileName;
        private String macFileName;
        private String unicode;
        private PDFObject id;
        private boolean volatileFile;
        private PDFObject embeddedFile;
        private PDFObject relatedFile;
        private String description;
        private PDFObject collectionItem;

        /*************************************************************************
         * The name of the file system that should be used to interpret this entry.
         * @return a {@link String} object.
         ************************************************************************/
        public String getFileSystem() {
            return this.fileSystem;
        }

        /*************************************************************************
         * The name of the file system that should be used to interpret this entry.
         * @param fileSystem a {@link String} object.
         ************************************************************************/
        public void setFileSystem(final String fileSystem) {
            this.fileSystem = fileSystem;
        }

        /*************************************************************************
         * Get the filename:
         * first try to get the file name for the used OS, if it's not available
         * return the common file name.
         * @return a {@link String} object.
         ************************************************************************/
        public String getFileName() {
            final String system = System.getProperty("os.name");
            if (system.startsWith("Windows")) {
                if (this.dosFileName != null) {
                    return this.dosFileName;
                }
            } else if (system.startsWith("mac os x")) {
                if (this.macFileName != null) {
                    return this.macFileName;
                }
            } else {
                if (this.unixFileName != null) {
                    return this.unixFileName;
                }
            }
            return this.fileName;
        }

        /*************************************************************************
         * The file name.
         * @param fileName a {@link String} object.
         ************************************************************************/
        public void setFileName(final String fileName) {
            this.fileName = fileName;
        }

        /*************************************************************************
         * A file specification string representing a DOS file name.
         * @return a {@link String} object.
         ************************************************************************/
        public String getDosFileName() {
            return this.dosFileName;
        }

        /*************************************************************************
         * A file specification string representing a DOS file name.
         * @param dosFileName a {@link String} object.
         ************************************************************************/
        public void setDosFileName(final String dosFileName) {
            this.dosFileName = dosFileName;
        }

        /*************************************************************************
         * A file specification string representing a unix file name.
         * @return a {@link String} object.
         ************************************************************************/
        public String getUnixFileName() {
            return this.unixFileName;
        }

        /*************************************************************************
         * A file specification string representing a unix file name.
         * @param unixFileName a {@link String} object.
         ************************************************************************/
        public void setUnixFileName(final String unixFileName) {
            this.unixFileName = unixFileName;
        }

        /*************************************************************************
         * A file specification string representing a mac file name.
         * @return a {@link String} object.
         ************************************************************************/
        public String getMacFileName() {
            return this.macFileName;
        }

        /*************************************************************************
         * A file specification string representing a mac file name.
         * @param macFileName a {@link String} object.
         ************************************************************************/
        public void setMacFileName(final String macFileName) {
            this.macFileName = macFileName;
        }

        /*************************************************************************
         * Unicode file name
         * @return a {@link String} object.
         ************************************************************************/
        public String getUnicode() {
            return this.unicode;
        }

        /*************************************************************************
         * Unicode file name
         * @param unicode a {@link String} object.
         ************************************************************************/
        public void setUnicode(final String unicode) {
            this.unicode = unicode;
        }

        /*************************************************************************
         * ID - array of two byte strings constituting a file identifier, which
         * should be included in the referenced file.
         *
         * @return PDFObject
         ************************************************************************/
        public PDFObject getId() {
            return this.id;
        }

        /*************************************************************************
         * ID - array of two byte strings constituting a file identifier, which
         * should be included in the referenced file.
         *
         * @param id a {@link String} object.
         ************************************************************************/
        public void setId(final PDFObject id) {
            this.id = id;
        }

        /*************************************************************************
         * Is the file volatile?
         * @return boolean
         ************************************************************************/
        public boolean isVolatileFile() {
            return this.volatileFile;
        }

        /*************************************************************************
         * Is the file volatile?
         * @param volatileFile a {@link String} object.
         ************************************************************************/
        public void setVolatileFile(final boolean volatileFile) {
            this.volatileFile = volatileFile;
        }

        /*************************************************************************
         * Dictionary of embedded file streams
         * @return PDFObject
         ************************************************************************/
        public PDFObject getEmbeddedFile() {
            return this.embeddedFile;
        }

        /*************************************************************************
         * Dictionary of embedded file streams
         * @param embeddedFile a {@link String} object.
         ************************************************************************/
        public void setEmbeddedFile(final PDFObject embeddedFile) {
            this.embeddedFile = embeddedFile;
        }

        /*************************************************************************
         * Dictionary of related files.
         * @return PDFObject
         ************************************************************************/
        public PDFObject getRelatedFile() {
            return this.relatedFile;
        }

        /*************************************************************************
         * Dictionary of related files.
         * @param relatedFile a {@link String} object.
         ************************************************************************/
        public void setRelatedFile(final PDFObject relatedFile) {
            this.relatedFile = relatedFile;
        }

        /*************************************************************************
         * File specification description
         * @return a {@link String} object.
         ************************************************************************/
        public String getDescription() {
            return this.description;
        }

        /*************************************************************************
         * File specification description
         * @param description a {@link String} object.
         ************************************************************************/
        public void setDescription(final String description) {
            this.description = description;
        }

        /*************************************************************************
         * Collection item dictionary
         * @return PDFObject
         ************************************************************************/
        public PDFObject getCollectionItem() {
            return this.collectionItem;
        }

        /*************************************************************************
         * Collection item dictionary
         * @param collectionItem a {@link String} object.
         ************************************************************************/
        public void setCollectionItem(final PDFObject collectionItem) {
            this.collectionItem = collectionItem;
        }
    }
}

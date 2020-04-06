package com.sanda.truckdoc.client.service.file;

import com.sanda.truckdoc.client.data.model.file.ConversionType;
import com.sanda.truckdoc.client.data.model.file.DocType;
import com.sanda.truckdoc.client.data.model.file.FileType;
import com.sanda.truckdoc.client.util.commons.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.message.BasicHeader;
import ch.boye.httpclientandroidlib.protocol.HTTP;

/**
 * TruckDoc mobile client class
 *
 * @author: Siarhei Zhmura
 */
public class CountingInputStreamEntity implements Cloneable, HttpEntity {

    private UploadListener listener;
    private final long length;
    private final String fileName;
    private boolean convertedOnClient = true;
    private FileType fileType;
    private DocType docType;
    private ConversionType conversionType;
    private final InputStream inputStream;
    protected Header contentType;
    protected Header contentEncoding;
    protected boolean chunked;

    public CountingInputStreamEntity(InputStream instream, long length, String fileName) {
        super();
        this.inputStream = instream;
        this.length = length;
        this.fileName = fileName;
    }

    public void setUploadListener(UploadListener listener) {
        this.listener = listener;
    }

    public String getFileName() {
        return fileName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public boolean isConvertedOnClient() {
        return convertedOnClient;
    }

    public void setConvertedOnClient(boolean convertedOnClient) {
        this.convertedOnClient = convertedOnClient;
    }

    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public ConversionType getConversionType() {
        return conversionType;
    }

    public void setConversionType(ConversionType conversionType) {
        this.conversionType = conversionType;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        OutputStream os = new CountingOutputStream(outstream);

        try {
            IOUtils.copyLarge(inputStream, outstream);
            os.flush();
        } finally {
            inputStream.close();
        }
    }

    class CountingOutputStream extends OutputStream {

        private long counter = 0L;
        private OutputStream outputStream;

        public CountingOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int oneByte) throws IOException {
            this.outputStream.write(oneByte);
            counter++;
            if (listener != null) {
                int percent = (int) ((counter * 100) / length);
                listener.onChange(percent);
            }
        }
    }

    public interface UploadListener {

        void onChange(int percent);
    }

    public boolean isRepeatable() {
        return true;
    }

    public long getContentLength() {
        return this.length;
    }

    public Header getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(Header contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public boolean isChunked() {
        return chunked;
    }

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    public Header getContentType() {
        return contentType;
    }

    public void setContentType(Header contentType) {
        this.contentType = contentType;
    }

    public void setContentType(String type) {
        Header h = null;
        if (type != null) {
            h = new BasicHeader(HTTP.CONTENT_TYPE, type);
        }
        setContentType(h);
    }

    public InputStream getContent() throws IOException {
        return this.inputStream;
    }

    /**
     * Tells that this entity is not streaming.
     *
     * @return <code>false</code>
     */
    public boolean isStreaming() {
        return false;
    }

    @Override
    public void consumeContent() throws IOException {
    }

    public Object clone() throws CloneNotSupportedException {
        // File instance is considered immutable
        // No need to make a copy of it
        return super.clone();
    }
}

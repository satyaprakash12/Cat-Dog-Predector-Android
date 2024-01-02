package satyaprakash;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
public class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final String VTwoHyphens = "--";
    private final String VLineEnd = "\r\n";
    private final String VBoundary = "client-" + System.currentTimeMillis();
    private Response.Listener<NetworkResponse> SListener;
    private Response.ErrorListener SErrorListener;
    private Map<String, String> SHeaders;
    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.SListener = listener;
        this.SErrorListener = errorListener;
    }
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (SHeaders != null) ? SHeaders : super.getHeaders();
    }
    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + VBoundary;
    }
    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            // populate text payload
            Map<String, String> params = getParams();
            if (params != null && params.size() > 0) {
                textParse(dos, params, getParamsEncoding());
            }
            // populate data byte payload
            Map<String, DataPart> data = getByteData();
            if (data != null && data.size() > 0) {
                dataParse(dos, data);
            }
            // close multipart form data after text and file data
            dos.writeBytes(VTwoHyphens + VBoundary + VTwoHyphens + VLineEnd);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return null;
    }
    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(
                    response,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
    @Override
    protected void deliverResponse(NetworkResponse response) {
        SListener.onResponse(response);
    }
    @Override
    public void deliverError(VolleyError error) {
        SErrorListener.onErrorResponse(error);
    }
    private void textParse(DataOutputStream dataOutputStream, Map<String, String> params, String encoding) throws IOException {
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                buildTextPart(dataOutputStream, entry.getKey(), entry.getValue());
            }
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + encoding, uee);
        }
    }

    private void dataParse(DataOutputStream dataOutputStream, Map<String, DataPart> data) throws IOException {
        for (Map.Entry<String, DataPart> entry : data.entrySet()) {
            buildDataPart(dataOutputStream, entry.getValue(), entry.getKey());
        }
    }
    private void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        dataOutputStream.writeBytes(VTwoHyphens + VBoundary + VLineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + VLineEnd);
        dataOutputStream.writeBytes(VLineEnd);
        dataOutputStream.writeBytes(parameterValue + VLineEnd);
    }
    private void buildDataPart(DataOutputStream dataOutputStream, DataPart dataFile, String inputName) throws IOException {
        dataOutputStream.writeBytes(VTwoHyphens + VBoundary + VLineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                inputName + "\"; filename=\"" + dataFile.getFileName() + "\"" + VLineEnd);
        if (dataFile.getType() != null && !dataFile.getType().trim().isEmpty()) {
            dataOutputStream.writeBytes("Content-Type: " + dataFile.getType() + VLineEnd);
        }
        dataOutputStream.writeBytes(VLineEnd);
        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(dataFile.getContent());
        int bytesAvailable = fileInputStream.available();
        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }
        dataOutputStream.writeBytes(VLineEnd);
    }
    public class DataPart {
        private String fName;
        private byte[] content;
        private String type;
        public DataPart() {
        }
        public DataPart(String name, byte[] data) {
            fName = name;
            content = data;
        }
        String getFileName() {
            return fName;
        }
        byte[] getContent() {
            return content;
        }
        String getType() {
            return type;
        }
    }
}
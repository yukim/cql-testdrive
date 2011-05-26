package com.github.yukim.cql.testdrive;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

@WebServlet("/cql")
public class CqlServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Resource(name="jdbc/keyspace1")
    private DataSource ds;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonFactory factory = new JsonFactory();
        JsonGenerator json = factory.createJsonGenerator(response.getWriter());

        response.addHeader("Content-Type", "application/javascript");

        Charset utf8 = Charset.forName("UTF-8");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();

            // TODO validate query
            String query = request.getParameter("query").trim();

            if (query.toLowerCase().startsWith("select")) {
                rs = stmt.executeQuery(query);
                //rs = stmt.executeQuery("SELECT * FROM test");
                json.writeStartObject();
                if (rs != null) {

                    ResultSetMetaData meta = rs.getMetaData();

                    rs.next(); // we need this before accessing metadata

                    int count = meta.getColumnCount();
                    if (count > 0) {
                        json.writeStringField("keyspace", meta.getSchemaName(1));
                        json.writeStringField("columnFamily", meta.getTableName(1));
                    }
                    json.writeArrayFieldStart("rows");
                    do {
                        count = meta.getColumnCount();
                        if (count > 0) {
                            json.writeStartObject();
                            for (int i = 1; i <= count; i++) {
                                int type = meta.getColumnType(i);
                                String valStr = "";
                                switch (type) {
                                    case Types.INTEGER:
                                        valStr = Integer.toString(rs.getInt(i));
                                        break;
                                    case Types.VARCHAR:
                                        valStr = rs.getString(i);
                                        break;
                                    default:
                                        valStr = toHexString(rs.getBytes(i));
                                        break;
                                }
                                json.writeStringField(meta.getColumnName(i), valStr);
                            }
                            json.writeEndObject();
                        }
                    } while (rs.next());
                    json.writeEndArray();
                } else {
                    json.writeStringField("error", "error");
                }
            } else if (!query.toLowerCase().startsWith("create") &&
                query.toLowerCase().startsWith("drop")) {
                
                if (stmt.execute(query)) {
                    json.writeStringField("success", "true");
                } else {
                    json.writeStringField("error", "error");
                }
            } else {
                json.writeStringField("error", "error");
            }

            json.writeEndObject();
        } catch (Exception e) {
            e.printStackTrace();

            response.setStatus(500);
            json.writeStartObject();
            json.writeStringField("error", "error");
            json.writeStringField("reason", e.getMessage());
            json.writeEndObject();
        }

        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }

        } catch (Exception e) {
        } finally {
          json.close();
        }
    }

    private String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        sb.append("0x");
        for (int i = 0; i < b.length; i++) {
            sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}

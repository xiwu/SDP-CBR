package gov.cdc.sdp.cbr.phinms;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import javax.sql.DataSource;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Loads data into the PHIN-MS tables.  Used primarily for testing purposes at 
 * this time, but may have production applications.
 * @author ECOLE
 *
 */
public class PhinMSProducer extends DefaultProducer {

    private static Logger logger = LogManager.getLogger("PhinMsInsert");

    private DataSource phinMsDs;
    private String tableName = "message_inq";

    private String phinMsInsertSql;
    private Connection connection;
    private PreparedStatement insertSql;

    public PhinMSProducer(Endpoint endpoint, String uri, DataSource ds, String tableName) {
        super(endpoint);

        this.phinMsDs = ds;
        this.tableName = tableName;
        phinMsInsertSql = "insert into " + tableName + " ";
    }

    @Override
    public PhinMSEndpoint getEndpoint() {
        return (PhinMSEndpoint) super.getEndpoint();
    }

    @Override
    public void process(final Exchange exchange) throws Exception {
        try {
            if (connection == null) {
                connection = phinMsDs.getConnection();
            }

            CSVParser parser = CSVParser.parse((String) exchange.getIn().getBody(), CSVFormat.DEFAULT);

            List<CSVRecord> records = parser.getRecords();

            if (records.size() == 0) {
                logger.error("No contents in file.");
                return;
            }

            CSVRecord headers = records.get(0);

            String sqlColumns = "";
            String sqlFields = "";
            int columnCount = 0;
            for (String header : headers) {
                sqlColumns = sqlColumns + header + (columnCount == headers.size() - 1 ? "" : ", ");
                sqlFields = sqlFields + "?" + (columnCount == headers.size() - 1 ? "" : ", ");
                columnCount++;
            }

            if (insertSql == null) {
                insertSql = connection
                        .prepareStatement(phinMsInsertSql + " (" + sqlColumns + ") values (" + sqlFields + ")");
            }

            for (int i = 1; i < records.size(); i++) {
                CSVRecord values = records.get(i);

                for (int j = 0; j < values.size(); j++) {
                    switch (headers.get(j)) {
                    case "recordId": // Should not be used in most cases -
                                     // autogenerated.
                        insertSql.setLong(j + 1, Long.parseLong(values.get(j)));
                        break;
                    case "payloadBinaryContent":
                        Blob blob = null;
                        try {
                            blob = connection.createBlob();
                            blob.setBytes(1, values.get(j).getBytes());
                            insertSql.setBlob(j + 1, blob);
                        } catch (java.sql.SQLFeatureNotSupportedException e) {
                            insertSql.setBytes(j + 1, values.get(j).getBytes());
                            log.warn("Error attempting to create blob data", e);
                        }

                        break;
                    case "payloadTextContent":
                        insertSql.setString(j + 1, values.get(j));
                        break;
                    default:
                        insertSql.setString(j + 1, values.get(j));
                    }
                }
                insertSql.addBatch();
            }

            insertSql.executeBatch();
        } catch (Exception e) {
            logger.error("An error occured when attempting to write to Phin-MS");
            e.printStackTrace();
        }
    }
}
/*
 * StaticDataSource.java
 *
 * Created on June 2, 2005, 12:46 PM
 */

package eunomia.core.data.staticData;

import java.sql.*;
import java.util.*;

import eunomia.core.data.*;
import eunomia.core.*;
import eunomia.core.data.staticData.*;
import eunomia.core.charter.*;

import org.apache.log4j.*;
import eunomia.core.data.flow.Flow;

/**
 * What the hell is this class for anyway?
 * @author  Mikhail Sosonkin
 */
public class StaticDataSource extends DataSource {
    protected Database database;
    private boolean ranksPrecomputed;
    private boolean tempTablesCreated;
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(StaticDataSource.class);
    }
    
    public StaticDataSource(Database db) {
        tempTablesCreated = false;
        ranksPrecomputed = false;
        database = db;
    }
    
    public void initiate() throws Exception {
        // should it be here?
        database.connect();
        
        database.execute("CREATE TEMPORARY TABLE top (num BIGINT UNSIGNED, ip BIGINT UNSIGNED)");
        database.execute("CREATE TEMPORARY TABLE counts (data_type TINYINT UNSIGNED, num BIGINT UNSIGNED, ip BIGINT UNSIGNED, amount BIGINT UNSIGNED)");
    }
    
    public void terminate() throws Exception {
        // should it be here?
        database.disconnect();
    }

    private void clearTemporaryTables() throws SQLException {
        database.execute("DELETE FROM top");
        database.execute("DELETE FROM counts");
    }
    
    private void preComputeRanks() throws SQLException {
        if(!ranksPrecomputed){
            clearTemporaryTables();
            logger.info("Precomputing ranks");
            database.execute("INSERT INTO top (num, ip) SELECT COUNT(source_ip) AS \"num\", source_ip FROM flows GROUP BY source_ip ORDER BY \"num\" DESC");
            database.execute("INSERT INTO counts SELECT data_type, COUNT(data_type) AS \"num\", source_ip, top.num AS amount FROM flows INNER JOIN top ON source_ip = ip GROUP BY data_type, source_ip ORDER BY top.num DESC");
            ranksPrecomputed = true;
        }
    }
    
    public Charter getMostActiveHosts(int startRank, int count) throws Exception {
        logger.info("Listing " + count + " ranks from " + startRank);
        preComputeRanks();
        
        StringBuilder que = new StringBuilder();
        que.append("SELECT num, data_type, INET_NTOA(ip) FROM counts LIMIT ");
        que.append(startRank + ", " + (count + Flow.NUM_TYPES));
        
        ResultSet res = database.executeQuery(que.toString());
        
/*        Charter data = new StaticCategoryData();
        String lastHost = null;
        while(res.next()){
            data.setValue(res.getDouble(1), Flow.typeNames[res.getInt(2)], lastHost = res.getString(3));
        }
        
        //must be a better way of doing this.
        data.removeColumn(lastHost);*/
        
        return null;
    }

    public void load() throws java.io.IOException {
    }

    public void save() throws java.io.IOException {
    }
}
/*
 * SQLStoredMap.java
 *
 * Created on June 8, 2008, 12:07 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db.sql;

import eunomia.module.receptor.libb.imsCore.EnvironmentKey;
import eunomia.module.receptor.libb.imsCore.bind.SerialObjectBinding;
import eunomia.module.receptor.libb.imsCore.creators.SqlEnvironmentKeyCreator;
import eunomia.module.receptor.libb.imsCore.db.*;
import eunomia.module.receptor.libb.imsCore.iterators.FilteredMultiIterator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SQLStoredMap implements DataStoredMap {
    private Connection rcon;
    private String[] tables;
    private String valueCol;
    private SerialObjectBinding bind;
    private SqlEnvironmentKeyCreator screate;
    
    public SQLStoredMap(Connection rcon, String[] tables, String valueCol, SerialObjectBinding bind, SqlEnvironmentKeyCreator screate) throws SQLException {
        this.valueCol = valueCol;
        this.tables = tables;
        this.rcon = rcon;
        this.bind = bind;
        this.screate = screate;
    }

    public int size() {
        if(tables.length == 0) {
            return 0;
        }
        
        int size = 0;
        try {
            Statement s = rcon.createStatement();
            StringBuilder q = new StringBuilder();
            for (int i = 0; i < tables.length; ++i) {
                q.append("SELECT COUNT(*) FROM ").append(tables[i]);
                
                if(i < tables.length - 1) {
                    q.append(" UNION ");
                }
            }
            
            ResultSet set = s.executeQuery(q.toString());
            
            while(set.next()) {    
                size += set.getInt(1);
            }
            
            set.close();
            s.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return size;
    }

    public FilteredMultiIterator valuesIterator() {
        List list = new ArrayList();
        list.add(new DemandIterator(null));
        
        return new FilteredMultiIterator(list);
    }

    public void remove(Object key) {
        try {
            for (int i = 0; i < tables.length; ++i) {
                StringBuilder q = new StringBuilder("DELETE FROM ");
                q.append(tables[i]).append(" WHERE ");
                
                String[] fields = screate.getColumnNames();
                for (int j = 0; j < fields.length; ++j) {
                    q.append(fields[j]).append("=?");
                    
                    if(j < fields.length - 1) {
                        q.append(" OR ");
                    }
                }
                
                PreparedStatement s = rcon.prepareStatement(q.toString());
                for (int j = 0; j < fields.length; ++j) {
                    screate.getSqlFieldValue(key, s, j + 1);
                }
                
                s.execute();
                s.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Collection duplicates(Object o) {
        return new Dup_Collection(o);
    }
    
    private class DemandIterator implements Iterator {
        private EnvironmentKey where;
        private Object next;
        private int curTable;
        private ResultSet curSet;
        private PreparedStatement curStat;
        
        public DemandIterator(EnvironmentKey where) {
            this.where = where;

            try {
                prepareNext();
            } catch (Exception ex) {
                ex.printStackTrace();
                next = null;
            }
        }
        
        private void prepareNext() throws Exception {
            if(tables.length == 0) {
                return;
            }
            
            while(curSet == null || !curSet.next()) {
                if(curSet != null) {
                    curSet.close();
                    curStat.close();
                }
                
                if(curTable >= tables.length) {
                    next = null;
                    return;
                }
                
                StringBuilder q = new StringBuilder("SELECT ");
                q.append(valueCol).append(" FROM ").append(tables[curTable]);
                
                if(where != null) {
                    q.append(" WHERE ");
                    String[] fields = screate.getColumnNames();
                    for (int j = 0; j < fields.length; ++j) {
                        q.append(fields[j]).append("=?");

                        if(j < fields.length - 1) {
                            q.append(" OR ");
                        }
                    }
                }
                
                curStat = rcon.prepareCall(q.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                curStat.setFetchSize(Integer.MIN_VALUE);
                
                if(where != null) {
                    String[] fields = screate.getColumnNames();
                    for (int j = 0; j < fields.length; ++j) {
                        screate.getSqlFieldValue(where, curStat, j + 1);
                    }
                }
                
                curSet = curStat.executeQuery();
                curTable++;
            }
            
            byte[] bytes = curSet.getBytes(1);
            next = bind.getObject(bytes);
        }
        
        public boolean hasNext() {
            return next != null;
        }

        public Object next() {
            Object tmp = next;
            try {
                prepareNext();
            } catch (Exception ex) {
                ex.printStackTrace();
                next = null;
            }
            
            return tmp;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    private class Dup_Collection implements Collection {
        private Object key;
        private int col_size = -1;
        
        public Dup_Collection(Object key) {
            this.key = key;
        }
        
        public int size() {
            if(col_size != -1) {
                // check the buffer first
                return col_size;
            }
            
            int size = 0;
            try {
                for (int i = 0; i < tables.length; ++i) {
                    StringBuilder q = new StringBuilder("SELECT ");
                    q.append(valueCol).append(" FROM ").append(tables[i]).append(" WHERE ");

                    String[] fields = screate.getColumnNames();
                    for (int j = 0; j < fields.length; ++j) {
                        q.append(fields[j]).append("=?");

                        if(j < fields.length - 1) {
                            q.append(" OR ");
                        }
                    }
                    
                    //String q = "SELECT " + valueCol + " FROM " + tables[i] + " WHERE " + keyCol + "=?";
                    PreparedStatement s = rcon.prepareCall(q.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    s.setFetchSize(Integer.MIN_VALUE);

                    for (int j = 0; j < fields.length; ++j) {
                        screate.getSqlFieldValue(key, s, j + 1);
                    }
                    
                    ResultSet set = s.executeQuery();
                    if(set.next()) {
                        size += set.getInt(1);
                    }
                    set.close();
                    s.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            col_size = size;
            
            return col_size;
        }

        public boolean isEmpty() {
            return size() == 0;
        }

        public Iterator iterator() {
            return new DemandIterator((EnvironmentKey) key);
        }

        public boolean contains(Object o) {
            throw new UnsupportedOperationException();
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        public Object[] toArray(Object[] a) {
            throw new UnsupportedOperationException();
        }

        public boolean add(Object e) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }
    }
}
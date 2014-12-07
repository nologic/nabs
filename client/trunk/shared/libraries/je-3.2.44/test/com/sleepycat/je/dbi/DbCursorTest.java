/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: DbCursorTest.java,v 1.80.2.1 2007/02/01 14:50:09 cwl Exp $
 */

package com.sleepycat.je.dbi;

import java.util.Comparator;
import java.util.Hashtable;

import com.sleepycat.je.DbInternal;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.util.StringDbt;
import com.sleepycat.je.util.TestUtils;

/**
 * Various unit tests for CursorImpl.
 */
public class DbCursorTest extends DbCursorTestBase {

    public DbCursorTest() 
        throws DatabaseException {

        super();
    }

    /**
     * Put a small number of data items into the database in a specific order
     * and ensure that they read back in ascending order.
     */
    public void testSimpleGetPut()
        throws Throwable {

        try {
            initEnv(false);
            doSimpleCursorPuts();

            DataWalker dw = new DataWalker(simpleDataMap) {
                    void perData(String foundKey, String foundData) {
                        assertTrue(foundKey.compareTo(prevKey) >= 0);
                        prevKey = foundKey;
                    }
                };
            dw.walkData();
            assertTrue(dw.nEntries == simpleKeyStrings.length);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Test the internal Cursor.advanceCursor() entrypoint.
     */
    public void testCursorAdvance()
        throws Throwable {

        try {
            initEnv(false);
            doSimpleCursorPuts();

	    StringDbt foundKey = new StringDbt();
	    StringDbt foundData = new StringDbt();
	    String prevKey = "";

	    OperationStatus status = cursor.getFirst(foundKey, foundData,
						     LockMode.DEFAULT);

	    /* 
	     * Advance forward and then back to the first.  Rest of scan
	     * should be as normal.
	     */
	    DbInternal.advanceCursor(cursor, foundKey, foundData);
	    DbInternal.retrieveNext
		(cursor, foundKey, foundData, LockMode.DEFAULT, GetMode.PREV);
	    int nEntries = 0;
	    while (status == OperationStatus.SUCCESS) {
                String foundKeyString = foundKey.getString();
                String foundDataString = foundData.getString();

		assertTrue(foundKeyString.compareTo(prevKey) >= 0);
		prevKey = foundKeyString;
                nEntries++;

		status = cursor.getNext(foundKey, foundData, LockMode.DEFAULT);
	    }

            assertTrue(nEntries == simpleKeyStrings.length);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Put a small number of data items into the database in a specific order
     * and ensure that they read back in descending order.
     */
    public void testSimpleGetPutBackwards()
        throws Throwable {

        try {
            initEnv(false);
            doSimpleCursorPuts();

            DataWalker dw = new BackwardsDataWalker(simpleDataMap) {
                    void perData(String foundKey, String foundData) {
                        if (!prevKey.equals("")) {
                            assertTrue(foundKey.compareTo(prevKey) <= 0);
                        }
                        prevKey = foundKey;
                    }

                    OperationStatus getFirst(StringDbt foundKey,
					     StringDbt foundData)
                        throws DatabaseException {

                        return cursor.getLast(foundKey, foundData,
                                              LockMode.DEFAULT);
                    }

                    OperationStatus getData(StringDbt foundKey,
					    StringDbt foundData)
                        throws DatabaseException {

                        return cursor.getPrev(foundKey, foundData,
                                              LockMode.DEFAULT);
                    }
                };
            dw.walkData();
            assertTrue(dw.nEntries == simpleKeyStrings.length);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Put a small number of data items into the database in a specific order
     * and ensure that they read back in descending order.  When "quux" is
     * found, insert "fub" into the database and make sure that it is also read
     * back in the cursor.
     */
    public void testSimpleGetPut2()
        throws Throwable {

        try {
            initEnv(false);
            doSimpleGetPut2("quux", "fub");
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public void doSimpleGetPut2(String whenFoundDoInsert,
                                String newKey)
        throws DatabaseException {

        doSimpleCursorPuts();

        DataWalker dw =
            new BackwardsDataWalker(whenFoundDoInsert, newKey, simpleDataMap) {
                void perData(String foundKey, String foundData)
                    throws DatabaseException {

                    if (foundKey.equals(whenFoundDoInsert)) {
                        putAndVerifyCursor(cursor2, new StringDbt(newKey),
                                           new StringDbt("ten"), true);
                        simpleDataMap.put(newKey, "ten");
                    }
                }

                OperationStatus getFirst(StringDbt foundKey,
					 StringDbt foundData)
                    throws DatabaseException {

                    return cursor.getLast(foundKey, foundData,
                                          LockMode.DEFAULT);
                }

                OperationStatus getData(StringDbt foundKey,
					StringDbt foundData)
                    throws DatabaseException {

                    return cursor.getPrev(foundKey, foundData,
                                          LockMode.DEFAULT);
                }
            };
        dw.walkData();
        assertTrue(dw.nEntries == simpleKeyStrings.length + 1);
    }

    /**
     * Iterate through each of the keys in the list of "simple keys".  For each
     * one, create the database afresh, iterate through the entries in
     * ascending order, and when the key being tested is found, insert the next
     * highest key into the database.  Make sure that the key just inserted is
     * retrieved during the cursor walk.  Lather, rinse, repeat.
     */
    public void testSimpleGetPutNextKeyForwardTraverse()
        throws Throwable {

        try {
            tearDown();
            for (int i = 0; i < simpleKeyStrings.length; i++) {
                setUp();
                initEnv(false);
                doSimpleGetPut(true,
                               simpleKeyStrings[i],
                               nextKey(simpleKeyStrings[i]),
                               1);
                tearDown();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Iterate through each of the keys in the list of "simple keys".  For each
     * one, create the database afresh, iterate through the entries in
     * ascending order, and when the key being tested is found, insert the next
     * lowest key into the database.  Make sure that the key just inserted is
     * not retrieved during the cursor walk.  Lather, rinse, repeat.
     */
    public void testSimpleGetPutPrevKeyForwardTraverse() 
        throws Throwable {

        try {
            tearDown();
            for (int i = 0; i < simpleKeyStrings.length; i++) {
                setUp();
                initEnv(false);
                doSimpleGetPut(true, simpleKeyStrings[i],
                               prevKey(simpleKeyStrings[i]), 0);
                tearDown();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Iterate through each of the keys in the list of "simple keys".  For each
     * one, create the database afresh, iterate through the entries in
     * descending order, and when the key being tested is found, insert the
     * next lowest key into the database.  Make sure that the key just inserted
     * is retrieved during the cursor walk.  Lather, rinse, repeat.
     */
    public void testSimpleGetPutPrevKeyBackwardsTraverse() 
        throws Throwable {

        try {
            tearDown();
            for (int i = 0; i < simpleKeyStrings.length; i++) {
                setUp();
                initEnv(false);
                doSimpleGetPut(false, simpleKeyStrings[i],
                               prevKey(simpleKeyStrings[i]), 1);
                tearDown();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Iterate through each of the keys in the list of "simple keys".  For each
     * one, create the database afresh, iterate through the entries in
     * descending order, and when the key being tested is found, insert the
     * next highest key into the database.  Make sure that the key just
     * inserted is not retrieved during the cursor walk.  Lather, rinse,
     * repeat.
     */
    public void testSimpleGetPutNextKeyBackwardsTraverse() 
        throws Throwable {

        try {
            tearDown();
            for (int i = 0; i < simpleKeyStrings.length; i++) {
                setUp();
                initEnv(false);
                doSimpleGetPut(true, simpleKeyStrings[i],
                               prevKey(simpleKeyStrings[i]), 0);
                tearDown();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for the above four tests.
     */
    private void doSimpleGetPut(boolean forward,
                                String whenFoundDoInsert,
                                String newKey,
                                int additionalEntries)
        throws DatabaseException {

        doSimpleCursorPuts();

        DataWalker dw;
        if (forward) {
            dw = new DataWalker(whenFoundDoInsert, newKey, simpleDataMap) {
                    void perData(String foundKey, String foundData)
                        throws DatabaseException {

                        if (foundKey.equals(whenFoundDoInsert)) {
                            putAndVerifyCursor(cursor2, new StringDbt(newKey),
                                               new StringDbt("ten"), true);
                            simpleDataMap.put(newKey, "ten");
                        }
                    }
                };
        } else {
            dw = new BackwardsDataWalker(whenFoundDoInsert,
					 newKey,
                                         simpleDataMap) {
		    void perData(String foundKey, String foundData)
			throws DatabaseException {

			if (foundKey.equals(whenFoundDoInsert)) {
			    putAndVerifyCursor(cursor2, new StringDbt(newKey),
					       new StringDbt("ten"), true);
			    simpleDataMap.put(newKey, "ten");
			}
		    }

		    OperationStatus getFirst(StringDbt foundKey,
					     StringDbt foundData)
			throws DatabaseException {

			return cursor.getLast(foundKey, foundData,
					      LockMode.DEFAULT);
		    }

		    OperationStatus getData(StringDbt foundKey,
					    StringDbt foundData)
			throws DatabaseException {

			return cursor.getPrev(foundKey, foundData,
					      LockMode.DEFAULT);
		    }
                };
        }
        dw.walkData();
        assertEquals(simpleKeyStrings.length + additionalEntries, dw.nEntries);
    }

    /**
     * Put a small number of data items into the database in a specific order
     * and ensure that they read back in descending order.  Replace the data
     * portion for each one, then read back again and make sure it was replaced
     * properly.
     */
    public void testSimpleReplace()
        throws Throwable {

        try {
            initEnv(false);
            doSimpleReplace();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public void doSimpleReplace()
        throws DatabaseException {

        doSimpleCursorPuts();

        DataWalker dw =
            new DataWalker(simpleDataMap) {
                void perData(String foundKey, String foundData)
                    throws DatabaseException {

                    String newData = foundData + "x";
                    cursor.putCurrent(new StringDbt(newData));
                    simpleDataMap.put(foundKey, newData);
                }
            };
        dw.walkData();
        dw = new DataWalker(simpleDataMap) {
                void perData(String foundKey, String foundData)
                    throws DatabaseException {

                    assertTrue(foundData.equals(simpleDataMap.get(foundKey)));
                }
            };
        dw.walkData();
    }

    /**
     * Insert N_KEYS data items into a tree.  Iterate through the tree in
     * ascending order.  After each element is retrieved, insert the next
     * lowest key into the tree.  Ensure that the element just inserted is not
     * returned by the cursor.  Ensure that the elements are returned in
     * ascending order.  Lather, rinse, repeat.
     */
    public void testLargeGetPutPrevKeyForwardTraverse() 
        throws Throwable {

        try {
            initEnv(false);
            doLargeGetPutPrevKeyForwardTraverse();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for above.
     */
    private void doLargeGetPutPrevKeyForwardTraverse()
        throws DatabaseException {

        Hashtable dataMap = new Hashtable();
        doLargePut(dataMap, N_KEYS);

        DataWalker dw = new DataWalker(dataMap) {
                void perData(String foundKey, String foundData)
                    throws DatabaseException {

                    assertTrue(foundKey.compareTo(prevKey) >= 0);
                    putAndVerifyCursor(cursor2,
                                       new StringDbt(prevKey(foundKey)),
                                       new StringDbt
                                       (Integer.toString(dataMap.size() +
                                                         nEntries)),
                                       true);
                    prevKey = foundKey;
                    assertTrue(dataMap.get(foundKey) != null);
                    dataMap.remove(foundKey);
                }
            };
        dw.walkData();
        if (dataMap.size() > 0) {
            fail("dataMap still has entries");
        }
        assertTrue(dw.nEntries == N_KEYS);
    }

    /**
     * Insert N_KEYS data items into a tree.  Iterate through the tree
     * in ascending order.  Ensure that count() always returns 1 for each
     * data item returned.
     */
    public void testLargeCount() 
        throws Throwable {

        try {
            initEnv(false);
            doLargeCount();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for above.
     */
    private void doLargeCount()
        throws DatabaseException {

        Hashtable dataMap = new Hashtable();
        doLargePut(dataMap, N_KEYS);

        DataWalker dw = new DataWalker(dataMap) {
                void perData(String foundKey, String foundData)
                    throws DatabaseException {

                    assertTrue(cursor.count() == 1);
                    assertTrue(foundKey.compareTo(prevKey) >= 0);
                    prevKey = foundKey;
                    assertTrue(dataMap.get(foundKey) != null);
                    dataMap.remove(foundKey);
                }
            };
        dw.walkData();
        if (dataMap.size() > 0) {
            fail("dataMap still has entries");
        }
        assertTrue(dw.nEntries == N_KEYS);
    }

    public void xxtestGetPerf() 
        throws Throwable {

        try {
            initEnv(false);
            final int N = 50000;
            int count = 0;
            doLargePutPerf(N);

            StringDbt foundKey = new StringDbt();
            StringDbt foundData = new StringDbt();
            OperationStatus status;
            status = cursor.getFirst(foundKey, foundData, LockMode.DEFAULT);

            while (status == OperationStatus.SUCCESS) {
                status = cursor.getNext(foundKey, foundData, LockMode.DEFAULT);
                count++;
            }
        
            assertTrue(count == N);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Insert a bunch of key/data pairs.  Read them back and replace each of
     * the data.  Read the pairs back again and make sure the replace did the
     * right thing.
     */
    public void testLargeReplace() 
        throws Throwable {

        try {
            initEnv(false);
            Hashtable dataMap = new Hashtable();
            doLargePut(dataMap, N_KEYS);

            DataWalker dw = new DataWalker(dataMap) {
                    void perData(String foundKey, String foundData)
                        throws DatabaseException {

                        String newData = foundData + "x";
                        cursor.putCurrent(new StringDbt(newData));
                        dataMap.put(foundKey, newData);
                    }
                };
            dw.walkData();
            dw = new DataWalker(dataMap) {
                    void perData(String foundKey, String foundData)
                        throws DatabaseException {

                        assertTrue(foundData.equals(dataMap.get(foundKey)));
                        dataMap.remove(foundKey);
                    }
                };
            dw.walkData();
            assertTrue(dw.nEntries == N_KEYS);
            assertTrue(dataMap.size() == 0);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Insert N_KEYS data items into a tree.  Iterate through the tree in
     * descending order.  After each element is retrieved, insert the next
     * highest key into the tree.  Ensure that the element just inserted is not
     * returned by the cursor.  Ensure that the elements are returned in
     * descending order.  Lather, rinse, repeat.
     */
    public void testLargeGetPutNextKeyBackwardsTraverse() 
        throws Throwable {

        try {
            tearDown();
            for (int i = 0; i < N_ITERS; i++) {
                setUp();
                initEnv(false);
                doLargeGetPutNextKeyBackwardsTraverse();
                tearDown();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for above.
     */
    private void doLargeGetPutNextKeyBackwardsTraverse()
        throws DatabaseException {

        Hashtable dataMap = new Hashtable();
        doLargePut(dataMap, N_KEYS);

        DataWalker dw = new BackwardsDataWalker(dataMap) {
                void perData(String foundKey, String foundData)
                    throws DatabaseException {

                    if (!prevKey.equals("")) {
                        assertTrue(foundKey.compareTo(prevKey) <= 0);
                    }
                    putAndVerifyCursor(cursor2,
                                       new StringDbt(nextKey(foundKey)),
                                       new StringDbt
                                       (Integer.toString(dataMap.size() +
                                                         nEntries)),
                                       true);
                    prevKey = foundKey;
                    assertTrue(dataMap.get(foundKey) != null);
                    dataMap.remove(foundKey);
                }

                OperationStatus getFirst(StringDbt foundKey,
					 StringDbt foundData)
                    throws DatabaseException {

                    return cursor.getLast(foundKey, foundData,
                                          LockMode.DEFAULT);
                }

                OperationStatus getData(StringDbt foundKey,
					StringDbt foundData)
                    throws DatabaseException {

                    return cursor.getPrev(foundKey, foundData,
                                          LockMode.DEFAULT);
                }
            };
        dw.walkData();
        if (dataMap.size() > 0) {
            fail("dataMap still has entries");
        }
        assertTrue(dw.nEntries == N_KEYS);
    }

    /**
     * Insert N_KEYS data items into a tree.  Iterate through the tree in
     * ascending order.  After each element is retrieved, insert the next
     * highest key into the tree.  Ensure that the element just inserted is
     * returned by the cursor.  Ensure that the elements are returned in
     * ascending order.  Lather, rinse, repeat.
     */
    public void testLargeGetPutNextKeyForwardTraverse() 
        throws Throwable {

        try {
            initEnv(false);
            doLargeGetPutNextKeyForwardTraverse(N_KEYS);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for above.
     */
    private void doLargeGetPutNextKeyForwardTraverse(int nKeys)
        throws DatabaseException {

        Hashtable dataMap = new Hashtable();
        Hashtable addedDataMap = new Hashtable();
        doLargePut(dataMap, nKeys);

        DataWalker dw = new DataWalker(dataMap, addedDataMap) {
                void perData(String foundKey, String foundData) 
                    throws DatabaseException {

                    assertTrue(foundKey.compareTo(prevKey) >= 0);
                    if (addedDataMap.get(foundKey) == null) {
                        String newKey = nextKey(foundKey);
                        String newData =
                            Integer.toString(dataMap.size() + nEntries);
                        putAndVerifyCursor(cursor2,
                                           new StringDbt(newKey),
                                           new StringDbt(newData),
                                           true);
                        addedDataMap.put(newKey, newData);
                        prevKey = foundKey;
                        assertTrue(dataMap.get(foundKey) != null);
                        dataMap.remove(foundKey);
                    } else {
                        addedDataMap.remove(foundKey);
                    }
                }
            };
        dw.walkData();
        if (dataMap.size() > 0) {
            fail("dataMap still has entries");
        }
        if (addedDataMap.size() > 0) {
            System.out.println(addedDataMap);
            fail("addedDataMap still has entries");
        }
        assertTrue(dw.nEntries == nKeys * 2);
    }

    /**
     * Insert N_KEYS data items into a tree.  Iterate through the tree in
     * descending order.  After each element is retrieved, insert the next
     * lowest key into the tree.  Ensure that the element just inserted is
     * returned by the cursor.  Ensure that the elements are returned in
     * descending order.  Lather, rinse, repeat.
     */
    public void testLargeGetPutPrevKeyBackwardsTraverse() 
        throws Throwable {

        try {
            initEnv(false);
            doLargeGetPutPrevKeyBackwardsTraverse(N_KEYS);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for above.
     */
    private void doLargeGetPutPrevKeyBackwardsTraverse(int nKeys) 
        throws DatabaseException {

        Hashtable dataMap = new Hashtable();
        Hashtable addedDataMap = new Hashtable();
        doLargePut(dataMap, nKeys);

        DataWalker dw = new BackwardsDataWalker(dataMap, addedDataMap) {
                void perData(String foundKey, String foundData) 
                    throws DatabaseException {

		    if (!prevKey.equals("")) {
                        assertTrue(foundKey.compareTo(prevKey) <= 0);
                    }
                    if (addedDataMap.get(foundKey) == null) {
                        String newKey = prevKey(foundKey);
                        String newData =
                            Integer.toString(dataMap.size() + nEntries);
                        putAndVerifyCursor(cursor2,
                                           new StringDbt(newKey),
                                           new StringDbt(newData),
                                           true);
                        addedDataMap.put(newKey, newData);
                        prevKey = foundKey;
                        assertTrue(dataMap.get(foundKey) != null);
                        dataMap.remove(foundKey);
                    } else {
                        addedDataMap.remove(foundKey);
                    }
                }

                OperationStatus getFirst(StringDbt foundKey,
					 StringDbt foundData)
                    throws DatabaseException {

                    return cursor.getLast(foundKey, foundData,
                                          LockMode.DEFAULT);
                }

                OperationStatus getData(StringDbt foundKey,
					StringDbt foundData)
                    throws DatabaseException {

                    return cursor.getPrev(foundKey, foundData,
                                          LockMode.DEFAULT);
                }
            };
        dw.walkData();
        if (dataMap.size() > 0) {
            fail("dataMap still has entries");
        }
        if (addedDataMap.size() > 0) {
            System.out.println(addedDataMap);
            fail("addedDataMap still has entries");
        }
        assertTrue(dw.nEntries == nKeys * 2);
    }

    /**
     * Insert N_KEYS data items into a tree.  Iterate through the tree in
     * ascending order.  After each element is retrieved, insert the next
     * highest and next lowest keys into the tree.  Ensure that the next
     * highest element just inserted is returned by the cursor.  Ensure that
     * the elements are returned in ascending order.  Lather, rinse, repeat.
     */
    public void testLargeGetPutBothKeyForwardTraverse() 
        throws Throwable {

        try {
            initEnv(false);
            doLargeGetPutBothKeyForwardTraverse(N_KEYS);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for above.
     */
    private void doLargeGetPutBothKeyForwardTraverse(int nKeys)
        throws DatabaseException {

        Hashtable dataMap = new Hashtable();
        Hashtable addedDataMap = new Hashtable();
        doLargePut(dataMap, nKeys);

        DataWalker dw = new DataWalker(dataMap, addedDataMap) {
                void perData(String foundKey, String foundData) 
                    throws DatabaseException {

                    assertTrue(foundKey.compareTo(prevKey) >= 0);
                    if (addedDataMap.get(foundKey) == null) {
                        String newKey = nextKey(foundKey);
                        String newData =
                            Integer.toString(dataMap.size() + nEntries);
                        putAndVerifyCursor(cursor2,
                                           new StringDbt(newKey),
                                           new StringDbt(newData),
                                           true);
                        addedDataMap.put(newKey, newData);
                        newKey = prevKey(foundKey);
                        newData = Integer.toString(dataMap.size() + nEntries);
                        putAndVerifyCursor(cursor2,
                                           new StringDbt(newKey),
                                           new StringDbt(newData),
                                           true);
                        prevKey = foundKey;
                        assertTrue(dataMap.get(foundKey) != null);
                        dataMap.remove(foundKey);
                    } else {
                        addedDataMap.remove(foundKey);
                    }
                }
            };
        dw.walkData();
        if (dataMap.size() > 0) {
            fail("dataMap still has entries");
        }
        if (addedDataMap.size() > 0) {
            System.out.println(addedDataMap);
            fail("addedDataMap still has entries");
        }
        assertTrue(dw.nEntries == nKeys * 2);
    }

    /**
     * Insert N_KEYS data items into a tree.  Iterate through the tree in
     * descending order.  After each element is retrieved, insert the next
     * highest and next lowest keys into the tree.  Ensure that the next lowest
     * element just inserted is returned by the cursor.  Ensure that the
     * elements are returned in descending order.  Lather, rinse, repeat.
     */
    public void testLargeGetPutBothKeyBackwardsTraverse() 
        throws Throwable {

        try {
            initEnv(false);
            doLargeGetPutBothKeyBackwardsTraverse(N_KEYS);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for above.
     */
    private void doLargeGetPutBothKeyBackwardsTraverse(int nKeys)
        throws DatabaseException {

        Hashtable dataMap = new Hashtable();
        Hashtable addedDataMap = new Hashtable();
        doLargePut(dataMap, nKeys);

        DataWalker dw = new BackwardsDataWalker(dataMap, addedDataMap) {
                void perData(String foundKey, String foundData)
                    throws DatabaseException {

                    if (!prevKey.equals("")) {
                        assertTrue(foundKey.compareTo(prevKey) <= 0);
                    }
                    if (addedDataMap.get(foundKey) == null) {
                        String newKey = nextKey(foundKey);
                        String newData =
                            Integer.toString(dataMap.size() + nEntries);
                        putAndVerifyCursor(cursor2,
                                           new StringDbt(newKey),
                                           new StringDbt(newData),
                                           true);
                        newKey = prevKey(foundKey);
                        newData = Integer.toString(dataMap.size() + nEntries);
                        putAndVerifyCursor(cursor2,
                                           new StringDbt(newKey),
                                           new StringDbt(newData),
                                           true);
                        addedDataMap.put(newKey, newData);
                        prevKey = foundKey;
                        assertTrue(dataMap.get(foundKey) != null);
                        dataMap.remove(foundKey);
                    } else {
                        addedDataMap.remove(foundKey);
                    }
                }

                OperationStatus getFirst(StringDbt foundKey,
					 StringDbt foundData)
                    throws DatabaseException {

                    return cursor.getLast(foundKey, foundData,
                                          LockMode.DEFAULT);
                }

                OperationStatus getData(StringDbt foundKey,
					StringDbt foundData)
                    throws DatabaseException {

                    return cursor.getPrev(foundKey, foundData,
                                          LockMode.DEFAULT);
                }
            };
        dw.walkData();
        if (dataMap.size() > 0) {
            fail("dataMap still has entries");
        }
        if (addedDataMap.size() > 0) {
            System.out.println(addedDataMap);
            fail("addedDataMap still has entries");
        }
        assertTrue(dw.nEntries == nKeys * 2);
    }

    /**
     * Insert N_KEYS data items into a tree.  Iterate through the tree in
     * ascending order.  After each element is retrieved, insert a new random
     * key/data pair into the tree.  Ensure that the element just inserted is
     * returned by the cursor if it is greater than the current element.
     * Ensure that the elements are returned in ascending order.  Lather,
     * rinse, repeat.
     */
    public void testLargeGetPutRandomKeyForwardTraverse() 
        throws Throwable {

        try {
            initEnv(false);
            doLargeGetPutRandomKeyForwardTraverse(N_KEYS);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for above.
     */
    private void doLargeGetPutRandomKeyForwardTraverse(int nKeys)
        throws DatabaseException {

        Hashtable dataMap = new Hashtable();
        Hashtable addedDataMap = new Hashtable();
        doLargePut(dataMap, nKeys);

        DataWalker dw = new DataWalker(dataMap, addedDataMap) {
                void perData(String foundKey, String foundData)
                    throws DatabaseException {

                    assertTrue(foundKey.compareTo(prevKey) >= 0);
                    byte[] key = new byte[N_KEY_BYTES];
                    TestUtils.generateRandomAlphaBytes(key);
                    String newKey = new String(key);
                    String newData =
                        Integer.toString(dataMap.size() + nEntries);
                    putAndVerifyCursor(cursor2,
                                       new StringDbt(newKey),
                                       new StringDbt(newData),
                                       true);
                    if (newKey.compareTo(foundKey) > 0) {
                        addedDataMap.put(newKey, newData);
                        extraVisibleEntries++;
                    }
                    if (addedDataMap.get(foundKey) == null) {
                        prevKey = foundKey;
                        assertTrue(dataMap.get(foundKey) != null);
                        dataMap.remove(foundKey);
                    } else {
                        if (addedDataMap.remove(foundKey) == null) {
                            fail(foundKey + " not found in either datamap");
                        }
                    }
                }
            };
        dw.walkData();
        if (dataMap.size() > 0) {
            fail("dataMap still has entries");
        }
        if (addedDataMap.size() > 0) {
            System.out.println(addedDataMap);
            fail("addedDataMap still has entries");
        }
        assertTrue(dw.nEntries == nKeys + dw.extraVisibleEntries);
    }

    /**
     * Insert N_KEYS data items into a tree.  Iterate through the tree in
     * descending order.  After each element is retrieved, insert a new random
     * key/data pair into the tree.  Ensure that the element just inserted is
     * returned by the cursor if it is less than the current element.  Ensure
     * that the elements are returned in descending order.  Lather, rinse,
     * repeat.
     */
    public void testLargeGetPutRandomKeyBackwardsTraverse() 
        throws Throwable {

        try {
            initEnv(false);
            doLargeGetPutRandomKeyBackwardsTraverse(N_KEYS);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for above.
     */
    private void doLargeGetPutRandomKeyBackwardsTraverse(int nKeys)
        throws DatabaseException {

        Hashtable dataMap = new Hashtable();
        Hashtable addedDataMap = new Hashtable();
        doLargePut(dataMap, nKeys);

        DataWalker dw = new BackwardsDataWalker(dataMap, addedDataMap) {
                void perData(String foundKey, String foundData) 
                    throws DatabaseException {

                    if (!prevKey.equals("")) {
                        assertTrue(foundKey.compareTo(prevKey) <= 0);
                    }
                    byte[] key = new byte[N_KEY_BYTES];
                    TestUtils.generateRandomAlphaBytes(key);
                    String newKey = new String(key);
                    String newData =
                        Integer.toString(dataMap.size() + nEntries);
                    putAndVerifyCursor(cursor2,
                                       new StringDbt(newKey),
                                       new StringDbt(newData),
                                       true);
                    if (newKey.compareTo(foundKey) < 0) {
                        addedDataMap.put(newKey, newData);
                        extraVisibleEntries++;
                    }
                    if (addedDataMap.get(foundKey) == null) {
                        prevKey = foundKey;
                        assertTrue(dataMap.get(foundKey) != null);
                        dataMap.remove(foundKey);
                    } else {
                        if (addedDataMap.remove(foundKey) == null) {
                            fail(foundKey + " not found in either datamap");
                        }
                    }
                }

                OperationStatus getFirst(StringDbt foundKey,
					 StringDbt foundData)
                    throws DatabaseException {

                    return cursor.getLast(foundKey, foundData,
                                          LockMode.DEFAULT);
                }

                OperationStatus getData(StringDbt foundKey,
					StringDbt foundData)
                    throws DatabaseException {

                    return cursor.getPrev(foundKey, foundData,
                                          LockMode.DEFAULT);
                }
            };
        dw.walkData();
        if (dataMap.size() > 0) {
            fail("dataMap still has entries");
        }
        if (addedDataMap.size() > 0) {
            System.out.println(addedDataMap);
            fail("addedDataMap still has entries");
        }
        assertTrue(dw.nEntries == nKeys + dw.extraVisibleEntries);
    }

    /**
     * Insert N_KEYS data items into a tree.  Set a btreeComparison function.
     * Iterate through the tree in ascending order.  Ensure that the elements
     * are returned in ascending order.
     */
    public void testLargeGetForwardTraverseWithNormalComparisonFunction() 
        throws Throwable {

        try {
            tearDown();
            btreeComparisonFunction = btreeComparator;
            setUp();
            initEnv(false);
            doLargeGetForwardTraverseWithNormalComparisonFunction();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for above.
     */
    private void doLargeGetForwardTraverseWithNormalComparisonFunction()
        throws DatabaseException {

        Hashtable dataMap = new Hashtable();
        doLargePut(dataMap, N_KEYS);

        DataWalker dw = new DataWalker(dataMap) {
                void perData(String foundKey, String foundData)
                    throws DatabaseException {

                    assertTrue(foundKey.compareTo(prevKey) >= 0);
                    prevKey = foundKey;
                    assertTrue(dataMap.get(foundKey) != null);
                    dataMap.remove(foundKey);
                }
            };
        dw.walkData();
        if (dataMap.size() > 0) {
            fail("dataMap still has entries");
        }
        assertTrue(dw.nEntries == N_KEYS);
    }

    /**
     * Insert N_KEYS data items into a tree.  Set a reverse order
     * btreeComparison function. Iterate through the tree in ascending order.
     * Ensure that the elements are returned in ascending order.
     */
    public void testLargeGetForwardTraverseWithReverseComparisonFunction() 
        throws Throwable {

        try {
            tearDown();
            btreeComparisonFunction = reverseBtreeComparator;
            setUp();
            initEnv(false);
            doLargeGetForwardTraverseWithReverseComparisonFunction();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * Helper routine for above.
     */
    private void doLargeGetForwardTraverseWithReverseComparisonFunction()
        throws DatabaseException {

        Hashtable dataMap = new Hashtable();
        doLargePut(dataMap, N_KEYS);

        DataWalker dw = new DataWalker(dataMap) {
                void perData(String foundKey, String foundData)
                    throws DatabaseException {

                    if (prevKey.length() != 0) {
                        assertTrue(foundKey.compareTo(prevKey) <= 0);
                    }
                    prevKey = foundKey;
                    assertTrue(dataMap.get(foundKey) != null);
                    dataMap.remove(foundKey);
                }
            };
        dw.walkData();
        if (dataMap.size() > 0) {
            fail("dataMap still has entries");
        }
        assertTrue(dw.nEntries == N_KEYS);
    }

    public void testIllegalArgs()
        throws Throwable {

        try {
            initEnv(false);
            /* Put some data so that we can get a cursor. */
            doSimpleCursorPuts();

            DataWalker dw = new DataWalker(simpleDataMap) {
                    void perData(String foundKey, String foundData) {

                        /* getCurrent() */
                        try {
                            cursor.getCurrent(new StringDbt(""),
                                              null,
                                              LockMode.DEFAULT);
                            fail("didn't throw NullPointerException");
                        } catch (NullPointerException IAE) {
                        } catch (DatabaseException DBE) {
                            fail("threw DatabaseException not " +
				 "NullPointerException");
                        }

                        try {
                            cursor.getCurrent(null,
                                              new StringDbt(""),
                                              LockMode.DEFAULT);
                            fail("didn't throw NullPointerException");
                        } catch (NullPointerException IAE) {
                        } catch (DatabaseException DBE) {
                            fail("threw DatabaseException not " +
				 "NullPointerException");
                        }

                        /* getFirst() */
                        try {
                            cursor.getFirst(new StringDbt(""),
                                            null,
                                            LockMode.DEFAULT);
                            fail("didn't throw NullPointerException");
                        } catch (NullPointerException IAE) {
                        } catch (DatabaseException DBE) {
                            fail("threw DatabaseException not " +
				 "NullPointerException");
                        }

                        try {
                            cursor.getFirst(null,
                                            new StringDbt(""),
                                            LockMode.DEFAULT);
                            fail("didn't throw NullPointerException");
                        } catch (NullPointerException IAE) {
                        } catch (DatabaseException DBE) {
                            fail("threw DatabaseException not " +
				 "NullPointerException");
                        }

                        /* getNext(), getPrev, getNextDup,
                           getNextNoDup, getPrevNoDup */
                        try {
                            cursor.getNext(new StringDbt(""),
                                           null,
                                           LockMode.DEFAULT);
                            fail("didn't throw NullPointerException");
                        } catch (NullPointerException IAE) {
                        } catch (DatabaseException DBE) {
                            fail("threw DatabaseException not " +
				 "NullPointerException");
                        }

                        try {
                            cursor.getNext(null,
                                           new StringDbt(""),
                                           LockMode.DEFAULT);
                            fail("didn't throw NullPointerException");
                        } catch (NullPointerException IAE) {
                        } catch (DatabaseException DBE) {
                            fail("threw DatabaseException not " +
				 "NullPointerException");
                        }

                        /* putXXX() */
                        try {
                            cursor.put(new StringDbt(""), null);
                            fail("didn't throw NullPointerException");
                        } catch (NullPointerException IAE) {
                        } catch (DatabaseException DBE) {
                            fail("threw DatabaseException not " +
				 "NullPointerException");
                        }

                        try {
                            cursor.put(null, new StringDbt(""));
                            fail("didn't throw NullPointerException");
                        } catch (NullPointerException IAE) {
                        } catch (DatabaseException DBE) {
                            fail("threw DatabaseException not " +
				 "NullPointerException");
                        }
                    }
                };
            dw.walkData();
            assertTrue(dw.nEntries == simpleKeyStrings.length);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public void testCursorOutOfBoundsBackwards() 
        throws Throwable {

        try {
            initEnv(false);
            int count = 0;
            doSimpleCursorPuts();

            StringDbt foundKey = new StringDbt();
            StringDbt foundData = new StringDbt();
            OperationStatus status;
            status = cursor.getFirst(foundKey, foundData, LockMode.DEFAULT);

            assertEquals(OperationStatus.SUCCESS, status);
            assertEquals("aaa", foundKey.getString());
            assertEquals("four", foundData.getString());

            status = cursor.getPrev(foundKey, foundData, LockMode.DEFAULT);

            assertEquals(OperationStatus.NOTFOUND, status);

            status = cursor.getNext(foundKey, foundData, LockMode.DEFAULT);

            assertEquals(OperationStatus.SUCCESS, status);
            assertEquals("bar", foundKey.getString());
            assertEquals("two", foundData.getString());
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public void testCursorOutOfBoundsForwards() 
        throws Throwable {

        try {
            initEnv(false);
            int count = 0;
            doSimpleCursorPuts();

            StringDbt foundKey = new StringDbt();
            StringDbt foundData = new StringDbt();
            OperationStatus status;
            status = cursor.getLast(foundKey, foundData, LockMode.DEFAULT);

            assertEquals(OperationStatus.SUCCESS, status);
            assertEquals("quux", foundKey.getString());
            assertEquals("seven", foundData.getString());

            status = cursor.getNext(foundKey, foundData, LockMode.DEFAULT);
            assertEquals(OperationStatus.NOTFOUND, status);

            status = cursor.getPrev(foundKey, foundData, LockMode.DEFAULT);

            assertEquals(OperationStatus.SUCCESS, status);
            assertEquals("mumble", foundKey.getString());
            assertEquals("eight", foundData.getString());
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public void testTwiceClosedCursor()
	throws Throwable {

        try {
            initEnv(false);
            doSimpleCursorPuts();
            Cursor cursor = exampleDb.openCursor(null, null);
            cursor.close();
            try {
                cursor.close();
                fail("didn't catch DatabaseException for twice closed cursor");
            } catch (DatabaseException DBE) {
            }
            try {
                cursor.put
                    (new StringDbt("bogus"), new StringDbt("thingy"));
                fail("didn't catch DatabaseException for re-use of cursor");
            } catch (DatabaseException DBE) {
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public void testTreeSplittingWithDeletedIdKey()
	throws Throwable {

	treeSplittingWithDeletedIdKeyWorker();
    }

    public void testTreeSplittingWithDeletedIdKeyWithUserComparison()
	throws Throwable {

	tearDown();
	btreeComparisonFunction = btreeComparator;
	setUp();
	treeSplittingWithDeletedIdKeyWorker();
    }

    static private Comparator btreeComparator = new BtreeComparator();

    static private Comparator reverseBtreeComparator =
        new ReverseBtreeComparator();

    private void treeSplittingWithDeletedIdKeyWorker()
	throws Throwable {

	initEnv(false);
	StringDbt data = new StringDbt("data");

	Cursor cursor = exampleDb.openCursor(null, null);
	cursor.put(new StringDbt("AGPFX"), data);
	cursor.put(new StringDbt("AHHHH"), data);
	cursor.put(new StringDbt("AIIII"), data);
	cursor.put(new StringDbt("AAAAA"), data);
	cursor.put(new StringDbt("AABBB"), data);
	cursor.put(new StringDbt("AACCC"), data);
	cursor.close();
	exampleDb.delete(null, new StringDbt("AGPFX"));
	exampleEnv.compress();
	cursor = exampleDb.openCursor(null, null);
	cursor.put(new StringDbt("AAAAB"), data);
	cursor.put(new StringDbt("AAAAC"), data);
	cursor.close();
	validateDatabase();
    }
}

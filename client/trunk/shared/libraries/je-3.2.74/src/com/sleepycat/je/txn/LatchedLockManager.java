/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2008 Oracle.  All rights reserved.
 *
 * $Id: LatchedLockManager.java,v 1.11.2.5 2008/01/07 15:14:17 cwl Exp $
 */

package com.sleepycat.je.txn;

import java.util.Set;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DeadlockException;
import com.sleepycat.je.LockStats;
import com.sleepycat.je.dbi.DatabaseImpl;
import com.sleepycat.je.dbi.EnvironmentImpl;
import com.sleepycat.je.dbi.MemoryBudget;
import com.sleepycat.je.latch.Latch;

/**
 * LatchedLockManager uses latches to implement its critical sections.
 */
public class LatchedLockManager extends LockManager {

    public LatchedLockManager(EnvironmentImpl envImpl)
    	throws DatabaseException {
        super(envImpl);
    }

    /**
     * @see LockManager#lookupLock
     */
    protected Lock lookupLock(Long nodeId)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            return lookupLockInternal(nodeId, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#attemptLock
     */
    protected LockAttemptResult attemptLock(Long nodeId,
                                            Locker locker,
                                            LockType type,
                                            boolean nonBlockingRequest)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            return attemptLockInternal(nodeId, locker, type,
                                       nonBlockingRequest, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#makeTimeoutMsg
     */
    protected DeadlockException makeTimeoutMsg(String lockOrTxn,
					       Locker locker,
					       long nodeId,
					       LockType type,
					       LockGrantType grantType,
					       Lock useLock,
					       long timeout,
					       long start,
					       long now,
					       DatabaseImpl database)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            return makeTimeoutMsgInternal(lockOrTxn, locker,
                                          nodeId, type, grantType,
                                          useLock, timeout, start, now,
					  database);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#releaseAndNotifyTargets
     */
    protected Set releaseAndFindNotifyTargets(long nodeId,
                                              Locker locker)
        throws DatabaseException {

	long nid = nodeId;
	int lockTableIndex = getLockTableIndex(nid);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            return releaseAndFindNotifyTargetsInternal
		(nodeId, locker, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#transfer
     */
    void transfer(long nodeId,
                  Locker owningLocker,
                  Locker destLocker,
                  boolean demoteToRead)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            transferInternal(nodeId, owningLocker, destLocker,
			     demoteToRead, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#transferMultiple
     */
    void transferMultiple(long nodeId,
                          Locker owningLocker,
                          Locker [] destLockers)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            transferMultipleInternal(nodeId, owningLocker,
				     destLockers, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#demote
     */
    void demote(long nodeId, Locker locker)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            demoteInternal(nodeId, locker, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#isLocked
     */
    boolean isLocked(Long nodeId)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            return isLockedInternal(nodeId, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#isOwner
     */
    boolean isOwner(Long nodeId, Locker locker, LockType type)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            return isOwnerInternal(nodeId, locker, type, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#isWaiter
     */
    boolean isWaiter(Long nodeId, Locker locker)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            return isWaiterInternal(nodeId, locker, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#nWaiters
     */
    int nWaiters(Long nodeId)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            return nWaitersInternal(nodeId, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#nOwners
     */
    int nOwners(Long nodeId)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            return nOwnersInternal(nodeId, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#getWriterOwnerLocker
     */
    Locker getWriteOwnerLocker(Long nodeId)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            return getWriteOwnerLockerInternal(nodeId, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#validateOwnership
     */
    protected boolean validateOwnership(Long nodeId,
                                        Locker locker,
                                        LockType type,
                                        boolean flushFromWaiters,
					MemoryBudget mb)
        throws DatabaseException {

	int lockTableIndex = getLockTableIndex(nodeId);
	Latch latch = lockTableLatches[lockTableIndex];
        latch.acquire();
        try {
            return validateOwnershipInternal
		(nodeId, locker, type, flushFromWaiters, mb, lockTableIndex);
        } finally {
            latch.release();
        }
    }

    /**
     * @see LockManager#dumpLockTable
     */
    protected void dumpLockTable(LockStats stats)
        throws DatabaseException {

	for (int i = 0; i < nLockTables; i++) {
	    lockTableLatches[i].acquire();
	    try {
		dumpLockTableInternal(stats, i);
	    } finally {
		lockTableLatches[i].release();
	    }
	}
    }
}

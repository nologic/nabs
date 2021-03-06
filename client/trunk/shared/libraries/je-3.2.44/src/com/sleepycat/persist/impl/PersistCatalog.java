/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2002,2007 Oracle.  All rights reserved.
 *
 * $Id: PersistCatalog.java,v 1.33.2.3 2007/06/14 13:06:05 mark Exp $
 */

package com.sleepycat.persist.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.evolve.DeletedClassException;
import com.sleepycat.persist.evolve.IncompatibleClassException;
import com.sleepycat.persist.evolve.Mutations;
import com.sleepycat.persist.evolve.Renamer;
import com.sleepycat.persist.model.AnnotationModel;
import com.sleepycat.persist.model.ClassMetadata;
import com.sleepycat.persist.model.EntityMetadata;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.raw.RawObject;
import com.sleepycat.util.RuntimeExceptionWrapper;

/**
 * The catalog of class formats for a store, along with its associated model
 * and mutations.
 *
 * @author Mark Hayes
 */
public class PersistCatalog implements Catalog {

    /**
     * Key to Data record in the catalog database.  In the JE 3.0.12 beta
     * version the formatList record is stored under this key and is converted
     * to a Data object when it is read.
     */
    private static final byte[] DATA_KEY = getIntBytes(-1);

    /**
     * Key to a JE 3.0.12 beta version mutations record in the catalog
     * database.  This record is no longer used because mutations are stored in
     * the Data record and is deleted when the beta version is detected.
     */
    private static final byte[] BETA_MUTATIONS_KEY = getIntBytes(-2);

    private static byte[] getIntBytes(int val) {
        DatabaseEntry entry = new DatabaseEntry();
        IntegerBinding.intToEntry(val, entry);
        assert entry.getSize() == 4 && entry.getData().length == 4;
        return entry.getData();
    }

    /**
     * Set by unit tests.
     */
    public static boolean expectNoClassChanges;

    /**
     * The object stored under DATA_KEY in the catalog database.
     */
    private static class Data implements Serializable {
        List<Format> formatList;
        Mutations mutations;
        transient boolean betaVersion;
    }

    /**
     * A list of all formats indexed by formatId.  Element zero is unused and
     * null, since IDs start at one; this avoids adjusting the ID to index the
     * list.  Some elements are null to account for predefined IDs that are not
     * used.
     *
     * <p>This field, like formatMap, is volatile because it is reassigned
     * when dynamically adding new formats.  See {@link getFormat(Class)}.</p>
     */
    private volatile List<Format> formatList;

    /**
     * A map of the current/live formats in formatList, indexed by class name.
     *
     * <p>This field, like formatList, is volatile because it is reassigned
     * when dynamically adding new formats.  See {@link getFormat(Class)}.</p>
     */
    private volatile Map<String,Format> formatMap;

    /**
     * A map of the latest formats (includes deleted formats) in formatList,
     * indexed by class name.
     *
     * <p>This field, like formatMap, is volatile because it is reassigned
     * when dynamically adding new formats.  See {@link getFormat(Class)}.</p>
     */
    private volatile Map<String,Format> latestFormatMap;

    /**
     * A temporary map of proxied class name to proxy class name.  Used during
     * catalog creation, and then set to null.  This map is used to force proxy
     * formats to be created prior to proxied formats. [#14665]
     */
    private Map<String,String> proxyClassMap;

    private boolean rawAccess;
    private EntityModel model;
    private Mutations mutations;
    private Database db;
    private int openCount;

    /**
     * The Store is normally present but may be null in unit tests (for
     * example, BindingTest).
     */
    private Store store;

    /**
     * Creates a new catalog, opening the database and reading it from a given
     * catalog database if it already exists.  All predefined formats and
     * formats for the given model are added.  For modified classes, old
     * formats are defined based on the rules for compatible class changes and
     * the given mutations.  If any format is changed or added, and the
     * database is not read-only, write the initialized catalog to the
     * database.
     */
    public PersistCatalog(Transaction txn,
                          Environment env,
                          String storePrefix,
                          String dbName,
                          DatabaseConfig dbConfig,
                          EntityModel modelParam,
                          Mutations mutationsParam,
                          boolean rawAccess,
                          Store store)
        throws DatabaseException {

        this.rawAccess = rawAccess;
        this.store = store;
        db = env.openDatabase(txn, dbName, dbConfig);
        openCount = 1;
        boolean success = false;
        try {
            Data catalogData = readData(txn);
            mutations = catalogData.mutations;
            if (mutations == null) {
                mutations = new Mutations();
            }

            /*
             * When the beta version is detected, force a re-write of the
             * catalog and disallow class changes.  This brings the catalog up
             * to date so that evolution can proceed correctly from then on.
             */
            boolean forceWriteData = catalogData.betaVersion;
            boolean disallowClassChanges = catalogData.betaVersion;

            /*
             * Store the given mutations if they are different from the stored
             * mutations, and force evolution to apply the new mutations.
             */
            boolean forceEvolution = false;
            if (mutationsParam != null &&
                !mutations.equals(mutationsParam)) {
                mutations = mutationsParam;
                forceWriteData = true;
                forceEvolution = true;
            }

            /* Get the existing format list, or copy it from SimpleCatalog. */
            formatList = catalogData.formatList;
            if (formatList == null) {
                formatList = SimpleCatalog.copyFormatList();

                /*
                 * Special cases: Object and Number are predefined but are not
                 * simple types.
                 */
                Format format = new NonPersistentFormat(Object.class);
                format.setId(Format.ID_OBJECT);
                formatList.set(Format.ID_OBJECT, format);
                format = new NonPersistentFormat(Number.class);
                format.setId(Format.ID_NUMBER);
                formatList.set(Format.ID_NUMBER, format);
            } else {
                if (SimpleCatalog.copyMissingFormats(formatList)) {
                    forceWriteData = true;
                }
            }

            /* Special handling for JE 3.0.12 beta formats. */
            if (catalogData.betaVersion) {
                Map<String,Format> formatMap = new HashMap<String,Format>();
                for (Format format : formatList) {
                    if (format != null) {
                        formatMap.put(format.getClassName(), format);
                    }
                }
                for (Format format : formatList) {
                    if (format != null) {
                        format.migrateFromBeta(formatMap);
                    }
                }
            }

            /*
             * If we should not use the current model, initialize the stored
             * model and return.
             */
            formatMap = new HashMap<String,Format>(formatList.size());
            latestFormatMap = new HashMap<String,Format>(formatList.size());
            if (rawAccess) {
                for (Format format : formatList) {
                    if (format != null) {
                        String name = format.getClassName();
                        if (format.isCurrentVersion()) {
                            formatMap.put(name, format);
                        }
                        if (format == format.getLatestVersion()) {
                            latestFormatMap.put(name, format);
                        }
                    }
                }
                for (Format format : formatList) {
                    if (format != null) {
                        format.initializeIfNeeded(this);
                    }
                }
                model = new StoredModel(this);
                success = true;
                return;
            }

            /*
             * We are opening a store that uses the current model. Default to
             * the AnnotationModel if no model is specified.
             */
            if (modelParam != null) {
                model = modelParam;
            } else {
                model = new AnnotationModel();
            }

            /*
             * Add all predefined (simple) formats to the format map.  The
             * current version of other formats will be added below.
             */
            for (int i = 0; i <= Format.ID_PREDEFINED; i += 1) {
                Format simpleFormat = formatList.get(i);
                if (simpleFormat != null) {
                    formatMap.put(simpleFormat.getClassName(), simpleFormat);
                }
            }

            /*
             * Known classes are those explicitly registered by the user via
             * the model, plus the predefined proxy classes.
             */
            List<String> knownClasses =
                new ArrayList<String>(model.getKnownClasses());
            addPredefinedProxies(knownClasses);

            /*
             * Create a temporary map of proxied class name to proxy class
             * name, using all known formats and classes.  This map is used to
             * force proxy formats to be created prior to proxied formats.
             * [#14665]
             */
            proxyClassMap = new HashMap<String,String>();
            for (Format oldFormat : formatList) {
                if (oldFormat == null || Format.isPredefined(oldFormat)) {
                    continue;
                }
                String oldName = oldFormat.getClassName();
                Renamer renamer = mutations.getRenamer
                    (oldName, oldFormat.getVersion(), null);
                String newName =
                    (renamer != null) ? renamer.getNewName() : oldName;
                addProxiedClass(newName);
            }
            for (String className : knownClasses) {
                addProxiedClass(className);
            }

            /*
             * Add known formats from the model and the predefined proxies.
             * In general, classes will not be present in an AnnotationModel
             * until an instance is stored, in which case an old format exists.
             * However, registered proxy classes are an exception and must be
             * added in advance.  And the user may choose to register new
             * classes in advance.  The more formats we define in advance, the
             * less times we have to write to the catalog database.
             */
            Map<String,Format> newFormats = new HashMap<String,Format>();
            for (String className : knownClasses) {
                createFormat(className, newFormats);
            }

            /*
             * Perform class evolution for all old formats, and throw an
             * exception that contains the messages for all of the errors in
             * mutations or in the definition of new classes.
             */
            Evolver evolver = new Evolver
                (this, storePrefix, mutations, newFormats, forceEvolution,
                 disallowClassChanges);
            for (Format oldFormat : formatList) {
                if (oldFormat == null || Format.isPredefined(oldFormat)) {
                    continue;
                }
                if (oldFormat.isEntity()) {
                    evolver.evolveFormat(oldFormat);
                } else {
                    evolver.addNonEntityFormat(oldFormat);
                }
            }
            evolver.finishEvolution();
            String errors = evolver.getErrors();
            if (errors != null) {
                throw new IncompatibleClassException(errors);
            }

            /*
             * Add the new formats remaining.  New formats that are equal to
             * old formats were removed from the newFormats map above.
             */
            for (Format newFormat : newFormats.values()) {
                addFormat(newFormat);
            }

            /* Initialize all formats. */
            for (Format format : formatList) {
                if (format != null) {
                    format.initializeIfNeeded(this);
                    if (format == format.getLatestVersion()) {
                        latestFormatMap.put(format.getClassName(), format);
                    }
                }
            }

            boolean needWrite =
                 newFormats.size() > 0 ||
                 evolver.areFormatsChanged();

            /* For unit testing. */
            if (expectNoClassChanges && needWrite) {
                throw new IllegalStateException
                    ("Unexpected changes " +
                     " newFormats.size=" + newFormats.size() +
                     " areFormatsChanged=" + evolver.areFormatsChanged());
            }

            /* Write the catalog if anything changed. */
            if ((needWrite || forceWriteData) &&
                !db.getConfig().getReadOnly()) {

                /*
                 * Only rename/remove databases if we are going to update the
                 * catalog to reflect those class changes.
                 */
                evolver.renameAndRemoveDatabases(env, txn);

                /*
                 * Note that we use the Data object that was read above, and
                 * the Data.betaVersion field determines whether to delete the
                 * old mutations record.
                 */
                catalogData.formatList = formatList;
                catalogData.mutations = mutations;
                writeData(txn, catalogData);
            } else if (forceWriteData) {
                throw new IllegalArgumentException
                    ("When an upgrade is required the store may not be " +
                     "opened read-only");
            }

            /* proxyClassMap was only needed for the duration of this ctor. */
            proxyClassMap = null;

            success = true;
        } finally {
            if (!success) {
                close();
            }
        }
    }

    public void getEntityFormats(Collection<Format> entityFormats) {
        for (Format format : formatMap.values()) {
            if (format.isEntity()) {
                entityFormats.add(format);
            }
        }
    }

    private void addProxiedClass(String className) {
        ClassMetadata metadata = model.getClassMetadata(className);
        if (metadata != null) {
            String proxiedClassName = metadata.getProxiedClassName();
            if (proxiedClassName != null) {
                proxyClassMap.put(proxiedClassName, className);
            }
        }
    }

    private void addPredefinedProxies(List<String> knownClasses) {
        knownClasses.add(CollectionProxy.ArrayListProxy.class.getName());
        knownClasses.add(CollectionProxy.LinkedListProxy.class.getName());
        knownClasses.add(CollectionProxy.HashSetProxy.class.getName());
        knownClasses.add(CollectionProxy.TreeSetProxy.class.getName());
        knownClasses.add(MapProxy.HashMapProxy.class.getName());
        knownClasses.add(MapProxy.TreeMapProxy.class.getName());
    }

    /**
     * Returns a map from format to a set of its superclass formats.  The
     * format for simple types, enums and class Object are not included.  Only
     * complex types have superclass formats as defined by
     * Format.getSuperFormat.
     */
    Map<Format,Set<Format>> getSubclassMap() {
        Map<Format,Set<Format>> subclassMap =
            new HashMap<Format,Set<Format>>();
        for (Format format : formatList) {
            if (format == null || Format.isPredefined(format)) {
                continue;
            }
            Format superFormat = format.getSuperFormat();
            if (superFormat != null) {
                Set<Format> subclass = subclassMap.get(superFormat);
                if (subclass == null) {
                    subclass = new HashSet<Format>();
                    subclassMap.put(superFormat, subclass);
                }
                subclass.add(format);
            }
        }
        return subclassMap;
    }

    /**
     * Returns the model parameter, default model or stored model.
     */
    public EntityModel getResolvedModel() {
        return model;
    }

    /**
     * Increments the reference count for a catalog that is already open.
     */
    public void openExisting() {
        openCount += 1;
    }

    /**
     * Decrements the reference count and closes the catalog DB when it reaches
     * zero.  Returns true if the database was closed or false if the reference
     * count is still non-zero and the database was left open.
     */
    public boolean close()
        throws DatabaseException {

        if (openCount == 0) {
            throw new IllegalStateException("Catalog is not open");
        } else {
            openCount -= 1;
            if (openCount == 0) {
                Database dbToClose = db;
                db = null;
                dbToClose.close();
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Returns the current merged mutations.
     */
    public Mutations getMutations() {
        return mutations;
    }

    /**
     * Convenience method that gets the class for the given class name and
     * calls createFormat with the class object.
     */
    public Format createFormat(String clsName, Map<String,Format> newFormats) {
        Class type;
        try {
            type = SimpleCatalog.classForName(clsName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException
                ("Class does not exist: " + clsName);
        }
        return createFormat(type, newFormats);
    }

    /**
     * If the given class format is not already present in the given map,
     * creates an uninitialized format, adds it to the map, and also collects
     * related formats in the map.
     */
    public Format createFormat(Class type, Map<String,Format> newFormats) {
        /* Return a new or existing format for this class. */
        String className = type.getName();
        Format format = newFormats.get(className);
        if (format != null) {
            return format;
        }
        format = formatMap.get(className);
        if (format != null) {
            return format;
        }
        /* Simple types are predefined. */
        assert !SimpleCatalog.isSimpleType(type) : className;
        /* Create format of the appropriate type. */
        String proxyClassName = null;
        if (proxyClassMap != null) {
            proxyClassName = proxyClassMap.get(className);
        }
        if (proxyClassName != null) {
            format = new ProxiedFormat(type, proxyClassName);
        } else if (type.isArray()) {
            format = type.getComponentType().isPrimitive() ?
                (new PrimitiveArrayFormat(type)) :
                (new ObjectArrayFormat(type));
        } else if (type.isEnum()) {
            format = new EnumFormat(type);
        } else if (type == Object.class || type.isInterface()) {
            format = new NonPersistentFormat(type);
        } else {
            ClassMetadata metadata = model.getClassMetadata(className);
            if (metadata == null) {
                throw new IllegalArgumentException
                    ("Class could not be loaded or is not persistent: " +
                     className);
            }
            if (metadata.getCompositeKeyFields() != null &&
                (metadata.getPrimaryKey() != null ||
                 metadata.getSecondaryKeys() != null)) {
                throw new IllegalArgumentException
                    ("A composite key class may not have primary or" +
                     " secondary key fields: " + type.getName());
            }
            try {
                type.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException
                    ("No default constructor: " + type.getName(), e);
            }
            if (metadata.getCompositeKeyFields() != null) {
                format = new CompositeKeyFormat
                    (type, metadata, metadata.getCompositeKeyFields());
            } else {
                EntityMetadata entityMetadata =
                    model.getEntityMetadata(className);
                format = new ComplexFormat(type, metadata, entityMetadata);
            }
        }
        /* Collect new format along with any related new formats. */
        newFormats.put(className, format);
        format.collectRelatedFormats(this, newFormats);

        return format;
    }

    /**
     * Adds a format and makes it the current format for the class.
     */
    private void addFormat(Format format) {
        addFormat(format, formatList, formatMap);
    }

    /**
     * Adds a format to the given the format collections, for use when
     * dynamically adding formats.
     */
    private void addFormat(Format format,
                           List<Format> list,
                           Map<String,Format> map) {
        format.setId(list.size());
        list.add(format);
        map.put(format.getClassName(), format);
    }
    
    /**
     * Installs an existing format when no evolution is needed, i.e, when the
     * new and old formats are identical.
     */
    void useExistingFormat(Format oldFormat) {
        assert oldFormat.isCurrentVersion();
        formatMap.put(oldFormat.getClassName(), oldFormat);
    }

    /**
     * Returns a set of all persistent (non-simple type) class names.
     */
    Set<String> getModelClasses() {
        Set<String> classes = new HashSet<String>();
        for (Format format : formatMap.values()) {
            if (format.isModelClass()) {
                classes.add(format.getClassName());
            }
        }
        return classes;
    }

    public Format getFormat(int formatId) {
        try {
            Format format = formatList.get(formatId);
            if (format == null) {
                throw new DeletedClassException
                    ("Format does not exist: " + formatId);
            }
            return format;
        } catch (NoSuchElementException e) {
            throw new DeletedClassException
                ("Format does not exist: " + formatId);
        }
    }


    /**
     * Get a format for a given class, creating it if it does not exist.
     *
     * <p>This method is called for top level entity instances by
     * PersistEntityBinding.  When a new entity subclass format is added we
     * call Store.openSecondaryIndexes so that previously unknown secondary
     * databases can be created, before storing the entity.  We do this here
     * while not holding a synchronization mutex, not in addNewFormat, to avoid
     * deadlocks. openSecondaryIndexes synchronizes on the Store. [#15247]</p>
     */
    public Format getFormat(Class cls) {
        Format format = formatMap.get(cls.getName());
        if (format == null) {
            if (model != null) {
                format = addNewFormat(cls);
                /* Detect and handle new entity subclass. [#15247] */
                if (store != null) {
                    Format entityFormat = format.getEntityFormat();
                    if (entityFormat != null && entityFormat != format) {
                        try {
                            store.openSecondaryIndexes
                                (null, entityFormat.getEntityMetadata(), null);
                        } catch (DatabaseException e) {
                            throw new RuntimeExceptionWrapper(e);
                        }
                    }
                }
            }
            if (format == null) {
                throw new IllegalArgumentException
                    ("Class is not persistent: " + cls.getName());
            }
        }
        return format;
    }

    public Format getFormat(String className) {
        return formatMap.get(className);
    }

    public Format getLatestVersion(String className) {
        return latestFormatMap.get(className);
    }

    /**
     * Adds a format for a new class.  Returns the format added for the given
     * class, or throws an exception if the given class is not persistent.
     *
     * <p>This method uses a copy-on-write technique to add new formats without
     * impacting other threads.</p>
     */
    private synchronized Format addNewFormat(Class cls) {

        /*
         * After synchronizing, check whether another thread has added the
         * format needed.  Note that this is not the double-check technique
         * because the formatMap field is volatile and is not itself checked
         * for null.  (The double-check technique is known to be flawed in
         * Java.)
         */
        Format format = formatMap.get(cls.getName());
        if (format != null) {
            return format;
        }

        /* Copy the read-only format collections. */
        List<Format> newFormatList = new ArrayList<Format>(formatList);
        Map<String,Format> newFormatMap =
            new HashMap<String,Format>(formatMap);
        Map<String,Format> newLatestFormatMap =
            new HashMap<String,Format>(latestFormatMap);

        /* Add the new format and all related new formats. */
        Map<String,Format> newFormats = new HashMap<String,Format>();
        format = createFormat(cls, newFormats);
        for (Format newFormat : newFormats.values()) {
            addFormat(newFormat, newFormatList, newFormatMap);
        }

        /*
         * Initialize new formats using a read-only catalog because we can't
         * update this catalog until after we store it (below).
         */
        Catalog newFormatCatalog =
            new ReadOnlyCatalog(newFormatList, newFormatMap);
        for (Format newFormat : newFormats.values()) {
            newFormat.initializeIfNeeded(newFormatCatalog);
            newLatestFormatMap.put(newFormat.getClassName(), newFormat);
        }

        /*
         * Write the updated catalog using auto-commit, then assign the new
         * collections.  The database write must occur before the collections
         * are used, since a format must be persistent before it can be
         * referenced by a data record.
         */
        try {
            Data catalogData = new Data();
            catalogData.formatList = newFormatList;
            catalogData.mutations = mutations;
            writeData(null, catalogData);
        } catch (DatabaseException e) {
            throw new RuntimeExceptionWrapper(e);
        }
        formatList = newFormatList;
        formatMap = newFormatMap;
        latestFormatMap = newLatestFormatMap;

        return format;
    }

    /**
     * Used to write the catalog when a format has been changed, for example,
     * when Store.evolve has updated a Format's EvolveNeeded property.  Uses
     * auto-commit.
     */
    public synchronized void flush()
        throws DatabaseException {

        Data catalogData = new Data();
        catalogData.formatList = formatList;
        catalogData.mutations = mutations;
        writeData(null, catalogData);
    }

    /**
     * Reads catalog Data, converting old versions as necessary.  An empty
     * Data object is returned if no catalog data currently exists.  Null is
     * never returned.
     */
    private Data readData(Transaction txn)
        throws DatabaseException {
        
        Data catalogData;
        DatabaseEntry key = new DatabaseEntry(DATA_KEY);
        DatabaseEntry data = new DatabaseEntry();
        OperationStatus status = db.get(txn, key, data, null);
        if (status == OperationStatus.SUCCESS) {
            ByteArrayInputStream bais = new ByteArrayInputStream
                (data.getData(), data.getOffset(), data.getSize());
            try {
                ObjectInputStream ois = new ObjectInputStream(bais);
                Object object = ois.readObject();
                assert ois.available() == 0;
                if (object instanceof Data) {
                    catalogData = (Data) object;
                } else {
                    if (!(object instanceof List)) {
                        throw new IllegalStateException
                            (object.getClass().getName());
                    }
                    catalogData = new Data();
                    catalogData.formatList = (List) object;
                    catalogData.betaVersion = true;
                }
                return catalogData;
            } catch (ClassNotFoundException e) {
                throw new DatabaseException(e);
            } catch (IOException e) {
                throw new DatabaseException(e);
            }
        } else {
            catalogData = new Data();
        }
        return catalogData;
    }

    /**
     * Writes catalog Data.  If txn is null, auto-commit is used.
     */
    private void writeData(Transaction txn, Data catalogData)
        throws DatabaseException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(catalogData);
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
        DatabaseEntry key = new DatabaseEntry(DATA_KEY);
        DatabaseEntry data = new DatabaseEntry(baos.toByteArray());
        db.put(txn, key, data);

        /*
         * Delete the unused beta mutations record if we read the beta version
         * record earlier.
         */
        if (catalogData.betaVersion) {
            key.setData(BETA_MUTATIONS_KEY);
            db.delete(txn, key);
            catalogData.betaVersion = false;
        }
    }

    public boolean isRawAccess() {
        return rawAccess;
    }

    public Object convertRawObject(RawObject o, IdentityHashMap converted) {
        Format format = (Format) o.getType();
        if (this != format.getCatalog()) {
	    String className = format.getClassName();
            format = getFormat(className);
            if (format == null) {
                throw new IllegalArgumentException
                    ("External raw type not found: " + className);
            }
        }
        Format proxiedFormat = format.getProxiedFormat();
        if (proxiedFormat != null) {
            format = proxiedFormat;
        }
        if (converted == null) {
            converted = new IdentityHashMap();
        }
        return format.convertRawObject(this, false, o, converted);
    }

    public void dump() {
        System.out.println("--- Begin formats ---");
        for (Format format : formatList) {
            if (format != null) {
                System.out.println
                    ("ID: " + format.getId() +
                     " class: " + format.getClassName() +
                     " version: " + format.getVersion() +
                     " current: " + 
                     (format == formatMap.get(format.getClassName())));
            }
        }
        System.out.println("--- End formats ---");
    }
}

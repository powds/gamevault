package com.gamevault.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class GameVaultDatabase_Impl extends GameVaultDatabase {
  private volatile VaultFolderDao _vaultFolderDao;

  private volatile VaultItemDao _vaultItemDao;

  private volatile HiddenAppDao _hiddenAppDao;

  private volatile GameStateDao _gameStateDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `vault_folders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `vault_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `path` TEXT NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER NOT NULL, `thumbnailPath` TEXT, `folderId` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `hidden_apps` (`packageName` TEXT NOT NULL, `appName` TEXT NOT NULL, `iconPath` TEXT, `dateHidden` INTEGER NOT NULL, PRIMARY KEY(`packageName`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `game_state` (`id` INTEGER NOT NULL, `score` INTEGER NOT NULL, `bestScore` INTEGER NOT NULL, `gridJson` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'a8eb3049413655a7b6911147acd7f039')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `vault_folders`");
        db.execSQL("DROP TABLE IF EXISTS `vault_items`");
        db.execSQL("DROP TABLE IF EXISTS `hidden_apps`");
        db.execSQL("DROP TABLE IF EXISTS `game_state`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsVaultFolders = new HashMap<String, TableInfo.Column>(3);
        _columnsVaultFolders.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultFolders.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultFolders.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVaultFolders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesVaultFolders = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoVaultFolders = new TableInfo("vault_folders", _columnsVaultFolders, _foreignKeysVaultFolders, _indicesVaultFolders);
        final TableInfo _existingVaultFolders = TableInfo.read(db, "vault_folders");
        if (!_infoVaultFolders.equals(_existingVaultFolders)) {
          return new RoomOpenHelper.ValidationResult(false, "vault_folders(com.gamevault.data.local.VaultFolderEntity).\n"
                  + " Expected:\n" + _infoVaultFolders + "\n"
                  + " Found:\n" + _existingVaultFolders);
        }
        final HashMap<String, TableInfo.Column> _columnsVaultItems = new HashMap<String, TableInfo.Column>(8);
        _columnsVaultItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("path", new TableInfo.Column("path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("size", new TableInfo.Column("size", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("dateAdded", new TableInfo.Column("dateAdded", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("thumbnailPath", new TableInfo.Column("thumbnailPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultItems.put("folderId", new TableInfo.Column("folderId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVaultItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesVaultItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoVaultItems = new TableInfo("vault_items", _columnsVaultItems, _foreignKeysVaultItems, _indicesVaultItems);
        final TableInfo _existingVaultItems = TableInfo.read(db, "vault_items");
        if (!_infoVaultItems.equals(_existingVaultItems)) {
          return new RoomOpenHelper.ValidationResult(false, "vault_items(com.gamevault.data.local.VaultItemEntity).\n"
                  + " Expected:\n" + _infoVaultItems + "\n"
                  + " Found:\n" + _existingVaultItems);
        }
        final HashMap<String, TableInfo.Column> _columnsHiddenApps = new HashMap<String, TableInfo.Column>(4);
        _columnsHiddenApps.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHiddenApps.put("appName", new TableInfo.Column("appName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHiddenApps.put("iconPath", new TableInfo.Column("iconPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHiddenApps.put("dateHidden", new TableInfo.Column("dateHidden", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHiddenApps = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHiddenApps = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHiddenApps = new TableInfo("hidden_apps", _columnsHiddenApps, _foreignKeysHiddenApps, _indicesHiddenApps);
        final TableInfo _existingHiddenApps = TableInfo.read(db, "hidden_apps");
        if (!_infoHiddenApps.equals(_existingHiddenApps)) {
          return new RoomOpenHelper.ValidationResult(false, "hidden_apps(com.gamevault.data.local.HiddenAppEntity).\n"
                  + " Expected:\n" + _infoHiddenApps + "\n"
                  + " Found:\n" + _existingHiddenApps);
        }
        final HashMap<String, TableInfo.Column> _columnsGameState = new HashMap<String, TableInfo.Column>(4);
        _columnsGameState.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameState.put("score", new TableInfo.Column("score", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameState.put("bestScore", new TableInfo.Column("bestScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameState.put("gridJson", new TableInfo.Column("gridJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGameState = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGameState = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGameState = new TableInfo("game_state", _columnsGameState, _foreignKeysGameState, _indicesGameState);
        final TableInfo _existingGameState = TableInfo.read(db, "game_state");
        if (!_infoGameState.equals(_existingGameState)) {
          return new RoomOpenHelper.ValidationResult(false, "game_state(com.gamevault.data.local.GameStateEntity).\n"
                  + " Expected:\n" + _infoGameState + "\n"
                  + " Found:\n" + _existingGameState);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "a8eb3049413655a7b6911147acd7f039", "9a92007906c094335b4ae753098b61cf");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "vault_folders","vault_items","hidden_apps","game_state");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `vault_folders`");
      _db.execSQL("DELETE FROM `vault_items`");
      _db.execSQL("DELETE FROM `hidden_apps`");
      _db.execSQL("DELETE FROM `game_state`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(VaultFolderDao.class, VaultFolderDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(VaultItemDao.class, VaultItemDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HiddenAppDao.class, HiddenAppDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(GameStateDao.class, GameStateDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public VaultFolderDao vaultFolderDao() {
    if (_vaultFolderDao != null) {
      return _vaultFolderDao;
    } else {
      synchronized(this) {
        if(_vaultFolderDao == null) {
          _vaultFolderDao = new VaultFolderDao_Impl(this);
        }
        return _vaultFolderDao;
      }
    }
  }

  @Override
  public VaultItemDao vaultItemDao() {
    if (_vaultItemDao != null) {
      return _vaultItemDao;
    } else {
      synchronized(this) {
        if(_vaultItemDao == null) {
          _vaultItemDao = new VaultItemDao_Impl(this);
        }
        return _vaultItemDao;
      }
    }
  }

  @Override
  public HiddenAppDao hiddenAppDao() {
    if (_hiddenAppDao != null) {
      return _hiddenAppDao;
    } else {
      synchronized(this) {
        if(_hiddenAppDao == null) {
          _hiddenAppDao = new HiddenAppDao_Impl(this);
        }
        return _hiddenAppDao;
      }
    }
  }

  @Override
  public GameStateDao gameStateDao() {
    if (_gameStateDao != null) {
      return _gameStateDao;
    } else {
      synchronized(this) {
        if(_gameStateDao == null) {
          _gameStateDao = new GameStateDao_Impl(this);
        }
        return _gameStateDao;
      }
    }
  }
}

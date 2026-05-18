package com.gamevault.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HiddenAppDao_Impl implements HiddenAppDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HiddenAppEntity> __insertionAdapterOfHiddenAppEntity;

  private final SharedSQLiteStatement __preparedStmtOfUnhideApp;

  public HiddenAppDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHiddenAppEntity = new EntityInsertionAdapter<HiddenAppEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `hidden_apps` (`packageName`,`appName`,`iconPath`,`dateHidden`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HiddenAppEntity entity) {
        statement.bindString(1, entity.getPackageName());
        statement.bindString(2, entity.getAppName());
        if (entity.getIconPath() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getIconPath());
        }
        statement.bindLong(4, entity.getDateHidden());
      }
    };
    this.__preparedStmtOfUnhideApp = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM hidden_apps WHERE packageName = ?";
        return _query;
      }
    };
  }

  @Override
  public Object hideApp(final HiddenAppEntity app, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHiddenAppEntity.insert(app);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object unhideApp(final String packageName, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUnhideApp.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, packageName);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUnhideApp.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<HiddenAppEntity>> getAllHiddenApps() {
    final String _sql = "SELECT * FROM hidden_apps";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"hidden_apps"}, new Callable<List<HiddenAppEntity>>() {
      @Override
      @NonNull
      public List<HiddenAppEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfIconPath = CursorUtil.getColumnIndexOrThrow(_cursor, "iconPath");
          final int _cursorIndexOfDateHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "dateHidden");
          final List<HiddenAppEntity> _result = new ArrayList<HiddenAppEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HiddenAppEntity _item;
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final String _tmpIconPath;
            if (_cursor.isNull(_cursorIndexOfIconPath)) {
              _tmpIconPath = null;
            } else {
              _tmpIconPath = _cursor.getString(_cursorIndexOfIconPath);
            }
            final long _tmpDateHidden;
            _tmpDateHidden = _cursor.getLong(_cursorIndexOfDateHidden);
            _item = new HiddenAppEntity(_tmpPackageName,_tmpAppName,_tmpIconPath,_tmpDateHidden);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getHiddenApp(final String packageName,
      final Continuation<? super HiddenAppEntity> $completion) {
    final String _sql = "SELECT * FROM hidden_apps WHERE packageName = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, packageName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<HiddenAppEntity>() {
      @Override
      @Nullable
      public HiddenAppEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfIconPath = CursorUtil.getColumnIndexOrThrow(_cursor, "iconPath");
          final int _cursorIndexOfDateHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "dateHidden");
          final HiddenAppEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final String _tmpIconPath;
            if (_cursor.isNull(_cursorIndexOfIconPath)) {
              _tmpIconPath = null;
            } else {
              _tmpIconPath = _cursor.getString(_cursorIndexOfIconPath);
            }
            final long _tmpDateHidden;
            _tmpDateHidden = _cursor.getLong(_cursorIndexOfDateHidden);
            _result = new HiddenAppEntity(_tmpPackageName,_tmpAppName,_tmpIconPath,_tmpDateHidden);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

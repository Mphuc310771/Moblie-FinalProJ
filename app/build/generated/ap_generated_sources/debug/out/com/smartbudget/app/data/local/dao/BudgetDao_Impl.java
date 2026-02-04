package com.smartbudget.app.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.smartbudget.app.data.local.entity.BudgetEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class BudgetDao_Impl implements BudgetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BudgetEntity> __insertionAdapterOfBudgetEntity;

  private final EntityInsertionAdapter<BudgetEntity> __insertionAdapterOfBudgetEntity_1;

  private final EntityDeletionOrUpdateAdapter<BudgetEntity> __deletionAdapterOfBudgetEntity;

  private final EntityDeletionOrUpdateAdapter<BudgetEntity> __updateAdapterOfBudgetEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSpentAmount;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBudgetsByMonthYear;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public BudgetDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBudgetEntity = new EntityInsertionAdapter<BudgetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `budgets` (`id`,`categoryId`,`limitAmount`,`month`,`year`,`spentAmount`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final BudgetEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getCategoryId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getCategoryId());
        }
        statement.bindDouble(3, entity.getLimitAmount());
        statement.bindLong(4, entity.getMonth());
        statement.bindLong(5, entity.getYear());
        statement.bindDouble(6, entity.getSpentAmount());
      }
    };
    this.__insertionAdapterOfBudgetEntity_1 = new EntityInsertionAdapter<BudgetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `budgets` (`id`,`categoryId`,`limitAmount`,`month`,`year`,`spentAmount`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final BudgetEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getCategoryId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getCategoryId());
        }
        statement.bindDouble(3, entity.getLimitAmount());
        statement.bindLong(4, entity.getMonth());
        statement.bindLong(5, entity.getYear());
        statement.bindDouble(6, entity.getSpentAmount());
      }
    };
    this.__deletionAdapterOfBudgetEntity = new EntityDeletionOrUpdateAdapter<BudgetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `budgets` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final BudgetEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfBudgetEntity = new EntityDeletionOrUpdateAdapter<BudgetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `budgets` SET `id` = ?,`categoryId` = ?,`limitAmount` = ?,`month` = ?,`year` = ?,`spentAmount` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final BudgetEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getCategoryId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getCategoryId());
        }
        statement.bindDouble(3, entity.getLimitAmount());
        statement.bindLong(4, entity.getMonth());
        statement.bindLong(5, entity.getYear());
        statement.bindDouble(6, entity.getSpentAmount());
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateSpentAmount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE budgets SET spentAmount = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteBudgetsByMonthYear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM budgets WHERE month = ? AND year = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM budgets";
        return _query;
      }
    };
  }

  @Override
  public long insert(final BudgetEntity budget) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfBudgetEntity.insertAndReturnId(budget);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public long insertIfNotExists(final BudgetEntity budget) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfBudgetEntity_1.insertAndReturnId(budget);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final BudgetEntity budget) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfBudgetEntity.handle(budget);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final BudgetEntity budget) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfBudgetEntity.handle(budget);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateSpentAmount(final long budgetId, final double amount) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateSpentAmount.acquire();
    int _argIndex = 1;
    _stmt.bindDouble(_argIndex, amount);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, budgetId);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdateSpentAmount.release(_stmt);
    }
  }

  @Override
  public void deleteBudgetsByMonthYear(final int month, final int year) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBudgetsByMonthYear.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, month);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, year);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteBudgetsByMonthYear.release(_stmt);
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public Long findBudgetId(final Long categoryId, final int month, final int year) {
    final String _sql = "SELECT id FROM budgets WHERE (categoryId IS NULL AND ? IS NULL OR categoryId = ?) AND month = ? AND year = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    if (categoryId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, categoryId);
    }
    _argIndex = 2;
    if (categoryId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, categoryId);
    }
    _argIndex = 3;
    _statement.bindLong(_argIndex, month);
    _argIndex = 4;
    _statement.bindLong(_argIndex, year);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final Long _result;
      if (_cursor.moveToFirst()) {
        if (_cursor.isNull(0)) {
          _result = null;
        } else {
          _result = _cursor.getLong(0);
        }
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<BudgetEntity>> getBudgetsByMonthYear(final int month, final int year) {
    final String _sql = "SELECT b.*, (SELECT COALESCE(SUM(amount), 0) FROM expenses e WHERE e.categoryId = b.categoryId AND strftime('%m', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%02d', ?) AND strftime('%Y', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%d', ?)) as spentAmount FROM budgets b WHERE b.categoryId IS NOT NULL AND b.month = ? AND b.year = ? ORDER BY b.categoryId";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, month);
    _argIndex = 2;
    _statement.bindLong(_argIndex, year);
    _argIndex = 3;
    _statement.bindLong(_argIndex, month);
    _argIndex = 4;
    _statement.bindLong(_argIndex, year);
    return __db.getInvalidationTracker().createLiveData(new String[] {"expenses",
        "budgets"}, false, new Callable<List<BudgetEntity>>() {
      @Override
      @Nullable
      public List<BudgetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfLimitAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "limitAmount");
          final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfSpentAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "spentAmount");
          final List<BudgetEntity> _result = new ArrayList<BudgetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BudgetEntity _item;
            _item = new BudgetEntity();
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            _item.setId(_tmpId);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            _item.setCategoryId(_tmpCategoryId);
            final double _tmpLimitAmount;
            _tmpLimitAmount = _cursor.getDouble(_cursorIndexOfLimitAmount);
            _item.setLimitAmount(_tmpLimitAmount);
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            _item.setMonth(_tmpMonth);
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            _item.setYear(_tmpYear);
            final double _tmpSpentAmount;
            _tmpSpentAmount = _cursor.getDouble(_cursorIndexOfSpentAmount);
            _item.setSpentAmount(_tmpSpentAmount);
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
  public BudgetEntity getTotalBudget(final int month, final int year) {
    final String _sql = "SELECT b.id, b.categoryId, b.limitAmount, b.month, b.year, (SELECT COALESCE(SUM(e.amount), 0) FROM expenses e INNER JOIN categories c ON e.categoryId = c.id WHERE c.type = 0 AND strftime('%m', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%02d', ?) AND strftime('%Y', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%d', ?)) as spentAmount FROM budgets b WHERE b.categoryId IS NULL AND b.month = ? AND b.year = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, month);
    _argIndex = 2;
    _statement.bindLong(_argIndex, year);
    _argIndex = 3;
    _statement.bindLong(_argIndex, month);
    _argIndex = 4;
    _statement.bindLong(_argIndex, year);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfCategoryId = 1;
      final int _cursorIndexOfLimitAmount = 2;
      final int _cursorIndexOfMonth = 3;
      final int _cursorIndexOfYear = 4;
      final int _cursorIndexOfSpentAmount = 5;
      final BudgetEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new BudgetEntity();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _result.setId(_tmpId);
        final Long _tmpCategoryId;
        if (_cursor.isNull(_cursorIndexOfCategoryId)) {
          _tmpCategoryId = null;
        } else {
          _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
        }
        _result.setCategoryId(_tmpCategoryId);
        final double _tmpLimitAmount;
        _tmpLimitAmount = _cursor.getDouble(_cursorIndexOfLimitAmount);
        _result.setLimitAmount(_tmpLimitAmount);
        final int _tmpMonth;
        _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
        _result.setMonth(_tmpMonth);
        final int _tmpYear;
        _tmpYear = _cursor.getInt(_cursorIndexOfYear);
        _result.setYear(_tmpYear);
        final double _tmpSpentAmount;
        _tmpSpentAmount = _cursor.getDouble(_cursorIndexOfSpentAmount);
        _result.setSpentAmount(_tmpSpentAmount);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<BudgetEntity> getTotalBudgetLive(final int month, final int year) {
    final String _sql = "SELECT b.id, b.categoryId, b.limitAmount, b.month, b.year, (SELECT COALESCE(SUM(e.amount), 0) FROM expenses e INNER JOIN categories c ON e.categoryId = c.id WHERE c.type = 0 AND strftime('%m', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%02d', ?) AND strftime('%Y', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%d', ?)) as spentAmount FROM budgets b WHERE b.categoryId IS NULL AND b.month = ? AND b.year = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, month);
    _argIndex = 2;
    _statement.bindLong(_argIndex, year);
    _argIndex = 3;
    _statement.bindLong(_argIndex, month);
    _argIndex = 4;
    _statement.bindLong(_argIndex, year);
    return __db.getInvalidationTracker().createLiveData(new String[] {"expenses", "categories",
        "budgets"}, false, new Callable<BudgetEntity>() {
      @Override
      @Nullable
      public BudgetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfCategoryId = 1;
          final int _cursorIndexOfLimitAmount = 2;
          final int _cursorIndexOfMonth = 3;
          final int _cursorIndexOfYear = 4;
          final int _cursorIndexOfSpentAmount = 5;
          final BudgetEntity _result;
          if (_cursor.moveToFirst()) {
            _result = new BudgetEntity();
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            _result.setId(_tmpId);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            _result.setCategoryId(_tmpCategoryId);
            final double _tmpLimitAmount;
            _tmpLimitAmount = _cursor.getDouble(_cursorIndexOfLimitAmount);
            _result.setLimitAmount(_tmpLimitAmount);
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            _result.setMonth(_tmpMonth);
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            _result.setYear(_tmpYear);
            final double _tmpSpentAmount;
            _tmpSpentAmount = _cursor.getDouble(_cursorIndexOfSpentAmount);
            _result.setSpentAmount(_tmpSpentAmount);
          } else {
            _result = null;
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
  public BudgetEntity getBudgetByCategory(final long categoryId, final int month, final int year) {
    final String _sql = "SELECT b.id, b.categoryId, b.limitAmount, b.month, b.year, (SELECT COALESCE(SUM(amount), 0) FROM expenses e WHERE e.categoryId = ? AND strftime('%m', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%02d', ?) AND strftime('%Y', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%d', ?)) as spentAmount FROM budgets b WHERE b.categoryId = ? AND b.month = ? AND b.year = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 6);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, categoryId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, month);
    _argIndex = 3;
    _statement.bindLong(_argIndex, year);
    _argIndex = 4;
    _statement.bindLong(_argIndex, categoryId);
    _argIndex = 5;
    _statement.bindLong(_argIndex, month);
    _argIndex = 6;
    _statement.bindLong(_argIndex, year);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfCategoryId = 1;
      final int _cursorIndexOfLimitAmount = 2;
      final int _cursorIndexOfMonth = 3;
      final int _cursorIndexOfYear = 4;
      final int _cursorIndexOfSpentAmount = 5;
      final BudgetEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new BudgetEntity();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _result.setId(_tmpId);
        final Long _tmpCategoryId;
        if (_cursor.isNull(_cursorIndexOfCategoryId)) {
          _tmpCategoryId = null;
        } else {
          _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
        }
        _result.setCategoryId(_tmpCategoryId);
        final double _tmpLimitAmount;
        _tmpLimitAmount = _cursor.getDouble(_cursorIndexOfLimitAmount);
        _result.setLimitAmount(_tmpLimitAmount);
        final int _tmpMonth;
        _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
        _result.setMonth(_tmpMonth);
        final int _tmpYear;
        _tmpYear = _cursor.getInt(_cursorIndexOfYear);
        _result.setYear(_tmpYear);
        final double _tmpSpentAmount;
        _tmpSpentAmount = _cursor.getDouble(_cursorIndexOfSpentAmount);
        _result.setSpentAmount(_tmpSpentAmount);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<BudgetEntity> getBudgetByCategoryLive(final long categoryId, final int month,
      final int year) {
    final String _sql = "SELECT b.id, b.categoryId, b.limitAmount, b.month, b.year, (SELECT COALESCE(SUM(amount), 0) FROM expenses e WHERE e.categoryId = ? AND strftime('%m', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%02d', ?) AND strftime('%Y', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%d', ?)) as spentAmount FROM budgets b WHERE b.categoryId = ? AND b.month = ? AND b.year = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 6);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, categoryId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, month);
    _argIndex = 3;
    _statement.bindLong(_argIndex, year);
    _argIndex = 4;
    _statement.bindLong(_argIndex, categoryId);
    _argIndex = 5;
    _statement.bindLong(_argIndex, month);
    _argIndex = 6;
    _statement.bindLong(_argIndex, year);
    return __db.getInvalidationTracker().createLiveData(new String[] {"expenses",
        "budgets"}, false, new Callable<BudgetEntity>() {
      @Override
      @Nullable
      public BudgetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfCategoryId = 1;
          final int _cursorIndexOfLimitAmount = 2;
          final int _cursorIndexOfMonth = 3;
          final int _cursorIndexOfYear = 4;
          final int _cursorIndexOfSpentAmount = 5;
          final BudgetEntity _result;
          if (_cursor.moveToFirst()) {
            _result = new BudgetEntity();
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            _result.setId(_tmpId);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            _result.setCategoryId(_tmpCategoryId);
            final double _tmpLimitAmount;
            _tmpLimitAmount = _cursor.getDouble(_cursorIndexOfLimitAmount);
            _result.setLimitAmount(_tmpLimitAmount);
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            _result.setMonth(_tmpMonth);
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            _result.setYear(_tmpYear);
            final double _tmpSpentAmount;
            _tmpSpentAmount = _cursor.getDouble(_cursorIndexOfSpentAmount);
            _result.setSpentAmount(_tmpSpentAmount);
          } else {
            _result = null;
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
  public BudgetEntity getBudgetById(final long id) {
    final String _sql = "SELECT * FROM budgets WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
      final int _cursorIndexOfLimitAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "limitAmount");
      final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
      final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
      final int _cursorIndexOfSpentAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "spentAmount");
      final BudgetEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new BudgetEntity();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _result.setId(_tmpId);
        final Long _tmpCategoryId;
        if (_cursor.isNull(_cursorIndexOfCategoryId)) {
          _tmpCategoryId = null;
        } else {
          _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
        }
        _result.setCategoryId(_tmpCategoryId);
        final double _tmpLimitAmount;
        _tmpLimitAmount = _cursor.getDouble(_cursorIndexOfLimitAmount);
        _result.setLimitAmount(_tmpLimitAmount);
        final int _tmpMonth;
        _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
        _result.setMonth(_tmpMonth);
        final int _tmpYear;
        _tmpYear = _cursor.getInt(_cursorIndexOfYear);
        _result.setYear(_tmpYear);
        final double _tmpSpentAmount;
        _tmpSpentAmount = _cursor.getDouble(_cursorIndexOfSpentAmount);
        _result.setSpentAmount(_tmpSpentAmount);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<BudgetEntity> getAllBudgetsSync() {
    final String _sql = "SELECT * FROM budgets ORDER BY month DESC, year DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
      final int _cursorIndexOfLimitAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "limitAmount");
      final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
      final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
      final int _cursorIndexOfSpentAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "spentAmount");
      final List<BudgetEntity> _result = new ArrayList<BudgetEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final BudgetEntity _item;
        _item = new BudgetEntity();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _item.setId(_tmpId);
        final Long _tmpCategoryId;
        if (_cursor.isNull(_cursorIndexOfCategoryId)) {
          _tmpCategoryId = null;
        } else {
          _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
        }
        _item.setCategoryId(_tmpCategoryId);
        final double _tmpLimitAmount;
        _tmpLimitAmount = _cursor.getDouble(_cursorIndexOfLimitAmount);
        _item.setLimitAmount(_tmpLimitAmount);
        final int _tmpMonth;
        _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
        _item.setMonth(_tmpMonth);
        final int _tmpYear;
        _tmpYear = _cursor.getInt(_cursorIndexOfYear);
        _item.setYear(_tmpYear);
        final double _tmpSpentAmount;
        _tmpSpentAmount = _cursor.getDouble(_cursorIndexOfSpentAmount);
        _item.setSpentAmount(_tmpSpentAmount);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

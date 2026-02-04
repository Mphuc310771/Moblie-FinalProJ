package com.smartbudget.app.utils;

import java.util.Stack;

/**
 * Undo/Redo history manager.
 * Allows users to undo their last actions.
 */
public class UndoManager<T> {

    public interface UndoAction<T> {
        void undo(T item);
        void redo(T item);
        String getDescription();
    }

    public static class UndoableAction<T> {
        public final T item;
        public final UndoAction<T> action;
        public final long timestamp;

        public UndoableAction(T item, UndoAction<T> action) {
            this.item = item;
            this.action = action;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private final Stack<UndoableAction<T>> undoStack = new Stack<>();
    private final Stack<UndoableAction<T>> redoStack = new Stack<>();
    private static final int MAX_HISTORY = 20;

    /**
     * Add an action to the undo history.
     */
    public void addAction(T item, UndoAction<T> action) {
        UndoableAction<T> undoableAction = new UndoableAction<>(item, action);
        undoStack.push(undoableAction);
        
        // Clear redo stack when new action is added
        redoStack.clear();
        
        // Limit history size
        while (undoStack.size() > MAX_HISTORY) {
            undoStack.remove(0);
        }
    }

    /**
     * Undo the last action.
     * @return true if undo was performed
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }

        UndoableAction<T> action = undoStack.pop();
        action.action.undo(action.item);
        redoStack.push(action);
        return true;
    }

    /**
     * Redo the last undone action.
     * @return true if redo was performed
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }

        UndoableAction<T> action = redoStack.pop();
        action.action.redo(action.item);
        undoStack.push(action);
        return true;
    }

    /**
     * Check if undo is available.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Check if redo is available.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Get the description of the last action.
     */
    public String getLastActionDescription() {
        if (undoStack.isEmpty()) {
            return null;
        }
        return undoStack.peek().action.getDescription();
    }

    /**
     * Clear all history.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * Get undo history count.
     */
    public int getUndoCount() {
        return undoStack.size();
    }
}

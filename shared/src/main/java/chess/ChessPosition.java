package chess;


/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row;
    private final int col;
    /**
     * @param row the row number (1-8)
     * @param col the column number (1-8)
     */

    public ChessPosition(int row, int col) {
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            throw new IllegalArgumentException("Row and column must be between 1 and 8.");
        }
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true; // If the same object, they are equal
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false; // Not equal if null or not the same class
        }
        ChessPosition that = (ChessPosition) obj;
        return row == that.row && col == that.col; // Equal if rows and columns match
    }

    /**
     * Generates a hash code for the ChessPosition.
     *
     * @return an integer hash code.
     */
    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + col; // Combine row and column values into a hash
        return result;
    }

    /**
     * Provides a string representation of the ChessPosition.
     *
     * @return a string in the format "(row, column)".
     */
    @Override
    public String toString() {
        return "ChessPosition{" +
                "row=" + row +
                ", col=" + col +
                '}';
    }
}
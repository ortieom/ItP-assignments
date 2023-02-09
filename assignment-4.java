import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;


public final class Main {
    /**
     * represents chess board.
     */
    private static Board chessBoard;

    /**
     * file input.
     */
    private static Scanner scanner;

    static {
        try {
            scanner = new Scanner(new File("input.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * file output.
     */
    private static FileOutputStream fout;

    static {
        try {
            fout = new FileOutputStream("output.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * number of chess piece type in input string.
     */
    private static final int TYPE_INDEX = 0;
    /**
     * number of chess piece color in input string.
     */
    private static final int COLOR_INDEX = 1;
    /**
     * number of chess piece x-coordinate in input string.
     */
    private static final int X_COORDINATE_INDEX = 2;
    /**
     * number of chess piece y-coordinate in input string.
     */
    private static final int Y_COORDINATE_INDEX = 3;
    /**
     * available chess piece types.
     */
    private static final List<String> PIECE_TYPES_LIST =
            Arrays.asList("Pawn", "King", "Knight", "Rook", "Queen", "Bishop");


    /**
     * entrypoint.
     * @param args command line arguments
     * @throws IOException if something is wrong with output
     */
    public static void main(String[] args) throws IOException {
        int boardSize = Integer.parseInt(scanner.nextLine());
        // declarations for input chess piece
        String[] chessPieceArguments;
        PieceColor pieceColor;
        PiecePosition piecePosition;
        ChessPiece chessPiece;

        // list for all chess pieces
        List<PiecePosition> allPieces = new ArrayList<>();

        try {
            chessBoard = new Board(boardSize);
            int numberOfPieces = readNumberOfPieces(2, boardSize * boardSize);  // reading number of pieces

            int totalCnt = 0;  // piece counter

            while (scanner.hasNext()) {
                totalCnt++;
                if (totalCnt > numberOfPieces) {
                    throw new InvalidNumberOfPiecesException();
                }

                String chessPieceArgumentsString = scanner.nextLine().replace("\n", "");
                chessPieceArguments = chessPieceArgumentsString.split(" ");

                if (!PIECE_TYPES_LIST.contains(chessPieceArguments[TYPE_INDEX])) {
                    throw new InvalidPieceNameException();
                }

                pieceColor = PieceColor.parse(chessPieceArguments[COLOR_INDEX]);

                piecePosition = new PiecePosition(Integer.parseInt(chessPieceArguments[X_COORDINATE_INDEX]),
                        Integer.parseInt(chessPieceArguments[Y_COORDINATE_INDEX]));
                if (!piecePosition.isValid(boardSize)) {
                    throw new InvalidPiecePositionException();
                }

                switch (chessPieceArguments[TYPE_INDEX]) {
                    case "Pawn":
                        chessPiece = new Pawn(piecePosition, pieceColor);
                        break;
                    case "King":
                        chessPiece = new King(piecePosition, pieceColor);
                        break;
                    case "Knight":
                        chessPiece = new Knight(piecePosition, pieceColor);
                        break;
                    case "Rook":
                        chessPiece = new Rook(piecePosition, pieceColor);
                        break;
                    case "Queen":
                        chessPiece = new Queen(piecePosition, pieceColor);
                        break;
                    case "Bishop":
                        chessPiece = new Bishop(piecePosition, pieceColor);
                        break;
                    default:
                        throw new InvalidPieceNameException();
                }

                chessBoard.addPiece(chessPiece);
                allPieces.add(piecePosition);
            }

            chessBoard.checkKings();  // to check that there are 1 king of each color

            if (totalCnt != numberOfPieces) {  // arrived fewer pieces than was declared
                throw new InvalidNumberOfPiecesException();
            }

            for (PiecePosition position: allPieces) {
                // output for every chess piece
                chessPiece = chessBoard.getPiece(position);
                String output = chessBoard.getPiecePossibleMoveCount(chessPiece)
                        + " " + chessBoard.getPiecePossibleCapturesCount(chessPiece) + "\n";
                fout.write(output.getBytes());
            }

        } catch (InvalidBoardSizeException ex) {
            fout.write((ex.getMessage() + "\n").getBytes());
        } catch (InvalidNumberOfPiecesException ex) {
            fout.write((ex.getMessage() + "\n").getBytes());
        } catch (InvalidPieceNameException ex) {
            fout.write((ex.getMessage() + "\n").getBytes());
        } catch (InvalidPieceColorException ex) {
            fout.write((ex.getMessage() + "\n").getBytes());
        } catch (InvalidPiecePositionException ex) {
            fout.write((ex.getMessage() + "\n").getBytes());
        } catch (InvalidGivenKingsException ex) {
            fout.write((ex.getMessage() + "\n").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            fout.close();
        }
        // I removed InvalidInputException because 6 previous exceptions cover all possible variants
    }

    /**
     * used to read number of pieces.
     * @param minimal lower bound
     * @param maximal upper bound
     * @return number of pieces
     * @throws InvalidNumberOfPiecesException if result is out of bounds
     */
    private static int readNumberOfPieces(int minimal, int maximal) throws InvalidNumberOfPiecesException {
        int number = Integer.parseInt(scanner.nextLine());
        if (number < minimal || number > maximal) {
            throw new InvalidNumberOfPiecesException();
        }
        return number;
    }

    private Main() { }  // so that Utility class does not have a public or default constructor, according to CheckStyle
}


class PiecePosition {
    /**
     * position of chess piece on X-axis.
     */
    private final int x;
    /**
     * position of chess piece on Y-axis.
     */
    private final int y;

    /**
     * creates a piece position class with specified coordinates.
     * @param onX int, X-coordinate of piece
     * @param onY int, Y-coordinate of piece
     */
    PiecePosition(int onX, int onY) {
        this.x = onX;
        this.y = onY;
    }

    /**
     * checks if provided position is possible on current board.
     * @param boardSize int, size of board
     * @return boolean validity
     */
    public boolean isValid(int boardSize) {
        return this.x >= 1 && this.y >= 1 && this.x <= boardSize && this.y <= boardSize;
    }

    /**
     * getter for X-coordinate.
     * @return int, X-coordinate
     */
    public int getX() {
        return this.x;
    }
    /**
     * getter for Y-coordinate.
     * @return int, Y-coordinate
     */
    public int getY() {
        return this.y;
    }

    /**
     * represents piece position in string.
     * @return string in format "{X-coordinate} {Y-coordinate}"
     */
    @Override
    public String toString() {
        return Integer.toString(this.x) + " " + Integer.toString(this.y);
    }
}


/**
 * represents color of chess piece.
 * either BLACK or WHITE
 */
enum PieceColor {
    /**
     * available colors.
     */
    WHITE, BLACK;

    /**
     * parses input string and determines color of piece.
     * @param st string with color provided by user
     * @return PieceColor
     * @throws InvalidPieceColorException if provided color is invalid
     */
    public static PieceColor parse(String st) throws InvalidPieceColorException {
        if (st.equals("White")) {
            return WHITE;
        } else if (st.equals("Black")) {
            return BLACK;
        } else {
            throw new InvalidPieceColorException();
        }
    }
}


/**
 * represents abstract chess piece.
 */
abstract class ChessPiece {
    /**
     * represents position of piece.
     */
    protected PiecePosition position;
    /**
     * represents color of piece.
     */
    protected PieceColor color;
    /**
     * stores all reachable positions on board.
     * declared as null because constructor does not have boardSize parameter.
     * determined after first call of any method
     */
    protected List<PiecePosition> possiblePositions = null;

    /**
     * creates a chess piece with specified position and color.
     * @param piecePosition position on the board
     * @param pieceColor color of a piece
     */
    ChessPiece(PiecePosition piecePosition, PieceColor pieceColor) {
        this.position = piecePosition;
        this.color = pieceColor;
    }

    /**
     * getter for position of chess piece.
     * @return PiecePosition
     */
    public PiecePosition getPosition() {
        return this.position;
    }
    /**
     * getter for color of chess piece.
     * @return PieceColor
     */
    public PieceColor getColor() {
        return this.color;
    }

    /**
     * writes into possiblePositions all reachable positions by this piece.
     * @param boardSize size of a bord
     */
    protected abstract void calculatePossiblePositions(int boardSize);

    /**
     * used to calculate number of possible moves.
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible moves for chess piece
     */
    public int getMovesCount(Map<String, ChessPiece> positions, int boardSize) {
        if (this.possiblePositions == null) {  // if possible positions are not calculated yet
            calculatePossiblePositions(boardSize);
        }

        int result = 0;

        for (PiecePosition piecePosition: this.possiblePositions) {  // for every possible new position
            ChessPiece piece = positions.get(piecePosition.toString());  // piece on considered position, null if empty
            if (piece == null || piece.getColor() != this.color) {
                // if position is empty or contains piece of another color and, therefore, can be freed to move there
                result++;
            }
        }

        return result;
    }
    /**
     * used to calculate number of possible captures.
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible captures for chess piece
     */
    public int getCapturesCount(Map<String, ChessPiece> positions, int boardSize) {
        if (this.possiblePositions == null) {  // if possible positions are not calculated yet
            calculatePossiblePositions(boardSize);
        }

        int result = 0;

        for (PiecePosition piecePosition: possiblePositions) {  // for every possible new position
            ChessPiece piece = positions.get(piecePosition.toString());  // piece on considered position, null if empty
            if (piece != null && piece.getColor() != this.color) {
                // if position is not empty and contains piece of another color to capture
                result++;
            }
        }

        return result;
    }
}


/**
 * used in BishopMovement and RookMovement since they are built on the same principle.
 * directions are set by offsetMultipliers
 */
interface ContinuousMovementsWithOffset {
    /**
     * used to calculate number of possible moves by continuously moving in provided directions.
     * @param position PiecePosition, position of considered chess piece
     * @param color PieceColor, color of considered chess piece
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @param offsetMultiplierX int[], array of multipliers for X-coordinate (1st, 2nd, 3rd, 4th direction)
     * @param offsetMultiplierY int[], same as offsetMultiplierX but for Y-coordinates
     * @return int, number of possible moves
     */
    default int getContinuousMovesCount(PiecePosition position, PieceColor color,
                                        Map<String, ChessPiece> positions, int boardSize,
                                        int[] offsetMultiplierX, int[] offsetMultiplierY) {
        int result = 0;  // return value
        // start position
        int x = position.getX();
        int y = position.getY();

        // flags represent availability of movement in corresponding direction
        boolean[] directionFlags = {true, true, true, true};

        int offset = 0;  // how many moves from start

        PiecePosition move;  // for considered move
        ChessPiece piece;  // for piece placed on the considered move

        int directionsAvailableCnt = 4;
        while (directionsAvailableCnt != 0) {  // while move is possible in at least 1 direction
            offset++;
            for (int i = 0; i < directionFlags.length; i++) {  // for every direction
                if (directionFlags[i]) {  // if direction is still available
                    int newX = x + (offsetMultiplierX[i] * offset);
                    int newY = y + (offsetMultiplierY[i] * offset);

                    move = new PiecePosition(newX, newY);
                    piece = positions.get(move.toString());

                    if (move.isValid(boardSize) && piece == null) {
                        // way is clear
                        result++;
                    } else if (move.isValid(boardSize) && piece != null && piece.color != color) {
                        // piece in the way can be attacked
                        directionFlags[i] = false;  // can not move further in this direction
                        directionsAvailableCnt--;
                        result++;  // but this position is possible for move
                    } else {
                        // either move is out of borders or piece of the same color is in the way
                        directionFlags[i] = false;  // can not move further in this direction & this move is impossible
                        directionsAvailableCnt--;
                    }
                }
            }
        }

        return result;
    }

    /**
     * used to calculate number of possible captures by continuously moving in provided directions.
     * @param position PiecePosition, position of considered chess piece
     * @param color PieceColor, color of considered chess piece
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @param offsetMultiplierX int[], array of multipliers for X-coordinate (1st, 2nd, 3rd, 4th direction)
     * @param offsetMultiplierY int[], same as offsetMultiplierX but for Y-coordinates
     * @return int, number of possible captures
     */
    default int getContinuousCapturesCount(PiecePosition position, PieceColor color,
                                           Map<String, ChessPiece> positions, int boardSize,
                                           int[] offsetMultiplierX, int[] offsetMultiplierY) {
        // almost same as getContinuousMovesCount
        int result = 0;  // return value
        // start position
        int x = position.getX();
        int y = position.getY();

        // flags represent availability of movement in corresponding direction
        boolean[] directionFlags = {true, true, true, true};
        int offset = 0;  // how many moves from start

        PiecePosition move;  // for considered move
        ChessPiece piece;  // for piece placed on the considered move

        int directionsAvailableCnt = 4;
        while (directionsAvailableCnt != 0) {  // while move is possible in at least 1 direction
            offset++;
            for (int i = 0; i < directionFlags.length; i++) {  // for every direction
                if (directionFlags[i]) {  // if direction is still available
                    int newX = x + (offsetMultiplierX[i] * offset);
                    int newY = y + (offsetMultiplierY[i] * offset);

                    move = new PiecePosition(newX, newY);
                    piece = positions.get(move.toString());

                    if (piece != null || !move.isValid(boardSize)) {
                        directionFlags[i] = false;  // can not move further in this direction
                        directionsAvailableCnt--;

                        if (piece != null && piece.color != color) {  // piece can be captured
                            result++;
                        }
                    }
                }
            }
        }

        return result;
    }
}


/**
 * represents actions of Bishop and partly of Queen.
 */
interface BishopMovement extends ContinuousMovementsWithOffset {
    /**
     * used to calculate number of possible diagonal moves.
     * @param position PiecePosition, position of considered chess piece
     * @param color PieceColor, color of considered chess piece
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible diagonal moves
     */
    default int getDiagonalMovesCount(PiecePosition position, PieceColor color,
                                      Map<String, ChessPiece> positions, int boardSize) {
        // multipliers for offset in X & Y with respect to direction: up left, up right, down left, down right
        int[] offsetMultiplierX = {-1, 1, -1, 1};
        int[] offsetMultiplierY = {1, 1, -1, -1};

        return getContinuousMovesCount(position, color, positions, boardSize, offsetMultiplierX, offsetMultiplierY);
    }

    /**
     * used to calculate number of possible diagonal captures.
     * @param position PiecePosition, position of considered chess piece
     * @param color PieceColor, color of considered chess piece
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible diagonal captures
     */
    default  int getDiagonalCapturesCount(PiecePosition position, PieceColor color,
                                          Map<String, ChessPiece> positions, int boardSize) {
        // multipliers for offset in X & Y with respect to direction: up left, up right, down left, down right
        int[] offsetMultiplierX = {-1, 1, -1, 1};
        int[] offsetMultiplierY = {1, 1, -1, -1};

        return getContinuousCapturesCount(position, color, positions, boardSize, offsetMultiplierX, offsetMultiplierY);
    }
}


/**
 * represents actions of Rook and partly of Queen.
 */
interface RookMovement extends ContinuousMovementsWithOffset {
    /**
     * used to calculate number of possible orthogonal moves.
     * @param position PiecePosition, position of considered chess piece
     * @param color PieceColor, color of considered chess piece
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible orthogonal moves
     */
    default int getOrthogonalMovesCount(PiecePosition position, PieceColor color,
                                        Map<String, ChessPiece> positions, int boardSize) {
        // multipliers for offset in X & Y with respect to direction: left, right, up, down
        int[] offsetMultiplierX = {-1, 1, 0, 0};
        int[] offsetMultiplierY = {0, 0, 1, -1};

        return getContinuousMovesCount(position, color, positions, boardSize, offsetMultiplierX, offsetMultiplierY);
    }

    /**
     * used to calculate number of possible orthogonal captures.
     * @param position PiecePosition, position of considered chess piece
     * @param color PieceColor, color of considered chess piece
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible orthogonal captures
     */
    default int getOrthogonalCapturesCount(PiecePosition position, PieceColor color,
                                           Map<String, ChessPiece> positions, int boardSize) {
        // multipliers for offset in X & Y with respect to direction: left, right, up, down
        int[] offsetMultiplierX = {-1, 1, 0, 0};
        int[] offsetMultiplierY = {0, 0, 1, -1};

        return getContinuousCapturesCount(position, color, positions, boardSize, offsetMultiplierX, offsetMultiplierY);
    }
}


/**
 * represents Knight chess piece.
 */
class Knight extends ChessPiece {
    /**
     * creates a Knight chess piece with specified position and color.
     * @param piecePosition position on the board
     * @param pieceColor color of a piece
     */
    Knight(PiecePosition piecePosition, PieceColor pieceColor) {
        super(piecePosition, pieceColor);
    }

    /**
     * writes into possiblePositions all reachable positions by this piece.
     * @param boardSize size of a bord
     */
    protected void calculatePossiblePositions(int boardSize) {
        int x = this.position.getX();
        int y = this.position.getY();

        // all possible moves for Knight
        PiecePosition[] moves = {
                new PiecePosition(x + 2, y + 1),
                new PiecePosition(x + 2, y - 1),
                new PiecePosition(x - 2, y + 1),
                new PiecePosition(x - 2, y - 1),
                new PiecePosition(x + 1, y + 2),
                new PiecePosition(x + 1, y - 2),
                new PiecePosition(x - 1, y + 2),
                new PiecePosition(x - 1, y - 2),
        };
        List<PiecePosition> result = new ArrayList<>();  // array for positions that belong to board
        for (PiecePosition piecePosition: moves) {
            if (piecePosition.isValid(boardSize)) {  // if position after considered move remains on board
                result.add(piecePosition);
            }
        }
        this.possiblePositions = new ArrayList<>(result);  // updating private variable
    }
}


/**
 * represents King chess piece.
 */
class King extends ChessPiece {
   /**
     * creates a King chess piece with specified position and color.
     * @param piecePosition position on the board
     * @param pieceColor color of a piece
     */
    King(PiecePosition piecePosition, PieceColor pieceColor) {
        super(piecePosition, pieceColor);
    }

    /**
     * writes into possiblePositions all reachable positions by this piece.
     * @param boardSize size of a bord
     */
    protected void calculatePossiblePositions(int boardSize) {
        int x = this.position.getX();
        int y = this.position.getY();

        List<PiecePosition> moves = new ArrayList<>();

        for (int dx = -1; dx < 2; dx++) {  // bias in x
            for (int dy = -1; dy < 2; dy++) {  // bias in y
                // trying all possible variants
                PiecePosition piecePosition = new PiecePosition(x + dx, y + dy);
                if (piecePosition.isValid(boardSize) && (dx != 0 || dy != 0)) {
                    moves.add(piecePosition);
                }
            }
        }

        this.possiblePositions = new ArrayList<>(moves);
    }
}


/**
 * represents Pawn chess piece.
 */
class Pawn extends ChessPiece {
    /**
     * creates a Pawn chess piece with specified position and color.
     * @param piecePosition position on the board
     * @param pieceColor color of a piece
     */
    Pawn(PiecePosition piecePosition, PieceColor pieceColor) {
        super(piecePosition, pieceColor);
    }

    @Override  // redundant here (useful for King & Knight)
    protected void calculatePossiblePositions(int boardSize) {
        return;
    }

    /**
     * used to calculate number of possible moves for Pawn.
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return number of possible moves
     */
    @Override
    public int getMovesCount(Map<String, ChessPiece> positions, int boardSize) {
        int result;  // return value

        int direction;  // +1 means forward, -1 - backwards
        if (this.color == PieceColor.WHITE) {
            direction = 1;
        } else {
            direction = -1;
        }

        PiecePosition moveForward = new PiecePosition(this.position.getX(), this.position.getY() + direction);
        ChessPiece pieceInFront = positions.get(moveForward.toString());

        if (moveForward.isValid(boardSize) && pieceInFront == null) {  // if cell in forward direction is empty
            result = 1;
        } else {
            result = 0;
        }

        return result + getCapturesCount(positions, boardSize);  // because captures also can be counted as moves
    }

    /**
     * used to calculate number of possible captures for Pawn.
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible captures
     */
    @Override
    public int getCapturesCount(Map<String, ChessPiece> positions, int boardSize) {
        int result = 0;  // return value

        int direction;  // +1 means forward, -1 - backwards
        if (this.color == PieceColor.WHITE) {
            direction = 1;
        } else {
            direction = -1;
        }

        // positions that can be attacked by Pawn
        PiecePosition move1 = new PiecePosition(this.position.getX() - 1, this.position.getY() + direction);
        PiecePosition move2 = new PiecePosition(this.position.getX() + 1, this.position.getY() + direction);
        // chess pieces on attacked positions
        ChessPiece piece1 = positions.get(move1.toString());
        ChessPiece piece2 = positions.get(move2.toString());

        if (move1.isValid(boardSize) && piece1 != null && this.color != piece1.getColor()) {
            // if cell is valid and there is a piece of another color
            result++;
        }
        if (move2.isValid(boardSize) && piece2 != null && this.color != piece2.getColor()) {
            result++;
        }

        return result;
    }
}


/**
 * represents Bishop chess piece.
 */
class Bishop extends ChessPiece implements BishopMovement {
    /**
     * creates a Bishop chess piece with specified position and color.
     * @param piecePosition position on the board
     * @param pieceColor color of a piece
     */
    Bishop(PiecePosition piecePosition, PieceColor pieceColor) {
        super(piecePosition, pieceColor);
    }

    @Override  // redundant here (useful for King & Knight)
    protected void calculatePossiblePositions(int boardSize) {
        return;
    }

    /**
     * used to calculate number of possible moves for Bishop.
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible moves
     */
    @Override
    public int getMovesCount(Map<String, ChessPiece> positions, int boardSize) {
        return getDiagonalMovesCount(this.position, this.color, positions, boardSize);
    }

    /**
     * used to calculate number of possible captures for Bishop.
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible captures
     */
    @Override
    public int getCapturesCount(Map<String, ChessPiece> positions, int boardSize) {
        return getDiagonalCapturesCount(this.position, this.color, positions, boardSize);
    }
}


/**
 * represents Rook chess piece.
 */
class Rook extends ChessPiece implements RookMovement {
    /**
     * creates a Rook chess piece with specified position and color.
     * @param piecePosition position on the board
     * @param pieceColor color of a piece
     */
    Rook(PiecePosition piecePosition, PieceColor pieceColor) {
        super(piecePosition, pieceColor);
    }

    @Override  // redundant here (useful for King & Knight)
    protected void calculatePossiblePositions(int boardSize) {
        return;
    }

    /**
     * used to calculate number of possible moves for Rook.
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible moves
     */
    @Override
    public int getMovesCount(Map<String, ChessPiece> positions, int boardSize) {
        return getOrthogonalMovesCount(this.position, this.color, positions, boardSize);
    }

    /**
     * used to calculate number of possible captures for Rook.
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible captures
     */
    @Override
    public int getCapturesCount(Map<String, ChessPiece> positions, int boardSize) {
        return getOrthogonalCapturesCount(this.position, this.color, positions, boardSize);
    }
}


/**
 * represents Queen chess piece.
 */
class Queen extends ChessPiece implements BishopMovement, RookMovement {
    /**
     * creates a Queen chess piece with specified position and color.
     * @param piecePosition position on the board
     * @param pieceColor color of a piece
     */
    Queen(PiecePosition piecePosition, PieceColor pieceColor) {
        super(piecePosition, pieceColor);
    }

    @Override  // redundant here (useful for King & Knight)
    protected void calculatePossiblePositions(int boardSize) {
        return;
    }

    /**
     * used to calculate number of possible moves for Queen.
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible moves
     */
    @Override
    public int getMovesCount(Map<String, ChessPiece> positions, int boardSize) {
        return getDiagonalMovesCount(this.position, this.color, positions, boardSize)
                + getOrthogonalMovesCount(this.position, this.color, positions, boardSize);
    }

    /**
     * used to calculate number of possible captures for Queen.
     * @param positions Map<String, ChessPiece>, positions of pieces on board
     * @param boardSize int, size of board
     * @return int, number of possible captures
     */
    @Override
    public int getCapturesCount(Map<String, ChessPiece> positions, int boardSize) {
        return getDiagonalCapturesCount(this.position, this.color, positions, boardSize)
                + getOrthogonalCapturesCount(this.position, this.color, positions, boardSize);
    }
}


class Board {
    /**
     * map to access chess pieces by position string.
     */
    private Map<String, ChessPiece> positionsToPieces = new HashMap<>();
    /**
     * size of the board.
     */
    private final int size;

    /**
     * shows if white king is already on board.
     */
    private boolean hasWhiteKing = false;
    /**
     * shows if black king is already on board.
     */
    private boolean hasBlackKing = false;

    /**
     * lower bound for size of the board.
     */
    private static final int MINIMAL_SIZE = 3;
    /**
     * upper bound for size of the board.
     */
    private static final int MAXIMAL_SIZE = 1000;

    /**
     * creates a Board with specified size.
     * @param boardSize size of the board
     * @throws InvalidBoardSizeException if size is too low or too high
     */
    Board(int boardSize) throws InvalidBoardSizeException {
        if (boardSize < MINIMAL_SIZE || boardSize > MAXIMAL_SIZE) {
            throw new InvalidBoardSizeException();
        }
        this.size = boardSize;
    }

    /**
     * used to calculate number of possible moves for chess piece.
     * @param piece considered chess piece
     * @return number of possible moves for chess piece
     */
    public int getPiecePossibleMoveCount(ChessPiece piece) {
        return piece.getMovesCount(this.positionsToPieces, this.size);
    }

    /**
     * used to calculate number of possible captures for chess piece.
     * @param piece considered chess piece
     * @return number of possible captures for chess piece
     */
    public int getPiecePossibleCapturesCount(ChessPiece piece) {
        return piece.getCapturesCount(this.positionsToPieces, this.size);
    }

    /**
     * used to add chess pieces on board.
     * @param piece ChessPiece
     * @throws InvalidPiecePositionException if piece's cell is already occupied
     * @throws InvalidGivenKingsException if extra kings are given
     */
    public void addPiece(ChessPiece piece) throws InvalidPiecePositionException, InvalidGivenKingsException {
        PiecePosition position = piece.getPosition();
        // checking kings on the board
        if (piece.getClass().getName().equals("King")) {
            if (piece.getColor() == PieceColor.WHITE) {
                if (hasWhiteKing) {  // there is white king already
                    throw new InvalidGivenKingsException();
                }
                hasWhiteKing = true;
            } else {
                if (hasBlackKing) {  // there is black king already
                    throw new InvalidGivenKingsException();
                }
                hasBlackKing = true;
            }
        }

        if (this.positionsToPieces.get(position.toString()) != null) {
            // if cell is already occupied
            throw new InvalidPiecePositionException();
        }

        this.positionsToPieces.put(position.toString(), piece);
    }

    /**
     * used to get chess piece by its position.
     * @param position PiecePosition
     * @return ChessPiece
     */
    public ChessPiece getPiece(PiecePosition position) {
        return this.positionsToPieces.get(position.toString());
    }

    /**
     * used to check if all kings are given.
     * @throws InvalidGivenKingsException if not all kings are given
     */
    public void checkKings() throws InvalidGivenKingsException {
        if (!(hasWhiteKing && hasBlackKing)) {
            throw new InvalidGivenKingsException();
        }
    }
}


class InvalidBoardSizeException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid board size";
    }
}


class InvalidNumberOfPiecesException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid number of pieces";
    }
}


class InvalidPieceNameException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid piece name";
    }
}


class InvalidPieceColorException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid piece color";
    }
}


class InvalidPiecePositionException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid piece position";
    }
}


class InvalidGivenKingsException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid given Kings";
    }
}


class InvalidInputException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid input";
    }
}

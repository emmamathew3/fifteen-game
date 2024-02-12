import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//rows are numbered 0 to 4
//columns are numbered 0 to 4

//Utils class to convert from coordinates to row and column number
//supports other row and column number operations
class CoordUtils {

  // method to convert row or column number to x or y coordinate
  int convertIndexToCoord(int index) {
    if (index >= 0 && index <= 4) {
      return index * 100 + 50;
    }
    else {
      throw new IllegalArgumentException("Index should be between 0 and 4");
    }
  }
}

//Utilities class for ArrayList 
//helps with general methods
class ArrayUtils {

  // Finds the index (from 0 to 15) of a given tile
  <T> int findIndex(ArrayList<ArrayList<T>> arr, T t) {
    int result = 0;

    for (int i = 0; i < arr.size(); i++) {
      for (int j = 0; j < arr.get(i).size(); j++) {
        if (arr.get(i).get(j).equals(t)) {
          return result;
        }
        else {
          result++;
        }
      }
    }

    return -1;
  }

}

// Represents an individual tile
class Tile {
  // The number on the tile. Use 0 to represent the space
  int value;

  // constructor for Tile
  Tile(int value) {
    this.value = value;
  }

  // Draws this tile onto the background at the specified logical coordinates
  WorldScene drawAt(int col, int row, WorldScene background, boolean correctPos) {
    background.placeImageXY(this.drawTile(correctPos), new CoordUtils().convertIndexToCoord(col),
        new CoordUtils().convertIndexToCoord(row));
    return background;
  }

  // draws the image of the tile factoring in whether its at the correct position
  WorldImage drawTile(boolean correctPos) {
    if (this.value == 0) {
      WorldImage bottomRect = new RectangleImage(100, 100, OutlineMode.SOLID, Color.black);
      WorldImage topRect = new RectangleImage(90, 90, OutlineMode.SOLID, Color.white);
      return new OverlayImage(topRect, bottomRect);
    }

    WorldImage topRect;
    if (correctPos) {
      topRect = new RectangleImage(90, 90, OutlineMode.SOLID, Color.orange);
    }
    else {
      topRect = new RectangleImage(90, 90, OutlineMode.SOLID, Color.cyan);
    }

    WorldImage bottomRect = new RectangleImage(100, 100, OutlineMode.SOLID, Color.black);
    WorldImage text = new TextImage((this.value + ""), 30, Color.black);
    return new OverlayImage(text, new OverlayImage(topRect, bottomRect));
  }

  // returns whether the tile is in the correct position
  boolean atCorrectPosition(int row, int column) {
    if (this.value == 0) {
      return row == 3 && column == 3;
    }
    else {
      return this.value == (row * 4) + column + 1;
    }
  }

  // returns whether two tiles are equal
  public boolean equals(Object o) {
    if (o instanceof Tile) {
      return ((Tile) o).value == this.value;
    }
    else {
      return false;
    }
  }

  // override hashcode
  public int hashCode() {
    return this.value * 29;
  }
}

class FifteenGame extends World {
  // represents the rows of tiles
  ArrayList<ArrayList<Tile>> tiles;

  // Constructor that takes in a random number
  FifteenGame(Random r) {
    ArrayList<Integer> nums = new ArrayList<Integer>(
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
    this.tiles = new ArrayList<ArrayList<Tile>>();

    for (int row = 0; row < 4; row++) {
      ArrayList<Tile> rowTiles = new ArrayList<Tile>();

      for (int col = 0; col < 4; col++) {
        // choose a random element from nums
        int element = nums.remove(r.nextInt(nums.size()));
        rowTiles.add(new Tile(element));
      }
      this.tiles.add(rowTiles);
    }
  }

  // Constructs a game based on a random number, unseeded
  FifteenGame() {
    this(new Random());
  }

  // Constructor that takes in an ArrayList<ArrayList<Tile>>
  FifteenGame(ArrayList<ArrayList<Tile>> tiles) {
    this.tiles = tiles;
  }

  // draws the game
  public WorldScene makeScene() {
    WorldScene w = new WorldScene(400, 400);

    for (int r = 0; r < 4; r++) {
      for (int c = 0; c < 4; c++) {
        if (this.tiles.get(r).get(c).atCorrectPosition(r, c)) {
          // in the right position
          w = this.tiles.get(r).get(c).drawAt(c, r, w, true);
        }
        else {
          // in the wrong position
          w = this.tiles.get(r).get(c).drawAt(c, r, w, false);
        }
      }
    }
    if (this.hasWorldEnded()) {
      return this.lastScene("Over");
    }
    else {
      return w;
    }
  }

  // handles keystrokes
  public void onKeyEvent(String k) {
    // needs to handle up, down, left, right to move the space
    // extra: handle "u" to undo moves
    // determines if the world has ended
    ArrayUtils u = new ArrayUtils();
    int indexOfSpace = u.findIndex(this.tiles, new Tile(0));
    int indexOfOther = -1;

    if (k.equals("right")) {
      indexOfOther = indexOfSpace - 1;
      if (indexOfSpace % 4 == 0) { // handles left overflow
        indexOfOther = -1;
      }
    }
    if (k.equals("left")) {
      indexOfOther = indexOfSpace + 1;
      if (indexOfOther % 4 == 0) { // handles right overflow
        indexOfOther = -1;
      }
    }
    if (k.equals("up")) {
      indexOfOther = indexOfSpace + 4;
    }
    if (k.equals("down")) {
      indexOfOther = indexOfSpace - 4;
    }

    if (indexOfOther >= 0 && indexOfOther <= 15) {
      this.swap(indexOfSpace, indexOfOther);
    }
  }

  // swaps two tiles by their indexes
  // indexes are numbers from 0 to 15
  public void swap(int index1, int index2) {
    int row1 = index1 / 4;
    int col1 = index1 - (row1 * 4);
    int row2 = index2 / 4;
    int col2 = index2 - (row2 * 4);

    Tile tile1 = this.tiles.get(row1).get(col1);
    Tile tile2 = this.tiles.get(row2).get(col2);
    this.tiles.get(row1).set(col1, tile2);
    this.tiles.get(row2).set(col2, tile1);
  }

  // Checks whether the world has ended
  public boolean hasWorldEnded() {
    for (int i = 0; i < this.tiles.size(); i++) {
      for (int j = 0; j < this.tiles.get(i).size(); j++) {
        if (!(this.tiles.get(i).get(j).atCorrectPosition(i, j))) {
          return false;
        }
      }
    }
    return true;
  }

  // End of world
  public WorldScene lastScene(String msg) {
    WorldScene w = new WorldScene(400, 400);
    w.placeImageXY(new RectangleImage(300, 200, OutlineMode.SOLID, Color.PINK), 200, 200);
    w.placeImageXY(new TextImage("You won!", 30, Color.black), 200, 200);
    return w;
  }
}

class Examples {

  FifteenGame fifteenGame;
  ArrayList<ArrayList<Tile>> tiles; // used in the testConstructorFifteenGame
  ArrayList<ArrayList<Tile>> tiles2;
  ArrayList<ArrayList<Tile>> tiles3;
  ArrayList<ArrayList<Tile>> tiles4;
  ArrayList<ArrayList<Tile>> tiles5;
  ArrayList<ArrayList<Tile>> shiftedTiles;
  FifteenGame gameWithOrderedTiles;
  FifteenGame gameComplete;
  FifteenGame game1;
  FifteenGame game2;
  WorldScene initWorld;

  void initData() {
    this.fifteenGame = new FifteenGame(new Random(20));
    this.tiles = new ArrayList<ArrayList<Tile>>();

    ArrayList<Tile> row1 = new ArrayList<Tile>(
        Arrays.asList(new Tile(0), new Tile(1), new Tile(2), new Tile(3)));
    ArrayList<Tile> row2 = new ArrayList<Tile>(
        Arrays.asList(new Tile(4), new Tile(5), new Tile(6), new Tile(7)));
    ArrayList<Tile> row3 = new ArrayList<Tile>(
        Arrays.asList(new Tile(8), new Tile(9), new Tile(10), new Tile(11)));
    ArrayList<Tile> row4 = new ArrayList<Tile>(
        Arrays.asList(new Tile(12), new Tile(13), new Tile(14), new Tile(15)));
    this.tiles2 = new ArrayList<ArrayList<Tile>>(Arrays.asList(row1, row2, row3, row4));

    this.gameWithOrderedTiles = new FifteenGame(this.tiles2);

    row1 = new ArrayList<Tile>(Arrays.asList(new Tile(1), new Tile(2), new Tile(3), new Tile(4)));
    row2 = new ArrayList<Tile>(Arrays.asList(new Tile(5), new Tile(6), new Tile(7), new Tile(8)));
    row3 = new ArrayList<Tile>(
        Arrays.asList(new Tile(9), new Tile(10), new Tile(11), new Tile(12)));
    row4 = new ArrayList<Tile>(
        Arrays.asList(new Tile(13), new Tile(14), new Tile(15), new Tile(0)));
    this.tiles3 = new ArrayList<ArrayList<Tile>>(Arrays.asList(row1, row2, row3, row4));

    this.gameComplete = new FifteenGame(this.tiles3);

    row1 = new ArrayList<Tile>(Arrays.asList(new Tile(7), new Tile(0), new Tile(13), new Tile(15)));
    row2 = new ArrayList<Tile>(Arrays.asList(new Tile(14), new Tile(6), new Tile(5), new Tile(1)));
    row3 = new ArrayList<Tile>(Arrays.asList(new Tile(9), new Tile(11), new Tile(10), new Tile(4)));
    row4 = new ArrayList<Tile>(Arrays.asList(new Tile(3), new Tile(2), new Tile(8), new Tile(12)));
    this.tiles4 = new ArrayList<ArrayList<Tile>>(Arrays.asList(row1, row2, row3, row4));

    this.game1 = new FifteenGame(this.tiles4);

    row1 = new ArrayList<Tile>(Arrays.asList(new Tile(12), new Tile(5), new Tile(13), new Tile(8)));
    row2 = new ArrayList<Tile>(Arrays.asList(new Tile(1), new Tile(0), new Tile(3), new Tile(10)));
    row3 = new ArrayList<Tile>(
        Arrays.asList(new Tile(15), new Tile(4), new Tile(14), new Tile(11)));
    row4 = new ArrayList<Tile>(Arrays.asList(new Tile(2), new Tile(9), new Tile(7), new Tile(6)));
    this.tiles5 = new ArrayList<ArrayList<Tile>>(Arrays.asList(row1, row2, row3, row4));

    this.game2 = new FifteenGame(this.tiles5);

    this.initWorld = new WorldScene(400, 400);

    // example for shifted tiles
    // corresponds to tiles2
    ArrayList<Tile> r1 = new ArrayList<Tile>(
        Arrays.asList(new Tile(1), new Tile(2), new Tile(3), new Tile(0)));
    ArrayList<Tile> r2 = new ArrayList<Tile>(
        Arrays.asList(new Tile(4), new Tile(5), new Tile(6), new Tile(7)));
    ArrayList<Tile> r3 = new ArrayList<Tile>(
        Arrays.asList(new Tile(8), new Tile(9), new Tile(10), new Tile(11)));
    ArrayList<Tile> r4 = new ArrayList<Tile>(
        Arrays.asList(new Tile(12), new Tile(13), new Tile(14), new Tile(15)));
    this.shiftedTiles = new ArrayList<ArrayList<Tile>>(Arrays.asList(r1, r2, r3, r4));
  }

  void testConstructorFifteenGame(Tester t) {
    this.initData();

    Random r = new Random(20);
    ArrayList<Integer> nums = new ArrayList<Integer>(
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));

    for (int row = 0; row < 4; row++) {
      ArrayList<Tile> rowTiles = new ArrayList<Tile>();

      for (int col = 0; col < 4; col++) {
        // choose a random element from nums
        int element = nums.remove(r.nextInt(nums.size()));
        rowTiles.add(new Tile(element));
      }
      this.tiles.add(rowTiles);
    }

    t.checkExpect(this.fifteenGame.tiles, this.tiles);
    t.checkExpect(this.fifteenGame.tiles.size(), 4);
    t.checkExpect(this.fifteenGame.tiles.get(0).size(), 4);
  }

  // tests for swap
  void testSwap(Tester t) {
    this.initData();

    this.gameWithOrderedTiles.swap(0, 13);
    ArrayList<Tile> row1 = new ArrayList<Tile>(
        Arrays.asList(new Tile(13), new Tile(1), new Tile(2), new Tile(3)));
    ArrayList<Tile> row2 = new ArrayList<Tile>(
        Arrays.asList(new Tile(4), new Tile(5), new Tile(6), new Tile(7)));
    ArrayList<Tile> row3 = new ArrayList<Tile>(
        Arrays.asList(new Tile(8), new Tile(9), new Tile(10), new Tile(11)));
    ArrayList<Tile> row4 = new ArrayList<Tile>(
        Arrays.asList(new Tile(12), new Tile(0), new Tile(14), new Tile(15)));
    ArrayList<ArrayList<Tile>> tilesAfterSwap = new ArrayList<ArrayList<Tile>>(
        Arrays.asList(row1, row2, row3, row4));

    t.checkExpect(this.gameWithOrderedTiles.tiles, tilesAfterSwap);

    this.gameWithOrderedTiles.swap(0, 0);
    t.checkExpect(this.gameWithOrderedTiles.tiles, tilesAfterSwap);

    this.gameWithOrderedTiles.swap(12, 15);
    row1 = new ArrayList<Tile>(Arrays.asList(new Tile(13), new Tile(1), new Tile(2), new Tile(3)));
    row2 = new ArrayList<Tile>(Arrays.asList(new Tile(4), new Tile(5), new Tile(6), new Tile(7)));
    row3 = new ArrayList<Tile>(Arrays.asList(new Tile(8), new Tile(9), new Tile(10), new Tile(11)));
    row4 = new ArrayList<Tile>(
        Arrays.asList(new Tile(15), new Tile(0), new Tile(14), new Tile(12)));
    tilesAfterSwap = new ArrayList<ArrayList<Tile>>(Arrays.asList(row1, row2, row3, row4));

    t.checkExpect(this.gameWithOrderedTiles.tiles, tilesAfterSwap);
  }

  // tests for convertIndexToCoord
  void testConvert(Tester t) {
    CoordUtils c = new CoordUtils();
    t.checkExpect(c.convertIndexToCoord(0), 50);
    t.checkExpect(c.convertIndexToCoord(1), 150);
    t.checkExpect(c.convertIndexToCoord(2), 250);
    t.checkExpect(c.convertIndexToCoord(3), 350);
    t.checkException(new IllegalArgumentException("Index should be between 0 and 4"), c,
        "convertIndexToCoord", -1);
    t.checkException(new IllegalArgumentException("Index should be between 0 and 4"), c,
        "convertIndexToCoord", 5);
  }

  // test drawTile
  void testDrawTile(Tester t) {
    t.checkExpect(new Tile(9).drawTile(true),
        new OverlayImage(new TextImage("9", 30, Color.black),
            new OverlayImage(new RectangleImage(90, 90, OutlineMode.SOLID, Color.orange),
                new RectangleImage(100, 100, OutlineMode.SOLID, Color.black))));

    t.checkExpect(new Tile(15).drawTile(true),
        new OverlayImage(new TextImage("15", 30, Color.black),
            new OverlayImage(new RectangleImage(90, 90, OutlineMode.SOLID, Color.orange),
                new RectangleImage(100, 100, OutlineMode.SOLID, Color.black))));

    t.checkExpect(new Tile(9).drawTile(false),
        new OverlayImage(new TextImage("9", 30, Color.black),
            new OverlayImage(new RectangleImage(90, 90, OutlineMode.SOLID, Color.cyan),
                new RectangleImage(100, 100, OutlineMode.SOLID, Color.black))));

    t.checkExpect(new Tile(15).drawTile(false),
        new OverlayImage(new TextImage("15", 30, Color.black),
            new OverlayImage(new RectangleImage(90, 90, OutlineMode.SOLID, Color.cyan),
                new RectangleImage(100, 100, OutlineMode.SOLID, Color.black))));

    t.checkExpect(new Tile(0).drawTile(true),
        new OverlayImage(new RectangleImage(90, 90, OutlineMode.SOLID, Color.white),
            new RectangleImage(100, 100, OutlineMode.SOLID, Color.black)));

    t.checkExpect(new Tile(0).drawTile(false),
        new OverlayImage(new RectangleImage(90, 90, OutlineMode.SOLID, Color.white),
            new RectangleImage(100, 100, OutlineMode.SOLID, Color.black)));
  }

  // test drawAt
  void testDrawAt(Tester t) {
    this.initData();
    this.initWorld.placeImageXY(new Tile(9).drawTile(true), new CoordUtils().convertIndexToCoord(2),
        new CoordUtils().convertIndexToCoord(3));

    t.checkExpect(new Tile(9).drawAt(2, 3, new WorldScene(400, 400), true), this.initWorld);

    this.initWorld.placeImageXY(new Tile(10).drawTile(false),
        new CoordUtils().convertIndexToCoord(1), new CoordUtils().convertIndexToCoord(3));

    t.checkExpect(
        new Tile(9).drawAt(2, 3, new Tile(10).drawAt(1, 3, new WorldScene(400, 400), false), true),
        this.initWorld);

    this.initWorld.placeImageXY(new Tile(13).drawTile(false), 50, 50);
    t.checkExpect(
        new Tile(13).drawAt(0, 0, new Tile(9).drawAt(2, 3,
            new Tile(10).drawAt(1, 3, new WorldScene(400, 400), false), true), false),
        this.initWorld);

    this.initWorld = new WorldScene(400, 400);
    this.initWorld.placeImageXY(new Tile(1).drawTile(false), 150, 250);
    t.checkExpect(new Tile(1).drawAt(1, 2, new WorldScene(400, 400), false), this.initWorld);
  }

  // test atCorrectPosition
  void testAtCorrectPos(Tester t) {
    t.checkExpect(new Tile(0).atCorrectPosition(3, 3), true);
    t.checkExpect(new Tile(0).atCorrectPosition(1, 0), false);
    t.checkExpect(new Tile(4).atCorrectPosition(0, 3), true);
    t.checkExpect(new Tile(4).atCorrectPosition(2, 2), false);
    t.checkExpect(new Tile(13).atCorrectPosition(3, 0), true);
    t.checkExpect(new Tile(13).atCorrectPosition(0, 3), false);
    t.checkExpect(new Tile(15).atCorrectPosition(3, 2), true);
    t.checkExpect(new Tile(15).atCorrectPosition(1, 2), false);
    t.checkExpect(new Tile(1).atCorrectPosition(0, 0), true);
    t.checkExpect(new Tile(1).atCorrectPosition(1, 0), false);
    t.checkExpect(new Tile(7).atCorrectPosition(1, 2), true);
    t.checkExpect(new Tile(7).atCorrectPosition(4, 5), false);
  }

  // test makeScene
  void testMakeScene(Tester t) {
    this.initData();
    t.checkExpect(this.gameComplete.makeScene(), this.gameComplete.lastScene("Over"));

    t.checkExpect(this.gameWithOrderedTiles.makeScene(),
        new Tile(0).drawAt(0, 0, new Tile(1).drawAt(1, 0, new Tile(2).drawAt(2, 0,
            new Tile(3).drawAt(3, 0, new Tile(4).drawAt(0, 1, new Tile(5).drawAt(1, 1,
                new Tile(6).drawAt(2, 1, new Tile(7).drawAt(3, 1, new Tile(8).drawAt(0, 2,
                    new Tile(9).drawAt(1, 2, new Tile(10).drawAt(2, 2, new Tile(11).drawAt(3, 2,
                        new Tile(12).drawAt(0, 3, new Tile(13).drawAt(1, 3,
                            new Tile(14).drawAt(2, 3,
                                new Tile(15).drawAt(3, 3, this.initWorld, false), false),
                            false), false),
                        false), false), false),
                    false), false), false),
                false), false), false),
            false), false), false));

    t.checkExpect(this.game1.makeScene(),
        new Tile(7).drawAt(0, 0, new Tile(0).drawAt(1, 0, new Tile(13).drawAt(2, 0,
            new Tile(15).drawAt(3, 0, new Tile(14).drawAt(0, 1, new Tile(6).drawAt(1, 1,
                new Tile(5).drawAt(2, 1, new Tile(1).drawAt(3, 1, new Tile(9).drawAt(0, 2,
                    new Tile(11).drawAt(1, 2, new Tile(10).drawAt(2, 2, new Tile(4).drawAt(3, 2,
                        new Tile(3).drawAt(0, 3, new Tile(2).drawAt(1, 3,
                            new Tile(8).drawAt(2, 3,
                                new Tile(12).drawAt(3, 3, this.initWorld, false), false),
                            false), false),
                        false), false), false),
                    true), false), false),
                true), false), false),
            false), false), false));

    t.checkExpect(this.game2.makeScene(),
        new Tile(12).drawAt(0, 0,
            new Tile(5).drawAt(1, 0, new Tile(13).drawAt(2, 0,
                new Tile(8).drawAt(3, 0, new Tile(1).drawAt(0, 1, new Tile(0).drawAt(1, 1,
                    new Tile(3).drawAt(2, 1, new Tile(10).drawAt(3, 1, new Tile(15).drawAt(0, 2,
                        new Tile(4).drawAt(1, 2, new Tile(14).drawAt(2, 2, new Tile(11).drawAt(3, 2,
                            new Tile(2).drawAt(0, 3, new Tile(9).drawAt(1, 3,
                                new Tile(7).drawAt(2, 3,
                                    new Tile(6).drawAt(3, 3, this.initWorld, false), false),
                                false), false),
                            false), false), false),
                        false), false), false),
                    false), false), false),
                false), false),
            false));
  }

  // tests findIndex
  void testFindIndex(Tester t) {
    this.initData();
    ArrayUtils u = new ArrayUtils();
    t.checkExpect(u.findIndex(this.tiles2, new Tile(12)), 12);
    t.checkExpect(u.findIndex(this.tiles2, new Tile(4)), 4);
    t.checkExpect(u.findIndex(this.tiles2, new Tile(0)), 0);
    t.checkExpect(u.findIndex(this.tiles4, new Tile(14)), 4);
    t.checkExpect(u.findIndex(this.tiles4, new Tile(0)), 1);
    t.checkExpect(u.findIndex(this.tiles4, new Tile(2)), 13);
  }

  // test equals
  void testEquals(Tester t) {
    t.checkExpect(new Tile(12).equals(new Tile(12)), true);
    t.checkExpect(new Tile(13).equals(new Tile(13)), true);
    t.checkExpect(new Tile(0).equals(new Tile(0)), true);
    t.checkExpect(new Tile(12).equals(new Tile(9)), false);
    t.checkExpect(new Tile(13).equals(new Tile(11)), false);
    t.checkExpect(new Tile(0).equals(new Tile(10)), false);
    t.checkExpect(new Tile(0).equals(10), false);
  }

  //test equals
  void testHashcode(Tester t) {
    t.checkExpect(new Tile(12).hashCode(), new Tile(12).hashCode());
    t.checkExpect(new Tile(2).hashCode(), new Tile(2).hashCode());
    t.checkExpect(new Tile(5).hashCode(), new Tile(5).hashCode());
    t.checkExpect(new Tile(5).hashCode() == new Tile(6).hashCode(), false);
    t.checkExpect(new Tile(3).hashCode() == new Tile(13).hashCode(), false);
  }

  // test onKey
  void testOnKey(Tester t) {
    this.initData();
    this.gameWithOrderedTiles.onKeyEvent("right");
    t.checkExpect(this.gameWithOrderedTiles.tiles, this.tiles2);
    this.gameWithOrderedTiles.onKeyEvent("down");
    t.checkExpect(this.gameWithOrderedTiles.tiles, this.tiles2);

    ArrayList<Tile> row1 = new ArrayList<Tile>(
        Arrays.asList(new Tile(4), new Tile(1), new Tile(2), new Tile(3)));
    ArrayList<Tile> row2 = new ArrayList<Tile>(
        Arrays.asList(new Tile(0), new Tile(5), new Tile(6), new Tile(7)));
    ArrayList<Tile> row3 = new ArrayList<Tile>(
        Arrays.asList(new Tile(8), new Tile(9), new Tile(10), new Tile(11)));
    ArrayList<Tile> row4 = new ArrayList<Tile>(
        Arrays.asList(new Tile(12), new Tile(13), new Tile(14), new Tile(15)));
    ArrayList<ArrayList<Tile>> tilesNew = new ArrayList<ArrayList<Tile>>(
        Arrays.asList(row1, row2, row3, row4));
    this.gameWithOrderedTiles.onKeyEvent("up");
    t.checkExpect(this.gameWithOrderedTiles.tiles, tilesNew);

    row1 = new ArrayList<Tile>(Arrays.asList(new Tile(4), new Tile(1), new Tile(2), new Tile(3)));
    row2 = new ArrayList<Tile>(Arrays.asList(new Tile(5), new Tile(0), new Tile(6), new Tile(7)));
    row3 = new ArrayList<Tile>(Arrays.asList(new Tile(8), new Tile(9), new Tile(10), new Tile(11)));
    row4 = new ArrayList<Tile>(
        Arrays.asList(new Tile(12), new Tile(13), new Tile(14), new Tile(15)));
    tilesNew = new ArrayList<ArrayList<Tile>>(Arrays.asList(row1, row2, row3, row4));
    this.gameWithOrderedTiles.onKeyEvent("left");
    t.checkExpect(this.gameWithOrderedTiles.tiles, tilesNew);

    this.gameComplete.onKeyEvent("left");
    t.checkExpect(this.gameComplete.tiles, this.tiles3);
    this.gameComplete.onKeyEvent("up");
    t.checkExpect(this.gameComplete.tiles, this.tiles3);

    row1 = new ArrayList<Tile>(Arrays.asList(new Tile(1), new Tile(2), new Tile(3), new Tile(4)));
    row2 = new ArrayList<Tile>(Arrays.asList(new Tile(5), new Tile(6), new Tile(7), new Tile(8)));
    row3 = new ArrayList<Tile>(Arrays.asList(new Tile(9), new Tile(10), new Tile(11), new Tile(0)));
    row4 = new ArrayList<Tile>(
        Arrays.asList(new Tile(13), new Tile(14), new Tile(15), new Tile(12)));
    tilesNew = new ArrayList<ArrayList<Tile>>(Arrays.asList(row1, row2, row3, row4));
    this.gameComplete.onKeyEvent("down");
    t.checkExpect(this.gameComplete.tiles, tilesNew);

    row1 = new ArrayList<Tile>(Arrays.asList(new Tile(1), new Tile(2), new Tile(3), new Tile(4)));
    row2 = new ArrayList<Tile>(Arrays.asList(new Tile(5), new Tile(6), new Tile(7), new Tile(8)));
    row3 = new ArrayList<Tile>(Arrays.asList(new Tile(9), new Tile(10), new Tile(0), new Tile(11)));
    row4 = new ArrayList<Tile>(
        Arrays.asList(new Tile(13), new Tile(14), new Tile(15), new Tile(12)));
    tilesNew = new ArrayList<ArrayList<Tile>>(Arrays.asList(row1, row2, row3, row4));
    this.gameComplete.onKeyEvent("right");
    t.checkExpect(this.gameComplete.tiles, tilesNew);
  }

  // testing hasWorldEnded
  void testHasWorldEnded(Tester t) {
    this.initData();
    t.checkExpect(new FifteenGame(this.shiftedTiles).hasWorldEnded(), false);
    t.checkExpect(new FifteenGame(this.tiles3).hasWorldEnded(), true);
    t.checkExpect(this.gameWithOrderedTiles.hasWorldEnded(), false);
    t.checkExpect(this.gameComplete.hasWorldEnded(), true);
  }

  // testing lastScene
  void testLastScene(Tester t) {
    this.initData();
    WorldScene w = new WorldScene(400, 400);
    w.placeImageXY(new RectangleImage(300, 200, OutlineMode.SOLID, Color.PINK), 200, 200);
    w.placeImageXY(new TextImage("You won!", 30, Color.black), 200, 200);
    t.checkExpect(this.gameComplete.lastScene("Over"), w);
    t.checkExpect(this.gameWithOrderedTiles.lastScene("Done"), w);
    t.checkExpect(this.game2.lastScene("Finished"), w);
  }

  ArrayList<Tile> row1 = new ArrayList<Tile>(
      Arrays.asList(new Tile(1), new Tile(2), new Tile(3), new Tile(4)));
  ArrayList<Tile> row2 = new ArrayList<Tile>(
      Arrays.asList(new Tile(5), new Tile(6), new Tile(7), new Tile(8)));
  ArrayList<Tile> row3 = new ArrayList<Tile>(
      Arrays.asList(new Tile(9), new Tile(10), new Tile(11), new Tile(0)));
  ArrayList<Tile> row4 = new ArrayList<Tile>(
      Arrays.asList(new Tile(13), new Tile(14), new Tile(15), new Tile(12)));
  ArrayList<ArrayList<Tile>> tilesNew = new ArrayList<ArrayList<Tile>>(
      Arrays.asList(row1, row2, row3, row4));
}

//to run the game
class ExampleFifteenGame {
  void testGame(Tester t) {
    FifteenGame g = new FifteenGame();
    g.bigBang(400, 400);
  }
}


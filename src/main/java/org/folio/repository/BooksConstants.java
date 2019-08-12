package org.folio.repository;

public class BooksConstants {
  private BooksConstants() {}

  public static final String BOOKS_TABLE_NAME = "books";
  public static final String GET_BOOKS = "SELECT * FROM %s";
  public static final String GET_BOOK_BY_ID = "SELECT * FROM %s WHERE id=?";
  public static final String POST_BOOK = "INSERT INTO %s (id, jsonb) VALUES (?,?)";
}

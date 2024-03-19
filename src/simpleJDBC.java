import java.sql.*;
import java.util.Scanner;

class SimpleJDBC {
    private static final String JDBC_URL = "jdbc:db2://winter2024-comp421.cs.mcgill.ca:50000/comp421";
    private static final String YOUR_USERID = "cs421g14";
    private static final String YOUR_PASSWORD = "v1nylRecord$";

    public static void main(String[] args) {
        String tableName = (args.length > 0) ? args[0] : "exampletbl";
        Connection con = null;
        Statement statement = null;

        try {
            DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());
            con = DriverManager.getConnection(JDBC_URL, YOUR_USERID, YOUR_PASSWORD);
            statement = con.createStatement();

            System.out.println(tableName);

            boolean exit = false;
            while (!exit) {
                System.out.println("\n*** MENU ***");
                System.out.println("1. Create Table");
                System.out.println("2. Insert Data");
                System.out.println("3. Query Table");
                System.out.println("4. Update Table");
                System.out.println("5. Drop Table");
                System.out.println("6. Quit");

                System.out.print("\nEnter your choice: ");
                int choice = getUserInput();

                switch (choice) {
                    case 1:
                        createTable(statement, tableName);
                        break;
                    case 2:
                        insertData(statement, tableName);
                        break;
                    case 3:
                        queryTable(statement, tableName);
                        break;
                    case 4:
                        updateTable(statement, tableName);
                        break;
                    case 5:
                        dropTable(statement, tableName);
                        break;
                    case 6:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createTable(Statement statement, String tableName) throws SQLException {
        try {
            String createSQL = "CREATE TABLE " + tableName + " (id INTEGER, name VARCHAR (25)) ";
            System.out.println(createSQL);
            statement.executeUpdate(createSQL);
            System.out.println("Table created successfully.");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void insertData(Statement statement, String tableName) throws SQLException {
        try {
            String[] insertSQLs = {
                    "INSERT INTO " + tableName + " VALUES ( 1 , 'Vicki' )",
                    "INSERT INTO " + tableName + " VALUES ( 2 , 'Vera' )",
                    "INSERT INTO " + tableName + " VALUES ( 3 , 'Franca' )"
            };

            for (String insertSQL : insertSQLs) {
                System.out.println(insertSQL);
                statement.executeUpdate(insertSQL);
                System.out.println("Insertion successful.");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void queryTable(Statement statement, String tableName) throws SQLException {
        try {
            String querySQL = "SELECT id, name FROM " + tableName + " WHERE NAME = 'Vicki'";
            System.out.println(querySQL);
            ResultSet rs = statement.executeQuery(querySQL);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                System.out.println("id: " + id);
                System.out.println("name: " + name);
            }
            System.out.println("Query successful.");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void updateTable(Statement statement, String tableName) throws SQLException {
        try {
            String updateSQL = "UPDATE " + tableName + " SET NAME = 'Mimi' WHERE id = 3";
            System.out.println(updateSQL);
            statement.executeUpdate(updateSQL);
            System.out.println("Update successful.");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void dropTable(Statement statement, String tableName) throws SQLException {
        try {
            String dropSQL = "DROP TABLE " + tableName;
            System.out.println(dropSQL);
            statement.executeUpdate(dropSQL);
            System.out.println("Table dropped successfully.");
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }



// Other parts of your code...

    private static Scanner scanner = new Scanner(System.in);

    private static int getUserInput() {
        int choice = -1;
        try {
            String input = scanner.nextLine().trim(); // Trim leading and trailing whitespace
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
        return choice;
    }
    private static void handleSQLException(SQLException e) {
        int sqlCode = e.getErrorCode();
        String sqlState = e.getSQLState();
        System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
        System.out.println(e);
    }
}
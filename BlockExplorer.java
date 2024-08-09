import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class BlockExplorer {
    public static void main(String[] args) {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            System.out.println("DATABASE_URL environment variable is not set.");
            return;
        }

        try {
            // Parse the DATABASE_URL
            URI dbUri = new URI(databaseUrl);

            // Construct JDBC URL
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();

            // Set up connection properties
            Properties props = new Properties();
            String userInfo = dbUri.getUserInfo();
            if (userInfo != null) {
                String[] userParts = userInfo.split(":");
                props.setProperty("user", userParts[0]);
                props.setProperty("password", userParts[1]);
            }

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter block number: ");
            int blockNumber = scanner.nextInt();

            // Load the PostgreSQL driver explicitly
            Class.forName("org.postgresql.Driver");

            try (Connection conn = DriverManager.getConnection(dbUrl, props);
                 PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT * FROM blocks WHERE number = ?")) {

                pstmt.setInt(1, blockNumber);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Block Details:");
                        System.out.println("Number: " + rs.getInt("number"));
                        System.out.println("Hash: " + rs.getString("hash"));
                        System.out.println("Timestamp: " + rs.getTimestamp("timestamp"));
                        System.out.println("Gas Used: " + rs.getLong("gas_used"));
                        System.out.println("Gas Limit: " + rs.getLong("gas_limit"));
                        System.out.println("Miner Hash: " + rs.getString("miner_hash"));
                        System.out.println("Nonce: " + rs.getString("nonce"));
                        // Add more fields as needed
                    } else {
                        System.out.println("Block not found.");
                    }
                }

            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }

        } catch (URISyntaxException | ClassNotFoundException e) {
            System.out.println("Error parsing DATABASE_URL or loading driver: " + e.getMessage());
        }
    }
}

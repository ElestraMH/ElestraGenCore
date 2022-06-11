package com.lilygens.gencore;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class database {
    static String folder = String.valueOf(Bukkit.getServer().getPluginManager().getPlugin("GenCore").getDataFolder());
    private static final String storage_url;

    public database() {
    }

    private static Connection connect() {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(storage_url);
        } catch (SQLException var2) {
            System.out.println(var2.getMessage());
        }

        return conn;
    }

    public static String[] queryPlayer(Player player) {
        String sql = "SELECT uuid, slots, placed, gen_data FROM players WHERE uuid = ?";

        try {
            Connection conn = connect();

            label83: {
                String[] var7;
                try {
                    PreparedStatement a;
                    label85: {
                        a = conn.prepareStatement(sql);

                        try {
                            a.setString(1, String.valueOf(player.getUniqueId()));
                            ResultSet rs = a.executeQuery();

                            while(rs.next()) {
                                String u = rs.getString("uuid");
                                if (Objects.equals(u, String.valueOf(player.getUniqueId()))) {
                                    String[] obj = new String[]{u, String.valueOf(rs.getInt("slots")), String.valueOf(rs.getInt("placed")), rs.getString("gen_data")};
                                    var7 = obj;
                                    break label85;
                                }
                            }
                        } catch (Throwable var10) {
                            if (a != null) {
                                try {
                                    a.close();
                                } catch (Throwable var9) {
                                    var10.addSuppressed(var9);
                                }
                            }

                            throw var10;
                        }

                        if (a != null) {
                            a.close();
                        }
                        break label83;
                    }

                    if (a != null) {
                        a.close();
                    }
                } catch (Throwable var11) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var8) {
                            var11.addSuppressed(var8);
                        }
                    }

                    throw var11;
                }

                if (conn != null) {
                    conn.close();
                }

                return var7;
            }

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException var12) {
            var12.printStackTrace();
        }

        return null;
    }

    public static void addPlayer(Player player) {
        String uuid = String.valueOf(player.getUniqueId());
        String sql = "INSERT INTO players(uuid,slots,placed,gen_data) VALUES(?,?,?,?)";
        try {
            Connection conn = connect();
            try {
                PreparedStatement a = conn.prepareStatement(sql);
                try {
                    a.setString(1, uuid);
                    a.setInt(2, events.getSlots(player));
                    a.setInt(3, events.getPlaced(player));
                    a.setString(4, events.getJson(player));
                    a.executeUpdate();
                    main.clearData(player);
                } catch (Throwable var9) {
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable var8) {
                            var9.addSuppressed(var8);
                        }
                    }
                    throw var9;
                }
                if (a != null) {
                    a.close();
                }
            } catch (Throwable var10) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Throwable var7) {
                        var10.addSuppressed(var7);
                    }
                }
                throw var10;
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException var11) {
            Bukkit.getLogger().severe(var11.getMessage());
        } catch (Exception var12) {
            var12.printStackTrace();
        }
    }

    public static void updatePlayerData(Player player) {
        String sql = "UPDATE players SET slots = ?, placed = ?, gen_data = ? WHERE uuid = ?";
        try {
            Connection b = connect();
            try {
                PreparedStatement a = b.prepareStatement(sql);
                try {
                    a.setInt(1, events.getSlots(player));
                    a.setInt(2, events.getPlaced(player));
                    a.setString(3, events.getJson(player));
                    a.setString(4, player.getUniqueId().toString());
                    a.executeUpdate();
                    main.clearData(player);
                } catch (Throwable var8) {
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable var7) {
                            var8.addSuppressed(var7);
                        }
                    }
                    throw var8;
                }
                if (a != null) {
                    a.close();
                }
            } catch (Throwable var9) {
                if (b != null) {
                    try {
                        b.close();
                    } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                    }
                }
                throw var9;
            }
            if (b != null) {
                b.close();
            }
        } catch (SQLException var10) {
            Bukkit.getLogger().severe(var10.getMessage());
        } catch (Exception var11) {
            var11.printStackTrace();
        }
    }

    public static void savePlayerData(Player player) {
        try {
            String[] query = queryPlayer(player);
            if (query == null) {
                addPlayer(player);
            } else {
                updatePlayerData(player);
            }
        } catch (NullPointerException var2) {
            Bukkit.getLogger().severe(String.valueOf(var2));
        }

    }

    public static void createNewTable() {
        String sql = "CREATE TABLE IF NOT EXISTS players (\n\tuuid text PRIMARY KEY,\n\tslots integer NOT NULL,\n\tplaced integer NOT NULL,\n\tgen_data json NOT NULL\n);";

        try {
            Connection conn = DriverManager.getConnection(storage_url);

            try {
                Statement stmt = conn.createStatement();

                try {
                    stmt.execute(sql);
                } catch (Throwable var7) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (stmt != null) {
                    stmt.close();
                }
            } catch (Throwable var8) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Throwable var5) {
                        var8.addSuppressed(var5);
                    }
                }

                throw var8;
            }

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException var9) {
            Bukkit.getLogger().severe(var9.getMessage());
        }

    }

    public static void setupDatabase() {
        File file = new File(folder + "/storage.db");
        if (!file.exists()) {
            try {
                Connection conn = DriverManager.getConnection(storage_url);

                try {
                    if (conn != null) {
                        createNewTable();
                        Bukkit.getLogger().info("Created database: " + storage_url);
                        conn.close();
                    }
                } catch (Throwable var5) {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Throwable var4) {
                            var5.addSuppressed(var4);
                        }
                    }

                    throw var5;
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException var6) {
                Bukkit.getLogger().severe("Error creating folder: SQLite");
            }
        }

    }

    static {
        storage_url = "jdbc:sqlite:" + folder + "/storage.db".replace("\\", "/");
    }
}

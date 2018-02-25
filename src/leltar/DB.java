package leltar;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DB {

    final String user = "raktaros";
    final String pass = "raktaros";
    String dbUrl;
    
    int aktev;
    
    public DB() {
        aktev = LocalDate.now().getYear();
        url_be();
    }
    
    private void url_be() {
        Properties beallitasok = new Properties();
        try (FileInputStream be = new FileInputStream("config.properties")) {
            beallitasok.load(be);
            String ip = beallitasok.getProperty("ip");
            dbUrl = "jdbc:mysql://" + ip + ":3306/nyilvantartas"
                    + "?useUnicode=true&characterEncoding=UTF-8";
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            dbUrl = "jdbc:mysql://localhost:3306/nyilvantartas"
                   + "?useUnicode=true&characterEncoding=UTF-8";
        }
    }

    public void termek_be(JTable tbl, JComboBox cb) {
        final DefaultTableModel tm = (DefaultTableModel)tbl.getModel();
        String s = "SELECT * FROM termek ORDER BY teremszam;";

        try (Connection kapcs = DriverManager.getConnection(dbUrl, user, pass);
                PreparedStatement parancs = kapcs.prepareStatement(s);
                ResultSet eredmeny = parancs.executeQuery()) {
            tm.setRowCount(0);
            cb.removeAllItems();
            while (eredmeny.next()) {
                Object sor[] = {
                    eredmeny.getString("teremid"),
                    eredmeny.getString("teremszam"),
                    eredmeny.getString("felhasznalas")
                };
                tm.addRow(sor);
                cb.addItem(eredmeny.getString("teremszam"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.exit(1);
        }
    }

    public void eszkozok_be(JTable tbl, JComboBox cb) {
        final DefaultTableModel tm = (DefaultTableModel)tbl.getModel();
        String s = "SELECT * FROM eszkozok ORDER BY nev;";

        try (Connection kapcs = DriverManager.getConnection(dbUrl,user,pass);
             PreparedStatement parancs = kapcs.prepareStatement(s);
             ResultSet eredmeny = parancs.executeQuery()) {
            tm.setRowCount(0);
            cb.removeAllItems();
            while (eredmeny.next()) {
                Object sor[] = {
                    eredmeny.getString("eszkozid"),
                    eredmeny.getString("nev"),
                    eredmeny.getString("ev")
                };
                tm.addRow(sor);
                cb.addItem(eredmeny.getString("nev"));
            }            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.exit(2);
        }
    }

    public void leltar_be(JTable tbl) {
        final DefaultTableModel tm = (DefaultTableModel)tbl.getModel();
        String s = "SELECT leltarid, "
                 + "termek.teremszam AS tsz, "
                 + "eszkozok.nev AS ek, "
                 + "egyeb "
                 + "FROM leltar "
                 + "JOIN eszkozok ON leltar.eszkozid=eszkozok.eszkozid "
                 + "JOIN termek ON leltar.teremid=termek.teremid;";

        try (Connection kapcs = DriverManager.getConnection(dbUrl,user,pass);
             PreparedStatement parancs = kapcs.prepareStatement(s);
             ResultSet eredmeny = parancs.executeQuery()) {
            tm.setRowCount(0);
            while (eredmeny.next()) {
                Object sor[] = {
                    eredmeny.getString("leltarid"),
                    eredmeny.getString("tsz"),
                    eredmeny.getString("ek"),
                    eredmeny.getString("egyeb")
                };
                tm.addRow(sor);
            }            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.exit(3);
        }
    }

    public int get_teremid(String teremszam) {
        String s = "SELECT teremid FROM termek WHERE teremszam=?";
        int tid = -1;
        try (Connection kapcs = DriverManager.getConnection(dbUrl,user,pass);
              PreparedStatement parancs = kapcs.prepareStatement(s)) {
             parancs.setString(1, teremszam);
             ResultSet eredmeny = parancs.executeQuery();
            if (eredmeny.next()) {
                tid = eredmeny.getInt("teremid");
            }            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        return tid;
    }
    
    public int get_eszkozid(String nev) {
        String s = "SELECT eszkozid FROM eszkozok WHERE nev=?";
        int eid = -1;
        try (Connection kapcs = DriverManager.getConnection(dbUrl,user,pass);
              PreparedStatement parancs = kapcs.prepareStatement(s)) {
             parancs.setString(1, nev);
             ResultSet eredmeny = parancs.executeQuery();
            if (eredmeny.next()) {
                eid = eredmeny.getInt("eszkozid");
            }            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        return eid;
    }
    
    private String levag(String s, int n) {
        return s.length() > n ? s.substring(0, n) : s;
    }
    
    public void terem_hozzaad(String tsz, String fh) {
        if (tsz.isEmpty())
            return;
        String s = "INSERT INTO termek (teremszam, felhasznalas) VALUES(?,?);";
        try (Connection kapcs = DriverManager.getConnection(dbUrl, user, pass);
                PreparedStatement parancs = kapcs.prepareStatement(s)) {
            parancs.setString(1, levag(tsz.trim(), 4));
            if (fh.isEmpty())
                parancs.setNull(2, java.sql.Types.VARCHAR);
            else
                parancs.setString(2, levag(fh.trim(), 30));
            parancs.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
    
    private int szam(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void eszkoz_hozzaad(String nev, String ev) {
        if (nev.isEmpty())
            return;
        String s = "INSERT INTO eszkozok (nev,ev) VALUES(?,?);";
        try (Connection kapcs = DriverManager.getConnection(dbUrl, user, pass);
                PreparedStatement parancs = kapcs.prepareStatement(s)) {
            parancs.setString(1, levag(nev.trim(), 50));
            int n = szam(ev);
            if (n > 1980 && n <= aktev)
                parancs.setInt(2, n);
            else
                parancs.setNull(2, java.sql.Types.INTEGER);
            parancs.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
    
    public void leltar_hozzaad(int teremid, int eszkozid, String adatok) {
        String s = "INSERT INTO leltar (teremid,eszkozid,egyeb) VALUES(?,?,?);";
        try (Connection kapcs = DriverManager.getConnection(dbUrl, user, pass);
                PreparedStatement parancs = kapcs.prepareStatement(s)) {
            parancs.setInt(1, teremid);
            parancs.setInt(2, eszkozid);
            if (!adatok.isEmpty())
                parancs.setString(3,levag(adatok.trim(), 30));
            else
                parancs.setNull(3,java.sql.Types.VARCHAR);
            parancs.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
    
    public void terem_modosit(int teremid, String tsz, String fh) {
        if (tsz.isEmpty())
            return;
        String s = "UPDATE termek SET teremszam=?, felhasznalas=? "
                 + "WHERE teremid=?";
        try (Connection kapcs = DriverManager.getConnection(dbUrl, user, pass);
                PreparedStatement parancs = kapcs.prepareStatement(s)) {
            parancs.setString(1, levag(tsz.trim(), 4));
            if (fh.isEmpty())
                parancs.setNull(2, java.sql.Types.VARCHAR);
            else
                parancs.setString(2, levag(fh.trim(), 30));
            parancs.setInt(3, teremid);
            parancs.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    public void eszkoz_modosit(int eszkozid, String nev, String ev) {
        if (nev.isEmpty())
            return;
        String s = "UPDATE eszkozok SET nev=?, ev=? "
                 + "WHERE eszkozid=?";
        try (Connection kapcs = DriverManager.getConnection(dbUrl, user, pass);
                PreparedStatement parancs = kapcs.prepareStatement(s)) {
            parancs.setString(1, levag(nev.trim(), 50));
            int n = szam(ev);
            if (n > 0)
                parancs.setInt(2, n);
            else
                parancs.setNull(2, java.sql.Types.INTEGER);
            parancs.setInt(3, eszkozid);
            parancs.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }        
    }
    
    public void leltar_modosit(int leltarid, int teremid, 
                               int eszkozid, String adatok) {
        String s = "UPDATE leltar SET teremid=?, eszkozid=?, egyeb=? "
                 + "WHERE leltarid=?";
        try (Connection kapcs = DriverManager.getConnection(dbUrl, user, pass);
                PreparedStatement parancs = kapcs.prepareStatement(s)) {
            parancs.setInt(1, teremid);
            parancs.setInt(2, eszkozid);
            if (!adatok.isEmpty())
                parancs.setString(3,levag(adatok.trim(), 30));
            else
                parancs.setNull(3,java.sql.Types.VARCHAR);
            parancs.setInt(4, leltarid);
            parancs.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }        
    }
    
    public void terem_torol(int teremid) {
        String s = "DELETE FROM termek WHERE teremid=?;";
        try (Connection kapcs = DriverManager.getConnection(dbUrl, user, pass);
                PreparedStatement parancs = kapcs.prepareStatement(s)) {
            parancs.setInt(1, teremid);
            parancs.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }                
    }
    public void eszkoz_torol(int eszkozid) {
        String s = "DELETE FROM eszkozok WHERE eszkozid=?;";
        try (Connection kapcs = DriverManager.getConnection(dbUrl, user, pass);
                PreparedStatement parancs = kapcs.prepareStatement(s)) {
            parancs.setInt(1, eszkozid);
            parancs.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }                
    }
    
    public void leltar_torol(int leltarid) {
        String s = "DELETE FROM leltar WHERE leltarid=?;";
        try (Connection kapcs = DriverManager.getConnection(dbUrl, user, pass);
                PreparedStatement parancs = kapcs.prepareStatement(s)) {
            parancs.setInt(1, leltarid);
            parancs.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }                
    }    
}

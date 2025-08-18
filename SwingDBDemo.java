import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SwingDBDemo extends JFrame {
    private JTextField txtId, txtName, txtAge;
    private JButton btnInsert, btnUpdate, btnDelete, btnLoad;
    private JTable table;
    private DefaultTableModel model;

    private Connection conn;

    public SwingDBDemo() {
        setTitle("Swing + JDBC CRUD Demo");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(1, 6, 5, 5));
        txtId = new JTextField();
        txtName = new JTextField();
        txtAge = new JTextField();
        inputPanel.add(new JLabel("ID:"));
        inputPanel.add(txtId);
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(txtName);
        inputPanel.add(new JLabel("Age:"));
        inputPanel.add(txtAge);
        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        btnInsert = new JButton("Insert");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnLoad = new JButton("Load Data");
        buttonPanel.add(btnInsert);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnLoad);
        add(buttonPanel, BorderLayout.SOUTH);

        model = new DefaultTableModel(new String[]{"ID", "Name", "Age"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 2) return Integer.class;
                return String.class;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        connectDB();

        btnLoad.addActionListener(e -> loadData());
        btnInsert.addActionListener(e -> insertData());
        btnUpdate.addActionListener(e -> updateData());
        btnDelete.addActionListener(e -> deleteData());

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                try { if (conn != null && !conn.isClosed()) conn.close(); } catch (SQLException ignored) {}
            }
        });
    }

    private void connectDB() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:demo.db");
            try (Statement s = conn.createStatement()) {
                s.execute("CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, age INTEGER NOT NULL)");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        model.setRowCount(0);
        String sql = "SELECT id, name, age FROM students ORDER BY id";
        try (Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                model.addRow(new Object[]{r.getInt(1), r.getString(2), r.getInt(3)});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertData() {
        String sql = "INSERT INTO students (name, age) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtName.getText());
            ps.setInt(2, Integer.parseInt(txtAge.getText()));
            ps.executeUpdate();
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Insert Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateData() {
        String sql = "UPDATE students SET name = ?, age = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtName.getText());
            ps.setInt(2, Integer.parseInt(txtAge.getText()));
            ps.setInt(3, Integer.parseInt(txtId.getText()));
            ps.executeUpdate();
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteData() {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtId.getText()));
            ps.executeUpdate();
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Delete Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingDBDemo().setVisible(true));
    }
}


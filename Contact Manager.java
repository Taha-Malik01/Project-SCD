
import java.io.Serializable;

public class Contact implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String phone;
    private String email;
    
    public Contact(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }
    

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    

    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    
    @Override
    public String toString() {
        return name + " | " + phone + " | " + email;
    }
}

import java.util.regex.Pattern;

public class ContactValidator {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[1-9][\\d]{7,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }
}

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ContactStorage {
    private static final String FILE_NAME = "contacts.dat";
    
    public static void saveContacts(List<Contact> contacts) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(contacts);
            System.out.println("Contacts saved successfully!");
        } catch (IOException e) {
            System.err.println("Error saving contacts: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    public static List<Contact> loadContacts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (List<Contact>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No saved contacts found. Starting with empty list.");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading contacts: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class ContactManagerGUI extends JFrame {
    private List<Contact> contacts;
    private DefaultTableModel tableModel;
    private JTable contactTable;
    private JTextField nameField, phoneField, emailField, searchField;
    private JButton addButton, editButton, deleteButton, clearButton, searchButton, showAllButton;
    private JLabel statusLabel;
    
    public ContactManagerGUI() {
        contacts = ContactStorage.loadContacts();
        initializeGUI();
        refreshTable();
        updateStatus();
    }
    
    private void initializeGUI() {
        setTitle("Contact Manager - Phonebook Application");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
        
        createInputPanel();
        createTablePanel();
        createButtonPanel();
        createStatusPanel();
        setSize(800, 600);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(700, 500));
    }
    
    private void createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Contact Information"));
        inputPanel.setBackground(new Color(240, 248, 255));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        inputPanel.add(nameField, gbc);
       
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        inputPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        phoneField = new JTextField(20);
        inputPanel.add(phoneField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        inputPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        emailField = new JTextField(20);
        inputPanel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        inputPanel.add(new JLabel("Search:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        searchField = new JTextField(20);
        inputPanel.add(searchField, gbc);
        
        add(inputPanel, BorderLayout.NORTH);
    }
    
    private void createTablePanel() {
        String[] columns = {"Name", "Phone", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        contactTable = new JTable(tableModel);
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactTable.setRowHeight(25);
        contactTable.getTableHeader().setBackground(new Color(70, 130, 180));
        contactTable.getTableHeader().setForeground(Color.WHITE);
        contactTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        contactTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = contactTable.getSelectedRow();
                if (selectedRow >= 0) {
                    loadContactToFields(selectedRow);
                    editButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                } else {
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(contactTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Contacts List"));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(245, 245, 245));
        
        addButton = new JButton("Add Contact");
        addButton.setBackground(new Color(34, 139, 34));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addContact());
        
        editButton = new JButton("Edit Contact");
        editButton.setBackground(new Color(255, 140, 0));
        editButton.setForeground(Color.WHITE);
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editContact());
        
        deleteButton = new JButton("Delete Contact");
        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteContact());
        
        clearButton = new JButton("Clear Fields");
        clearButton.setBackground(new Color(70, 130, 180));
        clearButton.setForeground(Color.WHITE);
        clearButton.addActionListener(e -> clearFields());
        
        searchButton = new JButton("Search");
        searchButton.setBackground(new Color(128, 0, 128));
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> searchContacts());
        
        showAllButton = new JButton("Show All");
        showAllButton.setBackground(new Color(0, 128, 128));
        showAllButton.setForeground(Color.WHITE);
        showAllButton.addActionListener(e -> showAllContacts());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(showAllButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createStatusPanel() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(230, 230, 230));
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.PAGE_END);
    }
    
    private void addContact() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        
        if (validateInput(name, phone, email)) {
            if (contacts.stream().anyMatch(c -> c.getName().equalsIgnoreCase(name))) {
                showErrorMessage("A contact with this name already exists!");
                return;
            }
            
            Contact newContact = new Contact(name, phone, email);
            contacts.add(newContact);
            refreshTable();
            clearFields();
            updateStatus();
            showSuccessMessage("Contact added successfully!");
            ContactStorage.saveContacts(contacts);
        }
    }
    
    private void editContact() {
        int selectedRow = contactTable.getSelectedRow();
        if (selectedRow < 0) {
            showErrorMessage("Please select a contact to edit!");
            return;
        }
        
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        
        if (validateInput(name, phone, email)) {
            final int currentIndex = selectedRow;
            if (contacts.stream().anyMatch(c -> 
                c.getName().equalsIgnoreCase(name) && 
                contacts.indexOf(c) != currentIndex)) {
                showErrorMessage("A contact with this name already exists!");
                return;
            }
            
            Contact contact = contacts.get(selectedRow);
            contact.setName(name);
            contact.setPhone(phone);
            contact.setEmail(email);
            
            refreshTable();
            clearFields();
            updateStatus();
            showSuccessMessage("Contact updated successfully!");
            ContactStorage.saveContacts(contacts);
        }
    }
    
    private void deleteContact() {
        int selectedRow = contactTable.getSelectedRow();
        if (selectedRow < 0) {
            showErrorMessage("Please select a contact to delete!");
            return;
        }
        
        Contact contact = contacts.get(selectedRow);
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete " + contact.getName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            contacts.remove(selectedRow);
            refreshTable();
            clearFields();
            updateStatus();
            showSuccessMessage("Contact deleted successfully!");
            ContactStorage.saveContacts(contacts);
        }
    }
    
    private void searchContacts() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            showErrorMessage("Please enter a search term!");
            return;
        }
        
        tableModel.setRowCount(0);
        int foundCount = 0;
        
        for (Contact contact : contacts) {
            if (contact.getName().toLowerCase().contains(searchTerm)) {
                tableModel.addRow(new Object[]{
                    contact.getName(),
                    contact.getPhone(),
                    contact.getEmail()
                });
                foundCount++;
            }
        }
        
        updateStatus();
        if (foundCount == 0) {
            showInfoMessage("No contacts found matching '" + searchTerm + "'");
        } else {
            showSuccessMessage("Found " + foundCount + " contact(s) matching '" + searchTerm + "'");
        }
    }
    
    private void showAllContacts() {
        searchField.setText("");
        refreshTable();
        updateStatus();
        showSuccessMessage("Showing all contacts");
    }
    
    private void clearFields() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        contactTable.clearSelection();
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void loadContactToFields(int index) {
        if (index >= 0 && index < contacts.size()) {
            Contact contact = contacts.get(index);
            nameField.setText(contact.getName());
            phoneField.setText(contact.getPhone());
            emailField.setText(contact.getEmail());
        }
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Contact contact : contacts) {
            tableModel.addRow(new Object[]{
                contact.getName(),
                contact.getPhone(),
                contact.getEmail()
            });
        }
    }
    
    private boolean validateInput(String name, String phone, String email) {
        if (!ContactValidator.isValidName(name)) {
            showErrorMessage("Please enter a valid name (at least 2 characters)!");
            nameField.requestFocus();
            return false;
        }
        
        if (!ContactValidator.isValidPhone(phone)) {
            showErrorMessage("Please enter a valid phone number!");
            phoneField.requestFocus();
            return false;
        }
        
        if (!ContactValidator.isValidEmail(email)) {
            showErrorMessage("Please enter a valid email address!");
            emailField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void updateStatus() {
        statusLabel.setText("Total Contacts: " + contacts.size() + 
                          " | Displayed: " + tableModel.getRowCount());
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exitApplication() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Do you want to save contacts before exiting?",
            "Save and Exit",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            ContactStorage.saveContacts(contacts);
            System.exit(0);
        } else if (result == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new ContactManagerGUI().setVisible(true);
        });
    }
}

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ContactManagerUtils {
    
    public static void openEmail(String emailAddress) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.MAIL)) {
                    URI emailURI = new URI("mailto:" + emailAddress);
                    desktop.mail(emailURI);
                }
            } catch (IOException | URISyntaxException e) {
                System.err.println("Error opening email client: " + e.getMessage());
            }
        }
    }
    
    public static String formatPhoneNumber(String phone) {
        String cleaned = phone.replaceAll("[^\\d+]", "");
        if (cleaned.length() >= 10) {
            return cleaned.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "($1) $2-$3");
        }
        return phone;
    }
    
    public static boolean exportContacts(java.util.List<Contact> contacts, String filename) {
        try (java.io.FileWriter writer = new java.io.FileWriter(filename)) {
            writer.write("Name,Phone,Email\n");
            for (Contact contact : contacts) {
                writer.write(String.format("%s,%s,%s\n", 
                    contact.getName(), 
                    contact.getPhone(), 
                    contact.getEmail()));
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting contacts: " + e.getMessage());
            return false;
        }
    }
}
